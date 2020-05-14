/*
 * Product.cpp
 *
 *  Created on: Nov 15, 2018
 *      Author: cels
 */
#include "Product.h"

#define DEBUG_

Product::Product(){}

Product::Product(String name_mac, String room, String productName){
  this->room = room;
  this->productName = productName;
  this->name_mac = name_mac;
  attributes = new attribStruct[0];
}


/**
 * This is the callback handler that will be called when this product's property changes value.
 * forHub = true : triggers transaction to the hub
 * 			false: only for propagating to display clients, does not trigger transaction to hub
 */
int (* valueChangeCallback) (int propertyIndex, boolean forHub);

void Product::setValueChangeCallback(int (* Callback) (int propertyIndex, boolean forHub)) {
	valueChangeCallback = Callback;
}
/**
* ssid		= the SSID of this property (from the COMPROPLIST table)
* directPin
* 		- if true (1), pin will be handled in the core Symphony.
* 		- if false(0), pin will be handled by the implementing ino class by providing callback functions.
* 	 		callback functions might need to do some calculation first before setting the value of the pi.
* pin		= the corresponding ESP8266 pin where this property is attached.  if < 0, this component is virtual and not attached to a pin
* gui		= the gui attributes
* 	pinType		= the type of pin {RADIO_CTL = 1, BUTTON_CTL = 2, SLIDER_CTL = 3 , RADIO_SNSR = 5, BUTTON_SNSR = 6, SLIDER_SNSR = 7, UNDEF = 99}
		= CTL = pinMode(as.pin, OUTPUT);
		= SNSR = pinMode(as.pin, INPUT);
* 	label		= the label to be displayed in the WebSocket page
* 	min			= the minimum value
* 	max			= the maximum value
* 	value		= the actual value
*/
void Product::addProperty(String ssid, boolean directPin, int8_t pin, Gui gui) {
	propertyChanged = true;
	attribStruct as;
	as.aid = size;
	size++;
	as.ssid = ssid;
	as.directPin = directPin;
	as.pin = pin;
	as.gui = gui.gui;
#ifdef DEBUG_
	Serial.printf("[Product]::addProperty data ssid:%s pin:%i type:%i",as.ssid.c_str(), as.pin, as.gui.pinType);
#endif
	if (pin < 0) {
		//this property is not physical (it is virtual), no need to set the pinmode
#ifdef DEBUG_
		Serial.println("[Product]::addProperty this is a virtual pin.");
#endif
	} else {
		if (as.gui.pinType == BUTTON_CTL || as.gui.pinType == RADIO_CTL || as.gui.pinType == SLIDER_CTL) {
			pinMode(as.pin, OUTPUT);
#ifdef DEBUG_
			Serial.printf("[Product]::addProperty setting pin%d as OUTPUT.\n", as.pin);
#endif
		} else {
			pinMode(as.pin, INPUT);
#ifdef DEBUG_
			Serial.printf("[Product]::addProperty setting pin%d as INPUT.", as.pin);
#endif
		}
	}
	as.directPin = directPin;
	as.ssid = ssid;
#ifdef DEBUG_
	Serial.printf("[Product]::addProperty size=%d, sizeof=%d\n", size, sizeof(attribStruct));
#endif
	attribStruct* temp = new attribStruct[size];
	for (int i=0; i<size; i++) {
		if ( i == size-1)
			temp[i] = as;
		else
			temp[i] = attributes[i];
	}
	delete [] attributes;
	attributes = temp;

//  if (size == 0) {
//	  attributes = (attribStruct *) malloc (sizeof(as));
//  } else {
//	  attributes = (attribStruct *) realloc(attributes, 100*(size+1));  //we are using 100 bytes here since we are assuming that the struct is small
//  }
//  attributes[size] = as;
//  pIndex++;
//  size = pIndex;
#ifdef DEBUG_
  Serial.println("[Product]::addProperty Done");
#endif
}
/**
 * directPin will be set to true (1), pin will be handled in the core Symphony.
 *
 */
void Product::addProperty(String ssid, int8_t pin, Gui gui){
	addProperty(ssid, true, pin, gui);
}

/**
 * directPin will be set to false(0) pin will be handled by the implementing ino class by providing callback functions.
 * 	 	callback functions might need to do some calculation first before setting the value of the pi.
 *
 */
void Product::addCallableProperty(String ssid, int8_t pin, Gui gui){
	addProperty(ssid, false, pin, gui);
}

/**
 *
 * The overloaded addProperty method
 * this is for the virtual property
 */
void Product::addVirtualProperty(String ssid, Gui gui) {
	addProperty(ssid, false, -1, gui);
}

/*
 * returns the property specified by the property SSID
 * returns null if not found
 */
attribStruct Product::getProperty(String ssid) {
  for (int i=0; i<size; i++) {
    if (strcmp(ssid.c_str(), attributes[i].ssid.c_str())==0) {
#ifdef DEBUG_
      Serial.print("[Product]::getProperty i=");Serial.print(i);
	  Serial.print("\tsize=");Serial.print(size);
	  Serial.print("\tpin=");Serial.print(attributes[i].pin);
	  Serial.print("\tp.pinType=");Serial.print(attributes[i].gui.pinType);
	  Serial.print("\tp.directPin=");Serial.print(attributes[i].directPin);
	  Serial.print("\tp.ssid=");Serial.print(attributes[i].ssid);
	  Serial.print("\tprop.value=");Serial.println(attributes[i].gui.value);
#endif
      return attributes[i];
    }
  }
  //we did not find any match
  attribStruct ps;
  ps.pin = -1;
  ps.gui.pinType = BUTTON_CTL;
  ps.directPin = false;
  ps.ssid = "NULL";
  return ps;
}

/*
 * returns the property specified by the ndex
 * returns null if not found
 */
attribStruct Product::getKeyVal(int index) {
	if (index < size) {
		return attributes[index];
	} else {
		//index is not correct
		attribStruct ps;
		ps.pin = -1;
		ps.gui.pinType = BUTTON_CTL;
		ps.directPin = false;
		ps.ssid = "NULL";
		return ps;
	}
}


/*
 * Sets the value of the product property with the given ssid then sends to MQTT.
 */
void Product::setValue(String ssid, int value, boolean forHub) {
#ifdef DEBUG_
      Serial.printf("[Product]::setValue ssid=%s value=%i\n", ssid.c_str(), value);
#endif
	for (int i=0; i<size; i++) {
	    if (strcmp(ssid.c_str(), attributes[i].ssid.c_str())==0) {
#ifdef DEBUG_
      Serial.print("[Product]::setValue before set i=");Serial.print(i);
		  Serial.print("\t size=");Serial.print(size);
		  Serial.print("\t pin=");Serial.print(attributes[i].pin);
		  Serial.print("\t p.pinType=");Serial.print(attributes[i].gui.pinType);
		  Serial.print("\t p.directPin=");Serial.print(attributes[i].directPin);
		  Serial.print("\t p.ssid=");Serial.print(attributes[i].ssid);
		  Serial.print("\t prop.value=");Serial.println(attributes[i].gui.value);
#endif
	      attributes[i].gui.value = value;
#ifdef DEBUG_
	      Serial.print("\t value is set to ");Serial.println(attributes[i].gui.value);
#endif
	      valueChangeCallback(i, forHub);
	    }
	  }
}
/*
 * Sets the value of the product property with the given index then sends to MQTT.
 */
void Product::setValueByIndex(int index, int value, boolean forHub) {
	attributes[index].gui.value = value;
#ifdef DEBUG_
	Serial.print("\t value is set to ");Serial.println(attributes[index].gui.value);
#endif
	valueChangeCallback(index, forHub);
}

/**
 * returns the String representation of this device
 * {
    "uid": "12345678",
    "parentGroups": [],
    "name": "Thing 1",
    "attributes": [
        {
            "mode": "controllable",
            "dataType": {
                "type": "binary",
                "constraints": {}
            },
            "name": "On/Off",
            "aid": "87654321",
            "value": 0
        },
        {
            "mode": "input",
            "dataType": {
                "type": "number",
                "constraints": {}
            },
            "name": "Temperature [C]",
            "aid": "abcdefgh",
            "value": 30.2
        }
    ]
}
 */
String Product::stringify() {
#ifdef DEBUG_
	Serial.println("[Product] stringify start");
#endif
	if (stringifyCache.length()== 0 || propertyChanged) {
		DynamicJsonBuffer jsonBuffer;
		JsonObject& regJson = jsonBuffer.createObject();
		regJson["uid"] = name_mac;
		JsonArray& gArray = regJson.createNestedArray("parentGroups");
		regJson["name"] = productName;
		JsonArray& pArray = regJson.createNestedArray("attributes");
		for (int i=0; i<size; i++) {
			attribStruct a = getKeyVal(i);
#ifdef DEBUG_
			Serial.printf("[Product] stringify ssid=%s label=%s, pintype=%i, aId=%i\n", a.ssid.c_str(), a.gui.label.c_str(), a.gui.pinType, a.aid);
#endif
			JsonObject& prop1 = pArray.createNestedObject();
			if (a.gui.pinType == BUTTON_CTL || a.gui.pinType == SLIDER_CTL) {
				prop1["mode"] = "controllable";
			} else {  //a.gui.pinType == BUTTON_SNSR || a.gui.pinType == SLIDER_SNSR
				prop1["mode"] = "input";
			}
			JsonObject& theType = prop1.createNestedObject("dataType");
			if (a.gui.pinType == BUTTON_CTL || a.gui.pinType == BUTTON_SNSR ) {
				theType["type"] = "binary";
				JsonObject& theTypeObj = theType.createNestedObject("constraints");
			} else { //if (a.gui.pinType == SLIDER_CTL || a.gui.pinType == SLIDER_SNSR )
				theType["type"] = "number";
				JsonObject& constraints = theType.createNestedObject("constraints");
				constraints["min"] = a.gui.min;
				constraints["max"] = a.gui.max;
			}
			prop1["name"] = a.gui.label;
			prop1["aid"] = a.aid;
			prop1["value"] = a.gui.value;
		}
		regJson.printTo(stringifyCache);
		propertyChanged = false;
	}
#ifdef DEBUG_
		Serial.printf("[Product] stringify \n\t%s\n", stringifyCache.c_str());
		Serial.println("[Product] stringify end");
#endif
	return stringifyCache;
}

/**
 * creates a jason object for this product then returns the string
 */
String Product::stringifyForGui() {
	if (stringifyGuiCache.length()== 0 || propertyChanged) {
		DynamicJsonBuffer jsonBuffer;
		JsonObject& json = jsonBuffer.createObject();
		json["cmd"] = 1;
		json["name_mac"] = name_mac;
		JsonArray& data = json.createNestedArray("data");
		for (int i=0; i<size; i++) {
			JsonObject& element = data.createNestedObject();
			element["typ"] = attributes[i].gui.pinType;
			element["lbl"] = attributes[i].gui.label;
			element["min"] = attributes[i].gui.min;
			element["max"] = attributes[i].gui.max;
			element["val"] = attributes[i].gui.value;
			element["grp"] = attributes[i].gui.group;
			element["id"] = attributes[i].ssid;
			element["hasPin"] = attributes[i].directPin;
		}
		json.printTo(stringifyGuiCache);
	}
	return stringifyGuiCache;
}
/**
 * creates a jason object for the values of this product then returns the string
 */
String Product::stringifyValues() {
	DynamicJsonBuffer jsonBuffer;
	JsonObject& json = jsonBuffer.createObject();
	json["core"] = WSCLIENT_DO_DISPLAY;
	json["cmd"] = CMD_VALUES;
	json["name_mac"] = name_mac;
	JsonArray& data = json.createNestedArray("data");
	for (int i=0; i<size; i++) {
		JsonObject& element = data.createNestedObject();
		element["val"] = attributes[i].gui.value;
		element["id"] = attributes[i].ssid;
	}
	String s;
	json.printTo(s);
	return s;
}
/*
 * Utility function to print the product
 */
void Product::print() {
  for (int i=0; i<size; i++) {
#ifdef DEBUG_
	  Serial.print("[Product]::print i=");Serial.print(i);
	  Serial.print("\tsize=");Serial.print(size);
      Serial.print("\tp.pin=");Serial.print(attributes[i].pin);
      Serial.print("\tp.pinType=");Serial.print(attributes[i].gui.pinType);
      Serial.print("\tp.directPin=");Serial.print(attributes[i].directPin);
      Serial.print("\tp.ssid=");Serial.print(attributes[i].ssid);
      Serial.print("\tprop.value=");Serial.println(attributes[i].gui.value);
#endif
  }
}

/*
 * returns the size of the properties array
 */
int Product::getSize() {
  return size;
}

Gui::Gui(String group, int pinType, String label, int min, int max, int value){
	gui.pinType = pinType;
	gui.label = label;
	gui.min = min;
	gui.max = max;
	gui.value = value;
	gui.group = group;
}


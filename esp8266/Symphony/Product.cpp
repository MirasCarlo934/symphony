/*
 * Product.cpp
 *
 *  Created on: Nov 15, 2018
 *      Author: cels
 */
#include "Product.h"

#define DEBUG_

Product::Product(){}

Product::Product(String name_mac, String room, String productType){
  this->room = room;
  this->productType = productType;
  this->name_mac = name_mac;
  attributes = new attribStruct[0];
}

/**
 *
 *
 * ssid		= the SSID of this property (from the COMPROPLIST table)
 * corePin	= if true (1), pin will be handled in the core Symphony.  if false (0), pin will be handled by the implementing ino class.
 * pin		= the corresponding ESP8266 pin where this property is attached.  if < 0, this component is virtual and not attached to a pin
 * gui		= the gui attributes
 * 	pinType		= the type of pin {RADIO_CTL = 1, BUTTON_CTL = 2, SLIDER_CTL = 3 , RADIO_SNSR = 5, BUTTON_SNSR = 6, SLIDER_SNSR = 7, UNDEF = 99}
			= CTL = pinMode(as.pin, OUTPUT);
			= SNSR = pinMode(as.pin, INPUT);
 * 	label		= the label to be displayed in the WebSocket page
 * 	min			= the minimum value
 * 	max			= the maximum value
 * 	value		= the actual value
 *
 */
void Product::addProperty(String ssid, boolean corePin, int8_t pin, Gui gui)
{
	size++;
	attribStruct as;
	as.ssid = ssid;
	as.corePin = corePin;
	as.pin = pin;
	as.gui = gui.gui;
#ifdef DEBUG_
	Serial.printf("SymphProduct::addProperty data ssid:%s pin:%i type:%i",as.ssid.c_str(), as.pin, as.gui.pinType);
#endif
	if (pin < 0) {
		//this property is not physical (it is virtual), no need to set the pinmode
#ifdef DEBUG_
		Serial.println("SymphProduct::addProperty this is a virtual pin.");
#endif
	} else {
		if (as.gui.pinType == BUTTON_CTL || as.gui.pinType == RADIO_CTL || as.gui.pinType == SLIDER_CTL) {
			pinMode(as.pin, OUTPUT);
#ifdef DEBUG_
			Serial.printf("SymphProduct::addProperty setting pin%d as OUTPUT.\n", as.pin);
#endif
		} else {
			pinMode(as.pin, INPUT);
#ifdef DEBUG_
			Serial.printf("SymphProduct::addProperty setting pin%d as INPUT.", as.pin);
#endif
		}
	}
	as.corePin = corePin;
	as.ssid = ssid;
#ifdef DEBUG_
	Serial.printf("SymphProduct::addProperty size=%d, sizeof=%d\n", size, sizeof(attribStruct));
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
  Serial.println("Done SymphProduct::addProperty");
#endif
}

/**
 *
 * The overloaded addProperty method
 * this is for the virtual property
 */
void Product::addProperty(String ssid, Gui gui) {
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
      Serial.print("SymphProduct::getProperty i=");Serial.print(i);
	  Serial.print("\tsize=");Serial.print(size);
	  Serial.print("\tpin=");Serial.print(attributes[i].pin);
	  Serial.print("\tp.pinType=");Serial.print(attributes[i].gui.pinType);
	  Serial.print("\tp.corepin=");Serial.print(attributes[i].corePin);
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
  ps.corePin = false;
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
		ps.corePin = false;
		ps.ssid = "NULL";
		return ps;
	}
}


/*
 * Sets the value of the product property with the given ssid
 */
void Product::setValue(String ssid, int value) {
	for (int i=0; i<size; i++) {
	    if (strcmp(ssid.c_str(), attributes[i].ssid.c_str())==0) {
#ifdef DEBUG_
      Serial.print("SymphProduct::setValue before set i=");Serial.print(i);
		  Serial.print("\t size=");Serial.print(size);
		  Serial.print("\t pin=");Serial.print(attributes[i].pin);
		  Serial.print("\t p.pinType=");Serial.print(attributes[i].gui.pinType);
		  Serial.print("\t p.corepin=");Serial.print(attributes[i].corePin);
		  Serial.print("\t p.ssid=");Serial.print(attributes[i].ssid);
		  Serial.print("\t prop.value=");Serial.println(attributes[i].gui.value);
#endif
	      attributes[i].gui.value = value;
	      Serial.print("\t value is set to ");Serial.println(attributes[i].gui.value);
	    }
	  }
}
/**
 * creates a jason object for this product then returns the string
 */
String Product::stringify() {
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
		element["hasPin"] = attributes[i].corePin;
	}
	String s;
	json.printTo(s);
	return s;
}
/**
 * creates a jason object for the values of this product then returns the string
 */
String Product::stringifyValues() {
	DynamicJsonBuffer jsonBuffer;
	JsonObject& json = jsonBuffer.createObject();
	json["core"] = 20;
	json["cmd"] = 2;
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
	  Serial.print("SymphProduct::print i=");Serial.print(i);
	  Serial.print("\tsize=");Serial.print(size);
      Serial.print("\tp.pin=");Serial.print(attributes[i].pin);
      Serial.print("\tp.pinType=");Serial.print(attributes[i].gui.pinType);
      Serial.print("\tp.corepin=");Serial.print(attributes[i].corePin);
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


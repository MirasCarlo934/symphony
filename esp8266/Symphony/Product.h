/*
 * Product.h

 *  name_mac	= name of this device (includes mac address)
    room		= room where this device is located;
    productType	= the product type;
 *
 * ssid		= the SSID of this property (from the COMPROPLIST table)
 * directPin	= if true (1), pin will be set directly with the value in the core Symphony.  if false (0), pin will be handled by the callback ino class since a computation might need to be done first before setting the pin value.
 * pin		= the corresponding ESP8266 pin where this property is attached.  if < 0, this component is virtual and not attached to a pin
 * gui		= the gui attributes
 * 	pinType		= the type of pin {RADIO_CTL = 1, BUTTON_CTL = 2, SLIDER_CTL = 3 , RADIO_SNSR = 5, BUTTON_SNSR = 6, SLIDER_SNSR = 7, UNDEF = 99}
			= CTL = pinMode(as.pin, OUTPUT);
			= SNSR = pinMode(as.pin, INPUT);
 * 	label		= the label to be displayed in the WebSocket page
 * 	min			= the minimum value
 * 	max			= the maximum value
 * 	value		= the actual value

	{"name_mac":"NAME", "room":"ROOM", "type":"Product Type",
		"props" :[
			{"ssid":"SSID", "directPin":1, "pin":12, "gui":{"type":"PINTYPE", "lbl":"LABEL", "min":"MIN", "max":"MAX", "value":1, "grp":"GROUP"}},
			{"ssid":"SSID", "directPin":1, "pin":12, "gui":{"type":"PINTYPE", "lbl":"LABEL", "min":"MIN", "max":"MAX", "value":1, "grp":"GROUP"}}
		]
	}


 */

#ifndef SYMPHONYPRODUCT_H_
#define SYMPHONYPRODUCT_H_

#include <Arduino.h>
#include <ArduinoJson.h>
#include "DataObjects.h"

/*
 * _CTL means pinmode is OUTPUT and GUI displays this as an enabled element that can control the device
 * _SNSR means pinmode is INPUT and GUI displays this as a disabled element that shows values from the state of the device
 * 		all sensors have value greater than or equal to 50
 *
 */
#define RADIO_CTL 10		//displays a radio button, enabled
#define BUTTON_CTL 20		//displays a checkbox button, enabled
#define SLIDER_CTL 30		//displays a slider button that opens a slider element, enabled
#define RADIO_SNSR 50		//displays a radio button, disabled
#define BUTTON_SNSR 60		//displays a checkbox button, disabled
#define SLIDER_SNSR 70		//displays a slider button that opens a slider element, disabled
#define RADIO_SUBMIT 80		//displays a radio button that (does not immediately send WS data), enabled
#define BUTTON_SUBMIT 81	//displays a checkbox button (does not immediately send WS data), enabled
#define SLIDER_SUBMIT 82	//displays a slider button that opens a slider element (does not immediately send WS data), enabled
#define SUBMIT 83			//displays a true button (submits data from all the _SUBMIT elements), enabled

/*
 * This is for the display of the component in the html
 */
struct guiStruct {
	int pinType;
	String label;
	int min;
	int max;
	int value;
	String group;
};

struct attribStruct {
  String ssid;
  boolean directPin = false;
  int8_t pin = 12;
  guiStruct gui;
};

class Gui {
public:
	guiStruct gui;
	Gui(String group, int pinType, String label, int min, int max, int value);
};
/*
 *
 */
class Product {
  public:
    String room;
    String productName;
    String name_mac = "";
    attribStruct *attributes = NULL;

    Product();
    Product(String name_mac, String room, String productName);
    /*
     * 	sets the callback that will handle changes in property values
     * 	forHub = 	true : triggers transaction to the hub
     * 				false: only for propagating to display clients, does not trigger transaction to hub
     */
    void setValueChangeCallback(int (* Callback) (int propertyIndex, boolean forHub));
    /**
     * Adds a property to this device.
     * ssid		= the SSID of this property (from the COMPROPLIST table)
     * pin		= the corresponding ESP8266 pin where this property is attached.  if < 0, this component is virtual and not attached to a pin
     * gui		= the gui attributes
     *
     *      directPin will be set to true (1), pin will be handled in the core Symphony.
     */
    void addProperty(String ssid, int8_t pin, Gui gui);
    /**
	 * Adds a property to this device.
	 * ssid		= the SSID of this property (from the COMPROPLIST table)
	 * pin		= the corresponding ESP8266 pin where this property is attached.  if < 0, this component is virtual and not attached to a pin
	 * gui		= the gui attributes
	 *
	 * 	 directPin will be set to false(0) pin will be handled by the implementing ino class by providing callback functions.
	 * 	 	callback functions might need to do some calculation first before setting the value of the pi.
	 */
    void addCallableProperty(String ssid, int8_t pin, Gui gui);
    /**
     * For virtual properties that are not physically connected to a pin.
     * Implementing ino class would need to handle the websocket and mqtt transactions by providing callback functions.
     */
    void addVirtualProperty(String ssid, Gui gui);

    attribStruct getProperty(String ssid);
    attribStruct getKeyVal(int index);
    void setValue(String ssid, int value, boolean forHub);
    String stringify();
    String stringifyValues();
    void print();
    int getSize();

  private:
    int pIndex = 0;
    int size = 0;
    /**
     * Adds a property to this device.
     * ssid		= the SSID of this property (from the COMPROPLIST table)
     * directPin
     * 		- if true (1), pin will be handled in the core Symphony.
     * 		- if false(0), pin will be handled by the implementing ino class by providing callback functions.
	 * 	 		callback functions might need to do some calculation first before setting the value of the pi.
     * pin		= the corresponding ESP8266 pin where this property is attached.  if < 0, this component is virtual and not attached to a pin
     * gui		= the gui attributes
     */
    void addProperty(String ssid, boolean directPin, int8_t pin, Gui gui);
};


#endif /* SYMPHONYPRODUCT_H_ */

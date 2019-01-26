#include "Arduino.h"
#include "SymphonyCore.h"

//#include "MqttUtil.h"

Symphony s = Symphony();
SymphProduct product = SymphProduct();

/*
 * Callback for Websocket events
 */
int WsCallback(uint8_t * payload, size_t length) {
	WsData wsdata = WsData(payload, length);
	Serial.printf("WsCallback payload=%s ssid=%s value=%s\n", payload, wsdata.getSSID().c_str(), wsdata.getValue().c_str());
	product.setValue(wsdata.getSSID(), atoi(wsdata.getValue().c_str()));
	return 0;
}
/*
 * Callback for MQTT events
 */
attribStruct MyMqttCallback(attribStruct property, int scmd) {
	MqttUtil::product.setValue(property.ssid, scmd);
	attribStruct returnPs;
	returnPs.pin = 15;
	returnPs.gui.value = scmd;
	returnPs.gui.pinType = SLIDER_OUT;
	return returnPs;
}

void setup()
{
	Serial.begin(115200);
	Serial.println("************START setup***************");
	product.productType = SpiffsUtil::readProductID();
	product.room = SpiffsUtil::readRoom();
	product.name = SpiffsUtil::readDisplayName();
	if (product.name.length() == 0)
		product.name = "symphony";
	product.addProperty(1, "0006", true, 12, SymphProduct::createGui("Button", BUTTON_OUT, "Switch", 0, 1, 0));//1, 12, BUTTON_OUT, true, "0006", "Switch", 0, 1, 0);
	product.print();
	s.setProduct(product);  //always set the product first before running the setup
	s.setWsCallback(&WsCallback);
	s.setMqttCallback(&MyMqttCallback);
	s.setup();
	Serial.println("************END setup***************");
}

void loop()
{
	s.loop();
}

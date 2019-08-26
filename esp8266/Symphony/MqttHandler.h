/*
 * MqttHandler.h
 *
 * Handler for MQTT
 *  includes connecting to the MQTT Server and parsing of the value objects.
 *
 *  Created on: Aug 25, 2019
 *      Author: cels
 */
#include <ESP8266WiFi.h>
#include <PubSubClient.h>

#ifndef MQTTHANDLER_H
#define MQTTHANDLER_H

class MqttHandler {
	public:
	MqttHandler();
	boolean connect(const char *id, const char *url, int port, WiFiClient wc); //to connect to MQTT server
	boolean loop(); //called in the loop of arduino
	private:
};


#endif /* MQTTHANDLER_H */

/*
 * MqttHandler.h
 *
 * Async Handler for MQTT
 *  includes connecting to the MQTT Server and parsing of the value objects.
 *
 *  Created on: Aug 25, 2019
 *      Author: cels
 */
#include <ESP8266WiFi.h>
#include <AsyncMqttClient.h>

#ifndef MQTTHANDLER_H
#define MQTTHANDLER_H

class MqttHandler {
	public:
	MqttHandler();
	void connect(const char *id, const char *url, int port); //to connect to MQTT server
	private:
};


#endif /* MQTTHANDLER_H */

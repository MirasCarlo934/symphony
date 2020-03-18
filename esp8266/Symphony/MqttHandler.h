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
#include "Product.h"

#ifndef MQTTHANDLER_H
#define MQTTHANDLER_H

class MqttHandler {
	public:
	boolean enabled = false;

	MqttHandler();
	//generic methods
	void setMsgCallback(void (* Callback) (char* topic, char* payload, size_t len));
	void setUrl(const char *url);	//sets the URL, can be uised if MqttHandler was instantiated using the default constructor
	void setPort(int port);	//sets the port, can be uised if MqttHandler was instantiated using the default constructor
	void setId(const char *id);	//sets the id, can be uised if MqttHandler was instantiated using the default constructor
	void connect(); //to connect to MQTT server. id, url and port should already have been set
	void publish(const char* payload, uint8_t qos); //publish message to mqtt server
	String getMyTopic(); //the topic where this device listens for control commands
	bool isConnected();

	private:
};


#endif /* MQTTHANDLER_H */

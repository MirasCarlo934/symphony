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
	MqttHandler();
	void setUrl(const char *url);	//sets the URL, can be uised if MqttHandler was instantiated using the default constructor
	void setPort(int port);	//sets the port, can be uised if MqttHandler was instantiated using the default constructor
	void setId(const char *id);	//sets the id, can be uised if MqttHandler was instantiated using the default constructor
	void setProduct(Product p);	//sets the Product definition of this device.  This will be used to communicate with other devices
	void connect(); //to connect to MQTT server. id, url and port should already have been set
	void publish(const char* payload, uint8_t qos); //publish message to mqtt server
	private:
};


#endif /* MQTTHANDLER_H */

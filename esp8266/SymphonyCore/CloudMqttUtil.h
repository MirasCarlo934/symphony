/*
 * CloudMqttUtil.h
 *
 * Utility Helper for connecting to the Cloud MQTT Server and parsing of the value objects.
 * We will be using Json objects.
 *
 *  Created on: June 25, 2017
 *      Author: cels
 */

#ifndef CLOUDMQTTUTIL_H_
#define CLOUDMQTTUTIL_H_

#include <ArduinoJson.h>
#include <PubSubClient.h>
#include <ESP8266WiFi.h>
#include "SymphonyProduct.h"
#include <ArduinoJson.h>

#define DEBUG_

class CloudMqttUtil {
	public:
	CloudMqttUtil();
	static PubSubClient *client;
	static const char *clientID;
	static WiFiClient wifiClient;
	static const char* server;
	static const char* user;
	static const char* pwd;
	static int mqttPort;
	static boolean connect(const char *id, const char *hostname, PubSubClient *c, WiFiClient wc); //to connect to MQTT server
	static void sendCommand(String ssid, int value);  //should return a response code
};


#endif /* CLOUDMQTTUTIL_H_ */

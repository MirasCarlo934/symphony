/*
 * CloudMqttUtil.cpp
 *
 * Utility Helper for connecting to the Cloud MQTT Server.
 *
 *  Created on: June 25, 2017
 *      Author: cels
 */
#include "CloudMqttUtil.h"

const char* CloudMqttUtil::server = "m11.cloudmqtt.com";
int CloudMqttUtil::mqttPort = 18261;
const char* CloudMqttUtil::clientID = "defaultID";
const char* CloudMqttUtil::user = "bmxbtbcf";
const char* CloudMqttUtil::pwd = "X1CaJmtiIf05";
WiFiClient CloudMqttUtil::wifiClient;
PubSubClient *CloudMqttUtil::client;
//SymphProduct CloudMqttUtil::product;

#define DEBUG_

/*
 * connects to the mqtt server
 * MQTT.connect (clientID, willTopic, willQoS, willRetain, willMessage)
 */
boolean CloudMqttUtil::connect(const char *id, const char *hostname, PubSubClient *c, WiFiClient wc){ //to connect to MQTT server
    boolean isConnected = false;
    CloudMqttUtil::clientID = id;
//    CloudMqttUtil::product = p;

    PubSubClient pubsubclient = PubSubClient (CloudMqttUtil::server, CloudMqttUtil::mqttPort, CloudMqttUtil::wifiClient);

    CloudMqttUtil::client = &pubsubclient;
    CloudMqttUtil::client = c;
    CloudMqttUtil::client->setServer(CloudMqttUtil::server, CloudMqttUtil::mqttPort);

    CloudMqttUtil::client->setClient(wifiClient);

	if (CloudMqttUtil::client->connect(CloudMqttUtil::clientID, CloudMqttUtil::user, CloudMqttUtil::pwd)) {
		isConnected = true;
		String payload = hostname; payload += " is alive! ";
		IPAddress ip = WiFi.localIP();
		payload += " ip is "; payload += ip.toString();
		#ifdef DEBUG_
		    Serial.print("\nsending IP to CloudMqtt:");Serial.println(payload);
		    Serial.print("\tpayload length to CloudMqtt:");Serial.println(payload.length());
		#endif
		if ( client->publish("/Symphony", (byte*) payload.c_str(), payload.length())) {
		  Serial.println("\tPublish to CloudMqtt success.");
		} else {
		  Serial.println("\tPublish to CloudMqtt failed ");
		}
	}
    return isConnected;
}

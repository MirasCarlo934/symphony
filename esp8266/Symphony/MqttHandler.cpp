/*
 * MqttHandler.cpp
 *
 *
 * Handler for MQTT
 *  includes connecting to the MQTT Server and parsing of the value objects.
 *
 *  Created on: Aug 25, 2019
 *      Author: cels
 */
#include "MqttHandler.h"

const char* mqttServer = "192.168.1.5";
int mqttPort = 1883;
PubSubClient pbClient;
boolean isConnected = false;



/*
 * callback for MQTT when a message arrives from the MQTT server
 * This calls the parseDefaultTopic to parse the response from default_topic.
 * The initial response of Bm from the SignIn is to the default_topic.
 * Response contains the topic that we need to listen to.
 */
void callback(char* topic, uint8_t* payload, unsigned int length) {
	Serial.println("\t\t[MqttHandler] ************** Message arrived:");
	Serial.println("\t\t[MqttHandler] ************** topic: " + String(topic));
	Serial.println("\\t\t[MqttHandler] ************** Length: " + String(length,DEC));
	// create character buffer with ending null terminator (string)
	char message_buff[200];   // initialise storage buffer
	int i = 0;
	for(i=0; i<length; i++) {
		message_buff[i] = payload[i];
	}
	message_buff[i] = '\0';
	if (strcmp(topic,"default_topic")==0) {

	} else if(strcmp(topic,"error_topic")==0) {
	//do nothing
	} else {

	}
}

/**
 * The default constructor
 */
MqttHandler::MqttHandler() {

}
/**
 * Connect to the MQTT server
 *  id = the identifier for this device (can use the MAC address)
 *  url = the server address
 *  port = the port of the mqtt server
 *  wc = WifiClient for the handling of events
 */
boolean MqttHandler::connect(const char *id, const char *url, int port, WiFiClient wc) { //to connect to MQTT server


	//copy the parameters to the global variables
	mqttServer = url;
	mqttPort = port;
	pbClient = PubSubClient(mqttServer, mqttPort, wc);
	pbClient.setCallback(callback);
	Serial.println("\t\t[MqttHandler] ************** connect 1");
	if (pbClient.connect(id, "/bm/WillTopic", 0, false, "Will Message")) {
		isConnected = true;
		Serial.println("\t\t[MqttHandler] ************** connected");
		if (pbClient.subscribe("control")) {
			Serial.println("\t\t[MqttHandler] ************** Subscribe to default_topic successful");
		} else {
			Serial.println("\t\t[MqttHandler] ************** Subscribe to default_topic failed");
		}
		String payload = "test lang ito";
		if ( pbClient.publish("BM", (byte*) payload.c_str(), payload.length())) {
			Serial.println("\t\t[MqttHandler] ************** Publish to BM success.");
		} else {
			Serial.println("\t\t[MqttHandler] ************** Publish to BM failed ");
		}
	} else {
		Serial.println("[MqttHandler] ************** not connected");
	}
}

/**
 * The function called in the loop of arduino
 */
boolean MqttHandler::loop() {
	pbClient.loop();
}

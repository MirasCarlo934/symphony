/*
 * MqttHandler.cpp
 *
 *
 * Async Handler for MQTT
 *  includes connecting to the MQTT Server and parsing of the value objects.
 *
 *  Created on: Aug 25, 2019
 *      Author: cels
 */
#include "MqttHandler.h"

AsyncMqttClient mqttClient;
const char* myId = "myMqttID";
const char* mqttServer = "localhost";
String myTopic = "BM/";
int mqttPort = 1883;

boolean connected = false;

/*
 * This is the callback handler in Symphony.cpp that will be called when a message arrives.
 */
void (* msgCallback) (char* topic, char* payload, size_t len);

void onMqttConnect(bool sessionPresent) {
  Serial.println("[MqttHandler] ************** Connected to MQTT.");
//  Serial.print("Session present: ");
//  Serial.println(sessionPresent);
  myTopic += myId;
  uint16_t packetIdSub = mqttClient.subscribe(myTopic.c_str(), 0);
//  Serial.print("Subscribing at QoS 2, packetId: ");
//  Serial.println(packetIdSub);
//  mqttClient.publish("BM", 0, true, "test 1");
//  Serial.println("[MqttHandler] ************** Publishing at QoS 0");
//  uint16_t packetIdPub1 = mqttClient.publish("BM", 1, true, "test 2");
//  Serial.print("Publishing at QoS 1, packetId: ");
//  Serial.println(packetIdPub1);
//  uint16_t packetIdPub2 = mqttClient.publish("BM", 2, true, "test 3");
//  Serial.print("Publishing at QoS 2, packetId: ");
//  Serial.println(packetIdPub2);
//  uint16_t packetIdPub3 = mqttClient.publish("BM", 2, true, thisProduct.stringify().c_str());
  connected = true;

}

void onMqttDisconnect(AsyncMqttClientDisconnectReason reason) {
  Serial.println("[MqttHandler] ************** Disconnected from MQTT.");
}

void onMqttSubscribe(uint16_t packetId, uint8_t qos) {
  Serial.println("[MqttHandler] ************** Subscribe acknowledged.");
  Serial.print("  packetId: ");
  Serial.println(packetId);
  Serial.print("  qos: ");
  Serial.println(qos);
}

void onMqttUnsubscribe(uint16_t packetId) {
  Serial.println("[MqttHandler] ************** Unsubscribe acknowledged.");
  Serial.print("  packetId: ");
  Serial.println(packetId);
}

void onMqttMessage(char* topic, char* payload, AsyncMqttClientMessageProperties properties, size_t len, size_t index, size_t total) {
  Serial.println("[MqttHandler] ************** Messsage received.");
//  Serial.print("[MqttHandler] **************   topic: ");
//  Serial.println(topic);
//  Serial.print("[MqttHandler]   qos: ");
//  Serial.print(properties.qos);
//  Serial.print(",  dup: ");
//  Serial.print(properties.dup);
//  Serial.print(",  retain: ");
//  Serial.print(properties.retain);
//  Serial.print(",  len: ");
//  Serial.print(len);
//  Serial.print(",  index: ");
//  Serial.print(index);
//  Serial.print(",  total: ");
//  Serial.println(total);
//  Serial.print("[MqttHandler]   payload: ");
//  char str2[len];
//  strncpy ( str2, payload, len );
//  Serial.println(str2);
  msgCallback(topic, payload,len);
}

void onMqttPublish(uint16_t packetId) {
  Serial.println("[MqttHandler] ************** Publish acknowledged.");
  Serial.print(" packetId: ");
  Serial.println(packetId);
}

/**
 * The default constructor
 */
MqttHandler::MqttHandler() {

}
/*
 *
 * Sets the callback that will do the parsing of the mqtt message
 * This will be from Symphony.cpp
 *
 */
void MqttHandler::setMsgCallback(void (* Callback) (char* topic, char* payload, size_t len)) {
	msgCallback= Callback;
}
/**
 * sets the URL, can be uised if MqttHandler was instantiated using the default constructor
 */
void MqttHandler::setUrl(const char *url) {
	Serial.printf("[MqttHandler] ************** url=%s\n", url);
	mqttServer = url;
}
/**
 * sets the port, can be uised if MqttHandler was instantiated using the default constructor
 */
void MqttHandler::setPort(int port) {
	mqttPort = port;
}
/**
 * sets the id, can be uised if MqttHandler was instantiated using the default constructor
 */
void MqttHandler::setId(const char *id) {
	myId = id;
}

/**
 * Connect to the MQTT server
 */
void MqttHandler::connect() { //to connect to MQTT server
	Serial.println("[MqttHandler] ************** Connecting.");
	mqttClient.setClientId(myId);
	mqttClient.onConnect(onMqttConnect);
	mqttClient.onDisconnect(onMqttDisconnect);
	mqttClient.onSubscribe(onMqttSubscribe);
	mqttClient.onUnsubscribe(onMqttUnsubscribe);
	mqttClient.onMessage(onMqttMessage);
	mqttClient.onPublish(onMqttPublish);
	Serial.printf("[MqttHandler] ************** url=%s port=%i\n", mqttServer, mqttPort);
	mqttClient.setServer(mqttServer, mqttPort);

	mqttClient.connect();
}

/**
 * Publish data to the mqtt server
 */
void MqttHandler::publish(const char* payload, uint8_t qos) {
	Serial.printf("[MqttHandler] ************** Publishing at QoS %i\n", qos);
	mqttClient.publish("BM", qos, true, payload);
}

/**
 * Return the value of connection status
 */
bool  MqttHandler::isConnected() {
	return connected;
}

/**
 * Returns the topic where this device listens for control commands
 */
String MqttHandler::getMyTopic() {
	return myTopic;
}

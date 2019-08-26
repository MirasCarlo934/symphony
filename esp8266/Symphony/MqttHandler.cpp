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

const char* mqttServer = "192.168.1.5";
int mqttPort = 1883;
AsyncMqttClient mqttClient;
boolean isConnected = false;

void onMqttConnect(bool sessionPresent) {
  Serial.println("\t\t[MqttHandler] ************** Connected to MQTT.");
  Serial.print("Session present: ");
  Serial.println(sessionPresent);
  uint16_t packetIdSub = mqttClient.subscribe("test/lol", 2);
  Serial.print("Subscribing at QoS 2, packetId: ");
  Serial.println(packetIdSub);
  mqttClient.publish("test/lol", 0, true, "test 1");
  Serial.println("\t\t[MqttHandler] ************** Publishing at QoS 0");
  uint16_t packetIdPub1 = mqttClient.publish("test/lol", 1, true, "test 2");
  Serial.print("Publishing at QoS 1, packetId: ");
  Serial.println(packetIdPub1);
  uint16_t packetIdPub2 = mqttClient.publish("test/lol", 2, true, "test 3");
  Serial.print("Publishing at QoS 2, packetId: ");
  Serial.println(packetIdPub2);
}

void onMqttDisconnect(AsyncMqttClientDisconnectReason reason) {
  Serial.println("\t\t[MqttHandler] ************** Disconnected from MQTT.");
}

void onMqttSubscribe(uint16_t packetId, uint8_t qos) {
  Serial.println("\t\t[MqttHandler] ************** Subscribe acknowledged.");
  Serial.print("  packetId: ");
  Serial.println(packetId);
  Serial.print("  qos: ");
  Serial.println(qos);
}

void onMqttUnsubscribe(uint16_t packetId) {
  Serial.println("\t\t[MqttHandler] ************** Unsubscribe acknowledged.");
  Serial.print("  packetId: ");
  Serial.println(packetId);
}

void onMqttMessage(char* topic, char* payload, AsyncMqttClientMessageProperties properties, size_t len, size_t index, size_t total) {
  Serial.println("\t\t[MqttHandler] ************** Publish received.");
  Serial.println("\t\t[MqttHandler] **************   topic: ");
  Serial.println(topic);
  Serial.print("  qos: ");
  Serial.println(properties.qos);
  Serial.print("  dup: ");
  Serial.println(properties.dup);
  Serial.print("  retain: ");
  Serial.println(properties.retain);
  Serial.print("  len: ");
  Serial.println(len);
  Serial.print("  index: ");
  Serial.println(index);
  Serial.print("  total: ");
  Serial.println(total);
}

void onMqttPublish(uint16_t packetId) {
  Serial.println("\t\t[MqttHandler] ************** Publish acknowledged.");
  Serial.print(" packetId: ");
  Serial.println(packetId);
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
void MqttHandler::connect(const char *id, const char *url, int port) { //to connect to MQTT server
	Serial.println("\t\t[MqttHandler] ************** Connecting.");
	//copy the parameters to the global variables
	mqttServer = url;
	mqttPort = port;
	mqttClient.onConnect(onMqttConnect);
	mqttClient.onDisconnect(onMqttDisconnect);
	mqttClient.onSubscribe(onMqttSubscribe);
	mqttClient.onUnsubscribe(onMqttUnsubscribe);
	mqttClient.onMessage(onMqttMessage);
	mqttClient.onPublish(onMqttPublish);
	mqttClient.setServer(mqttServer, mqttPort);

	mqttClient.connect();
}


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
String myId = "myMqttID";
const char* mqttServer = "localhost";
String subscribeTopic = "things/";
String publishTopic = "BM/";
String willTopic = "lastWill";
int mqttPort = 1883;
long timerMillis = 0, reconnectIntervalMillis = 10000;	//default 10 second reconnect interval

boolean connected = false, doReconnect = false;

/*
 * This is the callback handler in Symphony.cpp that will be called when a message arrives.
 */
void (* msgCallback) (char* topic, String payload, size_t len);

void onMqttConnect(bool sessionPresent) {
  Serial.println("[MQTT] Connected to MQTT.");
  /* We will subscribe to the following topics:
   *  	1. things/{uid}/parentGroups			for changes in the device parentGroups properties
   *  	2. things/{uid}/name					for changes in the device name properties
   *  	3. things/{uid}/attributes/#			for changes in the attribute values
   */
  subscribeTopic = "things/" + myId ;
  uint16_t packetIdSubMain = mqttClient.subscribe(subscribeTopic.c_str(), 0);
  String s1 = subscribeTopic +"/"+ "parentGroups";
  uint16_t packetIdSub1 = mqttClient.subscribe(s1.c_str(), 0);
  String s2 = subscribeTopic +"/"+ "name";
  uint16_t packetIdSub2 = mqttClient.subscribe(s2.c_str(), 0);
  String s3 = subscribeTopic +"/"+ "attributes/#";
  uint16_t packetIdSub3 = mqttClient.subscribe(s3.c_str(), 0);
  publishTopic = "BM/" + myId;
  Serial.printf("[MQTT] subscribed to topics \n\t%s\n\t%s\n\t%s\n\t%s\n", subscribeTopic.c_str(),s1.c_str(), s2.c_str(), s3.c_str());
  connected = true;
  doReconnect = false;
}

void onMqttDisconnect(AsyncMqttClientDisconnectReason reason) {
  Serial.printf("[MQTT] Disconnected from MQTT reason %i.\n", reason);
  //we send a message to the callback handler with topic="reboot" and len=reason
  connected = false;
  doReconnect = true;
}

void onMqttSubscribe(uint16_t packetId, uint8_t qos) {
  Serial.println("[MQTT] Subscribe acknowledged.");
  Serial.print("  packetId: ");
  Serial.println(packetId);
  Serial.print("  qos: ");
  Serial.println(qos);
}

void onMqttUnsubscribe(uint16_t packetId) {
  Serial.println("[MQTT] Unsubscribe acknowledged.");
  Serial.print("  packetId: ");
  Serial.println(packetId);
}

String payloadBuf;	//this is the buffer for messages that may be sent in multiple packets
void onMqttMessage(char* topic, char* payload, AsyncMqttClientMessageProperties properties, size_t len, size_t index, size_t total) {
  Serial.println("[MQTT] Messsage received.");
  Serial.print("[MQTT] **************   topic: ");
  Serial.println(topic);
  Serial.print("[MQTT]   qos: ");
  Serial.print(properties.qos);
  Serial.print(",  dup: ");
  Serial.print(properties.dup);
  Serial.print(",  retain: ");
  Serial.print(properties.retain);
  Serial.print(",  len: ");
  Serial.print(len);
  Serial.print(",  index: ");
  Serial.print(index);
  Serial.print(",  total: ");
  Serial.println(total);
  Serial.print("[MQTT]   payload: ");
  char str2[len];
  strncpy ( str2, payload, len );
  Serial.println(str2);
  if (index == 0) {
	payloadBuf = "";
  }
  auto pl = len;
  auto p = payload;
  while (pl--) {
	payloadBuf += *(p++);
  }
  if (index + len == total) {
	/// Do your stuff with complete payloadBuf here
	msgCallback(topic, payloadBuf, total);
  }

}

void onMqttPublish(uint16_t packetId) {
  Serial.println("[MQTT] Publish acknowledged.");
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
void MqttHandler::setMsgCallback(void (* Callback) (char* topic, String payload, size_t len)) {
	msgCallback= Callback;
}
/**
 * sets the URL, can be uised if MqttHandler was instantiated using the default constructor
 */
void MqttHandler::setUrl(const char *url) {
	Serial.printf("[MQTT] url=%s\n", url);
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
	Serial.println("[MQTT] Connecting.");
	mqttClient.setClientId(myId.c_str());
	mqttClient.onConnect(onMqttConnect);
	mqttClient.onDisconnect(onMqttDisconnect);
	mqttClient.onSubscribe(onMqttSubscribe);
	mqttClient.onUnsubscribe(onMqttUnsubscribe);
	mqttClient.onMessage(onMqttMessage);
	mqttClient.onPublish(onMqttPublish);
	willTopic = "BM/" + myId + "/active";
	mqttClient.setWill(willTopic.c_str(), 1, false, "false", 5);
	Serial.printf("[MQTT] url=%s port=%i\n", mqttServer, mqttPort);
	mqttClient.setServer(mqttServer, mqttPort);
	mqttClient.connect();
}

/**
 * Publish data to the mqtt server
 */
void MqttHandler::publish(const char* publishTopic, const char* payload) { //publish message to mqtt server
	Serial.printf("[MQTT] Publishing in topic %s\n", publishTopic);
	mqttClient.publish( publishTopic, 0, false, payload); //we are setting QOS of 0 to prevent from multiple sending of messages
}
/**
 * Publish data to the mqtt server
 */
void MqttHandler::publish(const char* payload) { //publish message to mqtt server
	Serial.printf("[MQTT] Publishing in topic %s\n", publishTopic.c_str());
	mqttClient.publish( publishTopic.c_str(), 0, false, payload); //we are setting QOS of 0 to prevent from multiple sending of messages
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
String MqttHandler::getSubscribedTopic() {
	return subscribeTopic;
}

/**
 * Returns the topic where this device sends messages to BM
 */
String MqttHandler::getPublishTopic() {
	return publishTopic;
}

/**
 * sets the reconnect interval
 */
void MqttHandler::setReconnectInterval(long interval) {
	reconnectIntervalMillis = interval;
}
/**
 * tries to reconnect to MQTT host using the interval set above
 */
void MqttHandler::reconnect() {
	if ( enabled && doReconnect) {
		if (millis() - timerMillis >= reconnectIntervalMillis) {
			Serial.println("[MQTT] Reconnecting.");
			mqttClient.connect();
			timerMillis = millis();
		}
	}
}

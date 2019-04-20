/*
 * This is the Symphony Socket
 * It is an ordinary wifi controllable socket.
 * But if it receives E1.31 data, it will synchronize to that if it receives a universe it recognizes.
 */

#include "Arduino.h"
#include "Symphony.h"
#include <WebSocketsClient.h>

#define MY_VERSION 2.21

String myName = "WebSocketClient";

long timerMilli = 0;


Product product;
Symphony s = Symphony();

WebSocketsClient webSocketClient;

void webSocketClientEvent(WStype_t type, uint8_t * payload, size_t length) {

	switch(type) {
		case WStype_DISCONNECTED:
			Serial.printf("[WSc] Disconnected!\n");
			break;
		case WStype_CONNECTED: {
			Serial.printf("[WSc] Connected to url: %s\n", payload);
			// send message to server when Connected
			webSocketClient.sendTXT("Connected");
		}
			break;
		case WStype_TEXT:
			Serial.printf("[WSc] get text: %s\n", payload);

			// send message to server
			// webSocket.sendTXT("message here");
			break;
		case WStype_BIN:
			Serial.printf("[WSc] get binary length: %u\n", length);
			hexdump(payload, length);

			// send data to server
			// webSocket.sendBIN(payload, length);
			break;
	}

}

/**
 * Start connection to the Websocket
 */
int startWebSocketClients() {
	// server address, port and URL
	webSocketClient.begin("192.168.0.111", 80, "/ws");

	// event handler
	webSocketClient.onEvent(webSocketClientEvent);
	// try ever 5000 again if connection has failed
	webSocketClient.setReconnectInterval(5000);

	// start heartbeat (optional)
	// ping server every 15000 ms
	// expect pong from server within 3000 ms
	// consider connection disconnected if pong is not received 2 times
	webSocketClient.enableHeartbeat(15000, 3000, 2);
}

void handleInit(AsyncWebServerRequest *request) {
	Serial.println("Start handleInit");
	DynamicJsonBuffer jsonBuffer;
	JsonObject& reply = jsonBuffer.createObject();
	reply["core"] = 7;
	reply["cmd"] = 10;
	reply["ssid"] = "0001";
	reply["cid"] = 25;
	reply["val"] =1;
	//{"cmd":10,"mac":"socket_5ccf7fc7fb7d","ssid":"0001","cid":29,"val":1}
	String replyStr;
	reply.printTo(replyStr);
	webSocketClient.sendTXT(replyStr);
	request->send(200, "text/html", "WS Client executed.");
	Serial.println("End handleInit");
}


/*
 * Callback function for the websocket transactions
 */
int wsHandler(AsyncWebSocket ws, AsyncWebSocketClient *client, JsonObject& json) {
//int wsHandler(AsyncWebSocket ws, AsyncWebSocketClient *client, uint8_t * payload, size_t len) {
	Serial.println("callback executed start");
	json.prettyPrintTo(Serial);
	if (json.containsKey("cmd")) {
			uint8_t cmd = json["cmd"];
			switch (cmd) {
			case 10:
				DynamicJsonBuffer jsonBuffer;
				JsonObject& reply = jsonBuffer.createObject();
				reply["core"] = 7;
				reply["cmd"] = 10;
				reply["ssid"] = "0001";
				reply["cid"] = 25;
				reply["val"] = json["val"];
				String replyStr;
				reply.printTo(replyStr);
				webSocketClient.sendTXT(replyStr);
				break;
			}
	}
	Serial.println("\ncallback executed end");
}

void setup()
{
	Serial.begin(115200);
	delay(10);
	Serial.println("\n\n************START WSClient Setup***************");
	s.setWsCallback(wsHandler);
	s.setup(myName);
	s.on("/init", HTTP_GET, handleInit);
	product = Product(s.myName, "Test", "Client");
	Gui gui1 = Gui("Client", BUTTON_CTL, "test", 0, 1, 1);
	product.addProperty("0001", false, 13, gui1);//add aproperty that has an attached pin
	s.setProduct(product);

	startWebSocketClients();

	Serial.printf("\n************END WSClient Setup Version%.2f***************", MY_VERSION);
}

// The loop function is called in an endless loop
void loop() {

	if (s.loop()) {
		webSocketClient.loop();
		if (millis() - timerMilli >= 1000) {
			timerMilli = millis();
		}
	}
}

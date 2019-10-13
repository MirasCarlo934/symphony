/*
 * This is the Symphony Firebase Test code
 * We will be using direct websocket to connect to firebase
 */

#include "Arduino.h"
#include "Symphony.h"
#include "version.h"
#include <WebSocketsClient.h>

Product product;
Symphony s = Symphony();
WebSocketsClient webSocket;

void handleInit(AsyncWebServerRequest *request) {
	char ver[10];
	sprintf(ver, "%u.%u", SYMPHONY_VERSION, MY_VERSION);
	String s = "Current symphony firebase version is ";
	s+= ver;
	request->send(200, "text/html", s.c_str());
	Serial.println("handleInit");
}

void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {

	switch(type) {
		case WStype_DISCONNECTED:
			Serial.printf("[*******WSc*******] Disconnected!\n");
			break;
		case WStype_CONNECTED: {
			Serial.printf("[*******WSc*******] Connected to url: %s\n", payload);

		}
			break;
		case WStype_TEXT:
		{
			/*
				We will receive data of these forms
				form1. data: "{"t":"c","d":{"t":"h","d":{"ts":1570898661383,"v":"5","h":"s-usc1c-nss-231.firebaseio.com","s":"f6RW480tyjkw24zajm2KeNOIUYb42H5R"}}}"
				form2. data: "{"t":"d","d":{"r":2,"b":{"s":"ok","d":{}}}}"
				form3. data: "{"t":"d","d":{"b":{"p":"text","d":"Hello World! 32"},"a":"d"}}"

					form1 is response to connection (as indicated by "t":"c")
					form2 is response from send message (notice "s":"ok"), it indicates message succesfully received
					form3 is message when node being listened to has a change (notice "p":"text"), it indicates datatype
			*/
			Serial.printf("[*******WSc*******] get text: %s\n", payload);
			DynamicJsonBuffer jsonMsgBuffer;
			JsonObject& jsonMsg = jsonMsgBuffer.parseObject(payload);
			String t = jsonMsg["t"].asString();
			if (t.equals("c")){
				//let us send this: {"t":"d","d":{"r":1,"a":"s","b":{"c":{"sdk.js.7-1-0":1}}}}
				DynamicJsonBuffer jsonBuff1;
				JsonObject& json1 = jsonBuff1.createObject();
				json1["t"] = "d";
				JsonObject& jsonD1 = json1.createNestedObject("d");
				jsonD1["r"] = 1;
				jsonD1["a"] = "s";
				JsonObject& jsonB1 = jsonD1.createNestedObject("b");
				JsonObject& jsonC1 = jsonB1.createNestedObject("c");
				jsonC1["sdk.js.7-1-0"] = 1;
				String request1;
				json1.printTo(request1);
				// send message to server
				webSocket.sendTXT(request1);
				json1.printTo(Serial);
				Serial.println("\n[*******WSc*******] done send1\n");
				//then send this: {"t":"d","d":{"r":2,"a":"q","b":{"p":"/text","h":""}}};
				DynamicJsonBuffer jsonBuff2;
				JsonObject& json2 = jsonBuff2.createObject();
				json2["t"] = "d";
				JsonObject& jsonD2 = json2.createNestedObject("d");
				jsonD2["r"] = 2;
				jsonD2["a"] = "q";
				JsonObject& jsonB2 = jsonD2.createNestedObject("b");
				jsonB2["p"] = "/text";		//this is the node to listen to
				jsonB2["h"] = "";
				String request2;
				json2.printTo(request2);
				// send message to server
				webSocket.sendTXT(request2);
				json2.printTo(Serial);
				Serial.println("\n[*******WSc*******] done send2\n");
			} else if (t.equals("d")){
				Serial.printf("\n[*******WSc*******] data from node %s\n", payload);
			}
			break;
		}
		case WStype_BIN:
			Serial.printf("[*******WSc*******] get binary length: %u\n", length);
			hexdump(payload, length);

			// send data to server
			// webSocket.sendBIN(payload, length);
			break;
        case WStype_PING:
            // pong will be send automatically
        	Serial.printf("[*******WSc*******] get ping\n");
            break;
        case WStype_PONG:
            // answer to a ping we send
        	Serial.printf("[*******WSc*******] get pong\n");
            break;
    }

}
//The setup function is called once at startup of the sketch
void setup()
{
	Serial.begin(115200);

	Serial.println("\n\n************START Symphony Firebase Setup***************");
	char ver[10];
	sprintf(ver, "%u.%u", SYMPHONY_VERSION, MY_VERSION);
	s.setup("firebase", ver);
	s.on("/init", HTTP_GET, handleInit);
	Gui gui1 = Gui("Firebase Test", BUTTON_CTL, "On/Off", 0, 1, true);
	product.addProperty("0001", false, 12, gui1);//add aproperty that has an attached pin
	s.setProduct(product);

	//	("wss://s-usc1c-nss-231.firebaseio.com/.ws?v=5&ns=symphony-dcc4c", "", 443)
	// server address, port and URL
	webSocket.beginSSL("s-usc1c-nss-231.firebaseio.com", 443, "/.ws?v=5&ns=symphony-dcc4c");
//	webSocket.begin("192.168.0.111", 8080, "/ws");

	// event handler
	webSocket.onEvent(webSocketEvent);

	// try ever 5000 again if connection has failed
	webSocket.setReconnectInterval(5000);

	// start heartbeat (optional)
	// ping server every 15000 ms
	// expect pong from server within 3000 ms
	// consider connection disconnected if pong is not received 2 times
//	webSocket.enableHeartbeat(15000, 3000, 2);

	Serial.printf("\n************END Symphony Socket Firebase Version %u.%u***************\n", SYMPHONY_VERSION, MY_VERSION);
}

// The loop function is called in an endless loop
void loop()
{
	if (s.loop()) {
		webSocket.loop();
	}
}

/*
 * MqttUtil.cpp
 *
 * Utility Helper for connecting to the MQTT Server and parsing of the value objects.
 * We will be using Json objects.
 *
 *  Created on: Mar 11, 2017
 *      Author: cels
 */
#include "MqttUtil.h"

const char* MqttUtil::server = "192.168.1.5";
int MqttUtil::mqttPort = 1883;
const char* MqttUtil::clientID = "defaultID";
WiFiClient MqttUtil::wifiClient;
PubSubClient *MqttUtil::client;
const char* MqttUtil::willTopic = "BM";
SymphProduct MqttUtil::product;
boolean MqttUtil::isConnectedToBM = false;

boolean isMqttMsgForMe = false;
char* myTopic = new char[40];
char* cid = new char[10];

#define DEBUG_

/*
 * Below callback passes the ssid and command back to Symphony.
 * Symphony has the logic for handling the ssid and command.
 */
void (* CommandCallback_) (String ssid, String cmd);

/*
 * Below callback is only used to update the SymphonyCore on the status of the BM connection.
 */
void (* BmStatusCB_) ();

/*
 * Parser for responses in the default_topic
 * The initial response of Bm from the SignIn is to the default_topic.
 * Response contains the topic that we need to listen to. So we get it here and store in myTopic.
 * Get also the CID of this device and store it in a variable
 */
uint8_t parseDefaultTopic(char* json) {
  StaticJsonBuffer<200> jsonBuffer;
  JsonObject& root = jsonBuffer.parseObject(json);
  // Test if parsing succeeds.
#ifdef DEBUG_
  Serial.println("****** inside parseDefaultTopic");
#endif
  if (!root.success()) {
    Serial.println("\tparseDefaultTopic parseObject() failed");
  } else {
    const char* response_RID = root["RID"];
#ifdef DEBUG_
    Serial.print("\tRegister Response RID is--");Serial.println(response_RID);
    Serial.print("\clientID is--");Serial.println(MqttUtil::clientID);
#endif
    if (strcmp(response_RID, "BM-exit")==0) {
    	//the BM has been disconnected
    	Serial.println(".\tBM has been disconnected.");
    	MqttUtil::isConnectedToBM = false;
    	CommandCallback_("BM_EXIT","EXIT");
    } else if (strcmp(response_RID,MqttUtil::clientID)==0) {
        //we only store the myTopic and cid if the registration response is for us
        strcpy(myTopic, root["topic"]);
        strcpy(cid, root["id"]);
        isMqttMsgForMe = true;
        MqttUtil::isConnectedToBM = true;
#ifdef DEBUG_
	Serial.printf("SignIn successful! myTopic is %s. my ID is %s", myTopic, cid);
	Serial.println(".\tThis message is for me.  Processing it.");
#endif
    } else {
	  isMqttMsgForMe = false;
	  Serial.println(".\tThis message is not for me.  Ignoring it.");
    }
  }
}

/**
 * Parses the JSON response from the MQTT server
 */
void handleJSONResponse(char* json) {
#ifdef DEBUG_
      Serial.println("will parse the JSON response");
#endif
      StaticJsonBuffer<200> jsonBuffer;
      JsonObject& root = jsonBuffer.parseObject(json);
      // Test if parsing succeeded.
      if (!root.success()) {
        Serial.println("\tparseObject() failed");
      } else {
        Serial.println("\tabout to parse");
        if (root.containsKey("RTY")) {
          const char* req_type = root["RTY"];
          if (strcmp(req_type,"poop")==0) {//this is a poop request
            Serial.println("\tthis is a poop command that we need to execute.");
            if (root.containsKey("property")) {
              const char* property = root["property"];
              String s = property;
              if (root.containsKey("value")) {
                const char* inst = root["value"];
                String scmd = inst;
                Serial.print("\tproperty=");Serial.print(property);Serial.print("\tinstruction=");Serial.println(scmd);
                CommandCallback_(s, scmd);
              }

            }
          } else {
            Serial.println("\tpoop request not found.");
          }
        } else {
          Serial.println("\tRTY key not found.");
        }
        if (root.containsKey("errormsg")) {
          const char* error = root["errormsg"];
          Serial.print("\terrormsg is--");Serial.println(error);
        }
      }
}
/*
 * callback for MQTT when a message arrives from the MQTT server
 * This calls the parseDefaultTopic to parse the response from default_topic.
 * The initial response of Bm from the SignIn is to the default_topic.
 * Response contains the topic that we need to listen to.
 */
void callback(char* topic, uint8_t* payload, unsigned int length) {
#ifdef DEBUG_
	Serial.println("Message arrived:");
	Serial.println("\t\ttopic: " + String(topic));
	Serial.println("\t\tLength: " + String(length,DEC));
//	CommandCallback_("test", "laang");
#endif
	// create character buffer with ending null terminator (string)
	char message_buff[200];   // initialise storage buffer
	int i = 0;
	for(i=0; i<length; i++) {
		message_buff[i] = payload[i];
	}
	message_buff[i] = '\0';
	String msgString = String(message_buff);
	Serial.println("\t\tMessage is: " + msgString);

	if (strcmp(topic,"default_topic")==0) {
		uint8_t response = parseDefaultTopic(message_buff);
		if (isMqttMsgForMe) {
			Serial.printf("Will subscribe to %s.\n", myTopic);
			if (MqttUtil::client->subscribe(myTopic)) {
				Serial.printf("Subscribe to %s successful.\n", myTopic);
			} else {
				Serial.printf("Subscribe to %s failed.\n", myTopic);
			}
			BmStatusCB_();
		}
	} else if(strcmp(topic,"error_topic")==0) {
	//do nothing
	} else {
		handleJSONResponse(message_buff);
	}
}

MqttUtil::MqttUtil() {

}

/*
 * connects to the mqtt server
 * MQTT.connect (clientID, willTopic, willQoS, willRetain, willMessage)
 */
boolean MqttUtil::connectToMQTT(const char *id, const char *url, int port, PubSubClient *c, WiFiClient wc, SymphProduct p) { //to connect to MQTT server
    boolean isConnected = false;
    MqttUtil::clientID = id;
    MqttUtil::server = url;
    MqttUtil::mqttPort = port;
    MqttUtil::product = p;
#ifdef DEBUG_
    Serial.println("Product in connectToMQTT");
    MqttUtil::product.print();
    Serial.printf("[connectToMQTT]connecting to %s(%d) as %s\n", url, port, MqttUtil::clientID);
#endif
    PubSubClient pubsubclient = PubSubClient (MqttUtil::server, MqttUtil::mqttPort, MqttUtil::wifiClient);

    MqttUtil::client = &pubsubclient;
    MqttUtil::client = c;
    MqttUtil::client->setServer(url, port);

    MqttUtil::client->setCallback(callback);
    MqttUtil::client->setClient(wifiClient);

	String payload = "{\"RID\":\""; payload += MqttUtil::clientID; payload +="\"";
	payload += ",\"CID\":\"";payload += MqttUtil::clientID; payload +="\"";
	payload += ",\"RTY\":\"bye\"}";
	char willMessage[payload.length()+1];
	for(int i=0; i<payload.length()+1; i++) {
		willMessage[i] = payload[i];
	}
	if (MqttUtil::client->connect(MqttUtil::clientID, MqttUtil::willTopic, 0, false, willMessage)) {
		isConnected = true;
		if (MqttUtil::client->subscribe("default_topic")) {
			Serial.println("\tSubscribe to default_topic successful");
		} else {
			Serial.println("\tSubscribe to default_topic failed");
		}
		if (MqttUtil::client->subscribe("error_topic")) {
			Serial.println("\tSubscribe to error_topic successful");
		} else {
			Serial.println("\tSubscribe to error_topic failed");
		}
		MqttUtil::signin(p.name, p.room, p.productType);
	}
    return isConnected;
}

/**
 * SignsIn to the BM
 *
    RID String MAC address of the registering component
    RTY String register
    CID String The Product SSID of the component (can be found in ‘COMCAT’ DB table
    name String Name of the registering component. This is the name that will be seen by the users.
    roomID String SSID of the room in which this component belongs to.
 */
void MqttUtil::signin(String ngalan, String room, String product) {

//  String payload = "{RID:5ccf7f15a492,CID:0000,RTY:register,name:Ngalan,roomID:J444,product:0000}";

    String payload = "{RID:"; payload += MqttUtil::clientID;
    payload += ",CID:"; payload += product;
    payload += ",RTY:register";
    payload += ",name:"; payload += ngalan;
    payload += ",roomID:"; payload += room;
    payload += ",product:"; payload += product;
    payload += "}";
#ifdef DEBUG_
    Serial.print("\SignIn to BM topic:");Serial.println(payload);
    Serial.print("\tpayload length is:");Serial.println(payload.length());
#endif
    if ( client->publish("BM", (byte*) payload.c_str(), payload.length())) {
      Serial.println("\tPublish to BM topic success.  Waiting for response from BM.");
    } else {
      Serial.println("\tPublish to BM topic failed ");
    }
}

/*
 * Sends the command to MQTT
 */
void MqttUtil::sendCommand(String ssid, int value) {
  if (isConnectedToBM) {
#ifdef DEBUG_
	Serial.println("Product in sendCommand");
	MqttUtil::product.print();
    Serial.print("inside sendCommand. CID:");Serial.print(cid);Serial.print(" topic:");Serial.println(myTopic);
    Serial.print("\tvalue=");Serial.println(value);
    Serial.print(" ssid=");Serial.println(ssid);
#endif
    String payload = "{\"RID\":\""; payload += product.deviceID; payload +="\"";
    payload += ",\"CID\":\""; payload += cid; payload +="\"";
    payload += ",\"property\":\"";payload += ssid; payload +="\"";
    payload += ",\"value\":\""; payload += value; payload +="\"";
    payload += ",\"RTY\":\"poop\"}";


#ifdef DEBUG_
    Serial.print("\nsending change property payload to BM:");Serial.println(payload);
    Serial.print("\tpayload length to BM:");Serial.println(payload.length());
    String data = ""; data += value;
#endif
    if ( client->publish("BM", (byte*) payload.c_str(), payload.length())) {
      Serial.println("\n\tPublish to BM success.");
    } else {
      Serial.println("\n\tPublish to BM failed ");
    }
  } else {
    Serial.println("inside sendCommand. Cannot send transaction since we are not yet connected to BM");
  }
}

/*
 * This is where the callback function for setting the value for a specific pin is set.
 * This callback is called by the handleJSONResponse function
 *
 */
void MqttUtil::setCommandCallback(void (* CommandCallback) (String ssid, String cmd)) {
  CommandCallback_ = CommandCallback;
}

/*
 * This is where the callback function for updating the status of BM is set.
 * This callback is called by the callback function
 *
 */
void MqttUtil::setBmStatusCB(void (* BmStatusCB) ()) {
	BmStatusCB_ = BmStatusCB;
}

/*
 * Unregisters the device from the BM
 */
void MqttUtil::unRegister() {
#ifdef DEBUG_
  Serial.print("inside unRegister. CID:");Serial.print(clientID);Serial.print(", topic:");Serial.println(myTopic);
#endif
  String payload = "{\"RID\":\""; payload += product.deviceID.c_str(); payload +="\"";
  payload += ",\"CID\":\""; payload += cid; payload +="\"";
  payload += ",\"RTY\":\"detach\"}";
#ifdef DEBUG_
  Serial.print("\nunregister payload to BM topic:");Serial.println(payload);
  Serial.print("\tpayload length to BM topic:");Serial.println(payload.length());
#endif
  if ( client->publish("BM", (byte*) payload.c_str(), payload.length())) {
    Serial.print("\n\tPublish to BM topic success.");
  } else {
    Serial.println("\n\tPublish to BM topic failed ");
  }
}

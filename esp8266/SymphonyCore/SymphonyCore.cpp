/*
 * SymphonyCore.cpp
 *
 * This is the core framework for the Symphony Products.
 * This will connect to an AP, the SSID and PassPhrase can be configured at <this>.ip/main
 * By default, AP Mode is enabled if this cannot connect to an AP.  This can be changed in <this>.ip/ota_setup
 * 		options: 1-default (AP enable if not connected to AP)
 * 				 2-AP always enabled
 * 				 3-AP always disabled
 *
 * Usage Notes:
 * 1. ALWAYS set the product first before calling setup
 *		s.setProduct(product);
 *		s.setup();
 * 2. ALWAYS set the wsCallback.  This is for the handling of requests from clients. This is via WebSockets.
 *		s.setWsCallback(WsCallback);
 *
 *  Created on: Mar 10, 2017
 *      Author: cels
 *	Release Notes Mar 10 2017
 *		1. Created Wifi Login
 *		2. Created AP
 *		3. Created OTA
 *		4. Created DNS/Captive Portal
 *		5. Created createMyName
 *
 *      TODO Mar 11 2017
 *      1. Wifi Login credentials should be stored in SPIFFS
 *      	this should have a webpage config - DONE
 *      2. Connect to MQTT server
 *      	this should have a webpage for config - DONE
 *      3. Status for AP (with IP), WifiStation (with IP), MQTT should be shown
 *      4. Create Object for communication to BM
 *      	register
 *      	unregister
 *      	send
 *      5. Create Root page - DONE (this is the control page)
 *      6. Create CaptivePortal page - DONE
 *      7. Connect to PushBullet
 *      8. Control page should display the current state of the device
 *
 *	Release Notes Mar 11 2017
 *		1. Added SymphProduct class
 *
 *  Release Notes Mar 13 2017
 *		1. Added RemoteDebug to enable logging via telnet
 *
 */


#include "SymphonyCore.h"
#define ver "1.1.1"

ESP8266WebServer server(80);


WebSocketsServer webSocketServer(81);
uint8_t webSocketClientCount = 0;
uint8_t Symphony::apMode = 1; //1-default (AP enable if not connected as station), 2-AP always enabled, 3-AP always disabled

// DNS server
const byte DNS_PORT = 53;
DNSServer dnsServer;
String homeHtml;
char * pHoneHtml;

File fsUploadFile;              // a File object to temporarily store the received file

String propertyHtml = "";

//below are for the firmware update
const char* fwUrlBase = "http://192.168.0.105:8080/static/esp-versions/";
String fwURL = String( fwUrlBase );
bool updateSuccess = false;
bool isUpdating = false;
int updateStatus = 200;
char firmwareUpdateMsg[100] = "";
//end for firmware update

/*
 * Below callback that the sketch should use to calculate the value to be set to a pin in ESP
 * this is used in the interpretation of the responses from MQTT
 */
attribStruct (* MyMqttCallback_) (attribStruct property, int cmd);

/*
 * Below callback will be used to calculate the value to be set to a pin in ESP
 * this is used for websocketserver
 */
int (* WsCallback_) (uint8_t * payload, size_t length);


String getMAC()
{
  uint8_t mac[6];
  char result[14];
  WiFi.macAddress(mac);

  sprintf( result, "%02x%02x%02x%02x%02x%02x", mac[ 0 ], mac[ 1 ], mac[ 2 ], mac[ 3 ], mac[ 4 ], mac[ 5 ] );


  return String( result );
}
String mac = getMAC();

/*
 * Callback for MQTT events
 * March 26 2017 - this is deprecated, can be deleted
 */
attribStruct MqttCallback(attribStruct property, int scmd) {
#ifndef PROD
	Serial.printf("\tMqttCallback index=%u scmd=%i ssid=%s value=%i\n", property.index, scmd,property.ssid.c_str(), property.gui.value);
#endif
	String data = ""; data += MqttUtil::product.name; data += ":";
	data += property.ssid; data += "="; data += scmd;
	for (int i=0; i<=webSocketClientCount; i++) {
		webSocketServer.sendTXT(i, data);
#ifdef DEBUG_
		Serial.print("  Sending to client");
		Serial.print(i);
		Serial.print(" data=");
		Serial.println(data);
#endif

	}
	MqttUtil::product.setValue(property.ssid, scmd);
	attribStruct returnPs;
	returnPs.pin = 15;
	returnPs.gui.value = scmd;
	returnPs.gui.pinType = SLIDER_OUT;
	return returnPs;
}

/*
 * The callback method for displaying the status of BM connection
 * called by MqttUtil as a callback when a successful BM connection arrives
 */
void BmStatusCallBack () {
	Serial.printf("BM connected:%d\n", MqttUtil::isConnectedToBM);
}

/*
 * The callback method for handling the command for a SSID
 * called by
 * 	1. webSocketEvent as a local Function when an event from a websocket arrives.
 *  2. MqttUtil as a callback when a MQTT data arrives
 */
void CommandCallback(String ssid, String scmd) {
#ifndef PROD
	Serial.printf("\tCommandCallback ssid:%s, cmd:%s\n", ssid.c_str(), scmd.c_str());
#endif
	if (strcmp(ssid.c_str(), "BM_EXIT")==0) {

	} else {
		int cmd = -1;
		  cmd = scmd.toInt();
		  attribStruct property = MqttUtil::product.getProperty(ssid);
	#ifdef DEBUG_
		  Serial.print("Received instruction SSID:");Serial.print(ssid);
		  Serial.print("\tgot the property from array. prop.pin=");Serial.print(property.pin);
		  Serial.print(", prop.pinType=");Serial.print(property.gui.pinType);
		  Serial.print(", prop.usepin=");Serial.print(property.usePin);
		  Serial.print(", prop.ssid=");Serial.print(property.ssid);
		  Serial.print(", prop.index=");Serial.println(property.index);
	#endif
		  attribStruct commandPs;
		  if (property.index >= 0 ) {
		    if (property.usePin) {
		      //we will use the passed parameter as the value to set
		      commandPs.gui.pinType = property.gui.pinType;
		      commandPs.pin = property.pin;
		#ifdef DEBUG_
		      Serial.printf("Will set pin%d to %d\n", commandPs.pin, cmd);
		#endif
		    } else {
		      //we need to call the callback function defined
		#ifdef DEBUG_
		    	Serial.printf("Will call MyMqttCallback\n");
		#endif
		      commandPs = MyMqttCallback_(property, cmd);
		    }
		    //if calling sketch already wrote to device, skip the setting of the pins
		    if (!commandPs.commandDone) {
		    	switch(commandPs.gui.pinType) {
				  case RADIO_OUT :
					Serial.println(" RADIO");
					digitalWrite(commandPs.pin, cmd);
					break;
				  case BUTTON_OUT :
					Serial.println(" BUTTON");
					digitalWrite(commandPs.pin, cmd);
					break;
				  case SLIDER_OUT :
					Serial.println(" analogWrite");
					analogWrite(commandPs.pin, cmd);
					break;
				}
		    } else {
		    	Serial.println(" callback already handled the command.");
		    }

		  } else {

		  }
		  char data[200];
		  strcpy(data, MqttUtil::product.name.c_str());
		  strcat(data, ":");
		  strcat(data, property.ssid.c_str());
		  strcat(data, "=");
		  strcat(data, scmd.c_str());
		  for (int i=0; i<=webSocketClientCount; i++) {
			  webSocketServer.sendTXT(i, data);
	#ifdef DEBUG_
			  Serial.printf("  Sending to client%d data=%s\n", i, data);
	#endif

		}
	}
}

/* Just a little test message for the root url
 */
void handleRoot() {
	server.send(200, "text/html", "<h1>You are connected</h1>");
	Serial.println("client request handled");
}

/* Just a little test message for not found urls
 */
void handleNotFound() {
	server.send(200, "text/html", "<h1>Not Found</h1>");
	Serial.println("handle not found");
}
///*
// * Show the main config/captive portal page
// */
//void showAdmin() {
//	server.send(200, "text/html", ADMIN_HTML);
////	server.send(200, "text/html", "<html>Admin Page</html>");
//	Serial.println("Main page/captive portal displayed");
//}

///*
// * Show the main device portal page
// */
//void showDeviceSetup() {
//	server.send(200, "text/html", DEVICE_HTML);
//	Serial.println("Device setup page displayed");
//}
//
///*
// * Show the wifi portal page
// */
//void showWifiSetup() {
//	server.send(200, "text/html", WIFI_HTML);
//	Serial.println("Wifi setup page displayed");
//}
//
///*
// * Show the main mquu portal page
// */
//void showMqttSetup() {
//	server.send(200, "text/html", MQTT_HTML);
//	Serial.println("Mqtt setup page displayed");
//}

/*
 * handles the wifi configuration
 */
void handleWifiConfig() {
	server.send(200, "text/html", "<h1>Input accepted!</h1>");
	String wssid = server.arg("ssid").c_str();
	String wpass = server.arg("pass").c_str();
	SpiffsUtil::saveSsid(server.arg("ssid").c_str());
	SpiffsUtil::savePwd(server.arg("pass").c_str());
	Symphony::apMode = server.arg("apmode").toInt();
	SpiffsUtil::saveMyMode(server.arg("apmode").c_str());
	Serial.printf("WiFi config is ssid:%s, pwd:%s, apmode:%d\n", wssid.c_str(), wpass.c_str(), Symphony::apMode);
	ESP.restart();
}

/*
 * Shows message that the firmware update is already done
 */
void showUpdateDone(){
	switch(updateStatus) {
		case 200:
			Serial.println( "Update Done" );
			server.send(200, "text/html", "{\"resp\":\"0\",\"msg\":\"Update Done\"}");
			break;
		case 203:
			Serial.println( "HTTP_UPDATE_FAILED" );
			server.send(200, "text/html", "{\"resp\":\"203\",\"msg\":\"UPDATE_FAILED\"}");
//			server.send(200, "text/html", firmwareUpdateMsg);
			break;
		case 204:
			Serial.println( "HTTP_UPDATE_NO_UPDATES");
			server.send(204, "text/html", "{\"resp\":\"0\",\"msg\":\"HTTP_UPDATE_NO_UPDATES\"}");
			break;
	}
}
/*
 * Shows the html page for getting consent to update the firmware
 */
void showUpdate() {
  fwURL.concat( mac );
  String fwVersionURL = fwURL;
  fwVersionURL.concat( ".version" );

  Serial.println( "Checking for firmware updates." );
  Serial.print( "MAC address: " );
  Serial.println(mac);
  Serial.print( "Firmware version URL: " );
  Serial.println( fwVersionURL );

  HTTPClient httpClient;
  httpClient.begin( fwVersionURL );
  int httpCode = httpClient.GET();
  if( httpCode == 200 ) {
	String newFWVersion = httpClient.getString();

	Serial.print( "Current firmware version: " );
	Serial.println( FW_VERSION );
	Serial.print( "Available firmware version: " );
	Serial.println( newFWVersion );

	int newVersion = newFWVersion.toInt();

	if( newVersion > FW_VERSION ) {
		server.send(200, "text/html", SHOWUPDATE);
		Serial.printf("show Firmware update page.\n");
	} else {
		server.send(200, "text/html", "Already on latest version");
	}
  } else {
	char response[75];
	sprintf(response, "%s%d","Firmware version check failed, got HTTP response code\n", httpCode);
	server.send(200, "text/html", response);
	Serial.print( "Firmware version check failed, got HTTP response code " );
	Serial.println( httpCode );
  }
  httpClient.end();
}

/*
 * This updates the firmware
 */
void updateFW() {

      Serial.println( "Preparing to update firmware." );
      isUpdating = true;
      fwURL.concat( mac );
      String fwImageURL = fwURL;
      fwImageURL.concat( ".bin" );
      Serial.printf("Getting new version of %s.\n",fwImageURL.c_str());
      t_httpUpdate_return ret = ESPhttpUpdate.update( fwImageURL );

      switch(ret) {
        case HTTP_UPDATE_FAILED:
          Serial.printf("HTTP_UPDATE_FAILED Error (%d): %s\n", ESPhttpUpdate.getLastError(), ESPhttpUpdate.getLastErrorString().c_str());
          isUpdating = false;
          updateStatus = 203;
          server.send(200, "text/html", "{\"resp\":\"203\",\"msg\":\"UPDATE_FAILED\"}");
          break;

        case HTTP_UPDATE_NO_UPDATES:
          Serial.println("HTTP_UPDATE_NO_UPDATES");
          updateStatus = 204;
          server.send(204, "text/html", "HTTP_UPDATE_NO_UPDATES");
          break;
        case HTTP_UPDATE_OK:
          Serial.println("HTTP_UPDATE_OK");
          updateStatus = 200;
          server.send(200, "text/html", "HTTP_UPDATE_OK");
		break;
      }
}

/*
 * Gets response from showUpdate page
 */
void handleUpdate() {
	Serial.printf("Firmware will be updated.  Value=%s url=%s\n", server.arg("update").c_str(), server.arg("fwurl").c_str());

	if (atoi(server.arg("update").c_str())==1) {
		server.send(200, "text/html", "{\"resp\":\"1\",\"msg\":\"Upload in progress...\"}");
		if (strlen(server.arg("fwurl").c_str()) > 0 )
			fwURL = server.arg("fwurl");
		updateFW();
	}
}


/*
 * handles the restart of the ESP
 */
void reStart() {
	server.send(200, "text/html", "<h1>Device will be restarted!</h1>");
	ESP.restart();
}

/*
 * Handles url requests from command
 * URL is:
 * /command?SoloSocket:0006=1
 */
void handleCommand(){
	int i = server.args();
	char str[100];
	strcpy(str, " i="+i);
	Serial.printf("Start handleCommand %s\n", str);
	for (int ctr=0; ctr<i; ctr++) {
		strcat(str,server.argName(ctr).c_str());
		strcat(str,"=");
		strcat(str,server.arg(ctr).c_str());
		Serial.printf("%d handleCommand %s\n", ctr,str);
	}
	String ssid = server.argName(0).substring(server.argName(0).indexOf(':') + 1);
	Serial.printf("handleCommand %s length %d\n", str,  strlen(str));
	Serial.printf("End handleCommand will send SSID=%s value=%s\n", ssid.c_str(), server.arg(0).c_str());
	MqttUtil::sendCommand(ssid, server.arg(0).toInt());
	CommandCallback(ssid, server.arg(0));
	server.send(200, "text/html", str);
}
/*
 * Shows the init page.
 * This is used to initially set the AP and passphrase that this device will use to connect to the wifi network.
 * This is also shown in the captive portal.
 * URL is:
 * /connect
 */
void showInit() {
	server.send(200, "text/html", SHOWINIT);
}

/*
 * handles the mqtt configuration
 * URL is:
 * /handleMqttConfig?ip=192.168.0.105&port=1883
 */
void handleMqttConfig() {
	server.send(200, "text/html", "<h1>Input accepted!</h1>");
	Serial.printf("MQTT config is ip:%s, port:%s\n",server.arg("ip").c_str(), server.arg("port").c_str());
	SpiffsUtil::saveMqttIP(server.arg("ip").c_str());
	ESP.restart();
}

/*
 * handles the manual registration
 */
void handleRegister() {
	server.send(200, "text/html", "<h1>Register input accepted!</h1>");
	SpiffsUtil::saveDisplayName(server.arg("pName").c_str());
	SpiffsUtil::saveRoom(server.arg("room").c_str());
	SpiffsUtil::saveProductID(server.arg("pID").c_str());
	Serial.printf("Manual registration name:%s, room:%s, id:%s\n", server.arg("pName").c_str(), server.arg("room").c_str(), server.arg("pID").c_str());
}

/*
 * handles the manual UNregistration
 */
void doUnRegister() {
	server.send(200, "text/html", "<h1>will do UnRegister!</h1>");
	Serial.printf("Manually unregister name=%s, ssid=%s\n", MqttUtil::product.name.c_str(), MqttUtil::product.attributes->ssid.c_str());
	MqttUtil::unRegister();
}

/* Just a little test message to show the main config/captive portal page
 */
void showControl() {
	server.send(200, "text/html", homeHtml);
	Serial.println("control page displayed");
}

/*
 * Page for uploading the css, html and js files
 */
void showUploadDataFiles() {
	server.send(200, "text/html", UPLOAD_HTML1);
	Serial.println("showUploadDataFiles setup page displayed");
}

/*
 * handles the upload of uploadAdminHtml
 */
void handleFileUpload(){ // upload a new file to the SPIFFS
  HTTPUpload& upload = server.upload();
  if(upload.status == UPLOAD_FILE_START){
    String filename = upload.filename;
    if(!filename.startsWith("/")) filename = "/"+filename;
    Serial.print("handleFileUpload Name: "); Serial.println(filename);
    fsUploadFile = SPIFFS.open(filename, "w");            // Open the file for writing in SPIFFS (create if it doesn't exist)
    filename = String();
  } else if(upload.status == UPLOAD_FILE_WRITE){
    if(fsUploadFile)
      fsUploadFile.write(upload.buf, upload.currentSize); // Write the received bytes to the file
  } else if(upload.status == UPLOAD_FILE_END){
    if(fsUploadFile) {                                    // If the file was successfully created
      fsUploadFile.close();                               // Close the file again
      Serial.print("handleFileUpload Size: "); Serial.println(upload.totalSize);
      server.send(200, "text/html", "File upload successful.");
    } else {
      server.send(500, "text/plain", "500: couldn't create file");
    }
  }
}
/*
 * returns the SPIFFS directory
 */
void handleGetFiles() {
	String str = "";
	Dir dir = SPIFFS.openDir("/");
	while (dir.next()) {
	    str += dir.fileName();
	    str += "<br>\n";
	}
	Serial.println(str);
	server.send(200, "text/html", str);
}
/*
 *  Returns the properties in the form:
 *  {typ:'Rad',lbl:'RED',val:'0007'},{typ:'Btn',lbl:'STOP',val:'0009'},{typ:'Rng',lbl:'Hue',val:'0011',min:'0',max:'360'}
 */
void showProperties() {
	server.send(200, "text/html", propertyHtml);
}

/*
 * This is the function for handling the events in websocket
 */
void webSocketEvent(uint8_t num, WStype_t type, uint8_t * payload, size_t length) {
#ifndef PROD
	Serial.println("WebSocketEvent triggered!");
#endif
    switch(type) {
        case WStype_DISCONNECTED:
#ifndef PROD
        	Serial.printf("[%u] Disconnected!\n", num);
#endif
            break;
        case WStype_CONNECTED:
            {
                IPAddress ip = webSocketServer.remoteIP(num);
#ifndef PROD
                Serial.printf("[%u] Connected from %d.%d.%d.%d url: %s\n", num, ip[0], ip[1], ip[2], ip[3], payload);
#endif
				// send message to client
                webSocketServer.sendTXT(num, "Connected");
                webSocketServer.sendTXT(num, MqttUtil::product.getHtml().c_str());
                webSocketClientCount = num;

                int propSize = MqttUtil::product.getSize();
                for (int ctr=0; ctr<propSize; ctr++) {
                	attribStruct theProperty = MqttUtil::product.getKeyVal(ctr);
                	char keyVal[30];
                	sprintf(keyVal, "%s:%s=%d", MqttUtil::product.name.c_str(), theProperty.ssid.c_str(), theProperty.gui.value);
                    webSocketServer.sendTXT(num, keyVal);
                }

//                product.print();
            }
            break;
        case WStype_TEXT:
        	{
//				Serial.printf("[%u] get Text: %s\n", num, payload); //payload is of the form SSID=value
				int result = WsCallback_(payload, length);
				WsData wsdata = WsData(payload, length);
				String value = wsdata.getValue();
				String data = ""; data += wsdata.getDeviceName(); data += ":";
				data += wsdata.getSSID(); data += "="; data += value;
				//we also need to send this to BM
				MqttUtil::sendCommand(wsdata.getSSID(), value.toInt());
				attribStruct ps = MqttUtil::product.getProperty(wsdata.getSSID());
#ifndef PROD
				Serial.printf("[%u] get Text: %s\n", num, payload); //payload is of the form SSID=value
				Serial.printf("Pin is %d, label is %s\n", ps.pin, ps.gui.label.c_str()); //payload is of the form SSID=value
				Serial.printf("webSocketClientCount is %d\n", webSocketClientCount); //webSocketClientCount
#endif
    		    CommandCallback(wsdata.getSSID(), value);//call the commandcallback to handle the data from websocket
//				for (int i=0; i<=webSocketClientCount; i++) {
//					if (num!=i) {
//						webSocketServer.sendTXT(i, data);
//#ifdef PROD
//						if (Debug.isActive(Debug.INFO)) { // Debug message long
//							Debug.printf("Sending '%s' to client%d\n", data.c_str(), i);
//						}
//#endif
//					}
//				}
			}
            break;
        case WStype_BIN:
        	Serial.printf("[%u] get binary length: %u\n", num, length);
            hexdump(payload, length);
            break;
    }
}


/*
 * Constructor
 */
Symphony::Symphony(){

}

/*
 * Initiates the Symphony module
 */
void Symphony::setup() {
#ifdef DEBUG_
	  uint32_t realSize = ESP.getFlashChipRealSize();
	  uint32_t ideSize = ESP.getFlashChipSize();
	  FlashMode_t ideMode = ESP.getFlashChipMode();
	  Serial.printf("\n[init]Sketch size: %u\n", ESP.getSketchSize());
	  Serial.printf("[init]Free Sketch size: %u\n", ESP.getFreeSketchSpace());
	  Serial.printf("[init]Flash real id:   %08X\n", ESP.getFlashChipId());
	  Serial.printf("[init]Flash real size: %u\n", realSize);
	  Serial.printf("[init]Flash ide  size: %u\n", ideSize);
	  Serial.printf("[init]Flash ide speed: %u\n", ESP.getFlashChipSpeed());
	  Serial.printf("[init]Flash ide mode:  %s\n", (ideMode == FM_QIO ? "QIO" : ideMode == FM_QOUT ? "QOUT" : ideMode == FM_DIO ? "DIO" : ideMode == FM_DOUT ? "DOUT" : "UNKNOWN"));
	  if(ideSize != realSize) {
		  Serial.printf("[init]Flash Chip configuration wrong!\n");
	  } else {
		  Serial.printf("[init]Flash Chip configuration ok.\n");
	  }
#endif
	Serial.printf("\n Core version %s\n\n", ver);
	String product_name = SpiffsUtil::readDisplayName();
	if (product_name.length() > 0) {
		product.name = product_name;
	}
	hostName = product.name;
	hostName.toLowerCase();
	Serial.println();
	Serial.println("---Start core setup---");
	createMyName();
	Serial.printf("Hostname is %s.%s\n", hostName.c_str(),"local");
	product.room = SpiffsUtil::readRoom();
	Serial.printf("Room is %s\n", product.room.c_str());
	if (Symphony::apMode==2) {
		WiFi.mode(WIFI_AP_STA); //set ESP in STA and AP modes
	} else {
		WiFi.mode(WIFI_STA); //set ESP in STA mode only
	}
	connectToWifi();		//we are connecting to the wifi AP
	if ((Symphony::apMode==1 && !isWifiConnected) || Symphony::apMode==2) {
		setupAP(); //but also opening a soft AP of our own, if enabled
	}
	if (isWifiConnected) {
		//send mqtt message to cloud
//		CloudMqttUtil::connect(myName.c_str(), hostName.c_str(), &cloudclient, wifiClient);
		//enable OTA loading of firmware
//		startOTA();
		//start the websocket server
		webSocketServer.begin();
		webSocketServer.onEvent(webSocketEvent);
		//connect to MQTT server
		String mqttUrl = SpiffsUtil::readMqttIP();
		if (mqttUrl.length() == 0) {
			Serial.println("No MQTT Server IP configured.");
		} else {
//			Serial.print("test lang ito name:");Serial.println(product.name);
			isMqttSuccessful = MqttUtil::connectToMQTT(myName.c_str(), mqttUrl.c_str(), 1883, &client, wifiClient, product);
			MqttUtil::setCommandCallback(&CommandCallback);
			MqttUtil::setBmStatusCB(&BmStatusCallBack);
			if (isMqttSuccessful) {
				Serial.println("Sucessfully connected to MQTT Server.");
			} else {
				Serial.println("Failed to connect to MQTT Server.");
			}
		}
	}
//Serial.println("1");
	homeHtml = CONTROL_HTML1;
//Serial.println("2");
	String tmp = product.name; tmp += " @ "; tmp += product.room;
//Serial.println("3");
	homeHtml.replace("$AAA$", tmp);
//Serial.println("4");
	homeHtml.replace("$NAME$", product.name);
//Serial.print("5 ");Serial.println(product.name);
	product.constructHtml();
//Serial.println("6");
	propertyHtml = product.array;
//	homeHtml.replace("$PROPS$", product.array);
//Serial.println("7");
	server.on("/", showControl);
//	server.serveStatic("/", SPIFFS, "/control.html");
//	server.on("/admin", showAdmin);
//	server.on("/device", showDeviceSetup);		//show device setup page
//	server.on("/mqtt", showMqttSetup);			//show mqtt setup page
//	server.on("/wifi", showWifiSetup);			//show mqtt setup page
	server.on("/showUploadFile", showUploadDataFiles);
	server.on("/handleWifiConfig", handleWifiConfig);
	server.on("/handleMqttConfig", handleMqttConfig);
	server.on("/handleRegister", handleRegister);
	server.on("/doUnRegister", doUnRegister);
	server.on("/restart", reStart);
	server.on("/uploadFile", HTTP_POST,                       // if the client posts to the upload page
	    [](){ server.send(200); },                          // Send status 200 (OK) to tell the client we are ready to receive
	    handleFileUpload                                    // Receive and save the file
	  );
	server.on("/getFiles", handleGetFiles);
	server.on("/command", handleCommand);
	server.on("/init", showInit);
	server.serveStatic("/admin", SPIFFS, "/admin.html");
	server.serveStatic("/symphony.css", SPIFFS, "/symphony.css");
	server.serveStatic("/control.css", SPIFFS, "/control.css");
	server.serveStatic("/symphony.js", SPIFFS, "/symphony.js");
	server.on("/properties.html", showProperties);

	//url below is for the http firmware update
	server.on("/firmware", showUpdate);
	server.on("/progress", showUpdateDone);
	server.on("/handleUpdate", handleUpdate);

	server.begin();
	showStatus();
	Serial.println("---End core setup---");
}

/*
 * The loop method
 */
void Symphony::loop() {
//	restartTimer = millis();  Dec 26 2017, we will not be using this
	bmLoginAttemptTimer = millis();
	server.handleClient();
	dnsServer.processNextRequest();
//	ArduinoOTA.handle();
	if (isMqttSuccessful) {
		client.loop();
		if (!isUpdating) {
			if (!MqttUtil::isConnectedToBM && (bmLoginAttemptTimer - bmLoginAttemptTimerPrev)>=maxBmLoginDelay) {
				Serial.println("Symphony::loop login to BM");
				MqttUtil::signin(product.name, product.room, product.productType);
				bmLoginAttemptTimerPrev = bmLoginAttemptTimer;
				bmLoginAttemptTimer = 0;
			}
		}
	}
	webSocketServer.loop();
}

/*
 * For printing test only
 */
void Symphony::print() {
	Serial.println("PRINT called");
}

void Symphony::connectToWifi() {
	ssid = SpiffsUtil::readSsid(); //"PLDTHOMEDSL";
	pwd = SpiffsUtil::readPwd();//"carlodredpia";
#ifdef DEBUG_
	Serial.println("core setup starting.");
	Serial.printf("Connecting to %s(%s)\n", ssid.c_str(), pwd.c_str());
#endif
	//sets the wifi login
	WiFi.begin(ssid.c_str(), pwd.c_str());
	int i = 0;
	//tries to connect to the AP
	while (WiFi.status() != WL_CONNECTED && i <= wifiMaxConnCount) {
		i++;
		delay(200);
	    Serial.print(".");
	}
#ifdef DEBUG_
	if (WiFi.status() == WL_CONNECTED) {
		Serial.println("\nWiFi connected");
		Serial.print("IP address: ");	Serial.println(WiFi.localIP());
		isWifiConnected = true;
	} else {
		Serial.println("\nWiFi not connected");
		isWifiConnected = false;
	}
#endif
}
//this is the procedure for setting up the AP
void Symphony::setupAP() {
	if (apMode==1) {
		WiFi.mode(WIFI_AP); //set ESP in AP mode only
	}
#ifdef DEBUG_
    Serial.println("\nConfiguring access point...");
#endif
	/* Soft AP network parameters */
	IPAddress netMsk(255, 255, 255, 0);
//    ap_ssid = generated by createMyName()
    WiFi.softAPConfig(apIP, apIP, netMsk);
    WiFi.softAP(ap_ssid.c_str(), ap_passphrase.c_str());

#ifdef DEBUG_
    Serial.print("SSID is ");Serial.print(ap_ssid);
    Serial.print(" password is ");Serial.println(ap_passphrase);
    delay(500);
    IPAddress myIP = WiFi.softAPIP();
    Serial.println("AP running!");Serial.print("AP IP address: ");Serial.println(myIP);
#endif
    /* Setup the DNS server redirecting all the domains to the apIP */
	dnsServer.setErrorReplyCode(DNSReplyCode::NoError);
	dnsServer.start(DNS_PORT, "*", apIP);
//	server.on("/generate_204", showAdmin);  //Android captive portal. Maybe not needed. Might be handled by notFound handler.
//	server.on("/generate_204", showAdmin);  //Android captive portal. Maybe not needed. Might be handled by notFound handler.
	server.serveStatic("/generate_204", SPIFFS, "/init");
//	server.on("/fwlink", showAdmin);  //Microsoft captive portal. Maybe not needed. Might be handled by notFound handler.
	server.serveStatic("/fwlink", SPIFFS, "/init");
	server.onNotFound ( handleNotFound );
}

/*
 * this creates the unique name of this device based on its mac address
 */
void Symphony::createMyName() {
	// Generate device name based on MAC address
	myName = "";
	ap_ssid = "AP_";
	uint8_t mac[6];
	WiFi.macAddress(mac);
//	for loop below could be used to generate the hostname based on the full MAC
//	for (int i = 0; i < 6; ++i) {
//		myName += String(mac[i], 16);
//	}
	//	we generate the hostname based on the first MAC values
	for (int i = 0; i < 6; ++i) {
			myName += String(mac[i], 16);
		}
	ap_ssid += myName;
	hostName = product.name;
	hostName += myName;
	hostName.toLowerCase();
#ifdef DEBUG_
  Serial.print("clientName=");Serial.println(myName);
#endif
}

/*
 * Sets the product of this device
 */
void Symphony::setProduct(SymphProduct p) {
	product = p;
}

/*
 * This is where we set the callback function for setting the value for a specific pin.
 * This callback is called by the webSocketEvent function
 *
 */
void Symphony::setWsCallback(int (* Callback) (uint8_t * payload, size_t length)) {
  WsCallback_ = Callback;
}
/*
 * This is where we set the callback function for setting the value for a specific pin.
 * This callback is called by the Mqtt event
 *
 */
void Symphony::setMqttCallback(attribStruct (* myMqttCB) (attribStruct property, int cmd)) {
//void Symphony::setMqttCallback(attribStruct (* MyMqttCallback) (attribStruct property, int scmd)) {
	MyMqttCallback_ = myMqttCB;
}

/*
 * This is for the OTA loading
 */
//void Symphony::startOTA() {
//  Serial.print("OTA starting, host is ");Serial.println(hostName.c_str());
//  ArduinoOTA.setHostname(hostName.c_str());
//  ArduinoOTA.onStart([]() {
//	if (Debug.isActive(Debug.INFO)) { // Debug message long
//		Debug.println("Start");
////		Serial.println("Start");
//    }
//	pinMode(2, OUTPUT);
//  });
//  ArduinoOTA.onEnd([]() {
//	if (Debug.isActive(Debug.INFO)) { // Debug message long
//		Debug.println("\nEnd");
//	}
//	pinMode(2, INPUT);
//  });
//  ArduinoOTA.onProgress([](unsigned int progress, unsigned int total) {
//	if (Debug.isActive(Debug.INFO)) { // Debug message long
//		Debug.printf("Progress: %u%%\r", (progress / (total / 100)));
//	}
//	digitalWrite(2, !digitalRead(2));
//  });
//  ArduinoOTA.onError([](ota_error_t error) {
//	if (Debug.isActive(Debug.INFO)) { // Debug message long
//		Debug.printf("Error[%u]: ", error);
//	}
//	if (error == OTA_AUTH_ERROR) Serial.println("OTA Auth Failed");
//	else if (error == OTA_BEGIN_ERROR) Serial.println("OTA Begin Failed");
//	else if (error == OTA_CONNECT_ERROR) Serial.println("OTA Connect Failed");
//	else if (error == OTA_RECEIVE_ERROR) Serial.println("OTA Receive Failed");
//	else if (error == OTA_END_ERROR) Serial.println("OTA End Failed");
//  });
//  ArduinoOTA.begin();
//  Serial.print("OTA is ready at ");Serial.println(hostName.c_str());
//}


///*
// * We are exposing this as a static method so that MqttUtil can call after it parses the data received from the subscribed topic
// */
//void Symphony::doCommand(String ssid, String scmd){  //static method for handling the command for the SSID
//	Serial.println("Symphony::doCommand");
//}
//
///* DEPRECATED Apr 15, 2017
// * We are exposing this as a static method so any object can call this method to manually trigger the Comma
// */
//void Symphony::doCommand(const char *ssid, const char *scmd){  //static method for handling the command for the SSID
//	Serial.println("Symphony::doCommand");
//	CommandCallback(ssid, scmd);
//}
/*
 * We are exposing this as a static method so any object can call this method to manually trigger the Command
 * This CommandCallbak is the same method as the Mqtt Callback.
 * CAUTION: Take note that the CommandCallback sends data to the websocket clients.
 */
void Symphony::setProperty(const char *ssid, const char *scmd){  //static method for handling the command for the SSID
	Serial.printf("Symphony::setProperty(%s, %s)\n", ssid, scmd);
	CommandCallback(ssid, scmd);
}
/*
 * This is to show the status of the device
 *   wifi connected (ip address)
 *   AP enabled SSID (ip address)
 *   MQTT connected
 *   BM connected
 */
void Symphony::showStatus() {
	Serial.printf("wifi connected:%d ip:%s hostname:%s.local\n", isWifiConnected, WiFi.localIP().toString().c_str(), hostName.c_str());
	if (apMode==1 || apMode==2)
		Serial.printf("AP Mode:%d ip:%s SSID:%s pass:%s\n", apMode, apIP.toString().c_str(), ap_ssid.c_str(), ap_passphrase.c_str());
	else
		Serial.printf("AP Mode:%d\n", apMode);
	Serial.printf("MQTT connected:%d\n", isMqttSuccessful);
	Serial.printf("BM connected:%d\n", MqttUtil::isConnectedToBM);
}

/**
 * Should be called before the Symphony::setup
 * this is used by the child class to add server URL
 */
void Symphony::on(const char* uri, ESP8266WebServer::THandlerFunction handler) {
	server.on(uri, handler);
}

/**
 * sends response to the webserver
 * this is used by the child class to send response to the client
 */
void Symphony::sendResponse(const char * response){
	server.send(200, "text/html", response);
	server.sendHeader("Done", response, true);
}

/**
 * sends message to websocket clients
 */
void Symphony::sendToWSClient() {
	for (int i=0; i<=webSocketClientCount; i++) {
		webSocketServer.sendTXT(i, "Disconnected");
	}
}

/***********************************************************************
*					Utility classes
************************************************************************/
/*
 * This is the class for the holder of data received by Websocket
 * Data is of the format <ssid>=<value>
 *    ex 0006=1
 */
WsData::WsData(uint8_t * payload, size_t length){
	ssid = "";
	value = "";
	boolean isDelimeter = false;
	boolean afterDelimeter = false;
	String tmp;
	for (int i=0; i< length; i++) {
//		Serial.print((char)payload[i]);
		if ((char)payload[i] == '=')
			isDelimeter = true;
		if (!isDelimeter) {
			tmp += (char)payload[i];
		} else {
			if (!afterDelimeter)
				afterDelimeter = true;
			else
				value += (char)payload[i];
		}
	}
	Serial.println(tmp);
	deviceName = tmp.substring(0, tmp.indexOf(':'));
	ssid = tmp.substring(tmp.indexOf(':') + 1);
#ifdef DEBUG_
	Serial.print("  deviceName=");Serial.print(deviceName);
	Serial.print("  ssid=");Serial.print(ssid);
	Serial.print("  value=");Serial.println(value);
#endif
}
String WsData::getDeviceName() {
	return deviceName;
}
String WsData::getSSID() {
	return ssid;
}
String WsData::getValue() {
	return value;
}



/*
 * DeviceDiscovery.h
 *
 *TODO Feb 10 2019
 *TODO Modify the data structure to this:
 *TODO { "client" : {"ip": 192
 *TODO 						- 168
 *TODO 							- 0
 *TODO 								[111: {"name" : name1, "mac" : macAddress},
 *TODO 						 		 112: {"name" : name1, "mac" : macAddress},
 *TODO 						 		 113: {"name" : name1, "mac" : macAddress}]},
 *TODO 	"server" : {"ip" : 192
 *TODO 						- 168
 *TODO 							- 0
 *TODO 								- 110
 *TODO 				"name" : name}
 *TODO
 *
 *
 * Enables the Symphony core to do simple device discovery.  Uses multicast to manage the ip addresses.
 * multicast ip is 239.1.2.3 port is 1234
 *
 *	json format
 *	{"mode":<int>, "name":<String>, "ip":<String>, "data":<array>}
 *	modes:
 *		1 : identify request
 *			- sent to identify itself to the other symphony devices
 *				* mandatory: "name": device name
 *				* mandatory: "ip":ip address [octet0, octet1, octet2, octet3]
 *			- if no response is received after 3 tries, will identify self if qualified as server
 *		10: identify response
 *			- response sent by the server, if there already is a server
 *				* mandatory: "data": server ip address of all the active devices
 *
 *  	2 : description request
 *  		- requests for more details on the device
 *  			* mandatory: "ip": ip address of target device
 *  	12: description response
 *  		- response sent by target device
 *  			* optional: "url": could contain url of device
 *  			* optional: "services": array that could contain services provided by device
 *  				[{"svc":<String>}]
 *
 *  	3 : notification request
 *  		- notification sent
 *  			* mandatory:
 *  	13: notification response
 *
 *
 *	transaction flow:
 *	identify
 *		client							server
 *		identify(ip,name)----------->saveClient(ip,name)
 *		end<-------------------------identify(server ip)
 *
 *	description
 *		client											targetDevice
 *		description(ip of target device)----------->getDescription(ip)
 *		end<----------------------------------------sendDescription(jsonArray)
 *
 *	notification
 *		TODO
 *
 *  Created on: Nov 3, 2018
 *      Author: cels
 */
#include <ESPAsyncUDP.h>

#ifndef DEVICEDISCOVERY_H_
#define DEVICEDISCOVERY_H_

#define DEBUG_DISCOVERY
#define MAX_ID_COUNT 3

#define ID_REQ 1
#define ID_RESP 11
#define DESC_REQ 2
#define  DESC_RESP 12
#define  NOTI_REQ 3
#define  NOTI_RESP 13

/**
 * The object that holds the discovery device info
 */
struct deviceStruct {
	IPAddress ip;		//the ip address of the device
	String name;		//the name of this device
	String mac;			//the mac address of the device
	boolean active;		//indicator if the device is still active
	long time;			//the time indicator in millis
};
deviceStruct *clientDevices = NULL;		//the array of clientDevices
deviceStruct serverDevice;

AsyncUDP udp;
DynamicJsonBuffer gJsonBuffer;
JsonObject& gJson = gJsonBuffer.createObject();
JsonArray& devices = gJsonBuffer.createArray();	//container for the device info
JsonArray& gServerIP = gJson.createNestedArray("serverIp");
bool isDiscoveryServer = false;
String deviceName;
String mac;
uint8_t idCounter = 0;
long timeMillis = millis();
long timerIntervalMillis = 120000;  //2-min timer interval

/**
 * The helper object that simplifies the handling of devices discovered.
 *
 * This creates JsonArray& devices;
 *
 * [
 * 	{"name":"test1","i":0,"ip":[192,168,0,103]},
 * 	{"name":"test2","i":0,"ip":[192,168,0,103]}
 * ]
 *
 *
 */
class DeviceInfo {
	public:
		DeviceInfo();
		void add(String name, IPAddress ip, String mac);
		void addServer(String name, IPAddress ip);
		void selectServer();
		void refresh();
		boolean exists(IPAddress findIP);
	private:
		long timeMs;
		int size = 0;
};
DeviceInfo::DeviceInfo() {
	timeMs = millis();
	clientDevices = new deviceStruct[0];
}
/*
 * adds a device object to JsonArray& devices;
 */
void DeviceInfo::add(String name, IPAddress ip, String mac) {
	JsonObject& device = devices.createNestedObject();
	device["name"] = name;
	device["mac"] = mac;
	device["time"] = millis();
	device["active"] = true;
	JsonArray& theIp = device.createNestedArray("ip");
	theIp.add(ip[0]);
	theIp.add(ip[1]);
	theIp.add(ip[2]);
	theIp.add(ip[3]);

	size++;
	deviceStruct ds;
	ds.ip = ip;
	ds.name = name;
	ds.mac = mac;
	ds.active = true;
	ds.time = millis();
	deviceStruct* temp = new deviceStruct[size];
	Serial.println("*****start**** using struct for client devices");
	for (int i=0; i<size; i++) {
		Serial.printf("     ********* client devices i:%i size:%i\n", i, size);
		if ( i == size-1) {
			temp[i] = ds;
			Serial.printf("        ****** client devices name:%s mac:%s IP:%u.%u.%u.%u\n", ds.name.c_str(), ds.mac.c_str(), ds.ip[0], ds.ip[1], ds.ip[2], ds.ip[3]);
		} else {
			temp[i] = clientDevices[i];
			Serial.printf("        ****** client devices name:%s mac:%s IP:%u.%u.%u.%u\n", clientDevices[i].name.c_str(), clientDevices[i].mac.c_str(), clientDevices[i].ip[0], clientDevices[i].ip[1], clientDevices[i].ip[2], clientDevices[i].ip[3]);
		}
	}
	delete [] clientDevices;
	clientDevices = temp;
	Serial.println("*****end****** using struct for client devices");
}
/*
 * This is called if no server is responding to sendIdentify requests (idCounter > MAX_ID_COUNT)
 * This will select the device with the lowest active ip address as the server.
 */
void DeviceInfo::selectServer() {
#ifdef DEBUG_DISCOVERY
		Serial.printf("[selectServer] count:%i, will nominate server:\n", idCounter);
#endif
		int lowest = 500;
		//we loop in the devices, to evaluate if our ip's 4th octet is the lowest from the active devices. If it is, then we will be the server
		for (int i = 0; i < devices.size(); i++) {
#ifdef DEBUG_DISCOVERY
			Serial.printf("[selectServer]devices IP: %u.%u.%u.%u, i:%i, me:%s, serverIp: %u.%u.%u.%u\n",
				devices[i]["ip"][0].as<int>(),
				devices[i]["ip"][1].as<int>(),
				devices[i]["ip"][2].as<int>(),
				devices[i]["ip"][3].as<int>(),i, WiFi.localIP().toString().c_str(),
				gServerIP[0].as<int>(),
				gServerIP[1].as<int>(),
				gServerIP[2].as<int>(),
				gServerIP[3].as<int>()
				);
			Serial.printf("[(struct)selectServer]devices IP: %u.%u.%u.%u, i:%i, me:%s, serverIp: %u.%u.%u.%u\n",
				clientDevices[i].ip[0], clientDevices[i].ip[1], clientDevices[i].ip[2],clientDevices[i].ip[3],
				i, WiFi.localIP().toString().c_str(),
				serverDevice.ip[0], serverDevice.ip[1], serverDevice.ip[2], serverDevice.ip[3]
				);
#endif
			if (clientDevices[i].ip[3] <= lowest && clientDevices[i].ip[3] != serverDevice.ip[3] && clientDevices[i].active) {
				lowest = clientDevices[i].ip[3];
			}
			if (devices[i]["ip"][3].as<int>() <= lowest && devices[i]["ip"][3].as<int>() != gServerIP[3].as<int>() && devices[i]["active"].as<bool>()) {
				lowest = devices[i]["ip"][3].as<int>();
			}
		}
		if (lowest == WiFi.localIP()[3] ) {
//			Serial.println("Ayos!");
			isDiscoveryServer = true;
			gServerIP[0] = WiFi.localIP()[0];
			gServerIP[1] = WiFi.localIP()[1];
			gServerIP[2] = WiFi.localIP()[2];
			gServerIP[3] = WiFi.localIP()[3];
			serverDevice.ip[0] = WiFi.localIP()[0];
			serverDevice.ip[1] = WiFi.localIP()[1];
			serverDevice.ip[2] = WiFi.localIP()[2];
			serverDevice.ip[3] = WiFi.localIP()[3];
		} else {
			isDiscoveryServer = false;		//we might need to relinquish server mode, this happens if many devices start at almost the same time (within 30s)
		}
}
/*
 * adds a server object to JsonArray& devices;
 */
void DeviceInfo::addServer(String name, IPAddress ip) {

}
/*
 * Loops into JsonArray& devices and determines if a device is still alive by looking at the ["i"] element.
 * if ["i"] > 7, device is still alive
 */
void DeviceInfo::refresh() {
	if (millis() - timeMs >= timerIntervalMillis * 10) {	//refresh rate is 10 * timerIntervalMillis
		timeMs = millis();
#ifdef DEBUG_DISCOVERY
		Serial.println("\n********************** DeviceInfo::refresh start");
		devices.printTo(Serial);
#endif
		for (int i = 0; i < size; i++) {
			//if time > 3* timerIntervalMillis & ip is not ip of this device, mark it as inactive
			if (millis() - clientDevices[i].time > 3 * timerIntervalMillis && clientDevices[i].ip[3] != WiFi.localIP()[3]) {
				clientDevices[i].active = false;
			}
		}
		for (int i = 0; i < devices.size(); i++) {
			//if time > 3* timerIntervalMillis & ip is not ip of this device, mark it as inactive
			if (millis() - devices[i]["time"].as<long>() > 3 * timerIntervalMillis && devices[i]["ip"][3] != WiFi.localIP()[3]) {
				devices[i]["active"] = false;
			}
		}
#ifdef DEBUG_DISCOVERY
		Serial.println("\n********************** DeviceInfo::refresh end");
#endif
	}
}
/*
 * Finds the findIP from the JsonArray& devices;
 * returns 	true if found, and increments i
 * 			false if not found
 */
boolean DeviceInfo::exists(IPAddress findIP) {
	boolean isFound = false;
#ifdef DEBUG_DISCOVERY
	Serial.println("\n********************** DeviceInfo::exists start");
	devices.printTo(Serial);
#endif
	for (int i = 0; i < devices.size(); i++) {
#ifdef DEBUG_DISCOVERY
		JsonArray& iArray = devices[i]["ip"];
		Serial.printf("\nDevice IP: %u.%u.%u.%u, find IP:%u.%u.%u.%u, i:%i\n",
				iArray[0].as<int>(), iArray[1].as<int>(), iArray[2].as<int>(), iArray[3].as<int>(),
//			devices[i]["ip"][0].as<int>(), devices[i]["ip"][1].as<int>(), devices[i]["ip"][2].as<int>(), devices[i]["ip"][3].as<int>(),
			findIP[0],findIP[1], findIP[2], findIP[3], i);
		char* s;
		Serial.printf("************ client Device (IP:%u.%u.%u.%u, active:%u find IP:%u.%u.%u.%u, i:%i time:",
			clientDevices[i].ip[0], clientDevices[i].ip[1], clientDevices[i].ip[2], clientDevices[i].ip[3],
			clientDevices[i].active,
			findIP[0],findIP[1], findIP[2], findIP[3], i);
		Serial.println(clientDevices[i].time);
#endif
		//we evaluate the 4th ip digit since we assume that these devices belong to the same network
		if (clientDevices[i].ip[3] == findIP[3]) {
#ifdef DEBUG_DISCOVERY
			Serial.printf("************ client Device found match IP:%u.%u.%u.%u, find IP:%u.%u.%u.%u, i:%i\n",
				clientDevices[i].ip[0], clientDevices[i].ip[1], clientDevices[i].ip[2], clientDevices[i].ip[3],
				findIP[0],findIP[1], findIP[2], findIP[3], i);
#endif
			clientDevices[i].active = true;
			clientDevices[i].time = millis();
		}
		if (devices[i]["ip"][3].as<int>() == findIP[3]) {
			isFound = true;
			devices[i]["active"] = true;
			devices[i]["time"] = millis();
			break;
		}
	}
#ifdef DEBUG_DISCOVERY
	Serial.println("\n********************** DeviceInfo::exists end");
#endif
	return isFound;
}

DeviceInfo deviceInfo = DeviceInfo();  //container of the devices that are discovered, includes self

/**
 *  handles the packets that arrive
 *  mode
 *  	1 : init
 *  	2 : response to init
 */
void packetArrived(AsyncUDPPacket packet) {
	//we received a data, let us determine if we are the server
	DynamicJsonBuffer jsonParsedBuffer;
	JsonObject & json = jsonParsedBuffer.parseObject(packet.data());
#ifdef DEBUG_DISCOVERY
	Serial.printf("\n[packetArrived] start, i am %s devices.size:%i\nGlobal json is:\n", isDiscoveryServer ? "Server" : "Client", devices.size());
	Serial.println("packet.data parsed:");
	json.printTo(Serial);
	Serial.println();
#endif
	if (json.containsKey("mode")) {
		uint8_t mode = json["mode"];
		if (mode == ID_REQ) {
/*
 * TODO
 *   need to remove the ip from the ipData array if a client is not sending identify command for 3 consecutive runs
 */

			//a client sent an identify command
#ifdef DEBUG_DISCOVERY
			Serial.println("**************** start ID_REQ MODE ***********************");
#endif
			if (json.containsKey("ip")) {
				if (deviceInfo.exists(packet.remoteIP())) {


				} else {
					//device is new, we add it to deviceInfo
					deviceInfo.add(json["name"], packet.remoteIP(), json["mac"]);
				}
			}
			if (isDiscoveryServer) {
				//send reply since i am the server
				DynamicJsonBuffer jsonBuffer;
				JsonObject& reply = jsonBuffer.createObject();
				reply["mode"] = ID_RESP;
				JsonArray& serverIP = reply.createNestedArray("serverIp");
				serverIP.add(WiFi.localIP()[0]);
				serverIP.add(WiFi.localIP()[1]);
				serverIP.add(WiFi.localIP()[2]);
				serverIP.add(WiFi.localIP()[3]);

				idCounter = 0;
				String replyStr;
				reply.printTo(replyStr);
				packet.printf(replyStr.c_str());
#ifdef DEBUG_DISCOVERY
				Serial.println("I am server, will reply with:");\
				reply.printTo(Serial);
				Serial.println("\ndone reply");
#endif
			}
#ifdef DEBUG_DISCOVERY
			Serial.println("**************** end ID_REQ MODE ***********************");
#endif
		} else if (mode == ID_RESP) {
#ifdef DEBUG_DISCOVERY
			Serial.println("**************** start ID_RESP MODE ***********************");
#endif
			idCounter = 0;
			if (json.containsKey("serverIp")) {
				//this is the response from the server
				gServerIP[0] = packet.remoteIP()[0];
				gServerIP[1] = packet.remoteIP()[1];
				gServerIP[2] = packet.remoteIP()[2];
				gServerIP[3] = packet.remoteIP()[3];

				serverDevice.ip[0] = packet.remoteIP()[0];
				serverDevice.ip[1] = packet.remoteIP()[1];
				serverDevice.ip[2] = packet.remoteIP()[2];
				serverDevice.ip[3] = packet.remoteIP()[3];

			}
#ifdef DEBUG_DISCOVERY
			Serial.println("Got server response.");
			gServerIP.printTo(Serial);
			Serial.println();
			Serial.println("**************** end ID_RESP MODE ***********************");
#endif
		} else if (mode == NOTI_REQ) {
			if (isDiscoveryServer) {
				DynamicJsonBuffer jsonBuffer;
				JsonObject& reply = jsonBuffer.createObject();
				reply["mode"] = NOTI_RESP;
//				reply["info"] = gJson;
				reply["info"] = devices;
				JsonArray& serverIP = reply.createNestedArray("serverIp");
				serverIP[0] = serverDevice.ip[0];
				serverIP[1] = serverDevice.ip[1];
				serverIP[2] = serverDevice.ip[2];
				serverIP[3] = serverDevice.ip[3];
				reply["server"] = gServerIP;
				reply["serverNew"] = serverIP;
				String replyStr;
				reply.printTo(replyStr);
				packet.printf(replyStr.c_str());
#ifdef DEBUG_DISCOVERY
				Serial.println("************* isDiscoveryServer");
				reply.printTo(Serial);
				Serial.println();
#endif
			}
		}
	}
#ifdef DEBUG_DISCOVERY
	Serial.println("[packetArrived] end\n");
#endif
}
/*
 * Sent to the UDP server to identify this device.
 * put this in the loop function of arduino code.
 * sendIndentify
 * 	timerInterval should not be too small as it will flood the network
 *
 */
void sendIdentify() {
	if (WiFi.status() == WL_CONNECTED ) {
		deviceInfo.refresh();
		if (millis() - timeMillis >= timerIntervalMillis) {	//send multicast every timerIntervalMillis ms
			timeMillis = millis();
			if (!isDiscoveryServer)
				idCounter++;
			DynamicJsonBuffer jsonIDBuffer;
			JsonObject& tmpJson = jsonIDBuffer.createObject();
			tmpJson["mode"] = 1;
			tmpJson["name"] = deviceName;
			tmpJson["mac"] = mac;
			JsonArray& data = tmpJson.createNestedArray("ip");
			data.add(WiFi.localIP()[0]);
			data.add(WiFi.localIP()[1]);
			data.add(WiFi.localIP()[2]);
			data.add(WiFi.localIP()[3]);
#ifdef DEBUG_DISCOVERY
			Serial.printf("[sendIdentify %s] count:%i, will send data:\n", isDiscoveryServer ? "Server" : "Client", idCounter);
			tmpJson.printTo(Serial);
			Serial.printf("\n[sendIdentify %s] data sent.\n", isDiscoveryServer ? "Server" : "Client");
#endif
			String dataToSend;
			tmpJson.printTo(dataToSend);
			udp.printf(dataToSend.c_str());		//send an init command to the udp server
		}
		if (idCounter > MAX_ID_COUNT) {
			//count for identify message already exceeded
#ifdef DEBUG_DISCOVERY
			Serial.printf("[sendIdentify] count:%i, Server not responding, will evaluate if i can be server:\n", idCounter);
			idCounter = 0;
#endif
			//nominateServer();
			deviceInfo.selectServer();
		}
	}
}
/**
 * the overloaded function
 */
void sendIdentify(long intervalMillis) {
	timerIntervalMillis = intervalMillis;
	sendIdentify();
}
/*
 * Starts a UDP server to listen to Symphony devices and stores their IP addresses.
 * this will enable "auto-discovery" from the udp server 239.1.2.3 port 1234
 */
void startDiscovery(String name, String mac_) {
#ifdef DEBUG_DISCOVERY
	Serial.println("[startDiscovery] start");
#endif
	deviceName = name;
	mac = mac_;
	if (udp.listenMulticast(IPAddress(239, 1, 2, 3), 1234)) {
		udp.onPacket(packetArrived);
		long timeMillis = millis();
		sendIdentify();
		deviceInfo.add(deviceName, WiFi.localIP(), mac_);
#ifdef DEBUG_DISCOVERY
		Serial.println("[startDiscovery]");
		gJson.printTo(Serial);
#endif
		if (gServerIP.size() <= 0) {
			//we create a new array of serverIP, we should ensure that there is only 1 IP.
			//possibility of creating more IPs is when connection to router is gone and this device reconnects
			gServerIP.add(0);
			gServerIP.add(0);
			gServerIP.add(0);
			gServerIP.add(0);
		}
		serverDevice.ip[0] = 0;
		serverDevice.ip[1] = 0;
		serverDevice.ip[2] = 0;
		serverDevice.ip[3] = 0;
	}
//#ifdef DEBUG_DISCOVERY
//	Serial.println("\n[startDiscovery] end");
//	Serial.println("************* start test JSON ******************");
//	DynamicJsonBuffer jsonBuff;
//	JsonObject& jsonObj = jsonBuff.createObject();
//	JsonObject& client = jsonObj.createNestedObject("client");
//	JsonArray& ip = client.createNestedArray("192.168.0");
//	JsonObject& device1 = ip.createNestedObject();
//	device1["id"] = "111";
//	device1["name"] = "device1";
//	device1["mac"] = "mac1";
//	JsonObject& device2 = ip.createNestedObject();
//	device2["id"] = "112";
//	device2["name"] = "device2";
//	device2["mac"] = "mac2";
//	JsonObject& server = jsonObj.createNestedObject("server");
//	server["ip"] = 192;
//	jsonObj.prettyPrintTo(Serial);
//	Serial.println("\n************* end test JSON ******************");
//#endif
}


#endif /* DEVICEDISCOVERY_H_ */


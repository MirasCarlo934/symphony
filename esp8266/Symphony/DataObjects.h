/*
 * DataObjects.h
 *
 *	Contains the data structure for the data being used to send via MQTT and Websocket
 *  Created on: Mar 28, 2020
 *      Author: cels
 */

#ifndef DATAOBJECTS_H_
#define DATAOBJECTS_H_

#include <Arduino.h>

#define  CORE_INIT 1					//a core command from the client for the initialization of the websocket, deprecated
#define  CORE_COMMIT_DEVICE_SETTINGS 2	//a core command from admin WS Client to commit AP, Passkey, Device Name, mqttEnabled, mqttIp and mqttPort. Device reboot is done.
#define  CORE_DELETE 3					//a core command from admin WS Client to delete file in Spiffs
#define  CORE_GETDEVICEINFO 4			//not used
#define  CORE_PING 5					//a core command from WS client to send heartbeat to device
#define  CORE_START_HEARTBEAT 8 		//a core command from device to start heartbeat to WS client. This will enable WS client to aware when we are done with reboot
#define  CORE_VALUES 20					//a core command from WS client requesting for the current values of the product.  This is used by the WS client to display the current state of the product.
#define  CORE_CONTROL 7					//a core command from WS client to control the device.

#define  CMD_INIT 1						//a command from device to the client for the initiation of the websocket, deprecated
#define  CMD_VALUES 2					//a command from device to the client when sending the values requested via CORE_VALUES
#define  WSCLIENT_CONTROL 7				//a core command from device to the WS Client
#define  WSCLIENT_DO_CMD 10				//a command from device to the client to change the state of the elements displayed. This is used to update the GUI when values change in the product's properties
#define  WSCLIENT_DO_DISPLAY 20			//a core command from device to the client to change the state of the elements displayed. This is used to update the GUI when values change in the product's properties
#define  CMD_PIN_CONTROL 10				//a command the client to change the state of the pins of the device. This should update the product's properties.

/*
 * Object for Websocket communication
 */
struct wsData {
	int core;
	int cmd;
	String mac;
	String property;
	int value;
};



#endif /* DATAOBJECTS_H_ */

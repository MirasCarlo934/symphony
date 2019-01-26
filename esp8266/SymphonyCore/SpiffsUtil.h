/*
 * SpiffsUtil.h
 *
 *  Created on: Apr 7, 2017
 *      Author: cels
 */
#include "FS.h"

#ifndef SPIFFSUTIL_H_
#define SPIFFSUTIL_H_

class SpiffsUtil {
public:
	SpiffsUtil();
	virtual ~SpiffsUtil();
	static void saveToSPIFFS(const char * filename, const char * data);
	static String readFrSPIFFS(const char * filename);
	static String readFrSPIFFS(const char * filename, int b);
	static void saveMqttIP(const char * mqttIP);
	static String readMqttIP();
	static void saveProductID(const char * productID);
	static String readProductID();
	static void saveDisplayName(const char * dispName);
	static String readDisplayName();
	static void saveRoom(const char * room);
	static String readRoom();
	static void saveSsid(const char * ssid);
	static String readSsid();
	static void savePwd(const char * pwd);
	static String readPwd();
	static void saveMyMode(const char * mode);
	static String readMyMode();
};

#endif /* SPIFFSUTIL_H_ */

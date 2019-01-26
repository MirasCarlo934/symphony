/*
 * Filemanager.h
 *
 *  Created on: Sep 29, 2018
 *      Author: cels
 */

#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>	//for some reason this "error: 'WiFiUDP' has not been declared" occurs if this is not included
#include "FS.h"

#define UPLOAD_START 0
#define UPLOAD_ONGOING 1
#define UPLOAD_END 2
#define UPLOAD_ABORTED 3

class Filemanager {
	public:
		//constructor
		Filemanager();
		//update the firmware
		int8_t updateFirmware(String filename, size_t index, uint8_t *data, size_t len, bool final);
		int8_t uploadFile(String filename, size_t index, uint8_t *data, size_t len, bool final);
		String getFiles();
		bool delFile(String path);
		void saveConfig(const char * data);
		String readConfig();
		int8_t saveToSPIFFS(const char * filename, const char * data);
		String readFrSPIFFS(const char * filename);
	private:
		uint16_t upload_status = 0;
		uint16_t update_status = 0;
		bool fwUpdateError = false;
};


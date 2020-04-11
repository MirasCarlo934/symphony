/**
 * Firmware.cpp
 *
 *  Created on: Sep 29, 2018
 *      Author: cels
 *
 * 	This is where the Firmware update is done.
 *
 */

#include "FileManager.h"
#ifndef ARDUINOJSON_H_
#include <ArduinoJson.h>
#endif

//#define DEBUG_

File fsUploadFile;              // a File object to temporarily store the received file

/**
 * 	the default constructor
 */
Filemanager::Filemanager() {
	bool result = SPIFFS.begin();	//we mount the SPIFFS here so that webServer.serveStatic would work
	if (result) {
		Serial.println("SPIFFS mounted successfully.");
	} else {
		Serial.println("Error mounting SPIFFS.");
	}
}

/*
 * updates the firmware
 * returns the following:
 *   0 		- 	success
 *   -1 	-	begin error
 *   -2		-	write error
 *   -3		-	end error
 */
int8_t Filemanager::updateFirmware(String filename, size_t index, uint8_t *data, size_t len, bool final) {
//	Serial.printf("*********updateFirmware filename=%s index=%d len=%d final=%d\n", filename.c_str(),index,len, final);
	  // handler for the file upload, get's the sketch bytes, and writes
	  // them through the Update object
	  if(update_status == UPLOAD_START){
		// Clean SPIFFS
		SPIFFS.end();
		pinMode(2, OUTPUT);
		update_status = UPLOAD_ONGOING;
		WiFiUDP::stopAll();

		uint32_t realSize = ESP.getFlashChipRealSize();
		uint32_t ideSize = ESP.getFlashChipSize();
		FlashMode_t ideMode = ESP.getFlashChipMode();

		Serial.printf("\n\nUpdate: %s\n", filename.c_str());

		Serial.printf("Flash real id:   %08X\n", ESP.getFlashChipId());
		Serial.printf("Flash real size: %u bytes\n", realSize);
		Serial.printf("Flash ide  size: %u bytes\n", ideSize);
		Serial.printf("Flash ide speed: %u Hz\n", ESP.getFlashChipSpeed());
		Serial.printf("Flash ide mode:  %s\n", (ideMode == FM_QIO ? "QIO" : ideMode == FM_QOUT ? "QOUT" : ideMode == FM_DIO ? "DIO" : ideMode == FM_DOUT ? "DOUT" : "UNKNOWN"));

		uint32_t maxSketchSpace = (ESP.getFreeSketchSpace() - 0x1000) & 0xFFFFF000;
		Serial.printf("maxSketchSpace: %d\n\n", maxSketchSpace);
		if(!Update.begin(maxSketchSpace)){//start with max available size
			Serial.printf("Error: size is zero\n");
			Update.printError(Serial);
			fwUpdateError = true;
			return -1;
		} else {
			if(Update.write(data, len) != len){
				Serial.printf("Error: write begin\n");
				fwUpdateError = true;
				return -2;
			}
		}
	  } else if(update_status == UPLOAD_ONGOING && !fwUpdateError && !final){
//	    	  Serial.print(".");
		digitalWrite(2, !digitalRead(2));
		if(Update.write(data, len) != len){
			Serial.printf("Error: write\n");
			fwUpdateError = true;
			return -2;
		}
	  } else if(update_status == UPLOAD_ONGOING && !fwUpdateError && final){
		digitalWrite(2, false);
		Serial.println("update.end");
		if(Update.write(data, len) != len){
			Serial.printf("Error: write\n");
			fwUpdateError = true;
			return -2;
		}
		if(Update.end(true)){ //true to set the size to the current progress
			Serial.printf("Update Success: %u\n", index + len);
		} else {
			Serial.printf("Error: end\n");
			fwUpdateError = true;
			return -3;
		}
		return 0;
	  }
	  delay(0);
	  return(1);
}
/*
 * uploads the file into SPIFFS
 */
int8_t Filemanager::uploadFile(String filename, size_t index, uint8_t *data, size_t len, bool final) {
//	Serial.printf("*********uploadFile filename=%s index=%d len=%d final=%d\n", filename.c_str(),index,len, final);
//	HTTPUpload& upload = server.upload();
	if(!index){
		String uploadFN = filename;
		if(!filename.startsWith("/"))
			uploadFN = "/"+filename;
		fsUploadFile = SPIFFS.open(uploadFN, "w");            // Open the file for writing in SPIFFS (create if it doesn't exist)
		if (fsUploadFile) {
			Serial.printf("file=%s successfully created/opened.\n", uploadFN.c_str());
			if (fsUploadFile.write(data, len) != len) {
				Serial.printf("Write error file=%s\n", uploadFN.c_str());
				fwUpdateError = true;
				return -7;
			}
		} else {
			Serial.printf("Cannot open file=%s\n", uploadFN.c_str());
			fwUpdateError = true;
			return -1;
		}

		upload_status = UPLOAD_ONGOING;
	} else if(upload_status == UPLOAD_ONGOING && !fwUpdateError && !final){
		if(fsUploadFile) {
			if (fsUploadFile.write(data, len) != len) { // Write the received bytes to the file
				Serial.printf("Write error\n");
				fwUpdateError = true;
				return -7;
			}
		} else {
			Serial.printf("Error in opening file=%s\n",filename.c_str());
			fwUpdateError = true;
			return -2;
		}
	} else if(upload_status == UPLOAD_ONGOING && !fwUpdateError && final){
		if(fsUploadFile) {                                    	// If the file was successfully created
			if (fsUploadFile.write(data, len) != len) { // Write the received bytes to the file
				Serial.printf("Write error\n");
				fwUpdateError = true;
				return -7;
			}
			fsUploadFile.close();                               // Close the file again
			Serial.printf("Upload file Success: %u\n", index + len);
//			server.send(200, "text/html", "File upload successful.");
			upload_status = UPLOAD_START;
			return 0;
		} else {
//			server.send(500, "text/plain", "500: couldn't create file");
			fwUpdateError = true;
			return -3;
		}
	}
	delay(0);
	return 1;
}
/*
 * Returns the files in SPIFFS
 * we use Json format
 * {"files":["file1","file2"...]}
 */
String Filemanager::getFiles() {
	Dir dir = SPIFFS.openDir("");
	StaticJsonBuffer<500> jsonBuffer;
	JsonObject& root = jsonBuffer.createObject();
	JsonArray& data = root.createNestedArray("files");
	while (dir.next()) {
		data.add(dir.fileName());
	}
	String retStr;
	root.printTo(retStr);
	return retStr;
}

/*
 * deletes the file
 */
bool Filemanager::delFile(String path) {
	SPIFFS.remove(path);
}

/*
 * Below is for saving to SPIFFS
 */
int8_t Filemanager::saveToSPIFFS(const char * filename, const char * data) {
  // always use this to "mount" the filesystem
	bool result = SPIFFS.begin();
#ifdef DEBUG_
	Serial.printf("SPIFFS File %s opened for writing: %d\n", filename, result);
#endif
	// open the file in write mode
	File f = SPIFFS.open(filename, "w");
	if (!f) {
#ifdef DEBUG_
		Serial.println("\tfile creation failed");
#endif
	}  else {
		f.println(data);
#ifdef DEBUG_
		Serial.printf("\tdone writing data:%s to file:%s\n", data, filename);
#endif
	}
	f.close();
}
/*
 * Below is for reading from SPIFFS
 */
String Filemanager::readFrSPIFFS(const char * filename) {
#ifdef DEBUG_
  Serial.printf("\t[readFrSPIFFS] start\n");
#endif
  // always use this to "mount" the filesystem
  bool result = SPIFFS.begin();

  if (result) { //we have succesfully mounted SPIFFS
#ifdef DEBUG_
	  Serial.printf("\t[readFrSPIFFS] SPIFFS File %s mounted\n", filename);
#endif
	  // this opens the file in read-mode
	  File f = SPIFFS.open(filename, "r");
	  if (!f) {
#ifdef DEBUG_
		  Serial.printf("\t[readFrSPIFFS] SPIFFS File %s does not exist\n", filename);
#endif
	  } else {
		// we could open the file
		  size_t size = f.size();
//		  size_t size = 56;
		  std::unique_ptr<char[]> buf(new char[size]);
		  f.readBytes(buf.get(), size);
		  f.close();
#ifdef DEBUG_
		  Serial.printf("\t[readFrSPIFFS]Data read from file %s is %s length:%d\n",filename, buf.get(), size);
#endif
		  return buf.get();
	  }
  }
  return "";
//  if (line.length()>0) {
//	  char retData [line.length()+1];
//	  memset(retData, '\0', sizeof(retData));
//	  strncpy(retData, line.c_str(), line.length());
//	  return retData;
//  } else
//	  return "";
}

/*
 * Below is for saving the config into SPIFFS
 */
void Filemanager::saveConfig(const char * data) {
	saveToSPIFFS("/cfg.json", data);
}
/*
 * Below is for getting the config into SPIFFS
 */
String Filemanager::readConfig() {
	return readFrSPIFFS("/cfg.json");
}

/*
 * SpiffsUtil.cpp
 *
 *------------------------------------------------------------------------
 * Utility functions for saving data into the SPIFFS data system
 * all files should have a header in the form:
 * lines=??? where ??? is the number of lines
 ------------------------------------------------------------------------
 *  Created on: Apr 7, 2017
 *      Author: cels
 */
#define DEBUG_
#include "SpiffsUtil.h"

SpiffsUtil::SpiffsUtil() {
	// TODO Auto-generated constructor stub

}

SpiffsUtil::~SpiffsUtil() {
	// TODO Auto-generated destructor stub
}

/*
 * Below is for saving to SPIFFS
 */
void SpiffsUtil::saveToSPIFFS(const char * filename, const char * data) {
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
    //write the mqtt IP end-of-line character
    f.println(data);
#ifdef DEBUG_
    Serial.printf("\tdone writing data:%s to file:%s\n", data, filename);
#endif
  }
  f.close();
}
/*
 * Below is for reading from SPIFFS
 * DEPRECATED Apr 09 2017
 */
String SpiffsUtil::readFrSPIFFS(const char * filename, int b) {
#ifdef DEBUG_
  Serial.printf("\t[readFrSPIFFS] start\n");
#endif
  // always use this to "mount" the filesystem
  bool result = SPIFFS.begin();
//  char c1[filename.length() + 1];
//  filename.toCharArray(c1, filename.length() + 1);
#ifdef DEBUG_
  Serial.printf("\t[readFrSPIFFS] SPIFFS File %s mounted\n", filename);
#endif
  String line = "";
  // this opens the file in read-mode
  File f = SPIFFS.open(filename, "r");
  if (!f) {
#ifdef DEBUG_
	  Serial.printf("\t[readFrSPIFFS] SPIFFS File %s does not exist\n", filename);
#endif
  } else {
    // we could open the file
    int i = 0;
    while(f.available()) {
      //Lets read line by line from the file
      line = f.readStringUntil('\n');
      i++;
#ifdef DEBUG_
      Serial.printf("\t[readFrSPIFFS]line%d Data read from file %s is %s length:%d\n",i, filename, line.c_str(), line.length());
#endif
    }
  }
  f.close();
  char * retData = new char(line.length());
  strcpy(retData, line.c_str());
  return retData;
}

/*
 * Below is for reading from SPIFFS
 */
String SpiffsUtil::readFrSPIFFS(const char * filename) {
#ifdef DEBUG_
  Serial.printf("\t[readFrSPIFFS] start\n");
#endif
  // always use this to "mount" the filesystem
  bool result = SPIFFS.begin();
  String line = "";
  if (result) { //we ahave succesfully mounted SPIFFS
#ifdef DEBUG_
	  Serial.printf("\t[readFrSPIFFS] SPIFFS File %s mounted\n", filename);
	  Serial.printf("\t[readFrSPIFFS]line:%s length:%d\n",line.c_str(), line.length());
#endif
	  // this opens the file in read-mode
	  File f = SPIFFS.open(filename, "r");
	  if (!f) {
#ifdef DEBUG_
		  Serial.printf("\t[readFrSPIFFS] SPIFFS File %s does not exist\n", filename);
#endif
	  } else {
		// we could open the file
		int i = 0;
		while(f.available()) {
		  //Lets read line by line from the file
		  line = f.readStringUntil('\n');
		  i++;
#ifdef DEBUG_
		  Serial.printf("\t[readFrSPIFFS]line%d Data read from file %s is %s length:%d\n",i, filename, line.c_str(), line.length());
#endif
		}
	  }
	  f.close();
  }
  Serial.printf("\t[readFrSPIFFS]Data read from file %s is %s length:%d\n",filename, line.c_str(), line.length());
  if (line.length()>0) {
	  char retData [line.length()];
	  memset(retData, '\0', sizeof(retData));
	  strncpy(retData, line.c_str(), line.length()-1);
	  return retData;
  } else
	  return "";
}

/*
 * Below is for saving the MQTT IP address
 */
void SpiffsUtil::saveMqttIP(const char * mqttIP) {
  saveToSPIFFS("/mqtt.txt", mqttIP);
}
String SpiffsUtil::readMqttIP() {
//  String line = readFrSPIFFS("/mqtt.txt");
  return readFrSPIFFS("/mqtt.txt");
}
/*
 * Below is for saving the Product ID of this device
 */
 void SpiffsUtil::saveProductID(const char * productID) {
  saveToSPIFFS("/p.txt", productID);
 }
 String SpiffsUtil::readProductID() {
  return readFrSPIFFS("/p.txt");
}
 /*
 * Below is for saving the Display name of this device
 */
 void SpiffsUtil::saveDisplayName(const char * dispName) {
  saveToSPIFFS("/d.txt", dispName);
 }
 String SpiffsUtil::readDisplayName(){
//  String line = readFrSPIFFS("/d.txt");
  return readFrSPIFFS("/d.txt");
 }
 /*
 * Below is for saving the room where this device is installed
 */
 void SpiffsUtil::saveRoom(const char * room){
  saveToSPIFFS("/r.txt", room);
 }
 String SpiffsUtil::readRoom(){
//  String line = readFrSPIFFS("/r.txt");
  return readFrSPIFFS("/r.txt");
 }
 /*
  * Below is for saving the SSID where we need to connect
  */
void SpiffsUtil::saveSsid(const char * ssid) {
	saveToSPIFFS("/ssid.txt", ssid);
}
String SpiffsUtil::readSsid() {
//	String line = readFrSPIFFS("/ssid.txt");
	return readFrSPIFFS("/ssid.txt");
}
void SpiffsUtil::savePwd(const char * pwd) {
	saveToSPIFFS("/pwd.txt", pwd);
}
String SpiffsUtil::readPwd() {
//	String line = readFrSPIFFS("/pwd.txt");
//	Serial.println(readFrSPIFFS("/test.html"));
	return readFrSPIFFS("/pwd.txt");
}
void SpiffsUtil::saveMyMode(const char * mode) {
	saveToSPIFFS("/mode.txt", mode);
}
String SpiffsUtil::readMyMode() {
//	String line = readFrSPIFFS("/mode.txt");
	return readFrSPIFFS("/mode.txt");
}

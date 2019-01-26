/*
 * PushBullet.cpp
 *
 *  Created on: Mar 18, 2017
 *      Author: cels
 */

#include "PushBullet.h"

WiFiClientSecure secureClient;
const char* host = "api.pushbullet.com";
const int httpsPort = 443;
const char* PushBulletAPIKEY = "amwCYLajxwvZzG42u4tdprlOLH4vinbO"; //
const char* fingerprint = "E7 06 F1 30 B1 5F 25 72 00 4D 99 F0 ED 90 4D F9 60 D3 FB 75"; //got it using https://www.grc.com/fingerprints.htm by inputting api.pushbullet.com

PushBullet::PushBullet() {
	// TODO Auto-generated constructor stub

}

/*---------------------------------------------------------------------
 * Below are for the pushbullet transactions
 *---------------------------------------------------------------------*/

boolean PushBullet::connectPushBullet() {
  // Use WiFiClientSecure class to create TLS connection
#ifdef DEBUG_
  Serial.print("connecting to ");
  Serial.println(host);
#endif
  if (!secureClient.connect(host, httpsPort)) {
    Serial.println("connection failed");
    return false;
  }
  if (secureClient.verify(fingerprint, host)) {
    Serial.println("certificate matches");
  } else {
    Serial.println("certificate doesn't match");
    return false;
  }

  return true;
}

void PushBullet::sendToPB(String msg) {
  String url = "/v2/pushes";
  String messagebody = "{\"type\": \"note\", \"title\": \"ESP8266\", \"body\": \""+msg+"\"}\r\n";
#ifdef DEBUG_
  Serial.print("requesting URL: ");
  Serial.println(url);
#endif
  secureClient.print(String("POST ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" +
               "Authorization: Bearer " + PushBulletAPIKEY + "\r\n" +
               "Content-Type: application/json\r\n" +
               "Content-Length: " +
               String(messagebody.length()) + "\r\n\r\n");
  secureClient.print(messagebody);
  Serial.println("[sendToPB]request sent");
  //print the response
  while (secureClient.available() == 0);
#ifdef DEBUG_
  Serial.println("--------below is the response-----------");
#endif
  while (secureClient.available()) {
    String line = secureClient.readStringUntil('\n');
#ifdef DEBUG_
    Serial.println(line);
#endif
  }
#ifdef DEBUG_
  Serial.println("end of response");
#endif
}


PushBullet::~PushBullet() {
	// TODO Auto-generated destructor stub
}


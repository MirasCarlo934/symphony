/*
 * PushBullet.h
 *
 *  Created on: Mar 18, 2017
 *      Author: miras
 */

#ifndef PUSHBULLET_H_
#define PUSHBULLET_H_

#define DEBUG_

#include <WiFiClientSecure.h>


class PushBullet {
public:
	PushBullet();
	boolean static connectPushBullet();
	void static sendToPB(String msg);
	virtual ~PushBullet();
private:

};

#endif /* PUSHBULLET_H_ */

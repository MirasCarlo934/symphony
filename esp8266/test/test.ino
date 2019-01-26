#include "Symphony.h"

Symphony s = Symphony();

void setup(void) {

	Serial.begin(115200);
	delay(10);
	Serial.println("\n\n************START test Setup***************");
	pinMode(12, OUTPUT);
	s.setup();
	Serial.println("\n\n************END test Setup***************");
}

void loop(void) {
	s.loop();
	delay(2000);
	digitalWrite(12, !digitalRead(12));
}


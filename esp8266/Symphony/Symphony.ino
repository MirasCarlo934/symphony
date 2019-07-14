#include "Symphony.h"

Symphony s = Symphony();
void setup()
{
	Serial.begin(115200);
	// Switch to station mode and disconnect just in case
	Serial.println("\n\n************START setup***************");
	s.setup("symphony", "1.0");
	Serial.println("************END setup***************");
}

void loop()
{
	s.loop();
}

@echo %1 %2
REM %1 is where the source files are, ex D:\git_symphony\esp8266\Symphony\version.h 
REM %2 is where the arduino libraries are, ex D:\projects\arduinolibraries\Symphony\*
for /f "tokens=3" %%v in (%1\version.h) do set version=%%v
@echo version=%version%
SET /A c = %version% + 1 
echo #define SYMPHONY_VERSION %c% > %1\version.h
copy %1\Symphony.* %2\*
copy %1\FileManager.* %2\*
copy %1\html.h %2\*
copy %1\version.h %2\*
copy %1\DeviceDiscovery.h %2\*
copy %1\Product.* %2\*
copy %1\MqttUtil.* %2\*

for /f "tokens=3" %%v in (D:\git_symphony\esp8266\Symphony\version.h) do set version=%%v
@echo version=%version%
SET /A c = %version% + 1 
echo #define SYMPHONY_VERSION %c% > D:\git_symphony\esp8266\Symphony\version.h
copy D:\git_symphony\esp8266\Symphony\Symphony.* D:\projects\arduinolibraries\Symphony\*
copy D:\git_symphony\esp8266\Symphony\FileManager.* D:\projects\arduinolibraries\Symphony\*
copy D:\git_symphony\esp8266\Symphony\html.h D:\projects\arduinolibraries\Symphony\*
copy D:\git_symphony\esp8266\Symphony\version.h D:\projects\arduinolibraries\Symphony\*
copy D:\git_symphony\esp8266\Symphony\DeviceDiscovery.h D:\projects\arduinolibraries\Symphony\*
copy D:\git_symphony\esp8266\Symphony\Product.* D:\projects\arduinolibraries\Symphony\*

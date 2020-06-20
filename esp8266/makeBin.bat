@echo arg 1 is the complete path of the project %1
@echo arg 2 is the projectname %2
@echo off
for /f "tokens=3" %%v in (%1\version.h) do set version=%%v
@echo current version is %version%
SET /A c = %version% + 1 
echo #define MY_VERSION %c% > %1\version.h
python3 "C:/Users/miras.DESKTOP-JHPGKS4/AppData/Local/Arduino15/packages/esp8266/hardware/esp8266/2.7.1/tools/elf2bin.py" --eboot "C:/Users/miras.DESKTOP-JHPGKS4/AppData/Local/Arduino15/packages/esp8266/hardware/esp8266/2.7.1/bootloaders/eboot/eboot.elf" --app %1/Release/%2.elf --flash_mode dio --flash_freq 40 --flash_size 4M --path "C:\sloeber\arduinoPlugin\packages\esp8266\tools\xtensa-lx106-elf-gcc\1.20.0-26-gb404fb9-2/bin" --out %1/Release/%2.bin


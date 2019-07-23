@echo off
@echo %1
for /f "tokens=3" %%v in (%1\version.h) do set version=%%v
@echo current version is %version%
SET /A c = %version% + 1 
echo #define LIGHT_VERSION %c% > %1\version.h

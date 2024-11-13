@echo off
cd ../
adb -s %1 install app\build\outputs\apk\debug\app-debug.apk

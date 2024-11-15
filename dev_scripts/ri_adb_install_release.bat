@echo off
cd ../
adb -s %1 install app\build\outputs\apk\release\app-release.apk
adb shell monkey -p com.catpuppyapp.puppygit.play.pro -c android.intent.category.LAUNCHER 1

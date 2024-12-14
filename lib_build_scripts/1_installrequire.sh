#!/bin/bash

#echo "Updating package list..."
#sudo apt update
#
#echo "Installing: curl cmake make tar libssl-dev maven unzip"
#sudo apt install -y curl cmake make tar libssl-dev maven unzip



export build_root=~/puppylibsbuild
mkdir -p $build_root
cd $build_root

export ndk_filename="android-ndk-r26d-linux"
echo "Downloading: ndk"
curl -L -o "${build_root}/$ndk_filename.zip" "https://dl.google.com/android/repository/${ndk_filename}.zip"
export ANDROID_NDK_ROOT=$build_root/android-ndk
mkdir -p $ANDROID_NDK_ROOT
echo "Extracting: ndk"
# -q : only show msg when err
unzip -q "$ndk_filename.zip"
mv android-ndk-r26d/* $ANDROID_NDK_ROOT

echo "Downloading: Android SDK"
# ANDROID_HOME is android sdk root, is sdk root, not ndk root
export ANDROID_HOME=$build_root/android-sdk
export CMAKE_VERSION=3.31.1
mkdir -p $ANDROID_HOME/cmdline-tools
curl -L -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip cmdline-tools.zip -d $ANDROID_HOME/
echo "install cmake by Android sdkmanager"
yes | $ANDROID_HOME/cmdline-tools/bin/sdkmanager --install "cmake;$CMAKE_VERSION" --sdk_root=$ANDROID_HOME
$ANDROID_HOME/cmdline-tools/bin/sdkmanager --list --sdk_root=$ANDROID_HOME
export CMAKE_PATH=$ANDROID_HOME/cmake/$CMAKE_VERSION/bin/cmake
$CMAKE_PATH --version

echo "Installation complete."

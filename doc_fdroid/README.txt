1. install curl and gradle:
sudo apt install curl gradle


2. download the puppygit latest release:
curl -L -o "PuppyGit.tar.gz" "https://github.com/catpuppyapp/PuppyGit/archive/refs/tags/1.0.6.7v43.tar.gz" 


3. unzip it
tar -xzf "PuppyGit.tar.gz" -C .
# rename dir "PuppyGit-version" to "PuppyGit"
mkdir -p PuppyGit
mv "PuppyGit-1.0.6.7v43"/* PuppyGit
rm -rf "PuppyGit-1.0.6.7v43"


4. go the the "lib_build_scripts" folder
cd PuppyGit/lib_build_scripts/


5. run "1_installrequire.sh", it will install necessary tools and download openjdk and the android-ndk


6. run "2_downloadsrc.sh", it will download and uncompress libssh2 libgit2 git24j and openssl src


7. run "2.5_buildjarlibs_fdroid.sh" and specify the lib out dir to PuppyGit jar libs dir, the new built will override the jar under PuppyGit libs:
./2.5_buildjarlibs_fdroid.sh PuppyGit/app/libs


8. run "3_buildlibs.sh" and specify the out dir to jni libs dir, after run the script, the libs will overrider the files under jniLibs:
./3_buildlibs.sh PuppyGit/app/src/main/jniLibs/


9. almost there, now cd to the PuppyGit dir, and run gradle and specify your signature:
./gradlew assembleRelease -DANDROID_NDK_HOME=/path/to/your/ndk and specify your signature

all done, now the app should exist in the output dir: PuppyGit/app/build/outputs/apk/release/app-release.apk

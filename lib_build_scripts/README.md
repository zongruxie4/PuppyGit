follow below commands to build the libs and apk:
```
# build the libs
export GITHUB_WORKSPACE=path_to_your_PuppyGit_src_cloned_from_git
cd $GITHUB_WORKSPACE
pushd lib_build_scripts
bash 1_installrequire.sh
bash 2_downloadsrc.sh
bash 3_buildlibs.sh ${GITHUB_WORKSPACE}/app/src/main/jniLibs

# build the apk
popd
chmod +x gradlew
./gradlew assembleRelease
```

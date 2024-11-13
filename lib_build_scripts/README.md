step by step build the libs:
install vargant: 
https://www.vagrantup.com/

install virtualbox:
https://www.virtualbox.org/

create a dir e.g. "vargant_workdir", cd to it, then run:
vagrant init

then copy all files from here to the "vargant_workdir", then run:
vargant up

then, run:
vargant ssh

after into virtual machine, then run:
cd /vagrant

then run the scripts by order

when build finished, you can find all libs at the path 'vargant_workdir/builts' on you host machine

---
others info:
the "3_buildlibs.sh" can build specified arch e.g.:
# build x86 only
./3_buildlibs.sh x86

# build arm32 and arm64 (armv7 and armv8), if the param more than one arch, must make them into a string
./3_buildlibs.sh "arm32 arm64"

# build all
./3_buildlibs.sh

# build all with all available archs
./3_buildlibs.sh "x86 x8664 arm32 arm64"

the clean.sh can do clean manullay when scripts faild, and the `archs` worked for `clean.sh` too

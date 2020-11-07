include Makefile.common

RESOURCE_DIR = src/main/resources

.phony: all package native native-all deploy

all: jni-header package

MKDIR_P:=mkdir -p
CP:=cp
GRADLE:=./gradlew
SRC:=src/main
CPPLINK_OUT:=$(TARGET)/native/$(cpplink)-$(OS_NAME)-$(OS_ARCH)
CPPLINK_BUILD:=$(CPPLINK_OUT)/$(LIBNAME)
OBJS:=$(patsubst $(SRC)/native/library/%.cpp,%.o,$(wildcard $(SRC)/native/library/*.cpp))
HEADERS:=lib/headers

CCFLAGS:= -I$(HEADERS) -I$(CPPLINK_OUT) $(CCFLAGS)


NATIVE_DIR=$(RESOURCE_DIR)/native/$(OS_NAME)/$(OS_ARCH)
NATIVE_TARGET_DIR:=$(TARGET)/native/$(OS_NAME)/$(OS_ARCH)
NATIVE_DLL:=$(NATIVE_DIR)/$(LIBNAME)

javaCompile:
	$(GRADLE) compileJava

jni-header: $(SRC)/native/CPPLink.h

$(SRC)/native/CPPLink.h: javaCompile
	cp $(TARGET)/generated/sources/headers/java/main/fr_bastoup_spigotplusplus_link_CPPLink.h $(SRC)/native/CPPLink.h

test:
	$(GRADLE) test

clean: clean-native clean-java

%.o: $(SRC)/native/library/%.cpp
	$(CC) $(CCFLAGS) -c -o $(SQLITE_OUT)/object/$@ $<

$(CPPLINK_OUT)/$(LIBNAME): $(SRC)/native/CPPLink.cpp $(OBJS)
	$(MKDIR_P) $(CPPLINK_OUT)/object
	$(CC) $(CCFLAGS) -c -o $(CPPLINK_OUT)/object/CPPLink.o $(SRC)/native/CPPLink.cpp
	$(CC) $(CCFLAGS) -o $(CPPLINK_BUILD) $(CPPLINK_OUT)/object/CPPLink.o $(OBJS) $(LINKFLAGS)
	$(MKDIR_P) $(NATIVE_DIR)
	$(CP) $(CPPLINK_BUILD) $(NATIVE_DLL)
    # Workaround for strip Protocol error when using VirtualBox on Mac
	#cp $@ /tmp/$(@F)
	#$(STRIP) /tmp/$(@F)
	#cp /tmp/$(@F) $@
# For cross-compilation, install docker. See also https://github.com/dockcross/dockcross
# Disabled linux-armv6 build because of this issue; https://github.com/dockcross/dockcross/issues/190
native-all: native win32 win64 linux32 linux64 linux-arm linux-armv7 linux-arm64

native: $(NATIVE_DLL)

$(NATIVE_DLL): $(CPPLINK_OUT)/$(LIBNAME)

DOCKER_RUN_OPTS=--rm

win32: jni-header
	./docker/dockcross-windows-x86 -a $(DOCKER_RUN_OPTS) bash -c 'make native CROSS_PREFIX=i686-w64-mingw32.static- OS_NAME=Windows OS_ARCH=x86'

win64: jni-header
	./docker/dockcross-windows-x64 -a $(DOCKER_RUN_OPTS) bash -c 'make native CROSS_PREFIX=x86_64-w64-mingw32.static- OS_NAME=Windows OS_ARCH=x86_64'

linux32: jni-header
	make native OS_NAME=Linux OS_ARCH=x86

linux64: jni-header
	make native OS_NAME=Linux OS_ARCH=x86_64

linux-arm: jni-header
	./docker/dockcross-armv5 -a $(DOCKER_RUN_OPTS) bash -c 'make native CROSS_PREFIX=/usr/xcc/armv5-unknown-linux-gnueabi/bin/armv5-unknown-linux-gnueabi- OS_NAME=Linux OS_ARCH=arm'

linux-armv6: jni-header
	./docker/dockcross-armv6 -a $(DOCKER_RUN_OPTS) bash -c 'make native CROSS_PREFIX=arm-linux-gnueabihf- OS_NAME=Linux OS_ARCH=armv6'

linux-armv7: jni-header
	./docker/dockcross-armv7a -a $(DOCKER_RUN_OPTS) bash -c 'make native CROSS_PREFIX=/usr/xcc/arm-cortexa8_neon-linux-gnueabihf/bin/arm-cortexa8_neon-linux-gnueabihf- OS_NAME=Linux OS_ARCH=armv7'

linux-arm64: jni-header
	./docker/dockcross-arm64 -a $(DOCKER_RUN_OPTS) bash -c 'make native CROSS_PREFIX=/usr/xcc/aarch64-unknown-linux-gnueabi/bin/aarch64-unknown-linux-gnueabi- OS_NAME=Linux OS_ARCH=aarch64'

package: native-all
	$(GRADLE) assemble

clean-native:
	rm -f $(SRC)/native/CPPLink.h
	rm -rf $(RESOURCE_DIR)/native
	rm -rf $(TARGET)/native/

clean-java:
	$(GRADLE) clean

docker-linux64:
	docker build --network=host -f docker/Dockerfile.linux_x86_64 -t xerial/centos5-linux-x86_64 .

docker-linux32:
	docker build --network=host -f docker/Dockerfile.linux_x86 -t xerial/centos5-linux-x86 .


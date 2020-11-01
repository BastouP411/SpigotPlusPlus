include Makefile.common

RESOURCE_DIR = src/main/resources

.phony: all package native native-all deploy

all: jni-header package

deploy:
	gradle package deploy -DperformRelease=true

GRADLE:= gradle
SRC:=src/main
CPPLINK_OUT:=$(TARGET)/native/$(cpplink)-$(OS_NAME)-$(OS_ARCH)
OBJS:=$(patsubst $(SRC)/native/library/%.cpp,%.o,$(wildcard $(SRC)/native/library/*.cpp))
HEADERS:=lib/headers

$(info $(OBJS))
CCFLAGS:= -I$(HEADERS) -I$(CPPLINK_OUT) $(CCFLAGS)

javaCompile:
	$(GRADLE) compileJava

jni-header: $(SRC)/native/CPPLink.h

$(SRC)/native/CPPLink.h: javaCompile
	mv $(TARGET)/generated/sources/headers/java/main/fr_bastoup_spigotplusplus_link_CPPLink.h $(SRC)/native/CPPLink.h

test:
	$(GRADLE) test

clean: clean-native clean-java

%.o: $(SRC)/native/library/%.cpp
	$(CC) $(CCFLAGS) -c -o $(SQLITE_OUT)/$@ $<

$(CPPLINK_OUT)/$(LIBNAME): $(SRC)/native/CPPLink.cpp $(SRC)/native/CPPLink.h $(OBJS)
	$(CC) $(CCFLAGS) -c -o $(SQLITE_OUT)/CPPLink.o $(SRC)/native/CPPLink.cpp
	$(CC) $(CCFLAGS) -o $(SQLITE_OUT)/CPPLink.o $(OBJS) $(LINKFLAGS)
    # Workaround for strip Protocol error when using VirtualBox on Mac
	#cp $@ /tmp/$(@F)
	#$(STRIP) /tmp/$(@F)
	#cp /tmp/$(@F) $@

NATIVE_DIR=src/main/resources/native/$(OS_NAME)/$(OS_ARCH)
NATIVE_TARGET_DIR:=$(TARGET)/native/$(OS_NAME)/$(OS_ARCH)
NATIVE_DLL:=$(NATIVE_DIR)/$(LIBNAME)

# For cross-compilation, install docker. See also https://github.com/dockcross/dockcross
# Disabled linux-armv6 build because of this issue; https://github.com/dockcross/dockcross/issues/190
native-all: native win32 win64 mac64 linux32 linux64 linux-arm linux-armv7 linux-arm64 linux-android-arm linux-ppc64 alpine-linux64

native: $(NATIVE_DLL)

$(NATIVE_DLL): $(CPPLINK_OUT)/$(LIBNAME)
	@mkdir -p $(@D)
	cp $< $@
	@mkdir -p $(NATIVE_TARGET_DIR)
	cp $< $(NATIVE_TARGET_DIR)/$(LIBNAME)

DOCKER_RUN_OPTS=--rm

win32: jni-header
	./docker/dockcross-windows-x86 -a $(DOCKER_RUN_OPTS) bash -c 'make clean-native native CROSS_PREFIX=i686-w64-mingw32.static- OS_NAME=Windows OS_ARCH=x86'

win64: jni-header
	./docker/dockcross-windows-x64 -a $(DOCKER_RUN_OPTS) bash -c 'make clean-native native CROSS_PREFIX=x86_64-w64-mingw32.static- OS_NAME=Windows OS_ARCH=x86_64'

linux32: jni-header
	docker run $(DOCKER_RUN_OPTS) -ti -v $$PWD:/work xerial/centos5-linux-x86 bash -c 'make clean-native native OS_NAME=Linux OS_ARCH=x86'

linux64: jni-header
	docker run $(DOCKER_RUN_OPTS) -ti -v $$PWD:/work xerial/centos5-linux-x86_64 bash -c 'make clean-native native OS_NAME=Linux OS_ARCH=x86_64'

alpine-linux64: jni-header
	docker run $(DOCKER_RUN_OPTS) -ti -v $$PWD:/work xerial/alpine-linux-x86_64 bash -c 'make clean-native native OS_NAME=Linux-Alpine OS_ARCH=x86_64'

linux-arm: jni-header
	./docker/dockcross-armv5 -a $(DOCKER_RUN_OPTS) bash -c 'make clean-native native CROSS_PREFIX=/usr/xcc/armv5-unknown-linux-gnueabi/bin/armv5-unknown-linux-gnueabi- OS_NAME=Linux OS_ARCH=arm'

linux-armv6: $(SQLITE_UNPACKED) jni-header
	./docker/dockcross-armv6 -a $(DOCKER_RUN_OPTS) bash -c 'make clean-native native CROSS_PREFIX=arm-linux-gnueabihf- OS_NAME=Linux OS_ARCH=armv6'

linux-armv7: $(SQLITE_UNPACKED) jni-header
	./docker/dockcross-armv7a -a $(DOCKER_RUN_OPTS) bash -c 'make clean-native native CROSS_PREFIX=/usr/xcc/arm-cortexa8_neon-linux-gnueabihf/bin/arm-cortexa8_neon-linux-gnueabihf- OS_NAME=Linux OS_ARCH=armv7'

linux-arm64: $(SQLITE_UNPACKED) jni-header
	./docker/dockcross-arm64 -a $(DOCKER_RUN_OPTS) bash -c 'make clean-native native CROSS_PREFIX=/usr/xcc/aarch64-unknown-linux-gnueabi/bin/aarch64-unknown-linux-gnueabi- OS_NAME=Linux OS_ARCH=aarch64'

linux-android-arm: $(SQLITE_UNPACKED) jni-header
	./docker/dockcross-android-arm -a $(DOCKER_RUN_OPTS) bash -c 'make clean-native native CROSS_PREFIX=/usr/arm-linux-androideabi/bin/arm-linux-androideabi- OS_NAME=Linux OS_ARCH=android-arm'

linux-ppc64: $(SQLITE_UNPACKED) jni-header
	./docker/dockcross-ppc64 -a $(DOCKER_RUN_OPTS) bash -c 'make clean-native native CROSS_PREFIX=powerpc64le-linux-gnu- OS_NAME=Linux OS_ARCH=ppc64'

mac64: $(SQLITE_UNPACKED) jni-header
	docker run -it $(DOCKER_RUN_OPTS) -v $$PWD:/workdir -e CROSS_TRIPLE=x86_64-apple-darwin multiarch/crossbuild make clean-native native OS_NAME=Mac OS_ARCH=x86_64

# deprecated
mac32: $(SQLITE_UNPACKED) jni-header
	docker run -it $(DOCKER_RUN_OPTS) -v $$PWD:/workdir -e CROSS_TRIPLE=i386-apple-darwin multiarch/crossbuild make clean-native native OS_NAME=Mac OS_ARCH=x86

sparcv9:
	$(MAKE) native OS_NAME=SunOS OS_ARCH=sparcv9

package: native-all
	rm -rf target/dependency-maven-plugin-markers
	$(MVN) package

clean-native:
	rm $(SRC)/native/CPPLink.h
	rm -rf $(TARGET)/native/

clean-java:
	$(GRADLE) clean

docker-linux64:
	docker build -f docker/Dockerfile.linux_x86_64 -t xerial/centos5-linux-x86_64 .

docker-linux32:
	docker build -f docker/Dockerfile.linux_x86 -t xerial/centos5-linux-x86 .

docker-alpine-linux64:
	docker build -f docker/Dockerfile.alpine-linux_x86_64 -t xerial/alpine-linux-x86_64 .
#include <jni.h>
#include <iostream>
#include "CPPLink.h"


JNIEXPORT void JNICALL Java_fr_bastoup_spigotplusplus_link_CPPLink_loadPlugin(JNIEnv *env, jobject thisObj) {
    std::cout << "Test" << std::endl;
    return;
}


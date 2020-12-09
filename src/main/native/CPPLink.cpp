#include <jni.h>
#include <iostream>

JNIEXPORT void JNICALL Java_fr_bastoup_spigotplusplus_link_CPPLink_loadPlugin(JNIEnv *env, jclass *obj, jstring *str) {
    std::cout << "Bonjour" << std::endl;
    return;
}
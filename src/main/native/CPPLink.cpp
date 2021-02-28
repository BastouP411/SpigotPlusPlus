#include "CPPLink.h"

#include "jni.h"
#include <iostream>
#include "Util.h"


JNIEXPORT void JNICALL Java_fr_bastoup_spigotplusplus_link_CPPLink_loadPlugin(JNIEnv *env, jclass obj, jstring str) {
    std::cout << jstringToString(*env, str) << std::endl;
    return;
}
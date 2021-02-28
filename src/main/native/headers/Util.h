#ifndef CPPLINK_UTIL
#define CPPLINK_UTIL

#include "jni.h"
#include <string>

std::string jstringToString(JNIEnv env, jstring jStr);

#endif
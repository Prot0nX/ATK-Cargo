#include <jni.h>
#include <string>

extern "C" {
JNIEXPORT jstring JNICALL
Java_com_atk_atk_1cargo_api_Secrets_getBaseUrl(JNIEnv* env, jobject /* this */) {
    const char* baseUrl = "https://atk-nk.site/";
    return env->NewStringUTF(baseUrl);
}
}
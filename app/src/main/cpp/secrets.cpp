#include <jni.h>
#include <string>
#include <vector>

// XOR encryption key
const uint8_t XOR_KEY = 0x5A;

std::string decryptXor(const uint8_t* encryptedBytes, size_t length) {
    std::string decrypted;
    for (size_t i = 0; i < length; ++i) {
        decrypted += (char)(encryptedBytes[i] ^ XOR_KEY);
    }
    return decrypted;
}

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_atk_atk_1cargo_api_Secrets_getBaseUrl(JNIEnv* env, jobject /* this */) {
    // Original: "https://atk-nk.ir/Cargo/"
    const uint8_t baseUrl_bytes[] = { 0x32, 0x2E, 0x2E, 0x2A, 0x29, 0x60, 0x75, 0x75, 0x3B, 0x2E, 0x31, 0x77, 0x34, 0x31, 0x74, 0x33, 0x28, 0x75, 0x19, 0x3B, 0x28, 0x3D, 0x35, 0x75 };
    const size_t baseUrl_len = 24;
    return env->NewStringUTF(decryptXor(baseUrl_bytes, baseUrl_len).c_str());
}

JNIEXPORT jstring JNICALL
Java_com_atk_atk_1cargo_api_Secrets_getWebUserName(JNIEnv* env, jobject /* this */) {
    // Original: "AminTojarKhozestan"
    const uint8_t webUserName_bytes[] = { 0x1B, 0x37, 0x33, 0x34, 0x0E, 0x35, 0x30, 0x3B, 0x28, 0x11, 0x32, 0x35, 0x20, 0x3F, 0x29, 0x2E, 0x3B, 0x34 };
    const size_t webUserName_len = 18;
    return env->NewStringUTF(decryptXor(webUserName_bytes, webUserName_len).c_str());
}

JNIEXPORT jstring JNICALL
Java_com_atk_atk_1cargo_api_Secrets_getWebUserPass(JNIEnv* env, jobject /* this */) {
    // Original: "Njg4MDZlYzU0M2Ji"
    const uint8_t webUserPass_bytes[] = { 0x14, 0x30, 0x3D, 0x6E, 0x17, 0x1E, 0x00, 0x36, 0x03, 0x20, 0x0F, 0x6A, 0x17, 0x68, 0x10, 0x33 };
    const size_t webUserPass_len = 16;
    return env->NewStringUTF(decryptXor(webUserPass_bytes, webUserPass_len).c_str());
}

JNIEXPORT jstring JNICALL
Java_com_atk_atk_1cargo_api_Secrets_getAuthUser(JNIEnv* env, jobject /* this */) {
    // Original: "Amin_Tojar_Khozestan"
    const uint8_t AuthUser_bytes[] = { 0x1B, 0x37, 0x33, 0x34, 0x05, 0x0E, 0x35, 0x30, 0x3B, 0x28, 0x05, 0x11, 0x32, 0x35, 0x20, 0x3F, 0x29, 0x2E, 0x3B, 0x34 };
    const size_t AuthUser_len = 20;
    return env->NewStringUTF(decryptXor(AuthUser_bytes, AuthUser_len).c_str());
}

JNIEXPORT jstring JNICALL
Java_com_atk_atk_1cargo_api_Secrets_getAuthenticationX365(JNIEnv* env, jobject /* this */) {
    // Original: "c521ed219e0d4f5f9da78b6f7c7366f3a2dcb1a8451547c382c23548ebcdac53"
    const uint8_t AuthenticationX365_bytes[] = { 0x39, 0x6F, 0x68, 0x6B, 0x3F, 0x3E, 0x68, 0x6B, 0x63, 0x3F, 0x6A, 0x3E, 0x6E, 0x3C, 0x6F, 0x3C, 0x63, 0x3E, 0x3B, 0x6D, 0x62, 0x38, 0x6C, 0x3C, 0x6D, 0x39, 0x6D, 0x69, 0x6C, 0x6C, 0x3C, 0x69, 0x3B, 0x68, 0x3E, 0x39, 0x38, 0x6B, 0x3B, 0x62, 0x6E, 0x6F, 0x6B, 0x6F, 0x6E, 0x6D, 0x39, 0x69, 0x62, 0x68, 0x39, 0x68, 0x69, 0x6F, 0x6E, 0x62, 0x3F, 0x38, 0x39, 0x3E, 0x3B, 0x39, 0x6F, 0x69 };
    const size_t AuthenticationX365_len = 64;
    return env->NewStringUTF(decryptXor(AuthenticationX365_bytes, AuthenticationX365_len).c_str());
}

}
#include <jni.h>
#include <string>
#include <cmath>
#include <android/log.h>
#include <android/asset_manager_jni.h>
#include <android/bitmap.h>
#include "arcface.h"

#define TAG "ArcFaceJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static ArcFace* arcface = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_bankingapp_RegisterActivity_init(JNIEnv* env, jobject thiz,
                                                  jobject assetManager,
                                                  jstring paramFile,
                                                  jstring binFile) {
    if (arcface) return;

    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
    const char* param_path = env->GetStringUTFChars(paramFile, nullptr);
    const char* bin_path = env->GetStringUTFChars(binFile, nullptr);

    arcface = new ArcFace();
    arcface->loadModel(mgr, param_path, bin_path);

    env->ReleaseStringUTFChars(paramFile, param_path);
    env->ReleaseStringUTFChars(binFile, bin_path);
    LOGI("ArcFace model loaded.");
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_example_bankingapp_RegisterActivity_extractFeature(JNIEnv *env, jobject, jobject bitmap) {
    if (!arcface) {
        LOGE("ArcFace not initialized.");
        return nullptr;
    }

    AndroidBitmapInfo info;
    void* pixels = nullptr;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        LOGE("Failed to get bitmap info.");
        return nullptr;
    }
    LOGI("Bitmap info: width=%d, height=%d, format=%d", info.width, info.height, info.format);

    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        LOGE("Failed to lock pixels.");
        return nullptr;
    }

    int width = info.width;
    int height = info.height;
    unsigned char* bgrData = new unsigned char[width * height * 3];
    uint32_t* rgba = (uint32_t*)pixels;

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            uint32_t pixel = rgba[y * width + x];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            int idx = (y * width + x) * 3;
            bgrData[idx + 0] = b;
            bgrData[idx + 1] = g;
            bgrData[idx + 2] = r;
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);

    std::vector<float> embedding = arcface->getFeature(bgrData, width, height);
    delete[] bgrData;

    LOGI("Embedding size: %zu", embedding.size());
    if (embedding.empty()) {
        LOGE("Embedding failed: size = 0");
        return nullptr;
    }

    jfloatArray result = env->NewFloatArray(embedding.size());
    env->SetFloatArrayRegion(result, 0, embedding.size(), embedding.data());
    return result;
}

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

// ✅ init from RegisterActivity （bankingapp）
// ✅ RegisterActivity 用的 init（来自 bankingapp 模块）
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
    LOGI("ArcFace model loaded from RegisterActivity.");
}

// ✅ init from FaceRecognizer（faceauth）
// ✅ FaceRecognizer 用的 init（来自 faceauth 模块）
extern "C"
JNIEXPORT void JNICALL
Java_com_example_faceauth_FaceRecognizer_init(JNIEnv* env, jobject thiz,
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
    LOGI("ArcFace model loaded from FaceRecognizer.");
}

// ✅ bankingapp is called when registering: Bitmap ➝ 512-dimensional features
// ✅ bankingapp 注册时调用：Bitmap ➝ 512维特征
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

    if (embedding.empty()) {
        LOGE("Embedding failed: size = 0");
        return nullptr;
    }

    jfloatArray result = env->NewFloatArray(embedding.size());
    env->SetFloatArrayRegion(result, 0, embedding.size(), embedding.data());
    return result;
}

// ✅ Backend service comparison call: 512-dimensional feature similarity
// ✅ 后台服务比对调用：512维特征相似度
extern "C"
JNIEXPORT jfloat JNICALL
Java_com_example_faceauth_FaceRecognizer_cosineSimilarity(JNIEnv *env, jobject,
                                                          jfloatArray emb1_, jfloatArray emb2_) {
    jfloat* emb1 = env->GetFloatArrayElements(emb1_, NULL);
    jfloat* emb2 = env->GetFloatArrayElements(emb2_, NULL);

    float dot = 0.0f, norm1 = 0.0f, norm2 = 0.0f;
    for (int i = 0; i < 512; ++i) {
        dot += emb1[i] * emb2[i];
        norm1 += emb1[i] * emb1[i];
        norm2 += emb2[i] * emb2[i];
    }

    env->ReleaseFloatArrayElements(emb1_, emb1, 0);
    env->ReleaseFloatArrayElements(emb2_, emb2, 0);

    return dot / (std::sqrt(norm1) * std::sqrt(norm2) + 1e-5f);
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_example_faceauth_FaceRecognizer_getEmbedding(JNIEnv* env, jobject, jbyteArray bgr_, jint width, jint height) {
    if (!arcface) {
        LOGE("ArcFace not initialized.");
        return nullptr;
    }

    jbyte* bgrData = env->GetByteArrayElements(bgr_, NULL);
    int size = width * height * 3;

    std::vector<float> feature = arcface->getFeature(reinterpret_cast<unsigned char*>(bgrData), width, height);
    env->ReleaseByteArrayElements(bgr_, bgrData, 0);

    if (feature.empty()) {
        LOGE("Embedding extraction failed");
        return nullptr;
    }

    jfloatArray result = env->NewFloatArray(feature.size());
    env->SetFloatArrayRegion(result, 0, feature.size(), feature.data());
    return result;
}
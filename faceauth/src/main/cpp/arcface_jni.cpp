#include <jni.h>
#include <string>
#include <cmath>
#include <android/log.h>
#include <android/asset_manager_jni.h>
#include "arcface.h"

static ArcFace* arcface = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_faceauth_FaceRecognizer_init(JNIEnv* env, jobject,
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
}

extern "C"
JNIEXPORT jfloatArray JNICALL
        Java_com_example_faceauth_FaceRecognizer_getEmbedding(JNIEnv* env, jobject,
                                                              jbyteArray imageData, jint width, jint height) {
if (!arcface) return nullptr;

jbyte* data = env->GetByteArrayElements(imageData, nullptr);
std::vector<float> embedding = arcface->getFeature(reinterpret_cast<unsigned char*>(data), width, height);
env->ReleaseByteArrayElements(imageData, data, 0);

jfloatArray result = env->NewFloatArray(embedding.size());
env->SetFloatArrayRegion(result, 0, embedding.size(), embedding.data());
return result;
}

extern "C"
JNIEXPORT jfloat JNICALL
        Java_com_example_faceauth_FaceRecognizer_cosineSimilarity(JNIEnv* env, jobject,
                                                                  jfloatArray emb1, jfloatArray emb2) {
int len = env->GetArrayLength(emb1);
std::vector<float> a(len), b(len);
env->GetFloatArrayRegion(emb1, 0, len, a.data());
env->GetFloatArrayRegion(emb2, 0, len, b.data());

float dot = 0.0f, normA = 0.0f, normB = 0.0f;
for (int i = 0; i < len; ++i) {
dot += a[i] * b[i];
normA += a[i] * a[i];
normB += b[i] * b[i];
}
return dot / (std::sqrt(normA) * std::sqrt(normB) + 1e-5f);
}

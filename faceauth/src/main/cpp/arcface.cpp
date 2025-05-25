#include "arcface.h"
#include <net.h>
#include <cmath>

ncnn::Net net;

void ArcFace::loadModel(AAssetManager* mgr, const std::string& param, const std::string& bin) {
    net.load_param(mgr, param.c_str());
    net.load_model(mgr, bin.c_str());
}

std::vector<float> ArcFace::getFeature(unsigned char* bgr, int w, int h) {
    ncnn::Mat img = ncnn::Mat::from_pixels(bgr, ncnn::Mat::PIXEL_BGR, w, h);

    // resize to 112x112
    ncnn::Mat resized;
    ncnn::resize_bilinear(img, resized, 112, 112);

    const float mean_vals[3] = {127.5f, 127.5f, 127.5f};
    const float norm_vals[3] = {1 / 128.f, 1 / 128.f, 1 / 128.f};
    resized.substract_mean_normalize(mean_vals, norm_vals);

    ncnn::Extractor ex = net.create_extractor();
    ex.input("data", resized);

    ncnn::Mat out;
    ex.extract("fc1", out);

    std::vector<float> feat(512);
    for (int i = 0; i < 512; ++i) {
        feat[i] = out[i];
    }
    return feat;
}

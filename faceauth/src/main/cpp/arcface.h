#pragma once

#include <string>
#include <vector>
#include <android/asset_manager.h>

class ArcFace {
public:
    void loadModel(AAssetManager* mgr, const std::string& param, const std::string& bin);
    std::vector<float> getFeature(unsigned char* bgr, int w, int h);
};

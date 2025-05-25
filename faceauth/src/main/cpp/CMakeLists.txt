cmake_minimum_required(VERSION 3.10.2)
project("arcface")

# 引入 ncnn 预编译库
add_library(ncnn SHARED IMPORTED)
set_target_properties(ncnn PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libncnn.so
)

# 创建 native 库
add_library(arcface SHARED
        arcface.cpp
        arcface_jni.cpp
)

# 设置头文件路径
target_include_directories(arcface PRIVATE
        ${CMAKE_SOURCE_DIR}/ncnn/include/ncnn
)

# 查找日志库和 android 库
find_library(log-lib log)
find_library(android-lib android)   # ✅ 添加这行！

# 链接库
target_link_libraries(
        arcface
        ncnn
        ${log-lib}
        ${android-lib}   # ✅ 添加这行！
)

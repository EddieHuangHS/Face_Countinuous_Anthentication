cmake_minimum_required(VERSION 3.10.2)
project("arcface")

# Introduce ncnn library
# 引入 ncnn 预编译库
add_library(ncnn SHARED IMPORTED)
set_target_properties(ncnn PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libncnn.so
)

# Create native library
# 创建 native 库
add_library(arcface SHARED
        arcface.cpp
        arcface_jni.cpp
)

# Set the header file path
# 设置头文件路径
target_include_directories(arcface PRIVATE
        ${CMAKE_SOURCE_DIR}/ncnn/include/ncnn
)

# Find required system libraries
# 查找所需系统库
find_library(log-lib log)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)  # ✅ 修复 Bitmap undefined symbol 问题

# Linking logging library, Android library and jnigraphics library
# 链接日志库、Android 库和 jnigraphics 库
target_link_libraries(
        arcface
        ncnn
        ${log-lib}
        ${android-lib}
        ${jnigraphics-lib}  # ✅ 添加此项用于 Bitmap 支持
)

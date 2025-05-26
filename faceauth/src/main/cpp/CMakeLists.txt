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

# 查找所需系统库
find_library(log-lib log)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)  # ✅ 修复 Bitmap undefined symbol 问题

# 链接日志库、Android 库和 jnigraphics 库
target_link_libraries(
        arcface
        ncnn
        ${log-lib}
        ${android-lib}
        ${jnigraphics-lib}  # ✅ 添加此项用于 Bitmap 支持
)

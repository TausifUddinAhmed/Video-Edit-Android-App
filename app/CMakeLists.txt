cmake_minimum_required(VERSION 3.22.1)
set(ffmpeg_dir ${CMAKE_SOURCE_DIR}/Users/tausifchowdhury/Documents/S/ffmpeg-android-maker-master/output)
set(ffmpeg_libs ${ffmpeg_dir}/lib/${ANDROID_ABI})

include_directories(${ffmpeg_dir}/include)

set(
        # List variable name
        ffmpeg_libs_names
        # Values in the list
        avutil avformat avcodec swscale)

foreach (ffmpeg_lib_name ${ffmpeg_libs_names})
    add_library(
            ${ffmpeg_lib_name}
            SHARED
            IMPORTED)
    set_target_properties(
            ${ffmpeg_lib_name}
            PROPERTIES
            IMPORTED_LOCATION
            ${ffmpeg_libs}/lib${ffmpeg_lib_name}.so)
endforeach ()

add_library(
        # Name for a library to build
        video-config
        # Type of a library
        SHARED
        # All cpp files to compile
        src/main/cpp/main.cpp
        src/main/cpp/video_config.cpp
        src/main/cpp/video_config_jni.cpp
        src/main/cpp/frame_extractor.cpp
        src/main/cpp/utils.cpp)

target_link_libraries(
        # Library to link
        video-config
        # List of libraries to link against:
        # Library for writing messages in LogCat
        log
        # Library for processing Bitmap objects
        jnigraphics
        # FFmpeg libraries
        ${ffmpeg_libs_names})
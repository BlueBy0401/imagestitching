# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#set(jniLibs "${CMAKE_SOURCE_DIR}/src/main/jniLibs")
include_directories(src/main/jniLibs/include)
#include_directories(${distribution_DIR}/include)

add_library(
            libopencv_java
            SHARED
            IMPORTED)
set_target_properties(
            libopencv_java
            PROPERTIES IMPORTED_LOCATION
            ../../../../src/main/jniLibs/armeabi-v7a/libopencv_java.so)

#link_directories("src/main/jniLibs/include")

add_library( # Sets the name of the library.
             stitcher

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/stitcher.cpp)
#set_target_properties(stitcher PROPERTIES
 #     LIBRARY_OUTPUT_DIRECTORY
  #    ../../../../src/main/jniLibs/armeabi-v7a)
# Searches for a specified prebuilt library and stores the path as a ${ANDROID_ABI}
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       stitcher

                       # Links the target library to the log library
                       # included in the NDK.
                       libopencv_java
                       ${log-lib}
                       )
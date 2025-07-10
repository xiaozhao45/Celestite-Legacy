cmake_minimum_required(VERSION 3.5)

project(qimen LANGUAGES C)  # 项目名改为qimen，对应libqimen.so

set(CMAKE_C_STANDARD 99)

MESSAGE(STATUS "system name: ${CMAKE_SYSTEM_NAME}")
MESSAGE(STATUS "compiler id: ${CMAKE_C_COMPILER_ID}")

if ("${CMAKE_C_COMPILER_ID}" STREQUAL "MSVC")
    MESSAGE(STATUS "add windows compiler flag")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} /source-charset:utf-8 /EHsc")
    add_definitions(-D_CRT_SECURE_NO_WARNINGS)
    MESSAGE(STATUS "Compiler C_FLAGS:${CMAKE_C_FLAGS}")
endif()

include_directories(include)
aux_source_directory(src SRC_DIR)

# 关键修改：只编译JNI.c和src目录下的源文件，生成共享库
add_library(qimen SHARED JNI.c ${SRC_DIR})

# 链接Android日志库（如果JNI.c中使用了__android_log_print）
find_library(log-lib log)
target_link_libraries(qimen ${log-lib})
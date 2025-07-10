//
// Created by a1823（xiaozhao45） on 2025/7/6.
//
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <calendar.h>
#include <qimenzpzr.h>
#include <comline.h>
#include <qimenyp.h>
#include <qimenZg.h>

#include "jni.h"

// 将 jobjectArray 转换为 char** 的辅助函数
char** convertJStringArrayToCharArray(JNIEnv *env, jobjectArray stringArray, int argc) {
    char** argv = (char**)malloc(argc * sizeof(char*));
    if (argv == NULL) return NULL;

    for (int i = 0; i < argc; i++) {
        jstring string = (jstring)(*env)->GetObjectArrayElement(env, stringArray, i);
        if (string == NULL) {
            // 清理已分配的内存
            for (int j = 0; j < i; j++) {
                free(argv[j]);
            }
            free(argv);
            return NULL;
        }

        const char* rawString = (*env)->GetStringUTFChars(env, string, 0);
        argv[i] = (char*)malloc(strlen(rawString) + 1);
        strcpy(argv[i], rawString);
        (*env)->ReleaseStringUTFChars(env, string, rawString);
        (*env)->DeleteLocalRef(env, string);
    }

    return argv;
}

// 释放 char** 数组的辅助函数
void freeCharArray(char** argv, int argc) {
    if (argv == NULL) return;
    for (int i = 0; i < argc; i++) {
        if (argv[i] != NULL) {
            free(argv[i]);
        }
    }
    free(argv);
}

jstring JNICALL
Java_com_xiaozhao45_celestite_QimenJNI_00024Companion_calc(JNIEnv *env, jobject thiz, jint argc,
                                                           jobjectArray argv) {
    int nJushu = 0;
    int bIsAutoTime = 0;
    extern int nStyle;
    calSolar solar;

    // ParseCommand 期望4个参数：程序名 + 3个实际参数
    if (argc != 3) {
        return (*env)->NewStringUTF(env, "参数个数错误，需要3个参数");
    }

    // 为 C 参数数组分配空间（4个参数：程序名 + 3个实际参数）
    char** c_argv = (char**)malloc(4 * sizeof(char*));
    if (c_argv == NULL) {
        return (*env)->NewStringUTF(env, "参数转换失败");
    }

    // 第一个参数设为程序名
    c_argv[0] = (char*)malloc(strlen("qimen") + 1);
    strcpy(c_argv[0], "qimen");

    // 转换 Java 字符串数组的3个参数
    for (int i = 0; i < 3; i++) {
        jstring string = (jstring)(*env)->GetObjectArrayElement(env, argv, i);
        if (string == NULL) {
            // 清理已分配的内存
            for (int j = 0; j <= i; j++) {
                free(c_argv[j]);
            }
            free(c_argv);
            return (*env)->NewStringUTF(env, "参数转换失败");
        }

        const char* rawString = (*env)->GetStringUTFChars(env, string, 0);
        c_argv[i + 1] = (char*)malloc(strlen(rawString) + 1);
        strcpy(c_argv[i + 1], rawString);
        (*env)->ReleaseStringUTFChars(env, string, rawString);
        (*env)->DeleteLocalRef(env, string);
    }

    // 调用 ParseCommand，传递4个参数
    int re = ParseCommand(4, c_argv, &solar, &nJushu, &bIsAutoTime, &nStyle);

    // 释放转换的字符串数组
    for (int i = 0; i < 4; i++) {
        if (c_argv[i] != NULL) {
            free(c_argv[i]);
        }
    }
    free(c_argv);

    if (re != 0) {
        return (*env)->NewStringUTF(env, "参数解析错误");
    }
    // 备份原始stdout
    int stdout_backup = dup(STDOUT_FILENO);
    if (stdout_backup == -1) {
        return (*env)->NewStringUTF(env, "无法备份stdout");
    }

    // 创建管道
    int pipefd[2];
    if (pipe(pipefd) == -1) {
        close(stdout_backup);
        return (*env)->NewStringUTF(env, "无法创建管道");
    }

    // 重定向stdout到管道的写端
    if (dup2(pipefd[1], STDOUT_FILENO) == -1) {
        close(stdout_backup);
        close(pipefd[0]);
        close(pipefd[1]);
        return (*env)->NewStringUTF(env, "无法重定向stdout");
    }
    close(pipefd[1]); // 关闭写端，因为已经重定向了

    // 执行奇门计算
    int qimenRet = -1;
    switch (nStyle) {
        case 0:
            qimenRet = qimenZpZrRun(&solar, bIsAutoTime, nJushu);
            break;
        case 1:
            qimenRet = qimenYpRun(&solar, bIsAutoTime, nJushu);
            break;
        case 2:
            qimenRet = qimenZgRun(&solar, bIsAutoTime, nJushu);
            break;
        default:
            break;
    }

    char *output_buffer = NULL;
    if (qimenRet == 0) {
        // 调用PrintResult()，输出会被重定向到管道
        PrintResult();
        fflush(stdout); // 确保所有输出都被刷新到管道
    }

    // 恢复原始stdout
    dup2(stdout_backup, STDOUT_FILENO);
    close(stdout_backup);

    // 读取管道中的数据
    char buffer[16384]; // 增大缓冲区以容纳更多输出
    ssize_t total_read = 0;
    ssize_t bytes_read;

    // 分配输出缓冲区
    output_buffer = (char*)malloc(16384);
    if (output_buffer == NULL) {
        close(pipefd[0]);
        comlineFree();
        qimenFree();
        calendarFree();
        return (*env)->NewStringUTF(env, "内存分配失败");
    }

    memset(output_buffer, 0, 16384);

    // 读取所有输出
    while ((bytes_read = read(pipefd[0], buffer, sizeof(buffer) - 1)) > 0) {
        buffer[bytes_read] = '\0';
        if (total_read + bytes_read < 16383) {
            strcat(output_buffer, buffer);
            total_read += bytes_read;
        } else {
            break; // 防止缓冲区溢出
        }
    }

    close(pipefd[0]);

    // 释放资源
    comlineFree();
    qimenFree();
    calendarFree();

    // 创建Java字符串并返回
    jstring result;
    if (qimenRet == 0 && total_read > 0) {
        result = (*env)->NewStringUTF(env, output_buffer);
    } else if (qimenRet != 0) {
        result = (*env)->NewStringUTF(env, "奇门计算失败");
    } else {
        result = (*env)->NewStringUTF(env, "没有输出内容");
    }

    free(output_buffer);
    return result;
}
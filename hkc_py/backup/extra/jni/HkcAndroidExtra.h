/* 头文件begin */
#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <android/log.h>
#include <unistd.h>
/* 头文件end */

/* 宏定义begin */
//清0宏
#define MEM_ZERO(pDest, destSize) memset(pDest, 0, destSize)

#ifndef _Included_com_hkc_service_SystemUpdateService
#define _Included_com_hkc_service_SystemUpdateService
#ifdef __cplusplus
extern "C" {
#endif


/**
 *  * Class:     com_hkc_UninstallMoniter
 * Method:    getUninstallWebUrl
 * Signature: ()V
 */
JNIEXPORT jstring JNICALL Java_com_hkc_UninstallMoniter_getUninstallWebUrl(JNIEnv *, jobject);


/*
 * Class:     com_hkc_UninstallMoniter
 * Method:    listenSelfUninstall
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_hkc_UninstallMoniter_listenSelfUninstall(JNIEnv *, jobject,jstring,jstring);




#ifdef __cplusplus
}
#endif
#endif

/* 头文件begin */
#include "HkcAndroidExtra.h"
/* 头文件end */

#ifdef __cplusplus
extern "C" {
#endif

const char* uninstallWebUrl = "http://compath.sinaapp.com/index.php/Home/Uninstall/uninstall/";

/**
 * 获取被卸载时弹出的url地址
 */
JNIEXPORT jstring JNICALL Java_com_hkc_UninstallMoniter_getUninstallWebUrl(JNIEnv *env, jobject obj){

	 return (env)->NewStringUTF(uninstallWebUrl);
}

/*
 * Class:     com_hkc_service_SystemUpdateService
 * Method:    init
 * Signature: ()V
 */JNIEXPORT void JNICALL Java_com_hkc_UninstallMoniter_listenSelfUninstall(
		JNIEnv *env, jobject obj, jstring app_path, jstring uninstall_data) {

    //app的 data路径
	const char* str_app_path = (env)->GetStringUTFChars(app_path, 0);
	char array_app_path[strlen(str_app_path)+1];
	int i;
	for(i=0;i<strlen(str_app_path);i++){
		array_app_path[i]= str_app_path[i];
	}
	array_app_path[i] = '\0';


	//卸载时要打开的url
	const char* webUrl= "http://compath.sinaapp.com/index.php/Home/Uninstall/uninstall/?package=";
	char uninstallUrl[strlen(webUrl)+strlen(str_app_path)+1];
	int j;
	for (j = 0; j < strlen(webUrl);j++) {
		uninstallUrl[j] = webUrl[j];
	}
	for (j = strlen(webUrl); j < (strlen(webUrl)+strlen(str_app_path));j++) {
		uninstallUrl[j] = str_app_path[j-strlen(webUrl)];
	}
	uninstallUrl[j] = '\0';



	//fork子进程，以执行轮询任务
	pid_t pid = fork();
	if (pid < 0) {
	} else if (pid == 0) {
		//子进程轮询"/data/data/pym.test.uninstalledmoniter"目录是否存在，若不存在则说明已被卸载
		while (1) {

			FILE *p_file = fopen(array_app_path, "r");
			if (p_file != NULL) {
				fclose(p_file);

				sleep(0.2f);
				//目录存在log
			} else {
				execlp("am", "am", "start", "-a", "android.intent.action.VIEW",
						"-d", uninstallUrl, (char *) NULL);
			}
		}
	} else {
		//父进程直接退出，使子进程被init进程领养，以避免子进程僵死
	}
}



#ifdef __cplusplus
}
#endif

#include "com_ski_frs_web_filter_FaceInterface.h"
#include "FaceIns.h"

// util function
jstring string2jstring(JNIEnv *, const char *);
std::string jstring2string(JNIEnv *, jstring);


JNIEXPORT jint JNICALL Java_com_ski_frs_web_filter_FaceInterface_init(JNIEnv * env, jclass clazz, jint device) {
	return InitializeFaceLib(device);
}


JNIEXPORT jint JNICALL Java_com_ski_frs_web_filter_FaceInterface_free(JNIEnv * env, jclass clazz) {
	return ReleaseFaceLib();
}


JNIEXPORT jstring JNICALL Java_com_ski_frs_web_filter_FaceInterface_fv(JNIEnv * env, jclass clazz, jstring path) {
	std::string str_path = jstring2string(env, path);
	cv::Mat pic = cv::imread(str_path, -1);
	VEC_FACERECTINFO vec;
	int mark = LocationFace(pic, vec);

	std::string fv;
	char fvc[] = {mark};
	fv = fvc;

	return string2jstring(env, fv.data());
}

jstring string2jstring(JNIEnv * env, const char * pat)
{
	//定义java String类 strClass 
	jclass classString = (env)->FindClass("Ljava/lang/String;");
	//获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String 
	jmethodID ctorID = (env)->GetMethodID(classString, "<init>", "([BLjava/lang/String;)V");
	//建立byte数组 
	jbyteArray bytes = (env)->NewByteArray(strlen(pat));
	//将char* 转换为byte数组 
	(env)->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte*)pat);
	// 设置String, 保存语言类型,用于byte数组转换至String时的参数 
	jstring encoding = (env)->NewStringUTF("GB2312");
	//将byte数组转换为java String,并输出 
	return (jstring)(env)->NewObject(classString, ctorID, bytes, encoding);
}


std::string jstring2string(JNIEnv* env, jstring jstr)
{
	char* rtn = NULL;
	jclass classString = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF("GB2312");
	jmethodID mid = env->GetMethodID(classString, "getBytes", "(Ljava/lang/String;)[B");
	jbyteArray barr = (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
	jsize alen = env->GetArrayLength(barr);
	jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
	if (alen > 0)
	{
		rtn = (char*)malloc(alen + 1);
		memcpy(rtn, ba, alen);
		rtn[alen] = 0;
	}
	env->ReleaseByteArrayElements(barr, ba, 0);
	std::string stemp(rtn);
	free(rtn);
	return stemp;
}
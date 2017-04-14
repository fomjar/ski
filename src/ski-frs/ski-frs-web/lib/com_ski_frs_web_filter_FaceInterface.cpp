#include "com_ski_frs_web_filter_FaceInterface.h"
#include "FaceIns.h"

JNIEXPORT jlong JNICALL Java_com_ski_frs_web_filter_FaceInterface_initInstance
(JNIEnv * env, jclass clazz, jint device) {
	CFaceRecogniton * p_faceRecog = new CFaceRecogniton();
	int ret = p_faceRecog->InitializeFaceLib(0);
	printf("---------%d-------%p\r\n", ret, p_faceRecog);
	if (ERR_TYPE_SUCC == ret) return (jlong)p_faceRecog;
	else return ret;
}


JNIEXPORT jint JNICALL Java_com_ski_frs_web_filter_FaceInterface_freeInstance
(JNIEnv * env, jclass clazz, jlong instance) {
	CFaceRecogniton * p_faceRecog = (CFaceRecogniton *) instance;
	int ret = p_faceRecog->ReleaseFaceLib();
	delete p_faceRecog;
	p_faceRecog = NULL;
	return (jint) ret;
}



JNIEXPORT jstring JNICALL Java_com_ski_frs_web_filter_FaceInterface_fv
(JNIEnv * env, jclass clazz, jlong instance, jstring path) {
	CFaceRecogniton * p_faceRecog = (CFaceRecogniton *)instance;
	std::string str_path = env->GetStringUTFChars(path, false);
	cv::Mat pic = cv::imread(str_path, -1);
	std::string str;
	int mark = p_faceRecog->LocationFace(pic, str);

	char fvc[5] = {0};
	_itoa_s(mark, fvc, 10);
	std::string fv(fvc);
	fv += " ";
	fv += str;

	return env->NewStringUTF(fv.c_str());
}

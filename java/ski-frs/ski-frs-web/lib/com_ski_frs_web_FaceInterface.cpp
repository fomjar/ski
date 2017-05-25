#include "com_ski_frs_web_FaceInterface.h"
#include "FaceIns.h"


// util functions
jbyte * newJbytesFromJbyteArray(JNIEnv * env, jbyteArray ba);
jbyteArray newJbyteArrayFromJbytes(JNIEnv * env, jbyte * bytes);



// interface
JNIEXPORT jlong JNICALL Java_com_ski_frs_web_FaceInterface_initInstance
(JNIEnv * env, jclass clazz, jint device) {
	CFaceRecogniton * p_faceRecog = new CFaceRecogniton();
	int ret = p_faceRecog->InitializeFaceLib(0);
	if (ERR_TYPE_SUCC == ret) return (jlong)p_faceRecog;
	else return ret;
}


JNIEXPORT jint JNICALL Java_com_ski_frs_web_FaceInterface_freeInstance
(JNIEnv * env, jclass clazz, jlong instance) {
	CFaceRecogniton * p_faceRecog = (CFaceRecogniton *) instance;
	int ret = p_faceRecog->ReleaseFaceLib();
	delete p_faceRecog;
	p_faceRecog = NULL;
	return (jint) ret;
}




JNIEXPORT jint JNICALL Java_com_ski_frs_web_FaceInterface_fv_1path
(JNIEnv * env, jclass clazz, jlong instance, jbyteArray path, jbyteArray fv) {
	CFaceRecogniton * p_faceRecog = (CFaceRecogniton *)instance;

	char * bytes = (char *)newJbytesFromJbyteArray(env, path);
	std::string str_path = bytes;
	cv::Mat img = cv::imread(str_path, -1);
	free(bytes);

	std::string str;
	int mark = p_faceRecog->LocationFace(img, str);

	jbyte * buf = (jbyte *)str.c_str();
	env->SetByteArrayRegion(fv, 0, str.length(), buf);

	return mark;
}




JNIEXPORT jint JNICALL Java_com_ski_frs_web_FaceInterface_fv_1base64
(JNIEnv * env, jclass clazz, jlong instance, jbyteArray data, jbyteArray fv) {
	CFaceRecogniton * p_faceRecog = (CFaceRecogniton *)instance;

	char * bytes = (char *)newJbytesFromJbyteArray(env, data);

	std::string str;
	int mark = p_faceRecog->LocationFace((unsigned char *) bytes, str);

	free(bytes);

	jbyte * buf = (jbyte *)str.c_str();
	env->SetByteArrayRegion(fv, 0, str.length(), buf);

	return mark;
}





jbyte * newJbytesFromJbyteArray(JNIEnv * env, jbyteArray ba) {
	jsize len = env->GetArrayLength(ba);
	jbyte * buf = (jbyte *) malloc(len * sizeof(jsize));
	memset(buf, 0, len * sizeof(jsize));
	env->GetByteArrayRegion(ba, 0, len, buf);
	return buf;
}

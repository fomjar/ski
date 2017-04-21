#include "com_ski_frs_web_filter_FaceInterface.h"
#include "FaceIns.h"


// util functions
jbyte * newJbytesFromJbyteArray(JNIEnv * env, jbyteArray ba);
jbyteArray newJbyteArrayFromJbytes(JNIEnv * env, jbyte * bytes);



// interface
JNIEXPORT jlong JNICALL Java_com_ski_frs_web_filter_FaceInterface_initInstance
(JNIEnv * env, jclass clazz, jint device) {
	CFaceRecogniton * p_faceRecog = new CFaceRecogniton();
	int ret = p_faceRecog->InitializeFaceLib(0);
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



JNIEXPORT jbyteArray JNICALL Java_com_ski_frs_web_filter_FaceInterface_fv
(JNIEnv * env, jclass clazz, jlong instance, jbyteArray path) {
	CFaceRecogniton * p_faceRecog = (CFaceRecogniton *)instance;

	char * bytes = (char *)newJbytesFromJbyteArray(env, path);
	std::string str_path = bytes;
	cv::Mat pic = cv::imread(str_path, -1);
	free(bytes);

	std::string str;
	int mark = p_faceRecog->LocationFace(pic, str);

	char fvc[5] = {0};
	_itoa_s(mark, fvc, 10);
	std::string fv(fvc);
	fv += " ";
	fv += str;

	return newJbyteArrayFromJbytes(env, (jbyte *) fv.c_str());
}






jbyte * newJbytesFromJbyteArray(JNIEnv * env, jbyteArray ba) {
	jsize len = env->GetArrayLength(ba);
	jbyte * buf = (jbyte *) malloc(len * sizeof(jsize));
	memset(buf, 0, len * sizeof(jsize));
	env->GetByteArrayRegion(ba, 0, len, buf);
	return buf;
}

jbyteArray newJbyteArrayFromJbytes(JNIEnv * env, jbyte * bytes) {
	int len = strlen((const char *) bytes);
	jbyteArray ba = env->NewByteArray(len);
	env->SetByteArrayRegion(ba, 0, len, bytes);
	return ba;
}
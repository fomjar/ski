#include "com_ski_frs_web_filter_FaceInterface.h"
#include "FaceIns.h"

// util function
void mat2str(cv::Mat & mat, std::string & str);

JNIEXPORT jint JNICALL Java_com_ski_frs_web_filter_FaceInterface_init(JNIEnv * env, jclass clazz, jint device) {
	return InitializeFaceLib(device);
}


JNIEXPORT jint JNICALL Java_com_ski_frs_web_filter_FaceInterface_free(JNIEnv * env, jclass clazz) {
	return ReleaseFaceLib();
}


JNIEXPORT jstring JNICALL Java_com_ski_frs_web_filter_FaceInterface_fv(JNIEnv * env, jclass clazz, jstring path) {
	std::string str_path = env->GetStringUTFChars(path, false);
	cv::Mat pic = cv::imread(str_path, -1);
	cv::Mat mat;
	std::string str;

	int mark = LocationFace(pic, mat);
	mat2str(mat, str);
	//str = "123456.111";
	char fvc[5] = {0};
	_itoa_s(mark, fvc, 10);
	std::string fv(fvc);
	fv += " ";
	fv += str;

	return env->NewStringUTF(fv.c_str());
}

void mat2str(cv::Mat & mat, std::string & str) {
	int N = mat.cols;
	//float *array = (float*)malloc(sizeof(float）*N);
	float *array = new float[N];
	memcpy(array, mat.data, sizeof(float)* N);

	int res = N % 8;
	int newN = N - res;
	for (int i = 0; i < newN; i = i + 8)
	{
		// std::cout << i << std::endl;
		std::string fltTostr[8];
		std::stringstream mm[8];
		mm[0] << array[i];
		fltTostr[0] = mm[0].str();

		mm[1] << array[i + 1];
		fltTostr[1] = mm[1].str();

		mm[2] << array[i + 2];
		fltTostr[2] = mm[2].str();

		mm[3] << array[i + 3];
		fltTostr[3] = mm[3].str();

		mm[4] << array[i + 4];
		fltTostr[4] = mm[4].str();

		mm[5] << array[i + 5];
		fltTostr[5] = mm[5].str();

		mm[6] << array[i + 6];
		fltTostr[6] = mm[6].str();

		mm[7] << array[i + 7];
		fltTostr[7] = mm[7].str();
		std::string relAll = fltTostr[0] + " " + fltTostr[1] + " " + fltTostr[2] + " " + fltTostr[3] + " " + fltTostr[4] + " " + fltTostr[5] + " " + fltTostr[6] + " " + fltTostr[7] + " ";
		str = str + relAll;
	}
	for (int i = newN; i < N; i++)
	{
		std::string fltTostr;
		std::stringstream mm;
		mm << array;
		fltTostr = mm.str();
		str = str + fltTostr + " ";
	}
	delete [] array;
	//free(array);
	//array = NULL;

	//return 0;
}
#include <jni.h>
#include <string>
#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <android/log.h>

using  namespace cv;
using namespace std;


#define  LOG_TAG    "Ryu value"

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

extern "C"
{
int  counter=0;
bool  capFrame=true;
JNIEXPORT jstring JNICALL
Java_com_example_tsuki_demoptz_ScreenBrightnessChange_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

void CallSetFinalResult(JNIEnv *e, jobject obj, int value)
{
    jclass cls = e->GetObjectClass(obj);
    jmethodID mid = e->GetMethodID(cls, "setFinalResult", "(I)V");
    e->CallVoidMethod(obj, mid, value);
}
void CallSetExposureMethod(JNIEnv *e, jobject obj){
    jclass cls = e->GetObjectClass(obj);
    jmethodID mid = e->GetMethodID(cls, "setExposureTime", "(I)V");
    e->CallVoidMethod(obj, mid, 50);
}

JNIEXPORT void JNICALL Java_com_example_tsuki_demoptz_ScreenBrightnessChange_CallJavaMethod(JNIEnv *e, jobject obj)
{
    CallSetExposureMethod(e,obj);
}

void CalculateAparture(JNIEnv *e, jobject obj)
{
    jclass cls = e->GetObjectClass(obj);
    jmethodID mid = e->GetMethodID(cls, "setFinalResult", "(I)V");
    e->CallVoidMethod(obj, mid, 100);
}

JNIEXPORT jint JNICALL Java_com_example_tsuki_demoptz_ScreenBrightnessChange_getIntfromMat(JNIEnv *env, jobject thiz, long addrInputImage)
{
    cv::Mat* pInputImage = (cv::Mat*)addrInputImage;
    int total=0;
    for(int i=0; i<pInputImage->rows;i++)
    {
        for(int j=0; j<pInputImage->cols;j++)
        {
            int value =(int)pInputImage->at<uchar>(i,j);
//            LOGE("T_T %d",value);
            total += value;
        }
    }
    total = total/(pInputImage->cols*pInputImage->rows);
//    LOGE("Value %d",total);
    counter++;
    if(counter ==400)
    {
        Java_com_example_tsuki_demoptz_ScreenBrightnessChange_CallJavaMethod(env,thiz);
        LOGE("call change exposure Time to 150");
    }
    else if(counter==800)
    {
        Java_com_example_tsuki_demoptz_ScreenBrightnessChange_CallJavaMethod(env,thiz);
        LOGE("stop cap frame");
        capFrame = false;
        CalculateAparture(env,thiz);

    }
    return total;
}

JNIEXPORT void JNICALL Java_com_example_tsuki_demoptz_ScreenBrightnessChange_setInt2Mat(JNIEnv *env, jobject thiz, long addrInputImage, jint value)
{
    cv::Mat* pInputImage = (cv::Mat*)addrInputImage;
}

JNIEXPORT jboolean JNICALL Java_com_example_tsuki_demoptz_ScreenBrightnessChange_getVideoFrame(JNIEnv *e, jobject jobj)
{
    return capFrame;
}


}

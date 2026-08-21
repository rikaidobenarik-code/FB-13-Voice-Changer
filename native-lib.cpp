#include <jni.h>
#include <string>
#include <vector>
#include <cmath>
#include <android/log.h>

#define LOG_TAG "FB13_AudioEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_fb13_voicechanger_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "FB-13 C++ Native Core is Active 🎙️";
    return env->NewStringUTF(hello.c_str());
}

// خوارزمية تنقية التشوش (Noise Gate / Denoiser) محلياً
extern "C" JNIEXPORT jshortArray JNICALL
Java_com_fb13_voicechanger_MainActivity_processAudioDSP(
        JNIEnv* env,
        jobject /* this */,
        jshortArray audioData,
        jboolean denoise,
        jboolean reverb,
        jboolean studioMode,
        jint pitchLevel) {

    jsize length = env->GetArrayLength(audioData);
    jshort* body = env->GetShortArrayElements(audioData, 0);

    std::vector<short> processedBuffer(body, body + length);

    LOGI("Processing audio buffer of size: %d | Denoise: %d | Reverb: %d | Studio: %d", 
         length, denoise, reverb, studioMode);

    for (int i = 0; i < length; i++) {
        float sample = processedBuffer[i];

        // 1. فلتر تنقية التشوش البسيط (إزالة الأصوات الخافتة جداً التي تُعتبر تشويشاً)
        if (denoise) {
            if (abs(sample) < 150) {
                sample = 0; // إزالة الضوضاء الخلفية المنخفضة
            }
        }

        // 2. تضخيم الصوت المنخفض (Gain Booster) ليكون واضحاً وقوياً
        sample *= 1.8f; 

        // 3. تطبيق وضع استوديو الغناء أو الصدى إذا كان مفعلاً
        if (reverb || studioMode) {
            if (i > 1000) {
                // إضافة انعكاس خفيف للصوت (Echo/Reverb Simulation)
                sample = sample + (processedBuffer[i - 1000] * 0.3f);
            }
        }

        // حماية العينة من الخروج عن النطاق المسموح به للصوت
        if (sample > 32767) sample = 32767;
        if (sample < -32768) sample = -32768;

        processedBuffer[i] = (short)sample;
    }

    // إرجاع البيانات المعالجة نقية وواضحة
    jshortArray result = env->NewShortArray(length);
    env->SetShortArrayRegion(result, 0, length, processedBuffer.data());
    env->ReleaseShortArrayElements(audioData, body, 0);

    return result;
}

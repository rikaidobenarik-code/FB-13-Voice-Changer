package com.fb13.voicechanger

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AudioEngine(private val context: Context) {

    private val TAG = "FB13_AudioEngine"
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    // تحميل مكتبة الـ C++ الأصلية للمعالجة
    external fun processAudioDSP(audioData: ShortArray, denoise: Boolean, reverb: Boolean, studioMode: Boolean, pitchLevel: Int): ShortArray

    init {
        try {
            System.loadLibrary("fb13voiceengine")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "فشل في تحميل مكتبة C++ المحلية: ${e.message}")
        }
    }

    // بدء التقاط الصوت الحقيقي من الميكروفون
    fun startRecording(outputFile: File) {
        if (isRecording) return

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize
            )

            audioRecord?.startRecording()
            isRecording = true

            Thread {
                writeAudioDataToFile(outputFile)
            }.start()

        } catch (e: SecurityException) {
            Log.e(TAG, "خطأ في صلاحيات الميكروفون: ${e.message}")
        }
    }

    private fun writeAudioDataToFile(outputFile: File) {
        val data = ByteArray(minBufferSize)
        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(outputFile)
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        while (isRecording) {
            val read = audioRecord?.read(data, 0, data.size) ?: 0
            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                try {
                    outputStream.write(data, 0, read)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        try {
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // إيقاف التسجيل ومعالجة الصوت عبر نواة الـ C++
    fun stopRecording(denoise: Boolean, reverb: Boolean, studioMode: Boolean, pitchLevel: Int): File? {
        if (!isRecording) return null
        isRecording = false

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        Log.Println(Log.INFO, TAG, "تم إيقاف التسجيل وتطبيق فلاتر FB-13...")
        // سيتم ربط هذا الملف الناتج مع ShareHelper للمشاركة الفورية
        return null
    }
}

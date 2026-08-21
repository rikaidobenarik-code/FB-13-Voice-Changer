package com.fb13.voicechanger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerEffect: Spinner
    private lateinit var seekBarPitch: SeekBar
    private lateinit var btnDenoiser: ToggleButton
    private lateinit var btnReverb: ToggleButton
    private lateinit var btnStudioMode: ToggleButton
    private lateinit var btnRecord: Button

    private var isRecording = false
    private val PERMISSION_Code = 101

    companion0:
    // تحميل مكتبة الـ C++ المحلية التي أنشأناها (fb13voiceengine)
    init {
        System.loadLibrary("fb13voiceengine")
    }

    // تعريف الدوال الأصلية القادمة من C++ (Native Functions)
    external fun stringFromJNI(): String
    external fun processAudioDSP(audioData: ShortArray, denoise: Boolean, reverb: Boolean, studioMode: Boolean, pitchLevel: Int): ShortArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ربط عناصر الواجهة
        spinnerEffect = findViewById(R.id.spinnerEffect)
        seekBarPitch = findViewById(R.id.seekBarPitch)
        btnDenoiser = findViewById(R.id.btnDenoiser)
        btnReverb = findViewById(R.id.btnReverb)
        btnStudioMode = findViewById(R.id.btnStudioMode)
        btnRecord = findViewById(R.id.btnRecord)

        // إعداد القائمة المنسدلة للشخصيات والأصوات
        val effectsList = arrayOf("صوت طبيعي نقي", "صوت فتاة (ناعم)", "صوت رجل عميق", "صوت طفل", "روبوت آلي")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, effectsList)
        spinnerEffect.adapter = adapter

        // طلب صلاحيات التسجيل من الميكروفون
        checkPermissions()

        // اختبار عمل محرك C++ عند بدء التشغيل
        Toast.makeText(this, stringFromJNI(), Toast.LENGTH_SHORT).show()

        // زر التسجيل والتشغيل اللحظي
        btnRecord.setOnClickListener {
            if (!isRecording) {
                startAudioEngine()
            } else {
                stopAudioEngine()
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_Code)
        }
    }

    private fun startAudioEngine() {
        isRecording = true
        btnRecord.text = "إيقاف ومعالجة الصوت"
        btnRecord.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
        Toast.makeText(this, "جاري الالتقاط والتنقية اللحظية عبر FB-13...", Toast.LENGTH_SHORT).show()
    }

    private fun stopAudioEngine() {
        isRecording = false
        btnRecord.text = "بدء التسجيل اللحظي"
        btnRecord.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))

        // جلب خيارات المستخدم الحالية
        val isDenoised = btnDenoiser.isChecked
        val hasReverb = btnReverb.isChecked
        val isStudioActive = btnStudioMode.isChecked
        val pitchLevel = seekBarPitch.progress
        val selectedEffect = spinnerEffect.selectedItem.toString()

        // تجربة تمرير مصفوفة صوتية وهمية عبر محرك DSP (C++) لتطبيق الفلاتر والصدى وتضخيم الصوت المنخفض
        val dummyAudioBuffer = ShortArray(100) { (it * 10).toShort() }
        val processedBuffer = processAudioDSP(dummyAudioBuffer, isDenoised, hasReverb, isStudioActive, pitchLevel)

        Toast.makeText(
            this,
            "تمت المعالجة بنجاح ($selectedEffect)!\n- تنقية التشوش: $isDenoised\n- صدى واستوديو: ${hasReverb || isStudioActive}\n- حجم العينة النقية: ${processedBuffer.size}",
            Toast.LENGTH_LONG
        ).show()
    }
}

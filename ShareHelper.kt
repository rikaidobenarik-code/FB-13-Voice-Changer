package com.fb13.voicechanger

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {

    fun shareAudioFile(context: Context, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return

        // استخدام FileProvider للمشاركة الآمنة (مهم جداً في أندرويد الحديث)
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "شارك عبر FB-13"))
    }
}

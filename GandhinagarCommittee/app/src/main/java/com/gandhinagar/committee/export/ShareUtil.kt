package com.gandhinagar.committee.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareUtil {
    fun shareExcel(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Gandhinagar Committee backup")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share backup").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

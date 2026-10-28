package com.dmirzayants.runlab.repository

import android.content.Context
import android.graphics.Bitmap
import com.dmirzayants.runlab.constants.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunImageStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    val imagesDir = File(context.filesDir, Constants.RUN_IMAGES_DIR)

    suspend fun save(bitmap: Bitmap, timestamp: Long): String = withContext(Dispatchers.IO) {
        imagesDir.mkdirs()
        val file = File(imagesDir, "run_$timestamp.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        file.absolutePath
    }

    suspend fun delete(path: String?) = withContext(Dispatchers.IO) {
        path?.let { File(it).delete() }
    }

    private companion object {
        const val JPEG_QUALITY = 85
    }
}

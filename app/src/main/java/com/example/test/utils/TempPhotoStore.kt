package com.example.test.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

/**
 * Quản lý ảnh tạm của app: chụp/chọn ảnh được copy vào cacheDir để upload,
 * không ghi ra bộ nhớ ngoài nên không cần xin quyền storage.
 */
object TempPhotoStore {

    const val TEMP_DIR = "temp_photo"

    fun createFile(context: Context): File {
        val dir = tempDir(context).apply { mkdirs() }
        return File(dir, "photo_${System.currentTimeMillis()}.jpg")
    }

    /** Chép ảnh từ content Uri của thư viện vào bộ nhớ tạm. */
    fun copyToTemp(context: Context, uri: Uri): File? {
        return try {
            val file = createFile(context)
            val copied = context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            if (copied == null) {
                file.delete()
                null
            } else {
                file
            }
        } catch (e: IOException) {
            null
        } catch (e: SecurityException) {
            null
        }
    }

    fun uriForFile(context: Context, file: File): Uri =
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

    fun delete(path: String) {
        if (path.isNotEmpty()) {
            File(path).delete()
        }
    }

    /** Xoá toàn bộ ảnh tạm cũ khi mở màn hình mới. */
    fun clear(context: Context) {
        tempDir(context).listFiles()?.forEach { it.delete() }
    }

    private fun tempDir(context: Context): File = File(context.cacheDir, TEMP_DIR)
}

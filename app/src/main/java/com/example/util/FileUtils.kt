package com.example.util

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

data class FileMetadata(
  val uri: Uri,
  val fileName: String,
  val fileSize: Long,
  val formattedSize: String,
  val mimeType: String,
  val extension: String,
  val width: Int? = null,
  val height: Int? = null,
  val durationSeconds: Int? = null
)

object FileUtils {

  fun getFileMetadata(context: Context, uri: Uri): FileMetadata {
    val contentResolver = context.contentResolver
    var fileName = "attachment_${System.currentTimeMillis()}"
    var fileSize = 0L

    try {
      contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
          val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameIndex != -1) {
            val name = cursor.getString(nameIndex)
            if (!name.isNullOrBlank()) {
              fileName = name
            }
          }
          val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
          if (sizeIndex != -1) {
            fileSize = cursor.getLong(sizeIndex)
          }
        }
      }
    } catch (e: Exception) {
      // Fallback to URI path
      uri.lastPathSegment?.let { fileName = it }
    }

    // Resolve mime type
    var mimeType = contentResolver.getType(uri)
    if (mimeType.isNullOrBlank()) {
      val ext = MimeTypeMap.getFileExtensionFromUrl(fileName)
      mimeType = if (!ext.isNullOrBlank()) {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase()) ?: "application/octet-stream"
      } else {
        "application/octet-stream"
      }
    }

    val extension = if (fileName.contains(".")) {
      fileName.substringAfterLast(".", "")
    } else {
      MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
    }

    var width: Int? = null
    var height: Int? = null
    var durationSeconds: Int? = null

    // Extract image dimensions if applicable
    if (mimeType.startsWith("image/")) {
      try {
        contentResolver.openInputStream(uri)?.use { stream ->
          val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
          BitmapFactory.decodeStream(stream, null, options)
          if (options.outWidth > 0 && options.outHeight > 0) {
            width = options.outWidth
            height = options.outHeight
          }
        }
      } catch (_: Exception) {}
    }

    // Extract video or audio duration if applicable
    if (mimeType.startsWith("video/") || mimeType.startsWith("audio/")) {
      try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        durStr?.toLongOrNull()?.let {
          durationSeconds = (it / 1000).toInt()
        }
        if (mimeType.startsWith("video/")) {
          val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
          val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
          if (w != null && h != null) {
            width = w
            height = h
          }
        }
        retriever.release()
      } catch (_: Exception) {}
    }

    return FileMetadata(
      uri = uri,
      fileName = fileName,
      fileSize = fileSize,
      formattedSize = formatFileSize(fileSize),
      mimeType = mimeType,
      extension = extension,
      width = width,
      height = height,
      durationSeconds = durationSeconds
    )
  }

  fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val df = DecimalFormat("#,##0.#")
    return "${df.format(bytes / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
  }

  fun createTempImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images")
    if (!imagesDir.exists()) imagesDir.mkdirs()
    val file = File(imagesDir, "whisper_camera_${System.currentTimeMillis()}.jpg")
    return androidx.core.content.FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )
  }

  fun createTempVideoUri(context: Context): Uri {
    val filesDir = File(context.cacheDir, "files")
    if (!filesDir.exists()) filesDir.mkdirs()
    val file = File(filesDir, "whisper_video_${System.currentTimeMillis()}.mp4")
    return androidx.core.content.FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )
  }

  fun openFileWithIntent(context: Context, uri: Uri, mimeType: String): Boolean {
    return try {
      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      false
    }
  }

  fun openUrlInBrowser(context: Context, url: String): Boolean {
    return try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      false
    }
  }
}

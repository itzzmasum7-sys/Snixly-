package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.firebase.WhisperMessageDto
import com.example.ui.theme.*
import com.example.util.FileUtils
import kotlinx.coroutines.delay

@Composable
fun WhisperFullScreenImageViewer(
  imageUrl: String,
  title: String? = null,
  onDismiss: () -> Unit
) {
  var scale by remember { mutableFloatStateOf(1f) }
  var offset by remember { mutableStateOf(Offset.Zero) }
  val context = LocalContext.current

  val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
    scale = (scale * zoomChange).coerceIn(1f, 5f)
    if (scale > 1f) {
      offset += offsetChange
    } else {
      offset = Offset.Zero
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .testTag("full_screen_image_viewer")
    ) {
      AsyncImage(
        model = imageUrl,
        contentDescription = title ?: "Full screen image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            translationX = offset.x,
            translationY = offset.y
          )
          .transformable(state = transformState)
      )

      // Top Control Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 40.dp, start = 16.dp, end = 16.dp)
          .align(Alignment.TopCenter),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            .testTag("image_viewer_close_button")
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        if (!title.isNullOrBlank()) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
          )
        }

        IconButton(
          onClick = {
            FileUtils.openUrlInBrowser(context, imageUrl)
          },
          modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
        ) {
          Icon(Icons.Outlined.OpenInBrowser, contentDescription = "Open in browser", tint = Color.White)
        }
      }
    }
  }
}

@Composable
fun WhisperAudioPlayerBubble(
  message: WhisperMessageDto,
  isFromMe: Boolean,
  textColor: Color,
  metaColor: Color
) {
  var isPlaying by remember { mutableStateOf(false) }
  var currentSecond by remember { mutableIntStateOf(0) }
  val duration = message.voiceDurationSeconds ?: 12

  LaunchedEffect(isPlaying) {
    if (isPlaying) {
      while (isPlaying && currentSecond < duration) {
        delay(1000)
        currentSecond++
      }
      if (currentSecond >= duration) {
        isPlaying = false
        currentSecond = 0
      }
    }
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
  ) {
    IconButton(
      onClick = { isPlaying = !isPlaying },
      modifier = Modifier
        .size(36.dp)
        .background((if (isFromMe) Color.White else SnixlyGoldPrimary).copy(alpha = 0.2f), CircleShape)
    ) {
      Icon(
        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
        contentDescription = if (isPlaying) "Pause" else "Play",
        tint = textColor,
        modifier = Modifier.size(20.dp)
      )
    }

    // Dynamic wave bars
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(3.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val barHeights = remember { listOf(8, 16, 22, 14, 28, 18, 24, 12, 20, 16, 26, 10, 18, 14, 22, 8) }
      barHeights.forEachIndexed { index, h ->
        val progressPercent = if (duration > 0) currentSecond.toFloat() / duration.toFloat() else 0f
        val barPercent = index.toFloat() / barHeights.size.toFloat()
        val isActive = barPercent <= progressPercent

        Box(
          modifier = Modifier
            .width(3.dp)
            .height(h.dp)
            .background(
              color = if (isActive) textColor else textColor.copy(alpha = 0.35f),
              shape = RoundedCornerShape(2.dp)
            )
        )
      }
    }

    Text(
      text = String.format("%02d:%02d", currentSecond, duration),
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
      color = metaColor
    )
  }
}

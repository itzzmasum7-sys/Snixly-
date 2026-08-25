package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhisperAttachmentSheet(
  isDarkTheme: Boolean = false,
  onDismiss: () -> Unit,
  onPickPhotos: () -> Unit,
  onPickVideos: () -> Unit,
  onLaunchCamera: () -> Unit,
  onPickDocuments: () -> Unit,
  onPickAudio: () -> Unit,
  onShareLocation: () -> Unit,
  onShareContact: () -> Unit,
  onCreatePoll: () -> Unit,
  onCreateDrop: () -> Unit
) {
  val colors = MaterialTheme.snixly
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(vertical = 10.dp)
          .width(36.dp)
          .height(4.dp)
          .background(colors.border, RoundedCornerShape(2.dp))
      )
    },
    modifier = Modifier.testTag("whisper_attachment_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Share to Whisper Space",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.primaryText
          )
          Text(
            text = "Select media, documents, or live vibes",
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 4x2 Grid of Rich Real Attachments
      Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          AttachmentSheetItem(
            icon = Icons.Outlined.PhotoLibrary,
            label = "Photos",
            color = Color(0xFF6366F1),
            testTag = "attachment_option_photos",
            onClick = {
              onDismiss()
              onPickPhotos()
            }
          )
          AttachmentSheetItem(
            icon = Icons.Outlined.Videocam,
            label = "Videos",
            color = Color(0xFFEC4899),
            testTag = "attachment_option_videos",
            onClick = {
              onDismiss()
              onPickVideos()
            }
          )
          AttachmentSheetItem(
            icon = Icons.Outlined.PhotoCamera,
            label = "Camera",
            color = colors.accentGold,
            testTag = "attachment_option_camera",
            onClick = {
              onDismiss()
              onLaunchCamera()
            }
          )
          AttachmentSheetItem(
            icon = Icons.Outlined.Description,
            label = "Files",
            color = Color(0xFF0EA5E9),
            testTag = "attachment_option_files",
            onClick = {
              onDismiss()
              onPickDocuments()
            }
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          AttachmentSheetItem(
            icon = Icons.Outlined.Audiotrack,
            label = "Audio",
            color = Color(0xFF8B5CF6),
            testTag = "attachment_option_audio",
            onClick = {
              onDismiss()
              onPickAudio()
            }
          )
          AttachmentSheetItem(
            icon = Icons.Outlined.LocationOn,
            label = "Location",
            color = Color(0xFF10B981),
            testTag = "attachment_option_location",
            onClick = {
              onDismiss()
              onShareLocation()
            }
          )
          AttachmentSheetItem(
            icon = Icons.Outlined.ContactPhone,
            label = "Contact",
            color = Color(0xFFF59E0B),
            testTag = "attachment_option_contact",
            onClick = {
              onDismiss()
              onShareContact()
            }
          )
          AttachmentSheetItem(
            icon = Icons.Outlined.Poll,
            label = "Live Poll",
            color = SnixlyGoldDeep,
            testTag = "attachment_option_poll",
            onClick = {
              onDismiss()
              onCreatePoll()
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}

@Composable
private fun AttachmentSheetItem(
  icon: ImageVector,
  label: String,
  color: Color,
  testTag: String,
  onClick: () -> Unit
) {
  val colors = MaterialTheme.snixly
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(72.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(vertical = 4.dp)
      .testTag(testTag)
  ) {
    Box(
      modifier = Modifier
        .size(56.dp)
        .background(color.copy(alpha = 0.12f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = color,
        modifier = Modifier.size(26.dp)
      )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp, color = colors.primaryText),
      textAlign = TextAlign.Center,
      maxLines = 1
    )
  }
}

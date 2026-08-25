package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.firebase.WhisperMessageType
import com.example.ui.theme.*
import com.example.util.FileMetadata
import com.example.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhisperMediaPreviewScreen(
  items: List<FileMetadata>,
  onRemoveItem: (Int) -> Unit,
  onAddMore: () -> Unit,
  onSend: (caption: String) -> Unit,
  onCancel: () -> Unit,
  isCameraCapture: Boolean = false,
  onRetake: () -> Unit = {}
) {
  var activeIndex by remember { mutableStateOf(0) }
  var captionText by remember { mutableStateOf("") }
  val context = LocalContext.current

  // Ensure activeIndex is within bounds
  val safeIndex = activeIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
  val currentItem = items.getOrNull(safeIndex)

  if (items.isEmpty()) {
    LaunchedEffect(Unit) { onCancel() }
    return
  }

  Scaffold(
    containerColor = Color.Black,
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color.Black.copy(alpha = 0.7f),
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White,
          actionIconContentColor = Color.White
        ),
        title = {
          Column {
            Text(
              text = if (isCameraCapture) "Captured Photo" else "Preview Attachment (${items.size})",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            if (currentItem != null) {
              Text(
                text = "${currentItem.fileName} • ${currentItem.formattedSize}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onCancel, modifier = Modifier.testTag("preview_cancel_button")) {
            Icon(Icons.Default.Close, contentDescription = "Cancel")
          }
        },
        actions = {
          if (isCameraCapture) {
            TextButton(onClick = onRetake, modifier = Modifier.testTag("camera_retake_button")) {
              Text("Retake", color = SnixlyGoldPrimary, fontWeight = FontWeight.Bold)
            }
          } else {
            IconButton(onClick = onAddMore, modifier = Modifier.testTag("preview_add_more_button")) {
              Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = "Add More", tint = SnixlyGoldPrimary)
            }
          }
        }
      )
    },
    bottomBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.Black.copy(alpha = 0.85f))
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        // Thumbnail Strip when multiple items are selected
        if (items.size > 1) {
          LazyRow(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            itemsIndexed(items) { index, item ->
              val isSelected = index == safeIndex
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) SnixlyGoldPrimary else Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                  )
                  .clickable { activeIndex = index }
              ) {
                if (item.mimeType.startsWith("image/") || item.mimeType.startsWith("video/")) {
                  AsyncImage(
                    model = item.uri,
                    contentDescription = item.fileName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )
                } else {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .background(Color(0xFF202020)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.Description,
                      contentDescription = null,
                      tint = SnixlyGoldPrimary,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                }

                // Remove item badge
                Box(
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(18.dp)
                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                    .clickable {
                      onRemoveItem(index)
                      if (activeIndex >= items.size - 1) {
                        activeIndex = (items.size - 2).coerceAtLeast(0)
                      }
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(12.dp))
                }
              }
            }
          }
        }

        // Caption Box + Send Action
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = captionText,
            onValueChange = { captionText = it },
            placeholder = {
              Text("Add a caption...", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
            },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = SnixlyGoldPrimary,
              unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
              focusedContainerColor = Color(0xFF1E1E1E),
              unfocusedContainerColor = Color(0xFF1E1E1E),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("preview_caption_input")
          )

          IconButton(
            onClick = { onSend(captionText.trim()) },
            modifier = Modifier
              .size(48.dp)
              .background(SnixlyGoldPrimary, CircleShape)
              .testTag("preview_send_button")
          ) {
            Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(22.dp))
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.Center
    ) {
      if (currentItem != null) {
        if (currentItem.mimeType.startsWith("image/")) {
          AsyncImage(
            model = currentItem.uri,
            contentDescription = currentItem.fileName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
              .fillMaxSize()
              .testTag("preview_image_view")
          )
        } else if (currentItem.mimeType.startsWith("video/")) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black),
            contentAlignment = Alignment.Center
          ) {
            AsyncImage(
              model = currentItem.uri,
              contentDescription = currentItem.fileName,
              contentScale = ContentScale.Fit,
              modifier = Modifier.fillMaxSize()
            )
            Box(
              modifier = Modifier
                .size(64.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(36.dp))
            }
          }
        } else {
          // Document / Audio / File card
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier
              .padding(32.dp)
              .fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = when {
                  currentItem.mimeType.startsWith("audio/") -> Icons.Outlined.Audiotrack
                  currentItem.extension.equals("pdf", ignoreCase = true) -> Icons.Outlined.PictureAsPdf
                  else -> Icons.Outlined.InsertDriveFile
                },
                contentDescription = null,
                tint = SnixlyGoldPrimary,
                modifier = Modifier.size(64.dp)
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = currentItem.fileName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "${currentItem.extension.uppercase()} • ${currentItem.formattedSize}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
              )
            }
          }
        }
      }
    }
  }
}

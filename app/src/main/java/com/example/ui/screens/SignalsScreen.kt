package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.SignalNotification
import com.example.model.SignalType
import com.example.ui.theme.*

@Composable
fun SignalsScreen(
  signals: List<SignalNotification>,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.background)
      .testTag("signals_screen")
  ) {
    // Header
    Surface(
      color = colors.surface,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBackClick) {
          Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = colors.primaryText)
        }
        Text(
          text = "Signals & Activity",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = colors.primaryText
        )
      }
    }
    HorizontalDivider(color = colors.border, thickness = 1.dp)

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(signals) { signal ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(colors.border, colors.border)))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Signal Icon / User Avatar combo
            Box(contentAlignment = Alignment.BottomEnd) {
              AsyncImage(
                model = signal.user.avatarUrl,
                contentDescription = signal.user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
              )

              val (badgeIcon, badgeColor) = when (signal.type) {
                SignalType.LIKE -> Icons.Filled.Favorite to SnixlyGoldPrimary
                SignalType.RESPARK -> Icons.Filled.Autorenew to SnixlyEmeraldActive
                SignalType.VAULT_SAVE -> Icons.Filled.Bookmark to SnixlyGoldDeep
                SignalType.WHISPER_REQUEST -> Icons.Filled.Lock to SnixlySapphireAccent
                SignalType.COMMENT -> Icons.Filled.ChatBubble to SnixlyGoldBright
                SignalType.MENTION -> Icons.Filled.AlternateEmail to SnixlyGoldPrimary
              }

              Box(
                modifier = Modifier
                  .size(18.dp)
                  .background(badgeColor, CircleShape)
                  .padding(2.dp),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = badgeIcon,
                  contentDescription = "Signal Badge",
                  tint = Color.White,
                  modifier = Modifier.size(12.dp)
                )
              }
            }

            // Notification Details
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = signal.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                color = colors.primaryText
              )
              if (signal.subtitle.isNotEmpty()) {
                Text(
                  text = signal.subtitle,
                  style = MaterialTheme.typography.bodySmall,
                  color = colors.secondaryText
                )
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = signal.timeAgo,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = colors.secondaryText
              )
            }
          }
        }
      }
    }
  }
}

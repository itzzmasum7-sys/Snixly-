package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun WhisperEmojiDrawer(
  isDarkTheme: Boolean,
  onEmojiSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Vibes", "Faces", "Gestures", "Objects", "Nature")

  val emojiCategories = remember {
    listOf(
      listOf("✨", "💫", "🌟", "🔥", "💎", "🌸", "🌿", "⚡", "🌙", "👑", "🔮", "🪄", "🥂", "🪐", "🤍", "💛"),
      listOf("😊", "🥰", "😎", "🤩", "🤔", "🥺", "😇", "🥳", "😌", "🙌", "🤍", "💖", "🫶", "👏", "🤝", "🎉"),
      listOf("👍", "🤙", "✌️", "🤞", "🤌", "👋", "🙏", "✍️", "🚀", "💡", "🧠", "🎯", "🎨", "🎬", "🎧", "📸"),
      listOf("☕", "🍵", "📚", "💻", "📱", "💼", "🔑", "🏆", "🥇", "🎪", "🎭", "🎵", "🎶", "🎙️", "🔔", "✉️"),
      listOf("🌸", "🌻", "🌴", "🌲", "🍀", "🌊", "🌈", "☀️", "⛅", "🌌", "🕊️", "🦋", "🦁", "🐬", "🍁", "🏔️")
    )
  }

  Surface(
    color = colors.surface,
    tonalElevation = 6.dp,
    modifier = modifier
      .fillMaxWidth()
      .height(240.dp)
      .testTag("whisper_emoji_drawer")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = colors.surfaceVariant,
        contentColor = colors.accentGold,
        divider = {}
      ) {
        tabs.forEachIndexed { index, tabName ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = {
              Text(
                text = tabName,
                fontSize = 12.sp,
                color = if (selectedTab == index) colors.accentGold else colors.secondaryText
              )
            }
          )
        }
      }

      val currentEmojis = emojiCategories.getOrElse(selectedTab) { emojiCategories[0] }
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 44.dp),
        modifier = Modifier
          .fillMaxSize()
          .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        items(currentEmojis) { emoji ->
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .clickable { onEmojiSelected(emoji) },
            contentAlignment = Alignment.Center
          ) {
            Text(text = emoji, fontSize = 22.sp)
          }
        }
      }
    }
  }
}

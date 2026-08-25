package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TrendingUp
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
import com.example.model.Post
import com.example.model.UserProfile
import com.example.ui.theme.*

@Composable
fun ExploreScreen(
  posts: List<Post>,
  onPostClick: (Post) -> Unit,
  onUserClick: (UserProfile) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly
  var searchQuery by remember { mutableStateOf("") }
  var selectedTopic by remember { mutableStateOf("All") }

  val topics = listOf("All", "Brutalist Spaces", "Analog Audio", "Kinetic Design", "Ceramics", "Minimalism")
  val trendingTags = listOf("#Architecture2026", "#NordicLight", "#MoogModular", "#ClayStudio", "#SnixlySpaces")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.background)
      .testTag("explore_screen")
  ) {
    // Search Bar Header
    Surface(
      color = colors.surface,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Discover creators, ideas, spaces...", color = colors.secondaryText, fontSize = 14.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.Search, contentDescription = "Search", tint = colors.accentGold)
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = colors.primaryText,
              unfocusedTextColor = colors.primaryText,
              focusedContainerColor = colors.background,
              unfocusedContainerColor = colors.background,
              focusedBorderColor = colors.accentGold,
              unfocusedBorderColor = colors.border
            ),
            modifier = Modifier.weight(1f)
          )

          IconButton(
            onClick = {},
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(colors.surfaceVariant)
          ) {
            Icon(
              imageVector = Icons.Outlined.FilterList,
              contentDescription = "Filter",
              tint = colors.primaryText
            )
          }
        }

        // Topics Horizontal Scroll
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          topics.forEach { topic ->
            val isSelected = selectedTopic == topic
            Surface(
              onClick = { selectedTopic = topic },
              shape = RoundedCornerShape(16.dp),
              color = if (isSelected) colors.accentGold else colors.surfaceVariant,
              modifier = Modifier.height(32.dp)
            ) {
              Box(
                modifier = Modifier.padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = topic,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = if (isSelected) Color.White else colors.secondaryText
                )
              }
            }
          }
        }
      }
    }
    HorizontalDivider(color = colors.border, thickness = 1.dp)

    // Explore Content Grid
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      contentPadding = PaddingValues(16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      // Trending Tags Banner
      item(span = { GridItemSpan(2) }) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(colors.border, colors.border)))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.TrendingUp,
                contentDescription = "Trending",
                tint = colors.accentGold,
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = "Trending Waves",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                color = colors.primaryText
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              trendingTags.forEach { tag ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = colors.accentGold.copy(alpha = 0.2f),
                  modifier = Modifier.clickable {}
                ) {
                  Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.accentGold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Visual Posts Exploration Grid
      items(posts) { post ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClick(post) }
            .testTag("explore_item_${post.id}"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(colors.border, colors.border)))
        ) {
          Column {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(colors.surfaceVariant)
            ) {
              if (post.imageUrl != null) {
                AsyncImage(
                  model = post.imageUrl,
                  contentDescription = post.categoryTag ?: "Post visual",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              } else {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primaryText,
                    maxLines = 4
                  )
                }
              }

              if (post.categoryTag != null) {
                Box(
                  modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .background(colors.surface.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = post.categoryTag,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = colors.accentGold
                  )
                }
              }
            }

            // Small footer
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = post.author.name,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.primaryText,
                maxLines = 1
              )
              Text(
                text = "${post.likesCount} ♥",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = colors.accentGold
              )
            }
          }
        }
      }
    }
  }
}

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.LoopControlMode
import com.example.model.LoopItem
import com.example.model.UserProfile
import com.example.ui.theme.*

/**
 * 180-185. LOOPS SIGNATURE SUITE
 * Immersive full-screen video with Loop Pulse Rail, Control Modes (Minimal/Standard/Info+),
 * Clean View, Hold for Context, Learning Mode and Loop Trail.
 */
@Composable
fun LoopsScreen(
  loops: List<LoopItem>,
  onUserClick: (UserProfile) -> Unit,
  modifier: Modifier = Modifier
) {
  val pagerState = rememberPagerState(pageCount = { loops.size })
  var showContextSheet by remember { mutableStateOf<LoopItem?>(null) }
  var isCleanView by remember { mutableStateOf(false) } // 183. Loop Clean View
  var controlMode by remember { mutableStateOf(LoopControlMode.STANDARD) } // 181. Loop Control Modes
  var showLearningSheet by remember { mutableStateOf<LoopItem?>(null) } // 184. Loop Learning Mode
  var showTrailSheet by remember { mutableStateOf(false) } // 185. Loop Trail
  var loopLikes by remember { mutableStateOf(loops.associate { it.id to it.isLiked }) }
  var loopVaults by remember { mutableStateOf(loops.associate { it.id to it.isVaulted }) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag("loops_screen")
  ) {
    VerticalPager(
      state = pagerState,
      modifier = Modifier.fillMaxSize()
    ) { page ->
      val loop = loops[page]
      val isLiked = loopLikes[loop.id] == true
      val isVaulted = loopVaults[loop.id] == true

      Box(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(Unit) {
            detectTapGestures(
              onTap = {
                // 183. Tap to toggle Clean View
                isCleanView = !isCleanView
              },
              onLongPress = {
                // 182. Hold for Context Layer
                showContextSheet = loop
              }
            )
          }
      ) {
        // Loop Visual / Video Canvas
        AsyncImage(
          model = loop.videoThumbnailUrl,
          contentDescription = loop.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Gradient Dim Overlays (fade in clean view)
        AnimatedVisibility(
          visible = !isCleanView,
          enter = fadeIn(),
          exit = fadeOut(),
          modifier = Modifier.fillMaxSize()
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color.Black.copy(alpha = 0.45f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.85f)
                  )
                )
              )
          )
        }

        // 180. Loop Pulse Rail (Ultra-thin glowing edge progress indicator)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .align(Alignment.BottomCenter)
            .background(Color.White.copy(alpha = 0.2f))
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(0.65f)
              .fillMaxHeight()
              .background(Brush.horizontalGradient(listOf(SnixlyGoldPrimary, SnixlyGoldBright)))
          )
        }

        // Top Control Pill Bar (Mode Switcher & History)
        AnimatedVisibility(
          visible = !isCleanView,
          enter = fadeIn() + slideInVertically(),
          exit = fadeOut() + slideOutVertically(),
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              if (loop.seriesTag != null) {
                Surface(
                  color = SnixlyGoldPrimary,
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.clickable { showContextSheet = loop }
                ) {
                  Text(
                    text = "Series • ${loop.seriesTag}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              }

              // Control Mode Switcher Pill
              Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SnixlyGoldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.clickable {
                  controlMode = when (controlMode) {
                    LoopControlMode.MINIMAL -> LoopControlMode.STANDARD
                    LoopControlMode.STANDARD -> LoopControlMode.INFO_PLUS
                    LoopControlMode.INFO_PLUS -> LoopControlMode.MINIMAL
                  }
                }
              ) {
                Text(
                  text = "Mode: ${controlMode.name} ▾",
                  style = MaterialTheme.typography.labelSmall.copy(color = SnixlyGoldChampagne, fontWeight = FontWeight.Bold),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }

            // Learning Mode & Trail shortcuts
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = CircleShape,
                modifier = Modifier
                  .size(32.dp)
                  .clickable { showLearningSheet = loop },
                contentColor = Color.White
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.School, contentDescription = "Learning Mode", tint = SnixlyGoldPrimary, modifier = Modifier.size(16.dp))
                }
              }

              Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = CircleShape,
                modifier = Modifier
                  .size(32.dp)
                  .clickable { showTrailSheet = true },
                contentColor = Color.White
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.History, contentDescription = "Loop Trail", tint = Color.White, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }

        // Bottom Left Info Overlay
        AnimatedVisibility(
          visible = !isCleanView && controlMode != LoopControlMode.MINIMAL,
          enter = fadeIn(),
          exit = fadeOut(),
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 16.dp, bottom = 24.dp, end = 85.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Author
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.clickable { onUserClick(loop.author) }
            ) {
              AsyncImage(
                model = loop.author.avatarUrl,
                contentDescription = loop.author.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .border(1.5.dp, SnixlyGoldPrimary, CircleShape)
              )
              Column {
                Text(
                  text = loop.author.name,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Text(
                  text = "@${loop.author.username}",
                  style = MaterialTheme.typography.labelSmall.copy(color = SnixlyGoldChampagne)
                )
              }
            }

            Text(
              text = loop.title,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )

            Text(
              text = loop.description,
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.90f),
              maxLines = if (controlMode == LoopControlMode.INFO_PLUS) 4 else 2,
              overflow = TextOverflow.Ellipsis
            )

            // Info+ Extra Meta
            if (controlMode == LoopControlMode.INFO_PLUS) {
              Surface(
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 2.dp)
              ) {
                Text(
                  text = "📚 Part of 'Architectural Elegance' • 4 Chapters • 1080p 60fps Master",
                  style = MaterialTheme.typography.labelSmall.copy(color = SnixlyGoldBright),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            // Audio Track Indicator
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Audio",
                tint = SnixlyGoldSoft,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = loop.audioTrack,
                style = MaterialTheme.typography.labelSmall,
                color = SnixlyGoldChampagne,
                maxLines = 1
              )
            }
          }
        }

        // Right Pulse Rail Action Bar
        AnimatedVisibility(
          visible = !isCleanView,
          enter = fadeIn() + slideInHorizontally { it / 2 },
          exit = fadeOut() + slideOutHorizontally { it / 2 },
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 14.dp, bottom = 24.dp)
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            // Like
            PulseRailAction(
              icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
              label = if (isLiked) (loop.likesCount + 1).toString() else loop.likesCount.toString(),
              color = if (isLiked) SnixlyGoldBright else Color.White,
              onClick = {
                loopLikes = loopLikes.toMutableMap().apply { put(loop.id, !isLiked) }
              }
            )

            // Comments
            PulseRailAction(
              icon = Icons.Outlined.ChatBubbleOutline,
              label = loop.commentsCount.toString(),
              color = Color.White,
              onClick = { showContextSheet = loop }
            )

            // Vault
            PulseRailAction(
              icon = if (isVaulted) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
              label = "Vault",
              color = if (isVaulted) SnixlyGoldPrimary else Color.White,
              onClick = {
                loopVaults = loopVaults.toMutableMap().apply { put(loop.id, !isVaulted) }
              }
            )

            // Respark / Share
            PulseRailAction(
              icon = Icons.Outlined.Autorenew,
              label = "Respark",
              color = Color.White,
              onClick = {}
            )

            // Audio Disc Spin
            val infiniteTransition = rememberInfiniteTransition(label = "disc")
            val rotation by infiniteTransition.animateFloat(
              initialValue = 0f,
              targetValue = 360f,
              animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
              ),
              label = "disc_rotate"
            )

            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .border(2.dp, SnixlyGoldPrimary, CircleShape)
                .rotate(rotation),
              contentAlignment = Alignment.Center
            ) {
              AsyncImage(
                model = loop.author.avatarUrl,
                contentDescription = "Audio disc",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
              )
            }
          }
        }
      }
    }
  }

  val colors = MaterialTheme.snixly

  // 182. Hold For Context Dialog
  showContextSheet?.let { loop ->
    AlertDialog(
      onDismissRequest = { showContextSheet = null },
      confirmButton = {
        Button(
          onClick = { showContextSheet = null },
          colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
        ) {
          Text("Done", color = Color.White)
        }
      },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Info, contentDescription = null, tint = colors.accentGold)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Loop Context Layer", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Title: ${loop.title}", fontWeight = FontWeight.Bold, color = colors.primaryText)
          Text("Creator: ${loop.author.name} (@${loop.author.username})", color = colors.secondaryText)
          Text("Audio Series: ${loop.audioTrack}", color = colors.accentGold, fontWeight = FontWeight.SemiBold)
          HorizontalDivider(color = colors.border)
          Text("Full Concept Note:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.primaryText)
          Text(loop.description, style = MaterialTheme.typography.bodyMedium, color = colors.primaryText)
        }
      },
      containerColor = colors.surface
    )
  }

  // 184. Loop Learning Mode Sheet
  showLearningSheet?.let { loop ->
    AlertDialog(
      onDismissRequest = { showLearningSheet = null },
      confirmButton = {
        Button(
          onClick = { showLearningSheet = null },
          colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
        ) {
          Text("Save to Knowledge Vault 📚", color = Color.White)
        }
      },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.School, contentDescription = null, tint = colors.accentGold)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Loop Learning Mode", fontWeight = FontWeight.Bold, color = colors.primaryText)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Structured educational takeaways for '${loop.title}':", fontSize = 13.sp, color = colors.secondaryText)
          HorizontalDivider(color = colors.border)
          Text("• Chapter 1 (0:00 - 0:15): Organic Form Analysis", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.primaryText)
          Text("• Chapter 2 (0:15 - 0:45): Light Dispersion & Surface Reflection", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.primaryText)
          Text("• Chapter 3 (0:45 - 1:00): Practical Studio Implementation", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.primaryText)
        }
      },
      containerColor = colors.surface
    )
  }

  // 185. Loop Trail (Private History Sheet)
  if (showTrailSheet) {
    AlertDialog(
      onDismissRequest = { showTrailSheet = false },
      confirmButton = {
        TextButton(onClick = { showTrailSheet = false }) {
          Text("Close", color = colors.accentGold, fontWeight = FontWeight.Bold)
        }
      },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.History, contentDescription = null, tint = colors.accentGold)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Loop Trail (Private History)", fontWeight = FontWeight.Bold, color = colors.primaryText)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Your private watch history (never publicly exposed):", fontSize = 13.sp, color = colors.secondaryText)
          HorizontalDivider(color = colors.border)
          loops.take(3).forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(16.dp))
              Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = colors.primaryText)
            }
          }
        }
      },
      containerColor = colors.surface
    )
  }
}

@Composable
private fun PulseRailAction(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  color: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable { onClick() }
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .background(Color.Black.copy(alpha = 0.5f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = color,
        modifier = Modifier.size(26.dp)
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
      color = Color.White
    )
  }
}

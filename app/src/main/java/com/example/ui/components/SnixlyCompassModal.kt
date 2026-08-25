package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

/**
 * 162. SNIXLY COMPASS & PERSONAL COMMAND BAR
 * Signature Centralized Control Ring & Executive Assistant Surface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnixlyCompassModal(
  onDismiss: () -> Unit,
  onNavigateTo: (String) -> Unit,
  currentSocialEnergy: SocialEnergy,
  onUpdateSocialEnergy: (SocialEnergy) -> Unit,
  currentPrivacyState: PrivacyQuickState,
  onUpdatePrivacyState: (PrivacyQuickState) -> Unit,
  recentDestinations: List<RecentDestination>,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly
  var selectedTab by remember { mutableStateOf(CompassAction.COMMAND_BAR) }
  var commandText by remember { mutableStateOf("") }
  var commandFeedback by remember { mutableStateOf<String?>(null) }
  var safeArrivalStatus by remember { mutableStateOf<String?>("Not Active") }
  var showAttentionReceipt by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    tonalElevation = 8.dp,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    dragHandle = {
      Column(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .width(48.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(colors.accentGold.copy(alpha = 0.6f))
        )
      }
    }
  ) {
    Column(
      modifier = modifier
        .fillMaxWidth()
        .fillMaxHeight(0.90f)
        .padding(horizontal = 18.dp, vertical = 6.dp)
        .testTag("snixly_compass_modal")
    ) {
      // Header: Compass Signature Ring
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(colors.accentGold, SnixlyGoldBright)))
              .padding(2.dp)
              .clip(CircleShape)
              .background(Color(0xFF151922)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Explore,
              contentDescription = "Compass",
              tint = colors.accentGold,
              modifier = Modifier.size(22.dp)
            )
          }
          Column {
            Text(
              text = "SNIXLY COMPASS",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = colors.primaryText
              )
            )
            Text(
              text = "Signature Command & Attention Hub",
              style = MaterialTheme.typography.labelSmall.copy(color = colors.secondaryText)
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("close_compass_button")
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.secondaryText)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 197. Personal Command Bar
      OutlinedTextField(
        value = commandText,
        onValueChange = {
          commandText = it
          commandFeedback = null
        },
        placeholder = {
          Text(
            "Type a command... ('Open Vault', 'Start Focus', 'Quiet Mode')",
            fontSize = 13.sp,
            color = colors.secondaryText
          )
        },
        leadingIcon = {
          Icon(Icons.Default.Terminal, contentDescription = null, tint = colors.accentGold)
        },
        trailingIcon = {
          if (commandText.isNotBlank()) {
            IconButton(
              onClick = {
                val query = commandText.trim().lowercase()
                when {
                  query.contains("vault") -> {
                    onNavigateTo("profile")
                    onDismiss()
                  }
                  query.contains("focus") -> {
                    onUpdateSocialEnergy(SocialEnergy.QUIET)
                    commandFeedback = "✨ Focus & Quiet Mode activated."
                  }
                  query.contains("loop") -> {
                    onNavigateTo("loops")
                    onDismiss()
                  }
                  query.contains("whisper") || query.contains("chat") -> {
                    onNavigateTo("whisper")
                    onDismiss()
                  }
                  query.contains("travel") -> {
                    onUpdatePrivacyState(PrivacyQuickState.TRAVEL)
                    commandFeedback = "✈️ Travel Privacy mode armed."
                  }
                  query.contains("receipt") || query.contains("attention") -> {
                    showAttentionReceipt = true
                  }
                  else -> {
                    commandFeedback = "Executed command: '$commandText'"
                  }
                }
                commandText = ""
              }
            ) {
              Icon(Icons.Default.ArrowForward, contentDescription = "Execute", tint = colors.accentGold)
            }
          }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = {
          if (commandText.isNotBlank()) {
            commandFeedback = "Executed command: '$commandText'"
            commandText = ""
          }
        }),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border,
          focusedContainerColor = colors.background,
          unfocusedContainerColor = colors.background,
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText
        ),
        modifier = Modifier.fillMaxWidth().testTag("compass_command_bar")
      )

      if (commandFeedback != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
          color = colors.accentGold.copy(alpha = 0.1f),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = commandFeedback!!,
            style = MaterialTheme.typography.bodySmall.copy(color = colors.accentGold, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Compass Action Hub Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("compass_tabs_row")
      ) {
        items(
          listOf(
            CompassAction.COMMAND_BAR to "Command Center ⚡",
            CompassAction.CATCH_ME_UP to "Catch Me Up ✨",
            CompassAction.FOCUS_MODE to "Social Energy 🧘",
            CompassAction.PRIVACY_STATES to "Privacy Shield 🛡️",
            CompassAction.YOUR_ALGORITHM to "Your Algorithm 🎛️",
            CompassAction.SAFE_ARRIVAL to "Safe Arrival 📍",
            CompassAction.RECENT_DESTINATIONS to "Backtrack 🔙"
          )
        ) { (action, label) ->
          val isSelected = selectedTab == action
          FilterChip(
            selected = isSelected,
            onClick = { selectedTab = action },
            label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = colors.accentGold,
              selectedLabelColor = Color.White,
              containerColor = colors.background,
              labelColor = colors.primaryText
            ),
            shape = RoundedCornerShape(10.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Content by Tab
      LazyColumn(
        modifier = Modifier.fillMaxSize().weight(1f),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        when (selectedTab) {
          CompassAction.COMMAND_BAR -> {
            item {
              Text(
                "Quick Executive Shortcuts",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = colors.primaryText)
              )
            }
            item {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CompassRingTile(
                  title = "Catch Me Up",
                  desc = "High-priority digests",
                  icon = Icons.Default.AutoAwesome,
                  color = colors.accentGold,
                  modifier = Modifier.weight(1f),
                  onClick = { selectedTab = CompassAction.CATCH_ME_UP }
                )
                CompassRingTile(
                  title = "Focus Mode",
                  desc = "${currentSocialEnergy.label}",
                  icon = Icons.Default.Spa,
                  color = Color(0xFF059669),
                  modifier = Modifier.weight(1f),
                  onClick = { selectedTab = CompassAction.FOCUS_MODE }
                )
              }
            }
            item {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CompassRingTile(
                  title = "Privacy States",
                  desc = "${currentPrivacyState.label}",
                  icon = Icons.Default.Security,
                  color = Color(0xFF0284C7),
                  modifier = Modifier.weight(1f),
                  onClick = { selectedTab = CompassAction.PRIVACY_STATES }
                )
                CompassRingTile(
                  title = "Attention Receipt",
                  desc = "Review session load",
                  icon = Icons.Default.ReceiptLong,
                  color = Color(0xFF8B5CF6),
                  modifier = Modifier.weight(1f),
                  onClick = { showAttentionReceipt = true }
                )
              }
            }
          }

          CompassAction.CATCH_ME_UP -> {
            // 202. Catch-up Structured Cards & 204. Smart Stopping Points
            item {
              Surface(
                color = colors.accentGold.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("You're Caught Up!", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = colors.primaryText))
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    "All 4 priority Connections and 2 active Whisper threads are up to date.",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
                  )
                }
              }
            }
            item {
              Text("Catch-Up Cards", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = colors.primaryText))
            }
            item {
              CatchUpCardItem(
                badge = "PEOPLE",
                title = "Elena Rostova shared 2 architectural studies",
                time = "32m ago",
                icon = Icons.Default.Person,
                onOpen = {
                  onNavigateTo("home")
                  onDismiss()
                }
              )
            }
            item {
              CatchUpCardItem(
                badge = "WHISPERS",
                title = "Marcus Vance pinned 'Kyoto Studio Session Schedule'",
                time = "1h ago",
                icon = Icons.Default.PushPin,
                onOpen = {
                  onNavigateTo("whisper")
                  onDismiss()
                }
              )
            }
            item {
              CatchUpCardItem(
                badge = "CREATOR",
                title = "Design Digest Loop #14 hit trending milestones",
                time = "3h ago",
                icon = Icons.Default.PlayCircle,
                onOpen = {
                  onNavigateTo("loops")
                  onDismiss()
                }
              )
            }
          }

          CompassAction.FOCUS_MODE -> {
            // 201. Social Energy Control & 200. Creator Workspace
            item {
              Text("Social Energy & Focus Rhythm", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = colors.primaryText))
              Text(
                "Choose how much social stimulation and recommendation priority you want during this session.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
              )
            }
            items(SocialEnergy.values()) { energy ->
              val isSelected = currentSocialEnergy == energy
              Surface(
                color = if (isSelected) colors.accentGold.copy(alpha = 0.12f) else colors.background,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) colors.accentGold else colors.border
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onUpdateSocialEnergy(energy) }
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = energy.label,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) colors.accentGold else colors.primaryText
                      )
                    )
                    Text(text = energy.desc, style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText))
                  }
                  RadioButton(selected = isSelected, onClick = { onUpdateSocialEnergy(energy) })
                }
              }
            }
          }

          CompassAction.PRIVACY_STATES -> {
            // 210. Privacy Quick States & 211. Travel Mode
            item {
              Text("Instant Privacy Bundles", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = colors.primaryText))
              Text(
                "Switch one-tap privacy configurations with automatic state snapshot protection.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
              )
            }
            items(PrivacyQuickState.values()) { privacyState ->
              val isSelected = currentPrivacyState == privacyState
              Surface(
                color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.1f) else colors.background,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) Color(0xFF0284C7) else colors.border
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onUpdatePrivacyState(privacyState) }
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = privacyState.label,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0284C7) else colors.primaryText
                      )
                    )
                    Text(text = privacyState.desc, style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText))
                  }
                  RadioButton(selected = isSelected, onClick = { onUpdatePrivacyState(privacyState) })
                }
              }
            }
          }

          CompassAction.YOUR_ALGORITHM -> {
            // Your Algorithm Control
            item {
              Text("Your Algorithm Tuning", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = colors.primaryText))
              Text("Transparently govern what the recommendation engine serves you.", style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText))
            }
            item {
              AlgorithmDialCard(title = "Serendipity vs Familiarity", value = 0.70f, desc = "Explores new creators with balanced relevance")
            }
            item {
              AlgorithmDialCard(title = "Visual Loops vs Deep Writing", value = 0.55f, desc = "Equal balance between video craft and editorial thoughts")
            }
            item {
              AlgorithmDialCard(title = "Connection Priority Bias", value = 0.85f, desc = "Strongly prioritizes posts from Inner Circles & Whispers")
            }
          }

          CompassAction.SAFE_ARRIVAL -> {
            // 212. Safe Arrival Check-In
            item {
              Text("Safe Arrival Check-in", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = colors.primaryText))
              Text(
                "Notify your trusted Circle automatically when you confirm safe arrival without public broadcast.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
              )
            }
            item {
              Surface(
                color = colors.background,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Text("Status: $safeArrivalStatus", fontWeight = FontWeight.Bold, color = colors.accentGold)
                  Spacer(modifier = Modifier.height(8.dp))
                  Button(
                    onClick = {
                      safeArrivalStatus = "Armed: Target 'Arrival at Tokyo Studio by 10:30 PM'"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text("Arm 60-Minute Arrival Check-In 📍", color = Color.White)
                  }
                }
              }
            }
          }

          CompassAction.RECENT_DESTINATIONS -> {
            // 164. Backtrack Navigation
            item {
              Text("Backtrack History", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = colors.primaryText))
              Text("Quickly resume recent authorized destinations.", style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText))
            }
            items(recentDestinations) { destination ->
              Surface(
                color = colors.background,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    onNavigateTo(destination.route)
                    onDismiss()
                  }
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  Icon(Icons.Default.History, contentDescription = null, tint = colors.accentGold)
                  Column(modifier = Modifier.weight(1f)) {
                    Text(destination.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = colors.primaryText))
                    Text(destination.subtitle, style = MaterialTheme.typography.labelSmall.copy(color = colors.secondaryText))
                  }
                  Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.secondaryText)
                }
              }
            }
          }
          else -> {}
        }
      }
    }
  }

  // 205. Attention Receipt Modal
  if (showAttentionReceipt) {
    AlertDialog(
      onDismissRequest = { showAttentionReceipt = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = colors.accentGold)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Attention Receipt", fontWeight = FontWeight.Bold, color = colors.primaryText)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Factual summary for this session (no moralizing or shaming):", fontSize = 13.sp, color = colors.secondaryText)
          HorizontalDivider(color = colors.border)
          ReceiptRow(label = "Active Space Time", value = "18 mins")
          ReceiptRow(label = "Whispers Handled", value = "3 conversations")
          ReceiptRow(label = "Saved to Vault", value = "2 inspirational loops")
          ReceiptRow(label = "Priority Updates Seen", value = "4 updates")
        }
      },
      confirmButton = {
        TextButton(onClick = { showAttentionReceipt = false }) {
          Text("Done", color = colors.accentGold, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
  val colors = MaterialTheme.snixly
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, fontSize = 13.sp, color = colors.primaryText)
    Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.accentGold)
  }
}

@Composable
private fun CompassRingTile(
  title: String,
  desc: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val colors = MaterialTheme.snixly
  Surface(
    color = color.copy(alpha = 0.08f),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    modifier = modifier.clickable { onClick() }
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
      Spacer(modifier = Modifier.height(8.dp))
      Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = colors.primaryText))
      Text(desc, style = MaterialTheme.typography.labelSmall.copy(color = colors.secondaryText), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
  }
}

@Composable
private fun CatchUpCardItem(
  badge: String,
  title: String,
  time: String,
  icon: ImageVector,
  onOpen: () -> Unit
) {
  val colors = MaterialTheme.snixly
  Surface(
    color = colors.background,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
    modifier = Modifier.fillMaxWidth().clickable { onOpen() }
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(colors.accentGold.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(18.dp))
      }
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(badge, style = MaterialTheme.typography.labelSmall.copy(color = colors.accentGold, fontWeight = FontWeight.Bold))
          Spacer(modifier = Modifier.width(6.dp))
          Text("• $time", style = MaterialTheme.typography.labelSmall.copy(color = colors.secondaryText))
        }
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = colors.primaryText), maxLines = 2)
      }
      Icon(Icons.Default.ArrowForward, contentDescription = null, tint = colors.secondaryText, modifier = Modifier.size(16.dp))
    }
  }
}

@Composable
private fun AlgorithmDialCard(title: String, value: Float, desc: String) {
  val colors = MaterialTheme.snixly
  var sliderPos by remember { mutableStateOf(value) }
  Surface(
    color = colors.background,
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = colors.primaryText))
        Text("${(sliderPos * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = colors.accentGold))
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(desc, style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText))
      Slider(
        value = sliderPos,
        onValueChange = { sliderPos = it },
        colors = SliderDefaults.colors(
          thumbColor = colors.accentGold,
          activeTrackColor = colors.accentGold,
          inactiveTrackColor = colors.border
        )
      )
    }
  }
}

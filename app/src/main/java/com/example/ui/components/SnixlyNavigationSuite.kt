package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GestureNavSettings
import com.example.model.VaultPurpose
import com.example.ui.theme.*

/**
 * 161. NAV SCRUB & SMART REPEAT-TAP BOTTOM NAVIGATION
 * With Compass Long-Press (162) and Smooth Navigation Transitions
 */
@Composable
fun SnixlySignatureBottomNav(
  currentDestination: String,
  onNavigate: (String) -> Unit,
  onCreateClick: () -> Unit,
  onOpenCompass: () -> Unit,
  gestureSettings: GestureNavSettings = GestureNavSettings(),
  onRepeatTap: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly
  val destinations = listOf(
    Triple("home", "Home", Icons.Outlined.Home to Icons.Filled.Home),
    Triple("explore", "Explore", Icons.Outlined.Search to Icons.Filled.Search),
    Triple("create", "Create", Icons.Outlined.Add to Icons.Filled.Add),
    Triple("loops", "Loops", Icons.Outlined.PlayCircle to Icons.Filled.PlayCircle),
    Triple("whisper", "Whisper", Icons.Outlined.ChatBubbleOutline to Icons.Filled.ChatBubble)
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(64.dp)
      .testTag("signature_bottom_nav"),
    color = colors.surface,
    tonalElevation = 6.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      destinations.forEach { (id, label, iconPair) ->
        val isSelected = currentDestination == id
        val (unselectedIcon, selectedIcon) = iconPair

        if (id == "create") {
          // Central Create Button with Compass Quick Action Hold
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(SnixlyGoldPrimary, SnixlyGoldBright, SnixlyGoldDeep)))
              .pointerInput(Unit) {
                detectTapGestures(
                  onTap = { onCreateClick() },
                  onLongPress = { onOpenCompass() }
                )
              }
              .testTag("nav_create_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Create / Compass Hold",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }
        } else {
          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .pointerInput(id) {
                detectTapGestures(
                  onTap = {
                    if (isSelected) {
                      onRepeatTap(id)
                    } else {
                      onNavigate(id)
                    }
                  },
                  onLongPress = {
                    onOpenCompass()
                  }
                )
              }
              .testTag("nav_tab_$id"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = if (isSelected) selectedIcon else unselectedIcon,
              contentDescription = label,
              tint = if (isSelected) colors.accentGold else colors.secondaryText,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) colors.accentGold else colors.secondaryText
              )
            )
          }
        }
      }
    }
  }
}

/**
 * 163. ONE-HAND ARC RADIAL NAVIGATION
 * Floating Corner Arc when enabled
 */
@Composable
fun OneHandArcOverlay(
  isLeftHandMode: Boolean,
  onNavigate: (String) -> Unit,
  onOpenCompass: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly
  var isExpanded by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    contentAlignment = if (isLeftHandMode) Alignment.BottomStart else Alignment.BottomEnd
  ) {
    if (isExpanded) {
      Surface(
        color = colors.cardBackground,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGold),
        modifier = Modifier.padding(bottom = 70.dp)
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text("One-Hand Arc", style = MaterialTheme.typography.labelSmall.copy(color = colors.accentGold, fontWeight = FontWeight.Bold))
          ArcItem(title = "Home", icon = Icons.Default.Home, onClick = { onNavigate("home"); isExpanded = false })
          ArcItem(title = "Explore", icon = Icons.Default.Search, onClick = { onNavigate("explore"); isExpanded = false })
          ArcItem(title = "Loops", icon = Icons.Default.PlayCircle, onClick = { onNavigate("loops"); isExpanded = false })
          ArcItem(title = "Whisper", icon = Icons.Default.ChatBubble, onClick = { onNavigate("whisper"); isExpanded = false })
          ArcItem(title = "Compass", icon = Icons.Default.Explore, onClick = { onOpenCompass(); isExpanded = false })
        }
      }
    }

    // Trigger Button
    Box(
      modifier = Modifier
        .size(42.dp)
        .clip(CircleShape)
        .background(Brush.linearGradient(listOf(SnixlyGoldPrimary, SnixlyGoldDeep)))
        .clickable { isExpanded = !isExpanded }
        .testTag("one_hand_arc_trigger"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.PanTool,
        contentDescription = "One Hand Arc",
        tint = Color.White,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
private fun ArcItem(title: String, icon: ImageVector, onClick: () -> Unit) {
  val colors = MaterialTheme.snixly
  Row(
    modifier = Modifier
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Icon(icon, contentDescription = null, tint = colors.primaryText, modifier = Modifier.size(18.dp))
    Text(title, color = colors.primaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
  }
}

/**
 * 216. SAVE WITH PURPOSE DIALOG
 * Long-press Save on any post/loop to choose categorized knowledge target
 */
@Composable
fun SaveWithPurposeDialog(
  onDismiss: () -> Unit,
  onSaveToPurpose: (VaultPurpose) -> Unit
) {
  val colors = MaterialTheme.snixly
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Bookmark, contentDescription = null, tint = colors.accentGold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          "Save to Vault with Purpose",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = colors.primaryText
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Organize this insight directly into your Knowledge Vault:", fontSize = 13.sp, color = colors.secondaryText)
        Spacer(modifier = Modifier.height(4.dp))
        VaultPurpose.values().forEach { purpose ->
          Surface(
            color = colors.surfaceVariant,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onSaveToPurpose(purpose)
                onDismiss()
              }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(purpose.label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.primaryText)
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = colors.secondaryText)
      }
    }
  )
}

package com.example.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Post
import com.example.model.ProfileCanvasType
import com.example.model.SpaceLayer
import com.example.model.UserProfile
import com.example.ui.components.SnixlyPostCard
import com.example.ui.theme.*

/**
 * 186-194. MY SPACE SIGNATURE SUITE
 * With Profile Canvas (Classic/Editorial/Minimal/Creator/Showcase), Preview As modes,
 * Space Layers (Public / Connection / Private Owner Workspace), Identity Strip & Ambient Cover.
 */
@Composable
fun ProfileScreen(
  user: UserProfile,
  userPosts: List<Post>,
  isCurrentUser: Boolean = true,
  onBackClick: (() -> Unit)? = null,
  onEditProfileClick: () -> Unit = {},
  onSettingsClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly

  var activeSpaceLayer by remember { mutableStateOf(SpaceLayer.PUBLIC) } // 188. Space Layers
  var profileCanvas by remember { mutableStateOf(ProfileCanvasType.CLASSIC) } // 186. Profile Canvas
  var previewAsMode by remember { mutableStateOf("Me (Owner)") } // 187. Profile Canvas Preview
  var showCanvasCustomizer by remember { mutableStateOf(false) }
  var selectedTab by remember { mutableStateOf(0) }

  val tabs = listOf("Posts", "Loops", "Resparks", "Knowledge Vault")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.background)
      .testTag("profile_screen")
  ) {
    // Top Bar
    Surface(
      color = colors.surface,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (onBackClick != null) {
            IconButton(
              onClick = onBackClick,
              modifier = Modifier.testTag("profile_back_button")
            ) {
              Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = colors.primaryText)
            }
          }
          Text(
            text = if (isCurrentUser) "My Space" else user.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.primaryText,
            modifier = Modifier.padding(start = if (onBackClick == null) 8.dp else 0.dp)
          )
        }

        if (isCurrentUser) {
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = { showCanvasCustomizer = true }) {
              Icon(Icons.Default.Palette, contentDescription = "Profile Canvas", tint = colors.accentGold)
            }
            IconButton(
              onClick = onSettingsClick,
              modifier = Modifier.testTag("settings_button")
            ) {
              Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = colors.primaryText,
                modifier = Modifier.size(24.dp)
              )
            }
          }
        }
      }
    }
    HorizontalDivider(color = colors.border, thickness = 1.dp)

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 24.dp)
    ) {
      // 191. Ambient Cover Banner
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
              Brush.horizontalGradient(
                colors = listOf(SnixlyDarkBackground, SnixlyDarkSurface, SnixlyGoldDeep)
              )
            )
        ) {
          // Decorative Parallax Ring
          Box(
            modifier = Modifier
              .size(160.dp)
              .align(Alignment.TopEnd)
              .offset(x = 40.dp, y = (-20).dp)
              .border(1.dp, colors.accentGold.copy(alpha = 0.3f), CircleShape)
          )

          // 187. Preview As Badge
          if (isCurrentUser) {
            Surface(
              color = Color.Black.copy(alpha = 0.6f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clickable { showCanvasCustomizer = true }
            ) {
              Text(
                text = "Viewing: $previewAsMode ▾",
                style = MaterialTheme.typography.labelSmall.copy(color = SnixlyGoldChampagne, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      // 2. Profile Details & Avatar Overlap
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-40).dp)
        ) {
          // Avatar & Actions
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            // Golden Halo Avatar
            Box(
              modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(SnixlyGoldPrimary, SnixlyGoldSoft)))
                .padding(3.dp)
                .clip(CircleShape)
                .background(colors.surface)
            ) {
              AsyncImage(
                model = user.avatarUrl,
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
              )
            }

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedButton(
                onClick = { showCanvasCustomizer = true },
                shape = RoundedCornerShape(20.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(colors.accentGold, colors.accentGold)))
              ) {
                Icon(Icons.Default.DashboardCustomize, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Canvas", color = colors.accentGold, fontSize = 12.sp)
              }

              Button(
                onClick = onEditProfileClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
              ) {
                Text(if (isCurrentUser) "Edit Space" else "Connect", fontSize = 12.sp, color = Color.White)
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Name & Verification
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = user.name,
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = colors.primaryText
            )
            if (user.isVerified) {
              Spacer(modifier = Modifier.width(6.dp))
              Icon(Icons.Filled.CheckCircle, contentDescription = "Verified", tint = colors.accentGold, modifier = Modifier.size(18.dp))
            }
          }

          Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText
          )

          if (user.bio.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(user.bio, style = MaterialTheme.typography.bodyMedium, color = colors.primaryText)
          }

          // 192. Profile Moments Status Pill
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.accentGold.copy(alpha = 0.2f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Moment: ${user.auraStatus}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = colors.accentGold)
              )
            }
          }

          // 190. Identity Strip (Current Focus, Music, Project)
          Spacer(modifier = Modifier.height(10.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            item {
              IdentityChip(icon = Icons.Default.RocketLaunch, label = "Focus: Snixly 2.0 Studio")
            }
            item {
              IdentityChip(icon = Icons.Default.MusicNote, label = "Ambient: Kyoto Synth Drones")
            }
            item {
              IdentityChip(icon = Icons.Default.Code, label = "Project: Spatial Compose")
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Statistics Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
          ) {
            StatItem(count = user.followersCount.toString(), label = "Followers")
            StatItem(count = user.followingCount.toString(), label = "Following")
            StatItem(count = user.loopsCount.toString(), label = "Loops")
            StatItem(count = "24", label = "Knowledge Vault")
          }

          // 188 & 189. Space Layers Selector (Public / Connection / Private Owner Workspace)
          if (isCurrentUser) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
              color = colors.surfaceVariant,
              shape = RoundedCornerShape(14.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(modifier = Modifier.padding(4.dp)) {
                listOf(
                  SpaceLayer.PUBLIC to "Public Layer",
                  SpaceLayer.CONNECTION to "Connections",
                  SpaceLayer.PRIVATE to "Private Layer 🔒"
                ).forEach { (layer, label) ->
                  val isSelected = activeSpaceLayer == layer
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(10.dp))
                      .background(if (isSelected) colors.accentGold else Color.Transparent)
                      .clickable { activeSpaceLayer = layer }
                      .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = label,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else colors.primaryText
                      )
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 188. Private Workspace View
      if (isCurrentUser && activeSpaceLayer == SpaceLayer.PRIVATE) {
        item {
          Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Owner-Only Private Workspace", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
            Text("Your drafts, scheduled releases, and private chat bookmarks are protected here.", fontSize = 12.sp, color = colors.secondaryText)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              PrivateWorkspaceTile("Drafts (3)", Icons.Default.EditNote, Modifier.weight(1f))
              PrivateWorkspaceTile("Scheduled (2)", Icons.Default.Schedule, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              PrivateWorkspaceTile("Memory Vault", Icons.Default.Bookmark, Modifier.weight(1f))
              PrivateWorkspaceTile("Saved in Chats", Icons.Default.ChatBubble, Modifier.weight(1f))
            }
          }
        }
      } else {
        // 3. Tab Layers: Posts, Loops, Resparks, Vault
        item {
          Surface(
            color = colors.surface,
            modifier = Modifier.fillMaxWidth().offset(y = (-20).dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier
                    .clickable { selectedTab = index }
                    .padding(vertical = 12.dp, horizontal = 8.dp)
                ) {
                  Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) colors.primaryText else colors.secondaryText
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Box(
                    modifier = Modifier
                      .height(2.dp)
                      .width(36.dp)
                      .background(
                        if (isSelected) colors.accentGold else Color.Transparent,
                        RoundedCornerShape(1.dp)
                      )
                  )
                }
              }
            }
          }
        }

        // 4. Space Content List
        items(userPosts) { post ->
          Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).offset(y = (-10).dp)) {
            SnixlyPostCard(
              post = post,
              onLikeToggle = {},
              onVaultToggle = {},
              onResparkToggle = {},
              onCommentClick = {},
              onShareClick = {},
              onAuthorClick = {}
            )
          }
        }
      }
    }
  }

  // 186 & 187. Profile Canvas Customization & Preview As Modal
  if (showCanvasCustomizer) {
    AlertDialog(
      onDismissRequest = { showCanvasCustomizer = false },
      title = { Text("Profile Canvas & View Modes", fontWeight = FontWeight.Bold, color = colors.primaryText) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("186. Profile Canvas Presentation:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.primaryText)
          ProfileCanvasType.values().forEach { canvasType ->
            Surface(
              color = if (profileCanvas == canvasType) colors.accentGold.copy(alpha = 0.2f) else colors.surfaceVariant,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { profileCanvas = canvasType }
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(canvasType.label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.primaryText)
                Text(canvasType.desc, fontSize = 11.sp, color = colors.secondaryText)
              }
            }
          }
          HorizontalDivider(color = colors.border)
          Text("187. Preview Space As:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.primaryText)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("Me (Owner)", "Public", "Connection").forEach { mode ->
              FilterChip(
                selected = previewAsMode == mode,
                onClick = { previewAsMode = mode },
                label = { Text(mode, fontSize = 11.sp) }
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showCanvasCustomizer = false },
          colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
        ) {
          Text("Apply Canvas", color = Color.White)
        }
      },
      containerColor = colors.surface
    )
  }
}

@Composable
private fun IdentityChip(icon: ImageVector, label: String) {
  val colors = MaterialTheme.snixly
  Surface(
    color = colors.surface,
    shape = RoundedCornerShape(20.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(icon, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(14.dp))
      Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colors.primaryText)
    }
  }
}

@Composable
private fun PrivateWorkspaceTile(title: String, icon: ImageVector, modifier: Modifier = Modifier) {
  val colors = MaterialTheme.snixly
  Surface(
    color = colors.surface,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
    modifier = modifier
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(icon, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(20.dp))
      Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
    }
  }
}

@Composable
private fun StatItem(count: String, label: String) {
  val colors = MaterialTheme.snixly
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = count,
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = colors.primaryText
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = colors.secondaryText
    )
  }
}

package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.FlashMoment
import com.example.model.Post
import com.example.model.PostType
import com.example.model.UserProfile
import com.example.ui.theme.*

@Composable
fun SnixlyTopHeader(
  onNotificationsClick: () -> Unit,
  onProfileClick: () -> Unit,
  currentUser: UserProfile,
  hasUnreadSignals: Boolean = true,
  onToggleTheme: (() -> Unit)? = null,
  isDarkTheme: Boolean = false,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(56.dp)
      .testTag("snixly_top_header"),
    color = colors.surface,
    tonalElevation = 0.dp
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentAlignment = Alignment.Center
    ) {
      // Left Brand Title
      Text(
        text = "SNIXLY",
        color = colors.accentGold,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        ),
        modifier = Modifier
          .align(Alignment.CenterStart)
          .testTag("brand_title")
      )

      // Right Action Cluster
      Row(
        modifier = Modifier.align(Alignment.CenterEnd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Theme Quick Toggle
        if (onToggleTheme != null) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .clickable { onToggleTheme() }
              .testTag("theme_toggle_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = "Toggle Theme",
              tint = colors.primaryText,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Signals / Notifications
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable { onNotificationsClick() }
            .testTag("signals_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Signals",
            tint = colors.primaryText,
            modifier = Modifier.size(22.dp)
          )
          if (hasUnreadSignals) {
            Box(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 6.dp, end = 6.dp)
                .size(8.dp)
                .background(SnixlyCrimsonAlert, CircleShape)
            )
          }
        }

        // Profile Avatar with Luxury Gold Halo
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                colors = listOf(SnixlyGoldPrimary, SnixlyGoldSoft)
              )
            )
            .padding(2.dp)
            .clip(CircleShape)
            .clickable { onProfileClick() }
            .testTag("profile_avatar_button"),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = currentUser.avatarUrl,
            contentDescription = "My Space Profile",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
          )
        }
      }
    }
  }
  HorizontalDivider(color = colors.border, thickness = 1.dp)
}

@Composable
fun SnixlyFeedTabs(
  selectedTab: Int,
  onTabSelected: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly

  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(colors.surface)
      .testTag("feed_tabs_row")
  ) {
    listOf("For You", "Following").forEachIndexed { index, title ->
      val isSelected = selectedTab == index
      Box(
        modifier = Modifier
          .weight(1f)
          .clickable { onTabSelected(index) }
          .padding(vertical = 12.dp)
          .testTag("feed_tab_$index"),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 14.sp
            ),
            color = if (isSelected) colors.primaryText else colors.secondaryText
          )
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .height(2.dp)
              .width(if (isSelected) 48.dp else 0.dp)
              .background(
                if (isSelected) colors.accentGold else Color.Transparent,
                RoundedCornerShape(1.dp)
              )
          )
        }
      }
    }
  }
  HorizontalDivider(color = colors.border, thickness = 1.dp)
}

@Composable
fun FlashMomentsRow(
  moments: List<FlashMoment>,
  onMomentClick: (FlashMoment) -> Unit,
  onAddFlashClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    moments.forEach { moment ->
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .clickable {
            if (moment.isOwnAdd) onAddFlashClick() else onMomentClick(moment)
          }
          .testTag("flash_moment_${moment.id}")
      ) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .border(
              width = 2.dp,
              color = if (moment.hasUnseen || moment.isOwnAdd) colors.accentGold else colors.border,
              shape = CircleShape
            )
            .padding(3.dp)
            .clip(CircleShape)
            .background(colors.surfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          if (moment.isOwnAdd) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add Flash",
              tint = colors.accentGold,
              modifier = Modifier.size(24.dp)
            )
          } else {
            AsyncImage(
              model = moment.imageUrl,
              contentDescription = moment.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = moment.title,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
          color = colors.secondaryText,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun QuickShareCard(
  user: UserProfile,
  onPostClick: () -> Unit,
  onPhotoClick: () -> Unit,
  onVideoClick: () -> Unit,
  onPollClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("quick_share_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(colors.border, colors.border)))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Top Input Trigger Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onPostClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        AsyncImage(
          model = user.avatarUrl,
          contentDescription = "User Avatar",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.surfaceVariant)
        )

        Box(
          modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .clip(CircleShape)
            .background(colors.background)
            .border(1.dp, colors.border, CircleShape)
            .padding(horizontal = 16.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          Text(
            text = "Share something...",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.8.dp)
      Spacer(modifier = Modifier.height(10.dp))

      // Action Button Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        QuickActionButton(
          icon = Icons.Outlined.Image,
          label = "Photo",
          onClick = onPhotoClick
        )
        QuickActionButton(
          icon = Icons.Outlined.Videocam,
          label = "Video",
          onClick = onVideoClick
        )
        QuickActionButton(
          icon = Icons.Outlined.Poll,
          label = "Poll",
          onClick = onPollClick
        )
      }
    }
  }
}

@Composable
private fun QuickActionButton(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit
) {
  val colors = MaterialTheme.snixly

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = colors.accentGold,
      modifier = Modifier.size(18.dp)
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
      color = colors.secondaryText
    )
  }
}

@Composable
fun SnixlyPostCard(
  post: Post,
  onLikeToggle: () -> Unit,
  onVaultToggle: () -> Unit,
  onResparkToggle: () -> Unit,
  onCommentClick: () -> Unit,
  onShareClick: () -> Unit,
  onAuthorClick: () -> Unit,
  onPollVote: (Int) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("post_card_${post.id}"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(colors.border, colors.border))),
    elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 1.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Header: Author + Time + Menu
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier
            .clickable { onAuthorClick() },
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          AsyncImage(
            model = post.author.avatarUrl,
            contentDescription = post.author.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
          )
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = post.author.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                ),
                color = colors.primaryText
              )
              if (post.author.isVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Filled.CheckCircle,
                  contentDescription = "Verified",
                  tint = colors.accentGold,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
            Text(
              text = buildString {
                append(post.timeAgo)
                if (!post.location.isNullOrEmpty()) {
                  append(" • ")
                  append(post.location)
                }
              },
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = colors.secondaryText
            )
          }
        }

        IconButton(
          onClick = {},
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More",
            tint = colors.secondaryText,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Post Content Text
      if (post.content.isNotEmpty()) {
        Text(
          text = post.content,
          style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, lineHeight = 20.sp),
          color = colors.primaryText,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
      }

      // Media / Poll
      if (post.type == PostType.IMAGE && post.imageUrl != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(colors.surfaceVariant)
        ) {
          AsyncImage(
            model = post.imageUrl,
            contentDescription = "Post Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )

          if (post.categoryTag != null) {
            Box(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(
                  color = colors.surface.copy(alpha = 0.9f),
                  shape = RoundedCornerShape(8.dp)
                )
                .border(
                  width = 1.dp,
                  color = colors.border.copy(alpha = 0.6f),
                  shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = post.categoryTag.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  fontSize = 10.sp
                ),
                color = colors.accentGold
              )
            }
          }
        }
      } else if (post.type == PostType.POLL && post.pollOptions != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          post.pollOptions.forEach { option ->
            val isSelected = post.userSelectedPollOption == option.id
            val progress = option.percent / 100f
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surfaceVariant)
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) colors.accentGold else colors.border,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable { onPollVote(option.id) }
            ) {
              Box(
                modifier = Modifier
                  .fillMaxHeight()
                  .fillMaxWidth(progress)
                  .background(
                    if (isSelected) colors.accentGold.copy(alpha = 0.25f) else colors.border.copy(alpha = 0.35f)
                  )
              )
              Row(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = option.text,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp
                  ),
                  color = colors.primaryText
                )
                Text(
                  text = "${option.percent}%",
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  ),
                  color = colors.secondaryText
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = colors.border, thickness = 0.8.dp)

      // Bottom Action Bar: Like / Comment / Share / Vault
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Like Action
          Row(
            modifier = Modifier
              .clickable { onLikeToggle() }
              .testTag("post_like_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = "Like",
              tint = if (post.isLiked) colors.accentGold else colors.secondaryText,
              modifier = Modifier.size(22.dp)
            )
            Text(
              text = if (post.likesCount > 999) String.format("%.1fk", post.likesCount / 1000.0) else post.likesCount.toString(),
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
              color = if (post.isLiked) colors.accentGold else colors.primaryText
            )
          }

          // Comment Action
          Row(
            modifier = Modifier
              .clickable { onCommentClick() }
              .testTag("post_comment_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.ChatBubbleOutline,
              contentDescription = "Comment",
              tint = colors.secondaryText,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = post.commentsCount.toString(),
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
              color = colors.primaryText
            )
          }

          // Share Action
          IconButton(
            onClick = onShareClick,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Share,
              contentDescription = "Share",
              tint = colors.secondaryText,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Vault / Bookmark Button
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (post.isVaulted) colors.accentGold.copy(alpha = 0.25f) else colors.surfaceVariant)
            .clickable { onVaultToggle() }
            .testTag("post_vault_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (post.isVaulted) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = "Vault",
            tint = if (post.isVaulted) colors.accentGold else colors.primaryText,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun SnixlyBottomNavigation(
  currentDestination: String,
  onNavigate: (String) -> Unit,
  onCreateClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(64.dp)
      .testTag("bottom_nav_bar"),
    color = colors.surface,
    tonalElevation = 0.dp
  ) {
    Column {
      HorizontalDivider(color = colors.border, thickness = 1.dp)
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Home
        BottomNavItem(
          icon = Icons.Filled.Home,
          label = "Home",
          isSelected = currentDestination == "home",
          onClick = { onNavigate("home") },
          tag = "nav_home"
        )

        // Explore
        BottomNavItem(
          icon = Icons.Outlined.Explore,
          label = "Explore",
          isSelected = currentDestination == "explore",
          onClick = { onNavigate("explore") },
          tag = "nav_explore"
        )

        // Centered Gradient FAB (Create)
        Box(
          modifier = Modifier
            .size(48.dp)
            .shadow(
              elevation = 6.dp,
              shape = RoundedCornerShape(16.dp),
              spotColor = colors.accentGold.copy(alpha = 0.4f)
            )
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(SnixlyGoldPrimary, SnixlyGoldDeep)
              ),
              shape = RoundedCornerShape(16.dp)
            )
            .clickable { onCreateClick() }
            .testTag("nav_create_fab"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Create",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }

        // Loops
        BottomNavItem(
          icon = Icons.Outlined.AllInclusive,
          label = "Loops",
          isSelected = currentDestination == "loops",
          onClick = { onNavigate("loops") },
          tag = "nav_loops"
        )

        // Whisper
        BottomNavItem(
          icon = Icons.Outlined.ChatBubbleOutline,
          label = "Whisper",
          isSelected = currentDestination == "whisper",
          onClick = { onNavigate("whisper") },
          tag = "nav_whisper"
        )
      }
    }
  }
}

@Composable
private fun BottomNavItem(
  icon: ImageVector,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  tag: String
) {
  val colors = MaterialTheme.snixly
  val iconColor by animateColorAsState(
    targetValue = if (isSelected) colors.accentGold else colors.secondaryText,
    label = "icon_color"
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 4.dp)
      .testTag(tag)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = iconColor,
      modifier = Modifier.size(24.dp)
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 10.sp
      ),
      color = iconColor
    )
  }
}

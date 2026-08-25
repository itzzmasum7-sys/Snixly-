package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Send
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
import com.example.model.FlashMoment
import com.example.model.Post
import com.example.model.UserProfile
import com.example.ui.components.FlashMomentsRow
import com.example.ui.components.QuickShareCard
import com.example.ui.components.SnixlyFeedTabs
import com.example.ui.components.SnixlyPostCard
import com.example.ui.theme.*

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.firebase.CommentDto
import com.example.data.firebase.PostRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  posts: List<Post>,
  flashMoments: List<FlashMoment>,
  currentUser: UserProfile,
  onLikeToggle: (String) -> Unit,
  onVaultToggle: (String) -> Unit,
  onResparkToggle: (String) -> Unit,
  onPollVote: (String, Int) -> Unit,
  onAddFlashClick: () -> Unit,
  onQuickPostClick: () -> Unit,
  onUserClick: (UserProfile) -> Unit,
  postRepository: PostRepository? = null,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly
  val coroutineScope = rememberCoroutineScope()

  var selectedTab by remember { mutableStateOf(0) }
  var selectedFilter by remember { mutableStateOf("All") }
  var showCatchMeUpDialog by remember { mutableStateOf(false) }
  var activeCommentPost by remember { mutableStateOf<Post?>(null) }
  var commentInputText by remember { mutableStateOf("") }
  var selectedFlashForPreview by remember { mutableStateOf<FlashMoment?>(null) }

  val filterChips = listOf("All", "Catch Me Up ✨", "Architecture", "Design", "Photography", "Sound")

  val filteredPosts = remember(posts, selectedFilter, selectedTab) {
    var list = if (selectedTab == 1) {
      posts.filter { it.author.id != currentUser.id }
    } else {
      posts
    }
    if (selectedFilter != "All" && selectedFilter != "Catch Me Up ✨") {
      list = list.filter { it.categoryTag?.equals(selectedFilter, ignoreCase = true) == true }
    }
    list
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.background)
      .testTag("home_screen")
  ) {
    // Feed Tabs: For You / Following
    SnixlyFeedTabs(
      selectedTab = selectedTab,
      onTabSelected = { selectedTab = it }
    )

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Flash Moments Row (Horizontal Scroll)
      item {
        FlashMomentsRow(
          moments = flashMoments,
          onMomentClick = { selectedFlashForPreview = it },
          onAddFlashClick = onAddFlashClick
        )
      }

      // 2. Intent / Category Filter Bar
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          filterChips.forEach { chip ->
            val isSelected = selectedFilter == chip
            val isCatchMeUp = chip.contains("Catch Me Up")
            Surface(
              onClick = {
                if (isCatchMeUp) {
                  showCatchMeUpDialog = true
                } else {
                  selectedFilter = chip
                }
              },
              shape = RoundedCornerShape(20.dp),
              color = when {
                isCatchMeUp -> colors.accentGold.copy(alpha = 0.2f)
                isSelected -> colors.accentGold
                else -> colors.surface
              },
              border = if (isSelected || isCatchMeUp) null else CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(colors.border, colors.border))),
              modifier = Modifier.height(34.dp)
            ) {
              Box(
                modifier = Modifier.padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = chip,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected || isCatchMeUp) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                  ),
                  color = when {
                    isCatchMeUp -> colors.accentGold
                    isSelected -> Color.White
                    else -> colors.secondaryText
                  }
                )
              }
            }
          }
        }
      }

      // 3. Quick Share Card
      item {
        QuickShareCard(
          user = currentUser,
          onPostClick = onQuickPostClick,
          onPhotoClick = onQuickPostClick,
          onVideoClick = onQuickPostClick,
          onPollClick = onQuickPostClick
        )
      }

      // 4. Feed Posts List
      items(filteredPosts, key = { it.id }) { post ->
        SnixlyPostCard(
          post = post,
          onLikeToggle = { onLikeToggle(post.id) },
          onVaultToggle = { onVaultToggle(post.id) },
          onResparkToggle = { onResparkToggle(post.id) },
          onCommentClick = { activeCommentPost = post },
          onShareClick = { /* Share dialog */ },
          onAuthorClick = { onUserClick(post.author) },
          onPollVote = { optionId -> onPollVote(post.id, optionId) }
        )
      }

      // Bottom Spacer for clean scrolling
      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Flash Preview Dialog
  selectedFlashForPreview?.let { flash ->
    AlertDialog(
      onDismissRequest = { selectedFlashForPreview = null },
      confirmButton = {
        TextButton(onClick = { selectedFlashForPreview = null }) {
          Text("Close", color = colors.accentGold)
        }
      },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          AsyncImage(
            model = flash.user.avatarUrl,
            contentDescription = flash.user.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
          )
          Column {
            Text(flash.user.name, style = MaterialTheme.typography.titleMedium, color = colors.primaryText)
            Text("Moments • 24h Flash", style = MaterialTheme.typography.labelSmall, color = colors.secondaryText)
          }
        }
      },
      text = {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
        ) {
          AsyncImage(
            model = flash.imageUrl,
            contentDescription = "Flash Moment",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }
      },
      containerColor = colors.surface
    )
  }

  // Catch Me Up AI Briefing Dialog
  if (showCatchMeUpDialog) {
    AlertDialog(
      onDismissRequest = { showCatchMeUpDialog = false },
      confirmButton = {
        Button(
          onClick = { showCatchMeUpDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
        ) {
          Text("Got it", color = Color.White)
        }
      },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "AI Briefing",
            tint = colors.accentGold
          )
          Text("SNIXLY Catch Me Up", style = MaterialTheme.typography.titleMedium, color = colors.primaryText)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Here's what your network created while you were away:",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText
          )
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("• Julian Vance shared an architectural light study from NYC.", style = MaterialTheme.typography.bodyMedium, color = colors.primaryText)
              Text("• Sarah Lin started a community poll on Autumn ceramic finishes.", style = MaterialTheme.typography.bodyMedium, color = colors.primaryText)
              Text("• Elena posted quiet morning reflections from Stockholm.", style = MaterialTheme.typography.bodyMedium, color = colors.primaryText)
            }
          }
        }
      },
      containerColor = colors.surface
    )
  }

  // Comments Bottom Sheet
  activeCommentPost?.let { post ->
    PostCommentsSheet(
      post = post,
      currentUser = currentUser,
      postRepository = postRepository,
      onDismiss = { activeCommentPost = null }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCommentsSheet(
  post: Post,
  currentUser: UserProfile,
  postRepository: PostRepository?,
  onDismiss: () -> Unit
) {
  val colors = MaterialTheme.snixly
  val coroutineScope = rememberCoroutineScope()
  var commentInputText by remember { mutableStateOf("") }
  val liveComments by (postRepository?.observeComments(post.id) ?: kotlinx.coroutines.flow.flowOf(emptyList()))
    .collectAsStateWithLifecycle(initialValue = emptyList())
  var localComments by remember(post.id) {
    mutableStateOf(
      listOf(
        CommentDto(
          id = "c1",
          postId = post.id,
          authorName = "Elena Rostova",
          authorUsername = "elena.vision",
          text = "The symmetry and ambient light in this perspective are phenomenal.",
          createdAt = System.currentTimeMillis() - 1000 * 60 * 15
        )
      )
    )
  }

  val displayComments = if (liveComments.isNotEmpty()) liveComments else localComments

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      Text(
        text = "Responses & Dialogue (${displayComments.size})",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = colors.primaryText
      )
      Spacer(modifier = Modifier.height(12.dp))

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 320.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (displayComments.isEmpty()) {
          item {
            Text(
              "No responses yet. Be the first to share an insight!",
              style = MaterialTheme.typography.bodyMedium,
              color = colors.secondaryText,
              modifier = Modifier.padding(vertical = 16.dp)
            )
          }
        } else {
          items(displayComments) { comment ->
            Row(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.Top,
              modifier = Modifier.fillMaxWidth()
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(colors.accentGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  comment.authorName.take(1).ifBlank { "U" },
                  fontWeight = FontWeight.Bold,
                  color = colors.accentGold
                )
              }
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(comment.authorName.ifBlank { comment.authorUsername }, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
                  Text("@${comment.authorUsername}", style = MaterialTheme.typography.labelSmall, color = colors.secondaryText)
                }
                Text(comment.text, style = MaterialTheme.typography.bodyMedium, color = colors.primaryText)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Add comment input
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = commentInputText,
          onValueChange = { commentInputText = it },
          placeholder = { Text("Write a thoughtful response...", color = colors.secondaryText) },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.primaryText,
            unfocusedTextColor = colors.primaryText,
            focusedBorderColor = colors.accentGold,
            unfocusedBorderColor = colors.border,
            focusedContainerColor = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant
          ),
          singleLine = true
        )
        IconButton(
          onClick = {
            if (commentInputText.isNotBlank()) {
              val text = commentInputText
              commentInputText = ""
              val newComment = CommentDto(
                id = java.util.UUID.randomUUID().toString(),
                postId = post.id,
                authorId = currentUser.id,
                authorName = currentUser.name,
                authorUsername = currentUser.username,
                authorAvatarUrl = currentUser.avatarUrl,
                text = text,
                createdAt = System.currentTimeMillis()
              )
              localComments = localComments + newComment
              coroutineScope.launch {
                postRepository?.addComment(post.id, currentUser, text)
              }
            }
          },
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors.accentGold)
        ) {
          Icon(
            imageVector = Icons.Outlined.Send,
            contentDescription = "Send",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

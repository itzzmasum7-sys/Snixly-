package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PollOption
import com.example.model.Post
import com.example.model.PostType
import com.example.model.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSheet(
  currentUser: UserProfile,
  onDismiss: () -> Unit,
  onPostCreated: (Post) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly
  var postText by remember { mutableStateOf("") }
  var imageUrlInput by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Architecture") }
  var selectedPrivacy by remember { mutableStateOf("Public") }
  var selectedCreationType by remember { mutableStateOf("Post") }

  // Poll options
  var pollOption1 by remember { mutableStateOf("") }
  var pollOption2 by remember { mutableStateOf("") }
  var pollOption3 by remember { mutableStateOf("") }

  val creationTypes = listOf("Post", "Moment", "Loop", "Poll")
  val categories = listOf("Architecture", "Design", "Photography", "Sound", "Minimalism", "Tech")
  val privacyScopes = listOf("Public", "Followers", "Close Circle", "Encrypted")

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    modifier = modifier.testTag("create_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Header: Title & Privacy Scope Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Create with Intent",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = colors.primaryText
        )

        // Privacy scope dropdown button
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = colors.surfaceVariant,
          modifier = Modifier.clickable {
            val nextIndex = (privacyScopes.indexOf(selectedPrivacy) + 1) % privacyScopes.size
            selectedPrivacy = privacyScopes[nextIndex]
          }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Outlined.Lock, contentDescription = "Privacy", tint = colors.accentGold, modifier = Modifier.size(14.dp))
            Text(selectedPrivacy, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
          }
        }
      }

      // Format Selector (Post / Moment / Loop / Poll)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        creationTypes.forEach { type ->
          val isSelected = selectedCreationType == type
          Surface(
            onClick = { selectedCreationType = type },
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) colors.accentGold else colors.surfaceVariant,
            modifier = Modifier.weight(1f).height(38.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = type,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) Color.White else colors.secondaryText
              )
            }
          }
        }
      }

      // Input TextField
      OutlinedTextField(
        value = postText,
        onValueChange = { postText = it },
        placeholder = {
          Text(
            when (selectedCreationType) {
              "Moment" -> "Caption your 24-hour flash moment..."
              "Loop" -> "Describe your ambient video loop..."
              "Poll" -> "Ask your inquiry or prompt for the space..."
              else -> "What are you exploring, designing, or contemplating?"
            },
            color = colors.secondaryText
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = colors.background,
          unfocusedContainerColor = colors.background,
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border,
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText
        )
      )

      // Media / Image URL Input (Optional or required for Moments/Loops)
      OutlinedTextField(
        value = imageUrlInput,
        onValueChange = { imageUrlInput = it },
        placeholder = { Text("Image / Media URL (Optional)", color = colors.secondaryText) },
        leadingIcon = { Icon(Icons.Outlined.Image, contentDescription = null, tint = colors.accentGold) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = colors.background,
          unfocusedContainerColor = colors.background,
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border,
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText
        )
      )

      // If Poll is selected, show Poll Options input
      if (selectedCreationType == "Poll") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Poll Choices", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.secondaryText)
          OutlinedTextField(
            value = pollOption1,
            onValueChange = { pollOption1 = it },
            placeholder = { Text("Option 1 (e.g., Brushed Titanium)", color = colors.secondaryText) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = pollOption2,
            onValueChange = { pollOption2 = it },
            placeholder = { Text("Option 2 (e.g., Raw Ceramic)", color = colors.secondaryText) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = pollOption3,
            onValueChange = { pollOption3 = it },
            placeholder = { Text("Option 3 (Optional)", color = colors.secondaryText) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }

      // Category Pill selector
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Category Curation", style = MaterialTheme.typography.labelSmall, color = colors.secondaryText)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          categories.take(4).forEach { cat ->
            val isSelected = selectedCategory == cat
            Surface(
              onClick = { selectedCategory = cat },
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) colors.accentGold.copy(alpha = 0.2f) else colors.surfaceVariant
            ) {
              Text(
                text = cat,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isSelected) colors.accentGold else colors.secondaryText,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      // Publish Action Button
      Button(
        onClick = {
          if (postText.isNotBlank()) {
            val postType = when (selectedCreationType) {
              "Moment" -> PostType.MOMENT_RECAP
              "Loop" -> PostType.LINK_CURATION
              "Poll" -> PostType.POLL
              else -> if (imageUrlInput.isNotBlank()) PostType.IMAGE else PostType.STANDARD
            }

            val builtPollOptions = if (postType == PostType.POLL) {
              listOfNotNull(
                if (pollOption1.isNotBlank()) PollOption(id = 0, text = pollOption1, votes = 0, percent = 0) else null,
                if (pollOption2.isNotBlank()) PollOption(id = 1, text = pollOption2, votes = 0, percent = 0) else null,
                if (pollOption3.isNotBlank()) PollOption(id = 2, text = pollOption3, votes = 0, percent = 0) else null
              )
            } else null

            val newPost = Post(
              author = currentUser,
              timeAgo = "Just now",
              location = currentUser.location,
              content = postText,
              type = postType,
              imageUrl = imageUrlInput.ifBlank { null },
              categoryTag = selectedCategory,
              privacyScope = selectedPrivacy,
              pollOptions = builtPollOptions,
              likesCount = 0,
              commentsCount = 0,
              resparksCount = 0
            )
            onPostCreated(newPost)
            onDismiss()
          }
        },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .padding(bottom = 8.dp)
          .testTag("publish_post_button"),
        enabled = postText.isNotBlank()
      ) {
        Text("Publish to Space", fontWeight = FontWeight.Bold, color = Color.White)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

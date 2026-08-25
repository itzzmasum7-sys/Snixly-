package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.firebase.WhisperConversationDto
import com.example.data.firebase.WhisperMessageType
import com.example.data.firebase.WhisperRepository
import com.example.model.PeopleHalo
import com.example.model.PresencePrivacy
import com.example.model.UserProfile
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 169-179. WHISPER PEOPLE-FIRST SUITE
 * With Conversation Aura, People Halo, Whisper Peek, Presence Privacy, and Security Shield.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhisperScreen(
  whisperRepository: WhisperRepository,
  activeUsers: List<UserProfile>,
  currentUser: UserProfile,
  onOpenProfile: (UserProfile) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly
  val coroutineScope = rememberCoroutineScope()
  val realConversations by whisperRepository.observeConversations(currentUser.id).collectAsStateWithLifecycle(initialValue = emptyList())

  var activeConversation by remember { mutableStateOf<WhisperConversationDto?>(null) }
  var showNewWhisperDialog by remember { mutableStateOf(false) }
  var peekConversation by remember { mutableStateOf<WhisperConversationDto?>(null) } // 173. Whisper Peek
  var currentPresence by remember { mutableStateOf(PresencePrivacy.FREE_TO_CHAT) } // 179. Presence Privacy
  var showPresenceDialog by remember { mutableStateOf(false) }
  var selectedHaloFilter by remember { mutableStateOf<PeopleHalo?>(null) } // 172. People Halo Filter

  // If a conversation is active, show the full-screen Whisper Chat Detail Screen
  if (activeConversation != null) {
    WhisperChatDetailScreen(
      conversation = activeConversation!!,
      currentUser = currentUser,
      whisperRepository = whisperRepository,
      onBackClick = { activeConversation = null },
      onOpenProfile = onOpenProfile,
      modifier = modifier
    )
    return
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.background)
      .testTag("whisper_screen")
  ) {
    // Header
    Surface(
      color = colors.surface,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Whisper",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = colors.primaryText
            )
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Private & Ephemeral",
              tint = SnixlyEmeraldActive,
              modifier = Modifier.size(16.dp)
            )
          }
          // Presence Pill Button
          Row(
            modifier = Modifier
              .clickable { showPresenceDialog = true }
              .padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = currentPresence.label,
              style = MaterialTheme.typography.labelSmall.copy(color = colors.accentGold, fontWeight = FontWeight.SemiBold)
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(14.dp))
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          IconButton(
            onClick = { showNewWhisperDialog = true },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(colors.surfaceVariant)
              .testTag("new_whisper_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.Edit,
              contentDescription = "New Whisper",
              tint = colors.accentGold,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
    HorizontalDivider(color = colors.border, thickness = 1.dp)

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 172. People Halo Filter Row
      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          item {
            FilterChip(
              selected = selectedHaloFilter == null,
              onClick = { selectedHaloFilter = null },
              label = { Text("All Circles") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = colors.accentGold,
                selectedLabelColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            )
          }
          items(PeopleHalo.values().filter { it != PeopleHalo.NONE }) { halo ->
            val isSelected = selectedHaloFilter == halo
            FilterChip(
              selected = isSelected,
              onClick = { selectedHaloFilter = if (isSelected) null else halo },
              label = { Text(halo.label) },
              leadingIcon = {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .background(Color(halo.colorHex), CircleShape)
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(halo.colorHex),
                selectedLabelColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            )
          }
        }
      }

      // Active People & Auras Row
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Active Aura Circles",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.secondaryText
          )
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(activeUsers.filter { it.id != currentUser.id }) { user ->
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                  .width(68.dp)
                  .clickable {
                    coroutineScope.launch {
                      val result = whisperRepository.getOrCreateDirectConversation(currentUser, user)
                      result.getOrNull()?.let { conv ->
                        activeConversation = conv
                      }
                    }
                  }
              ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                  // Luxury Gold Halo
                  AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = user.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                      .size(54.dp)
                      .clip(CircleShape)
                      .border(2.dp, colors.accentGold, CircleShape)
                  )
                  Box(
                    modifier = Modifier
                      .size(14.dp)
                      .background(SnixlyEmeraldActive, CircleShape)
                      .border(2.dp, colors.surface, CircleShape)
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = user.name.split(" ").first(),
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                  color = colors.primaryText,
                  maxLines = 1
                )
              }
            }
          }
        }
      }

      // Security Notice Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = colors.accentGold.copy(alpha = 0.15f)),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(colors.accentGold.copy(alpha = 0.4f), colors.accentGold.copy(alpha = 0.4f))))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Security,
              contentDescription = "Privacy Shield",
              tint = colors.accentGold,
              modifier = Modifier.size(24.dp)
            )
            Column {
              Text(
                text = "Cloud-Protected Conversations",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.accentGold
              )
              Text(
                text = "End-to-end cloud protection. Long-press any chat for Whisper Peek.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText
              )
            }
          }
        }
      }

      // Conversations List
      if (realConversations.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(colors.border, colors.border))),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .background(colors.accentGold.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(28.dp))
              }
              Spacer(modifier = Modifier.height(14.dp))
              Text("No Whispers Yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
              Spacer(modifier = Modifier.height(4.dp))
              Text("Start a private conversation or drop an ephemeral thought to your circle.", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = { showNewWhisperDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
              ) {
                Text("Start a Whisper", color = Color.White)
              }
            }
          }
        }
      } else {
        items(realConversations) { conv ->
          val targetId = conv.participantIds.firstOrNull { it != currentUser.id } ?: conv.participantIds.firstOrNull() ?: ""
          val pMap = conv.participantProfiles[targetId]
          val pName = pMap?.get("name") as? String ?: "Whisper Member"
          val pAvatar = pMap?.get("avatarUrl") as? String ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400"
          val pIsOnline = pMap?.get("isOnline") as? Boolean ?: false
          val unread = conv.unreadCounts[currentUser.id]?.toInt() ?: 0

          val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
          val timeStr = remember(conv.lastMessageTimestamp) { timeFormatter.format(Date(conv.lastMessageTimestamp)) }

          // 173. Whisper Card with Pointer Tap & Long-Press for Whisper Peek
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .pointerInput(conv.id) {
                detectTapGestures(
                  onTap = { activeConversation = conv },
                  onLongPress = { peekConversation = conv }
                )
              }
              .testTag("whisper_conv_${conv.id}"),
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
              // Participant Avatar with Ring
              Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                  model = pAvatar,
                  contentDescription = pName,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, colors.accentGold, CircleShape)
                )
                if (pIsOnline) {
                  Box(
                    modifier = Modifier
                      .size(12.dp)
                      .background(SnixlyEmeraldActive, CircleShape)
                      .border(2.dp, colors.surface, CircleShape)
                  )
                }
              }

              // Message Info
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                      text = pName,
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                      color = colors.primaryText
                    )
                    // 172. People Halo indicator
                    Box(modifier = Modifier.size(6.dp).background(colors.accentGold, CircleShape))
                  }
                  Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.secondaryText
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = conv.lastMessage,
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                  color = if (unread > 0) colors.primaryText else colors.secondaryText,
                  fontWeight = if (unread > 0) FontWeight.SemiBold else FontWeight.Normal,
                  maxLines = 1
                )
              }

              // Unread badge
              if (unread > 0) {
                Box(
                  modifier = Modifier
                    .size(22.dp)
                    .background(colors.accentGold, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = unread.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // 173. Whisper Peek Modal
  peekConversation?.let { conv ->
    val targetId = conv.participantIds.firstOrNull { it != currentUser.id } ?: ""
    val pMap = conv.participantProfiles[targetId]
    val pName = pMap?.get("name") as? String ?: "Whisper Member"
    val pAvatar = pMap?.get("avatarUrl") as? String ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400"

    AlertDialog(
      onDismissRequest = { peekConversation = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          AsyncImage(
            model = pAvatar,
            contentDescription = null,
            modifier = Modifier.size(36.dp).clip(CircleShape)
          )
          Column {
            Text("Whisper Peek: $pName", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
            Text("Secure preview", style = MaterialTheme.typography.labelSmall.copy(color = colors.accentGold))
          }
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Surface(
            color = colors.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text("Latest Message:", style = MaterialTheme.typography.labelSmall.copy(color = colors.secondaryText))
              Spacer(modifier = Modifier.height(4.dp))
              Text(conv.lastMessage, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.primaryText)
            }
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = { peekConversation = null }) { Text("Mark Read", color = colors.accentGold) }
            TextButton(onClick = { peekConversation = null }) { Text("Mute 🔕", color = colors.secondaryText) }
            TextButton(onClick = { peekConversation = null }) { Text("Pin 📌", color = colors.secondaryText) }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            activeConversation = conv
            peekConversation = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
        ) {
          Text("Open Conversation", color = Color.White)
        }
      },
      containerColor = colors.surface
    )
  }

  // 179. Presence Privacy Selection Dialog
  if (showPresenceDialog) {
    AlertDialog(
      onDismissRequest = { showPresenceDialog = false },
      title = { Text("Whisper Presence Privacy", fontWeight = FontWeight.Bold, color = colors.primaryText) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Control how your active status appears to your Circles:", fontSize = 13.sp, color = colors.secondaryText)
          PresencePrivacy.values().forEach { presence ->
            Surface(
              color = if (currentPresence == presence) colors.accentGold.copy(alpha = 0.2f) else colors.surfaceVariant,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  currentPresence = presence
                  showPresenceDialog = false
                }
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(presence.label, fontWeight = FontWeight.Bold, color = colors.primaryText)
                Text(presence.iconDesc, fontSize = 12.sp, color = colors.secondaryText)
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showPresenceDialog = false }) { Text("Close", color = colors.accentGold) }
      },
      containerColor = colors.surface
    )
  }

  // New Whisper User Selector Dialog
  if (showNewWhisperDialog) {
    AlertDialog(
      onDismissRequest = { showNewWhisperDialog = false },
      title = { Text("Start a New Whisper", fontWeight = FontWeight.Bold, color = colors.primaryText) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Select an active member from your circle:", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
          LazyColumn(modifier = Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(activeUsers.filter { it.id != currentUser.id }) { user ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    showNewWhisperDialog = false
                    coroutineScope.launch {
                      val res = whisperRepository.getOrCreateDirectConversation(currentUser, user)
                      res.getOrNull()?.let { activeConversation = it }
                    }
                  }
                  .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                AsyncImage(
                  model = user.avatarUrl,
                  contentDescription = user.name,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                )
                Column {
                  Text(user.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.primaryText)
                  Text("@${user.username}", style = MaterialTheme.typography.labelSmall, color = colors.secondaryText)
                }
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showNewWhisperDialog = false }) { Text("Close", color = colors.secondaryText) }
      },
      containerColor = colors.surface
    )
  }
}

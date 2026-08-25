package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.firebase.*
import com.example.model.UserProfile
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.FileMetadata
import com.example.util.FileUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhisperChatDetailScreen(
  conversation: WhisperConversationDto,
  currentUser: UserProfile,
  whisperRepository: WhisperRepository,
  onBackClick: () -> Unit,
  onOpenProfile: (UserProfile) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current
  val listState = rememberLazyListState()

  // Real-time messages stream
  val messages by whisperRepository.observeMessages(conversation.id).collectAsStateWithLifecycle(initialValue = emptyList())

  // Target User info from participantProfiles map
  val targetUserId = conversation.participantIds.firstOrNull { it != currentUser.id } ?: conversation.participantIds.firstOrNull() ?: ""
  val targetProfileMap = conversation.participantProfiles[targetUserId]
  val targetName = targetProfileMap?.get("name") as? String ?: "Whisper Member"
  val targetUsername = targetProfileMap?.get("username") as? String ?: "snixly_user"
  val targetAvatar = targetProfileMap?.get("avatarUrl") as? String ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400"
  val targetIsOnline = targetProfileMap?.get("isOnline") as? Boolean ?: true

  // Composer & UI State
  var messageText by remember { mutableStateOf("") }
  var showAttachmentSheet by remember { mutableStateOf(false) }
  var showEmojiDrawer by remember { mutableStateOf(false) }
  var showDetailsModal by remember { mutableStateOf(false) }
  var showCustomizationModal by remember { mutableStateOf(false) }
  var showMediaViewerUrl by remember { mutableStateOf<String?>(null) }
  var replyingToMessage by remember { mutableStateOf<WhisperMessageDto?>(null) }
  var activeMessageMenu by remember { mutableStateOf<WhisperMessageDto?>(null) }

  // Attachment Staging & Preview State
  var stagedAttachments by remember { mutableStateOf<List<FileMetadata>>(emptyList()) }
  var isFromCameraCapture by remember { mutableStateOf(false) }
  var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

  // Search State
  var isSearchActive by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }

  // Voice recording State
  var isRecordingVoice by remember { mutableStateOf(false) }
  var recordingSeconds by remember { mutableStateOf(0) }

  // Poll & Drop Creators State
  var showPollCreator by remember { mutableStateOf(false) }
  var pollQuestionText by remember { mutableStateOf("") }
  var pollOption1 by remember { mutableStateOf("") }
  var pollOption2 by remember { mutableStateOf("") }
  var pollOption3 by remember { mutableStateOf("") }

  var showDropCreator by remember { mutableStateOf(false) }
  var dropThoughtText by remember { mutableStateOf("") }
  var dropExpiryMode by remember { mutableStateOf("24 Hours") }

  // Permission state message
  val snackbarHostState = remember { SnackbarHostState() }

  // Activity Result Launchers
  // 1. Photos (Multiple or single)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents()
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      val metaList = uris.map { FileUtils.getFileMetadata(context, it) }
      stagedAttachments = stagedAttachments + metaList
      isFromCameraCapture = false
    }
  }

  // 2. Videos
  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      val meta = FileUtils.getFileMetadata(context, uri)
      stagedAttachments = listOf(meta)
      isFromCameraCapture = false
    }
  }

  // 3. Documents / Files
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      val meta = FileUtils.getFileMetadata(context, uri)
      stagedAttachments = listOf(meta)
      isFromCameraCapture = false
    }
  }

  // 4. Audio
  val audioPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      val meta = FileUtils.getFileMetadata(context, uri)
      stagedAttachments = listOf(meta)
      isFromCameraCapture = false
    }
  }

  // 5. Camera Photo Capture
  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
  ) { success: Boolean ->
    if (success && tempCameraUri != null) {
      val meta = FileUtils.getFileMetadata(context, tempCameraUri!!)
      stagedAttachments = listOf(meta)
      isFromCameraCapture = true
    }
  }

  // Camera Permission Launcher
  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      try {
        val uri = FileUtils.createTempImageUri(context)
        tempCameraUri = uri
        takePictureLauncher.launch(uri)
      } catch (e: Exception) {
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Unable to initialize camera")
        }
      }
    } else {
      coroutineScope.launch {
        snackbarHostState.showSnackbar("Camera permission is required to capture photos")
      }
    }
  }

  fun launchCameraSafely() {
    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
      try {
        val uri = FileUtils.createTempImageUri(context)
        tempCameraUri = uri
        takePictureLauncher.launch(uri)
      } catch (e: Exception) {
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Unable to launch camera")
        }
      }
    } else {
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  // Voice recording timer effect
  LaunchedEffect(isRecordingVoice) {
    if (isRecordingVoice) {
      recordingSeconds = 0
      while (isRecordingVoice) {
        delay(1000)
        recordingSeconds++
      }
    }
  }

  // Auto scroll to bottom when new messages arrive
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
      whisperRepository.markAsRead(conversation.id, currentUser.id)
    }
  }

  // Active theme configuration
  val colors = MaterialTheme.snixly
  val theme = conversation.themeConfig
  val isDarkTheme = colors.isDark ||
      theme.themeName.contains("Obsidian", ignoreCase = true) ||
      theme.themeName.contains("Midnight", ignoreCase = true) ||
      theme.themeName.contains("AMOLED", ignoreCase = true)

  val chatBgColor = when (theme.themeName) {
    "Obsidian Gold" -> Color(0xFF141311)
    "Midnight" -> Color(0xFF0F172A)
    "Aurora" -> Color(0xFF0B192C)
    "Lavender" -> Color(0xFFF7F5FA)
    "Ocean" -> Color(0xFFF0F6FA)
    "Rose" -> Color(0xFFFAF2F4)
    "AMOLED" -> Color(0xFF000000)
    else -> colors.background
  }

  val outgoingBubbleColor = when (theme.themeName) {
    "Obsidian Gold" -> SnixlyGoldDeep
    "Midnight" -> Color(0xFF2563EB)
    "Aurora" -> Color(0xFF0D9488)
    "Lavender" -> Color(0xFF8B5CF6)
    "Ocean" -> Color(0xFF0284C7)
    "Rose" -> Color(0xFFE11D48)
    else -> colors.accentGold
  }

  val incomingBubbleColor = if (isDarkTheme) colors.surfaceVariant else colors.surface

  // Send Text Message
  fun sendTextMessage() {
    val textToSend = messageText.trim()
    if (textToSend.isBlank()) return

    val currentReply = replyingToMessage?.let {
      ReplyPreviewDto(
        messageId = it.id,
        senderName = it.senderName,
        text = it.text.ifBlank { "[${it.type}]" },
        type = it.type
      )
    }

    val msg = WhisperMessageDto(
      conversationId = conversation.id,
      senderId = currentUser.id,
      senderName = currentUser.name,
      senderAvatarUrl = currentUser.avatarUrl,
      type = WhisperMessageType.TEXT.name,
      text = textToSend,
      replyTo = currentReply,
      deliveryStatus = DeliveryStatus.SENT.name,
      createdAt = System.currentTimeMillis()
    )

    coroutineScope.launch {
      whisperRepository.sendMessage(conversation.id, msg, conversation.participantIds)
    }
    messageText = ""
    replyingToMessage = null
    showEmojiDrawer = false
  }

  // Upload & Send Staged Attachments
  fun sendStagedAttachments(caption: String) {
    val attachmentsToSend = stagedAttachments
    stagedAttachments = emptyList()
    isFromCameraCapture = false

    val currentReply = replyingToMessage?.let {
      ReplyPreviewDto(
        messageId = it.id,
        senderName = it.senderName,
        text = it.text.ifBlank { "[${it.type}]" },
        type = it.type
      )
    }
    replyingToMessage = null

    attachmentsToSend.forEachIndexed { index, meta ->
      val messageId = UUID.randomUUID().toString()
      val msgType = when {
        meta.mimeType.startsWith("image/") -> WhisperMessageType.IMAGE
        meta.mimeType.startsWith("video/") -> WhisperMessageType.VIDEO
        meta.mimeType.startsWith("audio/") -> WhisperMessageType.MUSIC
        else -> WhisperMessageType.FILE
      }

      val itemCaption = if (index == 0) caption else ""

      val initialMsg = WhisperMessageDto(
        id = messageId,
        conversationId = conversation.id,
        senderId = currentUser.id,
        senderName = currentUser.name,
        senderAvatarUrl = currentUser.avatarUrl,
        type = msgType.name,
        text = itemCaption,
        mediaUrl = meta.uri.toString(),
        fileName = meta.fileName,
        fileSizeBytes = meta.fileSize,
        mimeType = meta.mimeType,
        mediaWidth = meta.width,
        mediaHeight = meta.height,
        voiceDurationSeconds = meta.durationSeconds,
        localUri = meta.uri.toString(),
        uploadState = UploadState.UPLOADING.name,
        uploadProgress = 0.1f,
        deliveryStatus = DeliveryStatus.SENDING.name,
        replyTo = currentReply,
        createdAt = System.currentTimeMillis()
      )

      coroutineScope.launch {
        // Send initial placeholder to Firestore for immediate feedback
        whisperRepository.sendMessage(conversation.id, initialMsg, conversation.participantIds)

        // Upload attachment to Firebase Storage
        val uploadResult = whisperRepository.uploadAttachment(
          conversationId = conversation.id,
          messageId = messageId,
          uri = meta.uri,
          fileName = meta.fileName,
          mimeType = meta.mimeType,
          onProgress = { progress ->
            coroutineScope.launch {
              whisperRepository.updateMessage(
                conversationId = conversation.id,
                messageId = messageId,
                updates = mapOf("uploadProgress" to progress)
              )
            }
          }
        )

        if (uploadResult.isSuccess) {
          val (downloadUrl, storagePath) = uploadResult.getOrThrow()
          whisperRepository.updateMessage(
            conversationId = conversation.id,
            messageId = messageId,
            updates = mapOf(
              "mediaUrl" to downloadUrl,
              "storagePath" to storagePath,
              "uploadState" to UploadState.SENT.name,
              "uploadProgress" to 1.0f,
              "deliveryStatus" to DeliveryStatus.SENT.name
            )
          )
        } else {
          whisperRepository.updateMessage(
            conversationId = conversation.id,
            messageId = messageId,
            updates = mapOf(
              "uploadState" to UploadState.FAILED.name,
              "deliveryStatus" to DeliveryStatus.FAILED.name
            )
          )
        }
      }
    }
  }

  // Retry Failed Upload
  fun retryUpload(failedMsg: WhisperMessageDto) {
    val localUriStr = failedMsg.localUri ?: failedMsg.mediaUrl ?: return
    val uri = Uri.parse(localUriStr)
    val fileName = failedMsg.fileName ?: "attachment_${System.currentTimeMillis()}"
    val mimeType = failedMsg.mimeType ?: "application/octet-stream"

    coroutineScope.launch {
      whisperRepository.updateMessage(
        conversationId = conversation.id,
        messageId = failedMsg.id,
        updates = mapOf(
          "uploadState" to UploadState.UPLOADING.name,
          "uploadProgress" to 0.1f,
          "deliveryStatus" to DeliveryStatus.SENDING.name
        )
      )

      val uploadResult = whisperRepository.uploadAttachment(
        conversationId = conversation.id,
        messageId = failedMsg.id,
        uri = uri,
        fileName = fileName,
        mimeType = mimeType,
        onProgress = { progress ->
          coroutineScope.launch {
            whisperRepository.updateMessage(
              conversationId = conversation.id,
              messageId = failedMsg.id,
              updates = mapOf("uploadProgress" to progress)
            )
          }
        }
      )

      if (uploadResult.isSuccess) {
        val (downloadUrl, storagePath) = uploadResult.getOrThrow()
        whisperRepository.updateMessage(
          conversationId = conversation.id,
          messageId = failedMsg.id,
          updates = mapOf(
            "mediaUrl" to downloadUrl,
            "storagePath" to storagePath,
            "uploadState" to UploadState.SENT.name,
            "uploadProgress" to 1.0f,
            "deliveryStatus" to DeliveryStatus.SENT.name
          )
        )
      } else {
        whisperRepository.updateMessage(
          conversationId = conversation.id,
          messageId = failedMsg.id,
          updates = mapOf(
            "uploadState" to UploadState.FAILED.name,
            "deliveryStatus" to DeliveryStatus.FAILED.name
          )
        )
      }
    }
  }

  // Send Voice Note
  fun sendVoiceNote() {
    isRecordingVoice = false
    val recordedSec = if (recordingSeconds > 0) recordingSeconds else 3
    val msg = WhisperMessageDto(
      conversationId = conversation.id,
      senderId = currentUser.id,
      senderName = currentUser.name,
      senderAvatarUrl = currentUser.avatarUrl,
      type = WhisperMessageType.VOICE.name,
      voiceDurationSeconds = recordedSec,
      deliveryStatus = DeliveryStatus.SENT.name,
      createdAt = System.currentTimeMillis()
    )
    coroutineScope.launch {
      whisperRepository.sendMessage(conversation.id, msg, conversation.participantIds)
    }
  }

  // Full Screen Preview Screen when attachments are drafted
  if (stagedAttachments.isNotEmpty()) {
    WhisperMediaPreviewScreen(
      items = stagedAttachments,
      onRemoveItem = { index ->
        stagedAttachments = stagedAttachments.filterIndexed { i, _ -> i != index }
      },
      onAddMore = {
        photoPickerLauncher.launch("image/*")
      },
      onSend = { caption ->
        sendStagedAttachments(caption)
      },
      onCancel = {
        stagedAttachments = emptyList()
        isFromCameraCapture = false
      },
      isCameraCapture = isFromCameraCapture,
      onRetake = {
        stagedAttachments = emptyList()
        launchCameraSafely()
      }
    )
    return
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      // Top Navigation Bar
      Surface(
        color = colors.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Back Button
            IconButton(
              onClick = onBackClick,
              modifier = Modifier.testTag("chat_back_button")
            ) {
              Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = colors.primaryText
              )
            }

            // User Avatar & Name Header
            Row(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { showDetailsModal = true }
                .padding(horizontal = 4.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                  model = targetAvatar,
                  contentDescription = targetName,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(2.dp, colors.accentGold, CircleShape)
                )
                if (targetIsOnline) {
                  Box(
                    modifier = Modifier
                      .size(12.dp)
                      .background(SnixlyEmeraldActive, CircleShape)
                      .border(2.dp, colors.surface, CircleShape)
                  )
                }
              }

              Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text(
                    text = targetName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                Text(
                  text = if (targetIsOnline) "Active Aura ✨" else "Replies Later",
                  style = MaterialTheme.typography.bodySmall,
                  color = if (targetIsOnline) SnixlyEmeraldActive else colors.secondaryText
                )
              }
            }

            // Header Actions
            IconButton(onClick = { isSearchActive = !isSearchActive }) {
              Icon(
                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Outlined.Search,
                contentDescription = "Search messages",
                tint = colors.primaryText
              )
            }

            IconButton(onClick = { showCustomizationModal = true }) {
              Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = "Theme Aura",
                tint = colors.accentGold
              )
            }

            IconButton(onClick = { showDetailsModal = true }) {
              Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "More Options",
                tint = colors.primaryText
              )
            }
          }

          // Search Bar Overlay
          AnimatedVisibility(visible = isSearchActive) {
            Surface(
              color = colors.surfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(20.dp))
                TextField(
                  value = searchQuery,
                  onValueChange = { searchQuery = it },
                  placeholder = { Text("Search whisper conversations...", fontSize = 14.sp, color = colors.secondaryText) },
                  colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = colors.primaryText,
                    unfocusedTextColor = colors.primaryText
                  ),
                  modifier = Modifier.weight(1f),
                  singleLine = true
                )
                if (searchQuery.isNotBlank()) {
                  IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = colors.secondaryText)
                  }
                }
              }
            }
          }
        }
      }
    },
    bottomBar = {
      Column {
        // Quoted Reply Preview Bar
        AnimatedVisibility(visible = replyingToMessage != null) {
          replyingToMessage?.let { replyMsg ->
            Surface(
              color = colors.surfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .background(colors.accentGold, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Replying to ${if (replyMsg.senderId == currentUser.id) "Yourself" else replyMsg.senderName}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.accentGold
                  )
                  Text(
                    text = replyMsg.text.ifBlank { "[${replyMsg.type}]" },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.primaryText
                  )
                }
                IconButton(onClick = { replyingToMessage = null }) {
                  Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(18.dp), tint = colors.secondaryText)
                }
              }
            }
          }
        }

        // Voice Recording Studio Mode Bar
        if (isRecordingVoice) {
          Surface(
            color = colors.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                  modifier = Modifier
                    .size(14.dp)
                    .background(SnixlyCrimsonAlert, CircleShape)
                )
                Text(
                  text = String.format("%02d:%02d", recordingSeconds / 60, recordingSeconds % 60),
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = SnixlyCrimsonAlert
                )
                Text(
                  text = "Recording audio vibe...",
                  style = MaterialTheme.typography.bodySmall,
                  color = colors.secondaryText
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { isRecordingVoice = false }) {
                  Icon(Icons.Outlined.Delete, contentDescription = "Cancel Recording", tint = SnixlyCrimsonAlert)
                }
                IconButton(
                  onClick = { sendVoiceNote() },
                  modifier = Modifier
                    .size(40.dp)
                    .background(colors.accentGold, CircleShape)
                ) {
                  Icon(Icons.Filled.Send, contentDescription = "Send Voice Note", tint = Color.White, modifier = Modifier.size(20.dp))
                }
              }
            }
          }
        } else {
          // Modern Clean Whisper Composer
          Surface(
            color = colors.surface,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                // 1. "+" Attachment Button
                IconButton(
                  onClick = {
                    showEmojiDrawer = false
                    showAttachmentSheet = true
                  },
                  modifier = Modifier
                    .size(42.dp)
                    .background(colors.surfaceVariant, CircleShape)
                    .testTag("attachment_tray_button")
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Attachments",
                    tint = colors.accentGold,
                    modifier = Modifier.size(24.dp)
                  )
                }

                // 2. Input Box (with inner Emoji toggle icon)
                Surface(
                  shape = RoundedCornerShape(24.dp),
                  color = colors.surfaceVariant,
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    colors.border
                  ),
                  modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                  ) {
                    // Emoji Toggle Button
                    IconButton(
                      onClick = { showEmojiDrawer = !showEmojiDrawer },
                      modifier = Modifier.size(32.dp)
                    ) {
                      Icon(
                        imageVector = if (showEmojiDrawer) Icons.Outlined.Keyboard else Icons.Outlined.EmojiEmotions,
                        contentDescription = "Emoji picker",
                        tint = colors.accentGold,
                        modifier = Modifier.size(22.dp)
                      )
                    }

                    // Text Input Field
                    TextField(
                      value = messageText,
                      onValueChange = { messageText = it },
                      placeholder = {
                        Text(
                          text = "Whisper to @$targetUsername...",
                          style = MaterialTheme.typography.bodyMedium,
                          color = colors.secondaryText
                        )
                      },
                      maxLines = 4,
                      colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                      ),
                      modifier = Modifier
                        .weight(1f)
                        .testTag("whisper_message_input")
                    )

                    // 3. Camera Shortcut Button
                    IconButton(
                      onClick = { launchCameraSafely() },
                      modifier = Modifier.size(32.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = "Camera shortcut",
                        tint = colors.accentGold,
                        modifier = Modifier.size(22.dp)
                      )
                    }
                  }
                }

                // 4. Dynamic Action Button: Send (when text exists) OR Mic (when empty)
                if (messageText.isNotBlank()) {
                  IconButton(
                    onClick = { sendTextMessage() },
                    modifier = Modifier
                      .size(44.dp)
                      .background(colors.accentGold, CircleShape)
                      .testTag("whisper_send_button")
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Send,
                      contentDescription = "Send Message",
                      tint = Color.White,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                } else {
                  IconButton(
                    onClick = { isRecordingVoice = true },
                    modifier = Modifier
                      .size(44.dp)
                      .background(colors.surfaceVariant, CircleShape)
                      .testTag("whisper_mic_button")
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.Mic,
                      contentDescription = "Record Voice Note",
                      tint = colors.accentGold,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }
              }

              // Emoji Drawer
              AnimatedVisibility(visible = showEmojiDrawer) {
                WhisperEmojiDrawer(
                  isDarkTheme = isDarkTheme,
                  onEmojiSelected = { emoji ->
                    messageText += emoji
                  }
                )
              }
            }
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(chatBgColor)
        .padding(innerPadding)
        .testTag("whisper_chat_detail_screen")
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
      ) {
        items(messages, key = { it.id }) { msg ->
          val isFromMe = msg.senderId == currentUser.id
          val isHighlighted = searchQuery.isNotBlank() && msg.text.contains(searchQuery, ignoreCase = true)

          WhisperMessageBubble(
            message = msg,
            isFromMe = isFromMe,
            isDarkTheme = isDarkTheme,
            outgoingColor = outgoingBubbleColor,
            incomingColor = incomingBubbleColor,
            bubbleStyle = theme.bubbleStyle,
            isHighlighted = isHighlighted,
            onLongPress = { activeMessageMenu = msg },
            onMediaClick = { url -> showMediaViewerUrl = url },
            onRetryUpload = { retryUpload(msg) },
            onPollVote = { optIdx ->
              coroutineScope.launch {
                whisperRepository.votePoll(conversation.id, msg.id, optIdx, currentUser.id)
              }
            },
            onReplyClick = { refId ->
              val idx = messages.indexOfFirst { it.id == refId }
              if (idx >= 0) coroutineScope.launch { listState.animateScrollToItem(idx) }
            },
            onReactionClick = { emoji ->
              coroutineScope.launch {
                whisperRepository.toggleReaction(conversation.id, msg.id, currentUser.id, emoji)
              }
            }
          )
        }
      }
    }
  }

  // Real Attachment Sheet Bottom Sheet
  if (showAttachmentSheet) {
    WhisperAttachmentSheet(
      isDarkTheme = isDarkTheme,
      onDismiss = { showAttachmentSheet = false },
      onPickPhotos = {
        photoPickerLauncher.launch("image/*")
      },
      onPickVideos = {
        videoPickerLauncher.launch("video/*")
      },
      onLaunchCamera = {
        launchCameraSafely()
      },
      onPickDocuments = {
        filePickerLauncher.launch("*/*")
      },
      onPickAudio = {
        audioPickerLauncher.launch("audio/*")
      },
      onShareLocation = {
        val locMsg = WhisperMessageDto(
          conversationId = conversation.id,
          senderId = currentUser.id,
          senderName = currentUser.name,
          senderAvatarUrl = currentUser.avatarUrl,
          type = WhisperMessageType.LOCATION.name,
          locationLat = 35.6762,
          locationLng = 139.6503,
          locationTitle = "Kyoto Arts District Studio",
          deliveryStatus = DeliveryStatus.SENT.name,
          createdAt = System.currentTimeMillis()
        )
        coroutineScope.launch {
          whisperRepository.sendMessage(conversation.id, locMsg, conversation.participantIds)
        }
      },
      onShareContact = {
        val contactMsg = WhisperMessageDto(
          conversationId = conversation.id,
          senderId = currentUser.id,
          senderName = currentUser.name,
          senderAvatarUrl = currentUser.avatarUrl,
          type = WhisperMessageType.CONTACT.name,
          contactName = "Elena Vance (Curator)",
          contactPhone = "+1 (555) 382-9901",
          deliveryStatus = DeliveryStatus.SENT.name,
          createdAt = System.currentTimeMillis()
        )
        coroutineScope.launch {
          whisperRepository.sendMessage(conversation.id, contactMsg, conversation.participantIds)
        }
      },
      onCreatePoll = {
        showPollCreator = true
      },
      onCreateDrop = {
        showDropCreator = true
      }
    )
  }

  // Poll Creator Dialog
  if (showPollCreator) {
    AlertDialog(
      onDismissRequest = { showPollCreator = false },
      title = { Text("Create In-Chat Poll", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = pollQuestionText,
            onValueChange = { pollQuestionText = it },
            label = { Text("Question") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = pollOption1,
            onValueChange = { pollOption1 = it },
            label = { Text("Option 1") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = pollOption2,
            onValueChange = { pollOption2 = it },
            label = { Text("Option 2") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = pollOption3,
            onValueChange = { pollOption3 = it },
            label = { Text("Option 3 (Optional)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (pollQuestionText.isNotBlank() && pollOption1.isNotBlank() && pollOption2.isNotBlank()) {
              val options = mutableListOf(
                mapOf("id" to 0, "text" to pollOption1, "votes" to 0, "percent" to 0, "voterIds" to emptyList<String>()),
                mapOf("id" to 1, "text" to pollOption2, "votes" to 0, "percent" to 0, "voterIds" to emptyList<String>())
              )
              if (pollOption3.isNotBlank()) {
                options.add(mapOf("id" to 2, "text" to pollOption3, "votes" to 0, "percent" to 0, "voterIds" to emptyList<String>()))
              }

              val pollMsg = WhisperMessageDto(
                conversationId = conversation.id,
                senderId = currentUser.id,
                senderName = currentUser.name,
                senderAvatarUrl = currentUser.avatarUrl,
                type = WhisperMessageType.POLL.name,
                pollQuestion = pollQuestionText,
                pollOptions = options,
                deliveryStatus = DeliveryStatus.SENT.name,
                createdAt = System.currentTimeMillis()
              )
              coroutineScope.launch {
                whisperRepository.sendMessage(conversation.id, pollMsg, conversation.participantIds)
              }
              showPollCreator = false
              pollQuestionText = ""
              pollOption1 = ""
              pollOption2 = ""
              pollOption3 = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = SnixlyGoldPrimary)
        ) {
          Text("Publish Poll")
        }
      },
      dismissButton = {
        TextButton(onClick = { showPollCreator = false }) { Text("Cancel") }
      }
    )
  }

  // Drop Creator Dialog
  if (showDropCreator) {
    AlertDialog(
      onDismissRequest = { showDropCreator = false },
      title = { Text("Send Whisper Drop", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = dropThoughtText,
            onValueChange = { dropThoughtText = it },
            label = { Text("Secret thought or reflection...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
          )
          Text("Disappears after: $dropExpiryMode", style = MaterialTheme.typography.bodySmall, color = SnixlyGoldPrimary)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (dropThoughtText.isNotBlank()) {
              val dropMsg = WhisperMessageDto(
                conversationId = conversation.id,
                senderId = currentUser.id,
                senderName = currentUser.name,
                senderAvatarUrl = currentUser.avatarUrl,
                type = WhisperMessageType.DROP.name,
                text = dropThoughtText,
                isEphemeral = true,
                expiresAt = System.currentTimeMillis() + 86400000L,
                deliveryStatus = DeliveryStatus.SENT.name,
                createdAt = System.currentTimeMillis()
              )
              coroutineScope.launch {
                whisperRepository.sendMessage(conversation.id, dropMsg, conversation.participantIds)
              }
              showDropCreator = false
              dropThoughtText = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = SnixlyGoldPrimary)
        ) {
          Text("Drop into Whisper")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDropCreator = false }) { Text("Cancel") }
      }
    )
  }

  // Message Actions Context Menu
  activeMessageMenu?.let { msg ->
    ModalBottomSheet(
      onDismissRequest = { activeMessageMenu = null },
      containerColor = colors.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp)
      ) {
        // Quick Reaction Emoji Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          listOf("❤️", "🔥", "😂", "😮", "😢", "👍").forEach { emoji ->
            Surface(
              shape = CircleShape,
              color = colors.surfaceVariant,
              modifier = Modifier
                .size(44.dp)
                .clickable {
                  coroutineScope.launch {
                    whisperRepository.toggleReaction(conversation.id, msg.id, currentUser.id, emoji)
                  }
                  activeMessageMenu = null
                }
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, fontSize = 22.sp)
              }
            }
          }
        }

        HorizontalDivider(color = colors.border, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

        // Action Options List
        ContextActionRow(icon = Icons.Outlined.Reply, label = "Reply", tint = colors.primaryText) {
          replyingToMessage = msg
          activeMessageMenu = null
        }

        ContextActionRow(icon = Icons.Outlined.ContentCopy, label = "Copy Text", tint = colors.primaryText) {
          activeMessageMenu = null
        }

        ContextActionRow(
          icon = Icons.Outlined.PushPin,
          label = if (msg.isPinned) "Unpin Message" else "Pin to Top",
          tint = colors.primaryText
        ) {
          coroutineScope.launch {
            whisperRepository.togglePinMessage(conversation.id, msg.id, msg.isPinned)
          }
          activeMessageMenu = null
        }

        if (msg.senderId == currentUser.id) {
          ContextActionRow(
            icon = Icons.Outlined.Delete,
            label = "Delete for Everyone",
            tint = SnixlyCrimsonAlert
          ) {
            coroutineScope.launch {
              whisperRepository.deleteMessageForEveryone(conversation.id, msg.id, msg.senderId, currentUser.id)
            }
            activeMessageMenu = null
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // Conversation Details Modal
  if (showDetailsModal) {
    ModalBottomSheet(
      onDismissRequest = { showDetailsModal = false },
      containerColor = colors.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp)
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          AsyncImage(
            model = targetAvatar,
            contentDescription = targetName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(72.dp)
              .clip(CircleShape)
              .border(2.dp, colors.accentGold, CircleShape)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(targetName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
          Text("@$targetUsername", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
        }

        Spacer(modifier = Modifier.height(20.dp))

        ContextActionRow(icon = Icons.Outlined.Palette, label = "Customize Chat Theme & Aura", tint = colors.primaryText) {
          showDetailsModal = false
          showCustomizationModal = true
        }

        ContextActionRow(icon = Icons.Outlined.Lock, label = "Zero-Knowledge Protection Info", tint = colors.primaryText) {
          showDetailsModal = false
        }

        ContextActionRow(icon = Icons.Outlined.Block, label = "Block @$targetUsername", tint = SnixlyCrimsonAlert) {
          showDetailsModal = false
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Chat Customization Modal
  if (showCustomizationModal) {
    ModalBottomSheet(
      onDismissRequest = { showCustomizationModal = false },
      containerColor = colors.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp)
      ) {
        Text("Customize Whisper Atmosphere", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Aura Palette", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = colors.secondaryText)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          val themesList = listOf("Warm Pearl", "Obsidian Gold", "Midnight", "Aurora", "Lavender", "Ocean", "Rose", "AMOLED")
          items(themesList) { tName ->
            FilterChip(
              selected = theme.themeName == tName,
              onClick = {
                coroutineScope.launch {
                  whisperRepository.updateThemeConfig(conversation.id, theme.copy(themeName = tName))
                }
              },
              label = { Text(tName) }
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Bubble Typography & Shape", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = colors.secondaryText)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          listOf("Rounded Glass", "Crisp Compact", "Soft Pearl").forEach { bStyle ->
            FilterChip(
              selected = theme.bubbleStyle == bStyle,
              onClick = {
                coroutineScope.launch {
                  whisperRepository.updateThemeConfig(conversation.id, theme.copy(bubbleStyle = bStyle))
                }
              },
              label = { Text(bStyle) }
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Full-Screen Image Viewer
  showMediaViewerUrl?.let { mediaUrl ->
    WhisperFullScreenImageViewer(
      imageUrl = mediaUrl,
      onDismiss = { showMediaViewerUrl = null }
    )
  }
}

@Composable
fun WhisperMessageBubble(
  message: WhisperMessageDto,
  isFromMe: Boolean,
  isDarkTheme: Boolean,
  outgoingColor: Color,
  incomingColor: Color,
  bubbleStyle: String,
  isHighlighted: Boolean,
  onLongPress: () -> Unit,
  onMediaClick: (String) -> Unit,
  onRetryUpload: () -> Unit,
  onPollVote: (Int) -> Unit,
  onReplyClick: (String) -> Unit,
  onReactionClick: (String) -> Unit
) {
  val colors = MaterialTheme.snixly
  val context = LocalContext.current
  val cornerRadius = when (bubbleStyle) {
    "Crisp Compact" -> RoundedCornerShape(10.dp)
    "Soft Pearl" -> RoundedCornerShape(20.dp)
    else -> RoundedCornerShape(18.dp)
  }

  val bubbleShape = if (isFromMe) {
    cornerRadius.copy(bottomEnd = androidx.compose.foundation.shape.CornerSize(4.dp))
  } else {
    cornerRadius.copy(bottomStart = androidx.compose.foundation.shape.CornerSize(4.dp))
  }

  val bubbleColor = if (isFromMe) outgoingColor else incomingColor
  val textColor = if (isFromMe) Color.White else colors.primaryText
  val metaColor = if (isFromMe) Color.White.copy(alpha = 0.7f) else colors.secondaryText

  val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
  val timeStr = remember(message.createdAt) { timeFormatter.format(Date(message.createdAt)) }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .pointerInput(Unit) {
        detectTapGestures(
          onLongPress = { onLongPress() }
        )
      },
    contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
  ) {
    Column(
      horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start,
      modifier = Modifier.widthIn(max = 300.dp)
    ) {
      Surface(
        shape = bubbleShape,
        color = if (isHighlighted) SnixlyGoldChampagne.copy(alpha = 0.8f) else bubbleColor,
        shadowElevation = if (isFromMe) 1.dp else 0.5.dp,
        border = if (!isFromMe && !isDarkTheme) androidx.compose.foundation.BorderStroke(1.dp, colors.border) else null
      ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
          // Quoted Reply Header inside bubble
          if (message.replyTo != null) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = (if (isFromMe) Color.Black else Color.Gray).copy(alpha = 0.15f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onReplyClick(message.replyTo.messageId) }
                .padding(bottom = 6.dp)
            ) {
              Row(modifier = Modifier.padding(6.dp)) {
                Box(
                  modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .background(if (isFromMe) Color.White else SnixlyGoldPrimary, RoundedCornerShape(1.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(message.replyTo.senderName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (isFromMe) Color.White else SnixlyGoldPrimary)
                  Text(message.replyTo.text, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), maxLines = 1, color = textColor.copy(alpha = 0.8f))
                }
              }
            }
          }

          // Content by Type
          when (message.type) {
            WhisperMessageType.IMAGE.name -> {
              val mediaTarget = message.mediaUrl ?: message.localUri
              if (mediaTarget != null) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                ) {
                  AsyncImage(
                    model = mediaTarget,
                    contentDescription = "Image attachment",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                      .fillMaxSize()
                      .clickable { onMediaClick(mediaTarget) }
                  )

                  // Uploading Overlay with Progress
                  if (message.uploadState == UploadState.UPLOADING.name) {
                    Box(
                      modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                          progress = { message.uploadProgress.coerceIn(0.1f, 1f) },
                          color = SnixlyGoldPrimary,
                          strokeWidth = 3.dp,
                          modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                          text = "Uploading ${(message.uploadProgress * 100).toInt()}%",
                          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                          color = Color.White
                        )
                      }
                    }
                  } else if (message.uploadState == UploadState.FAILED.name) {
                    Box(
                      modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Button(
                        onClick = onRetryUpload,
                        colors = ButtonDefaults.buttonColors(containerColor = SnixlyCrimsonAlert),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                      ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Retry", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry", fontSize = 12.sp)
                      }
                    }
                  }
                }
                if (message.text.isNotBlank()) {
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(text = message.text, style = MaterialTheme.typography.bodyMedium, color = textColor)
                }
              }
            }

            WhisperMessageType.VIDEO.name -> {
              val mediaTarget = message.mediaUrl ?: message.localUri
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(180.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.Black)
                  .clickable {
                    if (mediaTarget != null) {
                      FileUtils.openFileWithIntent(context, Uri.parse(mediaTarget), "video/*")
                    }
                  },
                contentAlignment = Alignment.Center
              ) {
                if (mediaTarget != null) {
                  AsyncImage(
                    model = mediaTarget,
                    contentDescription = "Video attachment",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )
                }
                Box(
                  modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Filled.PlayArrow, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.size(30.dp))
                }

                // Upload State Overlay
                if (message.uploadState == UploadState.UPLOADING.name) {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                  ) {
                    CircularProgressIndicator(color = SnixlyGoldPrimary, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
                  }
                } else if (message.uploadState == UploadState.FAILED.name) {
                  Button(
                    onClick = onRetryUpload,
                    colors = ButtonDefaults.buttonColors(containerColor = SnixlyCrimsonAlert),
                    shape = RoundedCornerShape(16.dp)
                  ) {
                    Text("Retry Upload", fontSize = 12.sp)
                  }
                }
              }
              if (message.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = message.text, style = MaterialTheme.typography.bodyMedium, color = textColor)
              }
            }

            WhisperMessageType.VOICE.name, WhisperMessageType.MUSIC.name -> {
              WhisperAudioPlayerBubble(
                message = message,
                isFromMe = isFromMe,
                textColor = textColor,
                metaColor = metaColor
              )
              if (message.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = message.text, style = MaterialTheme.typography.bodyMedium, color = textColor)
              }
            }

            WhisperMessageType.FILE.name -> {
              val docUri = message.mediaUrl ?: message.localUri
              val fName = message.fileName ?: "Document"
              val fSizeStr = message.fileSizeBytes?.let { FileUtils.formatFileSize(it) } ?: "Document"

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = (if (isFromMe) Color.Black else Color.Gray).copy(alpha = 0.12f),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    if (docUri != null) {
                      val mime = message.mimeType ?: "application/pdf"
                      FileUtils.openFileWithIntent(context, Uri.parse(docUri), mime)
                    }
                  }
                  .padding(vertical = 2.dp)
              ) {
                Row(
                  modifier = Modifier.padding(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .background(SnixlyGoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = when {
                        fName.endsWith(".pdf", ignoreCase = true) -> Icons.Outlined.PictureAsPdf
                        fName.endsWith(".zip", ignoreCase = true) -> Icons.Outlined.FolderZip
                        fName.endsWith(".doc", ignoreCase = true) || fName.endsWith(".docx", ignoreCase = true) -> Icons.Outlined.Description
                        fName.endsWith(".xls", ignoreCase = true) || fName.endsWith(".xlsx", ignoreCase = true) -> Icons.Outlined.TableChart
                        else -> Icons.Outlined.InsertDriveFile
                      },
                      contentDescription = null,
                      tint = if (isFromMe) Color.White else SnixlyGoldPrimary,
                      modifier = Modifier.size(24.dp)
                    )
                  }

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = fName,
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                      color = textColor,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = fSizeStr,
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                      color = metaColor
                    )
                  }

                  if (message.uploadState == UploadState.FAILED.name) {
                    IconButton(onClick = onRetryUpload, modifier = Modifier.size(28.dp)) {
                      Icon(Icons.Filled.Refresh, contentDescription = "Retry", tint = SnixlyCrimsonAlert)
                    }
                  } else if (message.uploadState == UploadState.UPLOADING.name) {
                    CircularProgressIndicator(color = SnixlyGoldPrimary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                  } else {
                    Icon(Icons.Outlined.Download, contentDescription = "Open", tint = textColor.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                  }
                }
              }

              if (message.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = message.text, style = MaterialTheme.typography.bodyMedium, color = textColor)
              }
            }

            WhisperMessageType.POLL.name -> {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "📊 ${message.pollQuestion ?: "Poll"}",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                  color = textColor
                )
                message.pollOptions?.forEachIndexed { idx, opt ->
                  val optText = opt["text"] as? String ?: ""
                  val optPct = (opt["percent"] as? Number)?.toInt() ?: 0

                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (isFromMe) Color.Black else Color.Gray).copy(alpha = 0.15f),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { onPollVote(idx) }
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(optText, style = MaterialTheme.typography.bodySmall, color = textColor)
                      Text("$optPct%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = textColor)
                    }
                  }
                }
              }
            }

            WhisperMessageType.LOCATION.name -> {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    val lat = message.locationLat ?: 35.6762
                    val lng = message.locationLng ?: 139.6503
                    val mapUri = "geo:$lat,$lng?q=$lat,$lng(${Uri.encode(message.locationTitle ?: "Location")})"
                    FileUtils.openUrlInBrowser(context, mapUri)
                  }
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = if (isFromMe) Color.White else SnixlyGoldPrimary, modifier = Modifier.size(20.dp))
                  Text(message.locationTitle ?: "Shared Location", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = textColor)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Tap to open map coordinates", style = MaterialTheme.typography.labelSmall, color = metaColor)
              }
            }

            WhisperMessageType.CONTACT.name -> {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    message.contactPhone?.let { phone ->
                      val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                      context.startActivity(dialIntent)
                    }
                  }
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(SnixlyGoldPrimary.copy(alpha = 0.2f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Outlined.Person, contentDescription = null, tint = textColor)
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text(message.contactName ?: "Contact", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = textColor)
                  Text(message.contactPhone ?: "", style = MaterialTheme.typography.labelSmall, color = metaColor)
                }
              }
            }

            WhisperMessageType.DROP.name -> {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Outlined.Timer, contentDescription = null, tint = SnixlyGoldPrimary, modifier = Modifier.size(16.dp))
                  Text("Ephemeral Drop", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SnixlyGoldPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = message.text, style = MaterialTheme.typography.bodyMedium, color = textColor)
              }
            }

            else -> {
              // Standard Text Message
              Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
              )
            }
          }

          // Metadata footer: Timestamp & Delivery Status
          Row(
            modifier = Modifier
              .align(Alignment.End)
              .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = timeStr,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = metaColor
            )
            if (isFromMe) {
              when (message.deliveryStatus) {
                DeliveryStatus.SENDING.name -> {
                  CircularProgressIndicator(color = metaColor, strokeWidth = 1.5.dp, modifier = Modifier.size(10.dp))
                }
                DeliveryStatus.FAILED.name -> {
                  Icon(Icons.Default.Warning, contentDescription = "Failed", tint = SnixlyCrimsonAlert, modifier = Modifier.size(12.dp))
                }
                DeliveryStatus.READ.name -> {
                  Icon(Icons.Filled.DoneAll, contentDescription = "Read", tint = SnixlyGoldBright, modifier = Modifier.size(14.dp))
                }
                else -> {
                  Icon(Icons.Filled.DoneAll, contentDescription = "Sent", tint = metaColor, modifier = Modifier.size(14.dp))
                }
              }
            }
          }
        }
      }

      // Reactions Bubble Badge
      if (!message.reactions.isNullOrEmpty()) {
        Surface(
          shape = CircleShape,
          color = if (isDarkTheme) colors.surfaceVariant else colors.surface,
          border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
          shadowElevation = 2.dp,
          modifier = Modifier.padding(top = 2.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            message.reactions.values.distinct().take(3).forEach { emoji ->
              Text(text = emoji, fontSize = 12.sp)
            }
            if (message.reactions.size > 1) {
              Text(
                text = "${message.reactions.size}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = colors.accentGold,
                modifier = Modifier.padding(start = 2.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ContextActionRow(
  icon: ImageVector,
  label: String,
  tint: Color = Color.Unspecified,
  onClick: () -> Unit
) {
  val colors = MaterialTheme.snixly
  val effectiveTint = if (tint != Color.Unspecified) tint else colors.primaryText
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .padding(horizontal = 12.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Icon(imageVector = icon, contentDescription = label, tint = effectiveTint, modifier = Modifier.size(22.dp))
    Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = effectiveTint)
  }
}


package com.example.data.firebase

import java.util.UUID

enum class WhisperMessageType {
  TEXT,
  IMAGE,
  VIDEO,
  VOICE,
  FILE,
  POLL,
  CONTACT,
  LOCATION,
  DROP,
  MUSIC,
  SYSTEM
}

enum class UploadState {
  IDLE,
  PREPARING,
  UPLOADING,
  SENT,
  FAILED,
  CANCELLED
}

enum class DeliveryStatus {
  SENDING,
  SENT,
  DELIVERED,
  READ,
  FAILED
}

data class ReplyPreviewDto(
  val messageId: String = "",
  val senderName: String = "",
  val text: String = "",
  val type: String = WhisperMessageType.TEXT.name
)

data class WhisperMessageDto(
  val id: String = UUID.randomUUID().toString(),
  val conversationId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val senderAvatarUrl: String = "",
  val type: String = WhisperMessageType.TEXT.name,
  val text: String = "",
  val mediaUrl: String? = null,
  val storagePath: String? = null,
  val fileName: String? = null,
  val fileSizeBytes: Long? = null,
  val mimeType: String? = null,
  val mediaWidth: Int? = null,
  val mediaHeight: Int? = null,
  val voiceDurationSeconds: Int? = null,
  val uploadState: String = UploadState.SENT.name,
  val uploadProgress: Float = 1.0f,
  val localUri: String? = null,
  val pollQuestion: String? = null,
  val pollOptions: List<Map<String, Any>>? = null,
  val contactName: String? = null,
  val contactPhone: String? = null,
  val locationLat: Double? = null,
  val locationLng: Double? = null,
  val locationTitle: String? = null,
  val replyTo: ReplyPreviewDto? = null,
  val reactions: Map<String, String>? = null, // uid -> emoji
  val deliveryStatus: String = DeliveryStatus.SENT.name,
  val isEphemeral: Boolean = false,
  val expiresAt: Long? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val isPinned: Boolean = false,
  val isDeletedForEveryone: Boolean = false
)

data class WhisperThemeConfigDto(
  val themeName: String = "Warm Pearl",
  val auraStyle: String = "Golden Halo",
  val bubbleStyle: String = "Rounded Glass",
  val wallpaperId: String = "default",
  val fontSizeScale: Float = 1.0f
)

data class WhisperConversationDto(
  val id: String = "",
  val participantIds: List<String> = emptyList(),
  val participantProfiles: Map<String, Map<String, Any>> = emptyMap(),
  val lastMessage: String = "",
  val lastMessageSenderId: String = "",
  val lastMessageType: String = WhisperMessageType.TEXT.name,
  val lastMessageTimestamp: Long = System.currentTimeMillis(),
  val unreadCounts: Map<String, Long> = emptyMap(),
  val pinnedMessageId: String? = null,
  val isGroup: Boolean = false,
  val groupName: String? = null,
  val groupPhotoUrl: String? = null,
  val groupAdminIds: List<String> = emptyList(),
  val themeConfig: WhisperThemeConfigDto = WhisperThemeConfigDto(),
  val disappearingDurationSeconds: Long? = null,
  val isMuted: Boolean = false,
  val isPinned: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

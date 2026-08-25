package com.example.model

import java.util.UUID

data class UserProfile(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val username: String,
  val avatarUrl: String,
  val bio: String = "",
  val location: String = "",
  val isVerified: Boolean = false,
  val followersCount: Int = 1240,
  val followingCount: Int = 380,
  val loopsCount: Int = 14,
  val isOnline: Boolean = true,
  val auraStatus: String = "In the zone ✨"
)

enum class PostType {
  STANDARD,
  IMAGE,
  POLL,
  LINK_CURATION,
  MOMENT_RECAP
}

data class PollOption(
  val id: Int,
  val text: String,
  val votes: Int,
  val percent: Int = 0
)

data class Post(
  val id: String = UUID.randomUUID().toString(),
  val author: UserProfile,
  val timeAgo: String,
  val location: String? = null,
  val content: String,
  val type: PostType = PostType.STANDARD,
  val imageUrl: String? = null,
  val categoryTag: String? = null,
  val pollOptions: List<PollOption>? = null,
  val userSelectedPollOption: Int? = null,
  val likesCount: Int = 0,
  val commentsCount: Int = 0,
  val resparksCount: Int = 0,
  val isLiked: Boolean = false,
  val isVaulted: Boolean = false,
  val isResparked: Boolean = false,
  val privacyScope: String = "Public"
)

data class FlashMoment(
  val id: String = UUID.randomUUID().toString(),
  val user: UserProfile,
  val imageUrl: String,
  val hasUnseen: Boolean = true,
  val isOwnAdd: Boolean = false,
  val title: String = ""
)

data class LoopItem(
  val id: String = UUID.randomUUID().toString(),
  val author: UserProfile,
  val title: String,
  val description: String,
  val videoThumbnailUrl: String,
  val audioTrack: String,
  val seriesTag: String? = null,
  val likesCount: Int = 2400,
  val commentsCount: Int = 182,
  val isLiked: Boolean = false,
  val isVaulted: Boolean = false
)

data class WhisperChat(
  val id: String = UUID.randomUUID().toString(),
  val participant: UserProfile,
  val lastMessage: String,
  val timestamp: String,
  val unreadCount: Int = 0,
  val isEncrypted: Boolean = true,
  val isEphemeral: Boolean = false,
  val auraColorHex: Long = 0xFFC8953E
)

data class SignalNotification(
  val id: String = UUID.randomUUID().toString(),
  val user: UserProfile,
  val title: String,
  val subtitle: String,
  val timeAgo: String,
  val type: SignalType,
  val isRead: Boolean = false
)

enum class SignalType {
  LIKE,
  RESPARK,
  COMMENT,
  MENTION,
  VAULT_SAVE,
  WHISPER_REQUEST
}

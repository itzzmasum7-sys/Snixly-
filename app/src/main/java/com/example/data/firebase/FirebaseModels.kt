package com.example.data.firebase

data class UserProfileDto(
  val uid: String = "",
  val fullName: String = "",
  val username: String = "",
  val email: String = "",
  val phone: String = "",
  val bio: String = "",
  val location: String = "",
  val avatarUrl: String = "",
  val coverUrl: String = "",
  val accountType: String = "Personal Space",
  val isVerified: Boolean = false,
  val isPrivate: Boolean = false,
  val auraStatus: String = "In the zone ✨",
  val followersCount: Long = 0,
  val followingCount: Long = 0,
  val loopsCount: Long = 0,
  val privacySettings: Map<String, Any> = defaultPrivacySettings(),
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

fun defaultPrivacySettings(): Map<String, Any> = mapOf(
  "isPrivate" to false,
  "showActiveStatus" to true,
  "whoCanMessage" to "Everyone",
  "whoCanMention" to "Everyone",
  "tagsApproval" to false,
  "storyPrivacy" to "All Followers",
  "loopPrivacy" to "Public",
  "dataSaver" to false,
  "theme" to "Light",
  "goldAccentTone" to "Signature Gold"
)

sealed class AuthState {
  object Initializing : AuthState()
  object Unauthenticated : AuthState()
  data class NeedsEmailVerification(val email: String) : AuthState()
  data class Authenticated(val uid: String, val email: String) : AuthState()
}

sealed class Resource<out T> {
  object Idle : Resource<Nothing>()
  object Loading : Resource<Nothing>()
  data class Success<out T>(val data: T) : Resource<T>()
  data class Error(val message: String) : Resource<Nothing>()
}

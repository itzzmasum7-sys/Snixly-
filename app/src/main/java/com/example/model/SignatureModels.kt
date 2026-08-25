package com.example.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

// ==========================================
// 158. GESTURE & NAVIGATION SETTINGS
// ==========================================
data class GestureNavSettings(
  val standardNavEnabled: Boolean = true,
  val snixlyGesturesEnabled: Boolean = true,
  val edgeGlideEnabled: Boolean = true,
  val compassHoldEnabled: Boolean = true,
  val oneHandArcEnabled: Boolean = false,
  val navScrubEnabled: Boolean = true,
  val peekNavigationEnabled: Boolean = true,
  val quickVaultGestureEnabled: Boolean = true,
  val gestureHaptics: HapticLevel = HapticLevel.SUBTLE,
  val gestureSensitivity: Float = 0.75f,
  val isLeftHandMode: Boolean = false,
  val showGestureHints: Boolean = true
)

enum class HapticLevel {
  OFF, SUBTLE, STANDARD
}

enum class UiDensity {
  COMFORTABLE, STANDARD, COMPACT
}

enum class MotionPreference {
  FULL, SUBTLE, REDUCED
}

// ==========================================
// 162 & 197. SNIXLY COMPASS & COMMANDS
// ==========================================
enum class CompassAction {
  CATCH_ME_UP,
  FOCUS_MODE,
  PRIVACY_STATES,
  YOUR_ALGORITHM,
  MEMORY_VAULT,
  QUIET_MODE,
  FAVORITES,
  COMMAND_BAR,
  RECENT_DESTINATIONS,
  SAFE_ARRIVAL
}

data class RecentDestination(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val subtitle: String,
  val route: String,
  val iconType: String,
  val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// 169 & 170. CONVERSATION AURA FAMILIES
// ==========================================
enum class AuraPreset(
  val displayName: String,
  val primaryHex: Long,
  val secondaryHex: Long,
  val surfaceHex: Long,
  val description: String
) {
  PEARL("Pearl", 0xFFE2D9C8, 0xFFFBF9F5, 0xFFF8F5EE, "Warm luminous ivory & soft golden reflections"),
  MIDNIGHT("Midnight", 0xFF1C2230, 0xFF0F141C, 0xFF151B26, "Deep obsidian slate with starlit accents"),
  GOLD_DUST("Gold Dust", 0xFFC8953E, 0xFFDFAC53, 0xFFFAF4EA, "Signature Snixly gilded luxury glow"),
  CALM("Calm", 0xFF7A9E9F, 0xFFB8D5D6, 0xFFF0F6F6, "Serene sage & gentle mindfulness tones"),
  PAPER("Paper", 0xFFE5DDD0, 0xFFF4EFE6, 0xFFFAF7F2, "Textured editorial canvas & warm print ink"),
  GLASS("Glass", 0xFF94A3B8, 0xFFE2E8F0, 0xFFF1F5F9, "Translucent frosted glass with crisp prism highlights"),
  MONO("Mono", 0xFF334155, 0xFF64748B, 0xFFF8FAFC, "Modern architectural charcoal & titanium"),
  WARM("Warm", 0xFFD97706, 0xFFFBBF24, 0xFFFFFBEB, "Amber dusk glow with cozy resonance"),
  OCEAN("Ocean", 0xFF0284C7, 0xFF38BDF8, 0xFFF0F9FF, "Deep azure tide with clean coastal clarity"),
  FOREST("Forest", 0xFF059669, 0xFF34D399, 0xFFECFDF5, "Organic emerald pine & tranquil dew"),
  AURORA_SOFT("Aurora Soft", 0xFF8B5CF6, 0xFFA78BFA, 0xFFF5F3FF, "Soft lavender glow with dreamy gradients"),
  STUDIO("Studio", 0xFF475569, 0xFF0F172A, 0xFFFFFFFF, "Neutral high-precision creator workspace")
}

data class AuraCustomization(
  val preset: AuraPreset = AuraPreset.GOLD_DUST,
  val bubbleStyle: String = "Organic Rounded", // Organic Rounded, Sharp Modern, Soft Pill
  val surfaceDepth: Float = 0.8f,
  val motionLevel: MotionPreference = MotionPreference.SUBTLE,
  val isSharedTheme: Boolean = false
)

// ==========================================
// 172. PEOPLE HALO STATES
// ==========================================
enum class PeopleHalo(val label: String, val colorHex: Long) {
  FAVORITE("Favorite", 0xFFC8953E),
  PRIORITY("Priority", 0xFFE11D48),
  CIRCLE("Inner Circle", 0xFF8B5CF6),
  CREATOR("Creator", 0xFF0284C7),
  BUSINESS("Collab/Business", 0xFF059669),
  RECENT("Recent", 0xFF64748B),
  NONE("Standard", 0xFFCBD5E1)
}

// ==========================================
// 179. WHISPER PRESENCE PRIVACY
// ==========================================
enum class PresencePrivacy(val label: String, val iconDesc: String) {
  FREE_TO_CHAT("Free to Chat 🟢", "Available for spontaneous conversation"),
  BUSY("In Focus / Busy 🟡", "Notifications muted, replies when free"),
  REPLIES_LATER("Replies Later ⏳", "Checking messages in evening"),
  INVISIBLE("Invisible ⚪", "Private presence enabled")
}

// ==========================================
// 181. LOOP CONTROL MODES
// ==========================================
enum class LoopControlMode {
  MINIMAL,
  STANDARD,
  INFO_PLUS
}

// ==========================================
// 186. PROFILE CANVAS TYPES
// ==========================================
enum class ProfileCanvasType(val label: String, val desc: String) {
  CLASSIC("Classic Space", "Balanced editorial header with full story grid"),
  EDITORIAL("Editorial Studio", "Magazine typography, full-bleed hero statement"),
  MINIMAL("Minimal Slate", "Clean distraction-free typography and compact identity"),
  CREATOR("Creator Hub", "Highlighted series, pinned releases and collab links"),
  SHOWCASE("Visual Showcase", "High-contrast portfolio cards and prominent loops")
}

// ==========================================
// 188. SPACE LAYERS
// ==========================================
enum class SpaceLayer {
  PUBLIC,
  CONNECTION,
  PRIVATE
}

// ==========================================
// 201. SOCIAL ENERGY
// ==========================================
enum class SocialEnergy(val label: String, val desc: String) {
  QUIET("Quiet Mode 🌿", "Reduced social prompts, minimal unread counts"),
  NORMAL("Balanced ✨", "Natural feed updates & connection highlights"),
  SOCIAL("Social Energy ⚡", "Prioritizes community activity & real-time whispers")
}

// ==========================================
// 210. PRIVACY QUICK STATES
// ==========================================
enum class PrivacyQuickState(val label: String, val desc: String) {
  STANDARD("Standard Privacy", "Default safe visibility for connections & discoveries"),
  PRIVATE("Private Sanctum", "Strict authorization, hidden online status & search index"),
  QUIET("Quiet Time", "Muted signals, hidden read receipts, no notifications"),
  TRAVEL("Travel Mode", "Protects precise location, offline vault priority, data saver")
}

// ==========================================
// 215 & 216. VAULT WITH PURPOSE
// ==========================================
enum class VaultPurpose(val label: String, val iconName: String) {
  WATCH_LATER("Watch Later ⏱️", "Video"),
  LEARN("Knowledge / Learn 📚", "Education"),
  IDEA("Idea / Sparks 💡", "Inspiration"),
  INSPIRATION("Aesthetic Mood 🎨", "Design"),
  BUY_RESEARCH("Research & Links 🔗", "Research"),
  GENERAL("Quick Save 📌", "Bookmark")
}

data class VaultItem(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val subtitle: String,
  val purpose: VaultPurpose = VaultPurpose.GENERAL,
  val category: String = "General",
  val savedAt: Long = System.currentTimeMillis(),
  val note: String? = null,
  val sourceUrl: String? = null
)

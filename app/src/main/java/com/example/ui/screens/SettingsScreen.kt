package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.UserProfile
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Production-ready SNIXLY Settings Screen
 * Features 12 comprehensive sections with full interactivity, state persistence,
 * bottom sheets, dialogs, search filtering, and snackbar notifications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  currentUser: UserProfile,
  onBackClick: () -> Unit,
  onUserUpdated: (UserProfile) -> Unit = {},
  onLogOut: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  var searchQuery by remember { mutableStateOf("") }

  // 1. Account States
  var userName by remember { mutableStateOf(currentUser.name) }
  var userHandle by remember { mutableStateOf(currentUser.username) }
  var userBio by remember { mutableStateOf(currentUser.bio) }
  var userLocation by remember { mutableStateOf(currentUser.location) }
  var userEmail by remember { mutableStateOf("alex.rivera@snixly.design") }
  var userPhone by remember { mutableStateOf("+1 (555) 382-9102") }
  var userAccountType by remember { mutableStateOf("Creator Space") } // Personal, Creator Space, Business
  var userAura by remember { mutableStateOf(currentUser.auraStatus) }

  // 2. Privacy States
  var isPrivateAccount by remember { mutableStateOf(false) }
  var showActivityStatus by remember { mutableStateOf(true) }
  var whoCanMessageMe by remember { mutableStateOf("Everyone") } // Everyone, Followers, Mutuals, Off
  var mentionsSetting by remember { mutableStateOf("Everyone") }
  var tagsSetting by remember { mutableStateOf("Review Required") }
  var storyPrivacy by remember { mutableStateOf("Public") }
  var loopPrivacy by remember { mutableStateOf("Public Stream") }

  // 3. Security States
  var twoFactorEnabled by remember { mutableStateOf(true) }
  var biometricAuthEnabled by remember { mutableStateOf(true) }
  var activeSessionsCount by remember { mutableStateOf(3) }

  // 4. Notification States
  var notifyLikes by remember { mutableStateOf(true) }
  var notifyComments by remember { mutableStateOf(true) }
  var notifyFollowers by remember { mutableStateOf(true) }
  var notifyWhispers by remember { mutableStateOf(true) }
  var notifyLoops by remember { mutableStateOf(true) }
  var notifyMentions by remember { mutableStateOf(true) }
  var notifyCreatorMilestones by remember { mutableStateOf(true) }

  // 5. Content Preference States
  var sensitiveContentLevel by remember { mutableStateOf("Standard") } // Standard, Less, Strict
  var hiddenWordsList by remember { mutableStateOf(listOf("spam", "promo", "crypto-airdrop", "bot")) }
  var feedCurationMode by remember { mutableStateOf("Curated Aura") } // Curated Aura, Chronological
  var showFlashMomentsStrip by remember { mutableStateOf(true) }

  // 6. Block & Restrict States
  var blockedUsersList by remember {
    mutableStateOf(
      listOf(
        "spambot_491" to "Automated Bot",
        "crypto_shill_99" to "Crypto Promoter",
        "phantom_ghost" to "Inactive Account"
      )
    )
  }
  var restrictedUsersList by remember {
    mutableStateOf(
      listOf("curious_scout" to "Limited Comments")
    )
  }
  var mutedAccountsList by remember {
    mutableStateOf(
      listOf("daily_noise_hub" to "Muted Posts & Loops")
    )
  }

  // 7. Appearance States
  var selectedTheme by remember { mutableStateOf("Light (Warm Pearl)") } // Light, Dark, System
  var accentTone by remember { mutableStateOf("Classic Gold") }
  var highContrastText by remember { mutableStateOf(false) }
  var smoothTransitions by remember { mutableStateOf(true) }

  // 8. Data & Storage States
  var isDataSaverEnabled by remember { mutableStateOf(false) }
  var mediaQuality by remember { mutableStateOf("Ultra HD (Lossless)") }
  var cachedDataSizeMb by remember { mutableStateOf(164.8f) }
  var downloadWifiOnly by remember { mutableStateOf(true) }
  var autoSaveLoopsToGallery by remember { mutableStateOf(false) }

  // 9. Creator & Business States
  var creatorModeEnabled by remember { mutableStateOf(true) }
  var businessModeEnabled by remember { mutableStateOf(false) }
  var analyticsPeriod by remember { mutableStateOf("30 Days") }
  var tipJarEnabled by remember { mutableStateOf(true) }
  var resparkRevenueShare by remember { mutableStateOf("50/50 Split") }

  // Active Dialog / Sheet States
  var activeModalSheet by remember { mutableStateOf<SettingsModalType?>(null) }
  var showLogoutConfirmDialog by remember { mutableStateOf(false) }
  var showDeactivateConfirmDialog by remember { mutableStateOf(false) }
  var showDeleteConfirmDialog by remember { mutableStateOf(false) }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      Surface(
        color = colors.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = onBackClick,
              modifier = Modifier.testTag("settings_back_button")
            ) {
              Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Back to Space",
                tint = colors.primaryText
              )
            }
            Text(
              text = "Settings & Preferences",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = colors.primaryText,
              modifier = Modifier.weight(1f)
            )
            IconButton(
              onClick = {
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("All settings saved securely to SNIXLY Vault")
                }
              }
            ) {
              Icon(
                imageVector = Icons.Outlined.DoneAll,
                contentDescription = "All Saved",
                tint = colors.accentGold
              )
            }
          }
          HorizontalDivider(color = colors.border, thickness = 1.dp)
        }
      }
    },
    containerColor = colors.background,
    modifier = modifier
      .fillMaxSize()
      .testTag("settings_screen")
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Search Bar
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search settings, privacy, security...", color = colors.secondaryText, fontSize = 14.sp) },
          leadingIcon = {
            Icon(Icons.Outlined.Search, contentDescription = "Search", tint = colors.accentGold)
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Outlined.Close, contentDescription = "Clear", tint = colors.secondaryText)
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_search_field"),
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            focusedBorderColor = colors.accentGold,
            unfocusedBorderColor = colors.border,
            focusedTextColor = colors.primaryText,
            unfocusedTextColor = colors.primaryText
          ),
          singleLine = true
        )
      }

      // Quick Profile Identity Card
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = colors.surface),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(SnixlyGoldSoft.copy(alpha = 0.6f), SnixlyGoldSoft.copy(alpha = 0.6f)))),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { activeModalSheet = SettingsModalType.EDIT_PROFILE }
            .testTag("settings_profile_card")
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colors.accentGold, SnixlyGoldSoft)))
                .padding(2.dp)
                .clip(CircleShape)
            ) {
              AsyncImage(
                model = currentUser.avatarUrl,
                contentDescription = userName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = userName,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = colors.primaryText
                )
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = colors.accentGold.copy(alpha = 0.2f)
                ) {
                  Text(
                    text = userAccountType,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = colors.accentGold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
              Text(
                text = "@$userHandle • $userEmail",
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "Aura: $userAura",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = colors.accentGold
              )
            }

            Icon(
              imageVector = Icons.Outlined.ChevronRight,
              contentDescription = "Edit Profile",
              tint = colors.secondaryText
            )
          }
        }
      }

      // SECTION 1: ACCOUNT
      if (searchQuery.isEmpty() || "account edit profile username email phone password type".contains(searchQuery.lowercase())) {
        item {
          SettingsSectionCard(title = "1. Account", icon = Icons.Outlined.Person) {
            SettingsNavigationRow(
              icon = Icons.Outlined.Badge,
              title = "Edit Profile",
              subtitle = "Name, bio, location, aura and profile photo",
              onClick = { activeModalSheet = SettingsModalType.EDIT_PROFILE }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.AlternateEmail,
              title = "Username",
              value = "@$userHandle",
              onClick = { activeModalSheet = SettingsModalType.CHANGE_USERNAME }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.Email,
              title = "Email Address",
              value = userEmail,
              badgeText = "Verified",
              badgeColor = SnixlyEmeraldActive,
              onClick = { activeModalSheet = SettingsModalType.CHANGE_EMAIL }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.Phone,
              title = "Phone Number",
              value = userPhone,
              onClick = { activeModalSheet = SettingsModalType.CHANGE_PHONE }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.VpnKey,
              title = "Password",
              subtitle = "Last updated 3 weeks ago",
              onClick = { activeModalSheet = SettingsModalType.CHANGE_PASSWORD }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.Stars,
              title = "Account Type",
              value = userAccountType,
              onClick = { activeModalSheet = SettingsModalType.ACCOUNT_TYPE }
            )
          }
        }
      }

      // SECTION 2: PRIVACY
      if (searchQuery.isEmpty() || "privacy private account activity status message mentions tags story loop".contains(searchQuery.lowercase())) {
        item {
          SettingsSectionCard(title = "2. Privacy", icon = Icons.Outlined.Shield) {
            SettingsToggleRow(
              icon = Icons.Outlined.Lock,
              title = "Private Account",
              subtitle = "Only approved followers can see your posts and loops",
              checked = isPrivateAccount,
              onCheckedChange = { isPrivateAccount = it }
            )
            SettingsDivider()
            SettingsToggleRow(
              icon = Icons.Outlined.RadioButtonChecked,
              title = "Activity Status",
              subtitle = "Show when you are active or in the zone ✨",
              checked = showActivityStatus,
              onCheckedChange = { showActivityStatus = it }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.Chat,
              title = "Who Can Message Me",
              value = whoCanMessageMe,
              onClick = { activeModalSheet = SettingsModalType.WHO_CAN_MESSAGE }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.AlternateEmail,
              title = "Mentions",
              value = mentionsSetting,
              onClick = { activeModalSheet = SettingsModalType.MENTIONS_PRIVACY }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.LocalOffer,
              title = "Tags",
              value = tagsSetting,
              onClick = { activeModalSheet = SettingsModalType.TAGS_PRIVACY }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.PhotoCamera,
              title = "Story / Flash Privacy",
              value = storyPrivacy,
              onClick = { activeModalSheet = SettingsModalType.STORY_PRIVACY }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.AllInclusive,
              title = "Loop Privacy",
              value = loopPrivacy,
              onClick = { activeModalSheet = SettingsModalType.LOOP_PRIVACY }
            )
          }
        }
      }

      // SECTION 3: SECURITY
      if (searchQuery.isEmpty() || "security password two-step 2fa login activity devices trusted".contains(searchQuery.lowercase())) {
        item {
          SettingsSectionCard(title = "3. Security", icon = Icons.Outlined.Security) {
            SettingsNavigationRow(
              icon = Icons.Outlined.LockReset,
              title = "Change Password",
              subtitle = "Secure your login credentials",
              onClick = { activeModalSheet = SettingsModalType.CHANGE_PASSWORD }
            )
            SettingsDivider()
            SettingsToggleRow(
              icon = Icons.Outlined.VpnKey,
              title = "Two-Step Verification (2FA)",
              subtitle = "Authenticator App & SMS backup codes",
              checked = twoFactorEnabled,
              onCheckedChange = { twoFactorEnabled = it }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.History,
              title = "Login Activity",
              subtitle = "$activeSessionsCount active devices currently authorized",
              onClick = { activeModalSheet = SettingsModalType.LOGIN_ACTIVITY }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.Devices,
              title = "Trusted Devices",
              subtitle = "Biometric & passkey verified hardware",
              onClick = { activeModalSheet = SettingsModalType.TRUSTED_DEVICES }
            )
          }
        }
      }

      // SECTION 4: NOTIFICATIONS
      if (searchQuery.isEmpty() || "notifications likes comments followers whisper loops mentions creator".contains(searchQuery.lowercase())) {
        item {
          SettingsSectionCard(title = "4. Notifications", icon = Icons.Outlined.Notifications) {
            SettingsToggleRow(
              icon = Icons.Outlined.FavoriteBorder,
              title = "Likes & Resparks",
              subtitle = "When people like or respark your creations",
              checked = notifyLikes,
              onCheckedChange = { notifyLikes = it }
            )
            SettingsDivider()
            SettingsToggleRow(
              icon = Icons.Outlined.ChatBubbleOutline,
              title = "Comments & Replies",
              subtitle = "Conversations on your shared spaces",
              checked = notifyComments,
              onCheckedChange = { notifyComments = it }
            )
            SettingsDivider()
            SettingsToggleRow(
              icon = Icons.Outlined.PersonAddAlt,
              title = "New Followers",
              subtitle = "When someone connects to your aura",
              checked = notifyFollowers,
              onCheckedChange = { notifyFollowers = it }
            )
            SettingsDivider()
            SettingsToggleRow(
              icon = Icons.Outlined.Lock,
              title = "Whisper Messages",
              subtitle = "End-to-end encrypted direct whisper pings",
              checked = notifyWhispers,
              onCheckedChange = { notifyWhispers = it }
            )
            SettingsDivider()
            SettingsToggleRow(
              icon = Icons.Outlined.AllInclusive,
              title = "Loops & Video Pulses",
              subtitle = "Loop remix alerts and trending creations",
              checked = notifyLoops,
              onCheckedChange = { notifyLoops = it }
            )
            SettingsDivider()
            SettingsToggleRow(
              icon = Icons.Outlined.AlternateEmail,
              title = "Mentions & Tags",
              subtitle = "When someone references you in a post",
              checked = notifyMentions,
              onCheckedChange = { notifyMentions = it }
            )
            SettingsDivider()
            SettingsToggleRow(
              icon = Icons.Outlined.MonetizationOn,
              title = "Creator Notifications",
              subtitle = "Tips received, analytics digests & milestones",
              checked = notifyCreatorMilestones,
              onCheckedChange = { notifyCreatorMilestones = it }
            )
          }
        }
      }

      // SECTION 5: CONTENT PREFERENCES
      if (searchQuery.isEmpty() || "content preferences sensitive muted hidden words not interested feed".contains(searchQuery.lowercase())) {
        item {
          SettingsSectionCard(title = "5. Content Preferences", icon = Icons.Outlined.Tune) {
            SettingsNavigationRow(
              icon = Icons.Outlined.FilterList,
              title = "Sensitive Content",
              value = sensitiveContentLevel,
              onClick = { activeModalSheet = SettingsModalType.SENSITIVE_CONTENT }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.VolumeOff,
              title = "Muted Accounts",
              value = "${mutedAccountsList.size} accounts",
              onClick = { activeModalSheet = SettingsModalType.MUTED_ACCOUNTS }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.Spellcheck,
              title = "Hidden Words & Phrases",
              value = "${hiddenWordsList.size} active filters",
              onClick = { activeModalSheet = SettingsModalType.HIDDEN_WORDS }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.VisibilityOff,
              title = "Not Interested History",
              subtitle = "Review and reset your recommendation filters",
              onClick = {
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("Recommendation history refreshed")
                }
              }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.DynamicFeed,
              title = "Feed Preferences",
              value = feedCurationMode,
              onClick = { activeModalSheet = SettingsModalType.FEED_PREFERENCES }
            )
          }
        }
      }

      // SECTION 6: BLOCK & RESTRICT
      if (searchQuery.isEmpty() || "block restrict blocked restricted muted accounts".contains(searchQuery.lowercase())) {
        item {
          SettingsSectionCard(title = "6. Block & Restrict", icon = Icons.Outlined.Block) {
            SettingsNavigationRow(
              icon = Icons.Outlined.PersonOff,
              title = "Blocked Accounts",
              value = "${blockedUsersList.size} blocked",
              onClick = { activeModalSheet = SettingsModalType.BLOCKED_ACCOUNTS }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.GppMaybe,
              title = "Restricted Accounts",
              value = "${restrictedUsersList.size} restricted",
              onClick = { activeModalSheet = SettingsModalType.RESTRICTED_ACCOUNTS }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.VolumeMute,
              title = "Muted Accounts",
              value = "${mutedAccountsList.size} muted",
              onClick = { activeModalSheet = SettingsModalType.MUTED_ACCOUNTS }
            )
          }
        }
      }

      // SECTION 7: APPEARANCE
      if (searchQuery.isEmpty() || "appearance theme light dark system default color contrast".contains(searchQuery.lowercase())) {
        item {
          SettingsSectionCard(title = "7. Appearance", icon = Icons.Outlined.Palette) {
            SettingsNavigationRow(
              icon = Icons.Outlined.DarkMode,
              title = "Theme Mode",
              value = selectedTheme,
              onClick = { activeModalSheet = SettingsModalType.THEME_SELECT }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.ColorLens,
              title = "SNIXLY Gold Accent",
              value = accentTone,
              onClick = { activeModalSheet = SettingsModalType.ACCENT_SELECT }
            )
            SettingsDivider()
            SettingsToggleRow(
              icon = Icons.Outlined.Contrast,
              title = "High Contrast Text",
              subtitle = "Enhance contrast for improved readability",
              checked = highContrastText,
              onCheckedChange = { highContrastText = it }
            )
            SettingsDivider()
            SettingsToggleRow(
              icon = Icons.Outlined.Animation,
              title = "Fluid Aura Animations",
              subtitle = "Enable smooth gesture transitions & glowing rings",
              checked = smoothTransitions,
              onCheckedChange = { smoothTransitions = it }
            )
          }
        }
      }

      // SECTION 8: DATA & STORAGE
      if (searchQuery.isEmpty() || "data storage data saver media quality cache download".contains(searchQuery.lowercase())) {
        item {
          SettingsSectionCard(title = "8. Data & Storage", icon = Icons.Outlined.Storage) {
            SettingsToggleRow(
              icon = Icons.Outlined.DataSaverOn,
              title = "Data Saver",
              subtitle = "Reduce video preload and lower image size on mobile",
              checked = isDataSaverEnabled,
              onCheckedChange = { isDataSaverEnabled = it }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.HighQuality,
              title = "Media Upload Quality",
              value = mediaQuality,
              onClick = { activeModalSheet = SettingsModalType.MEDIA_QUALITY }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.CleaningServices,
              title = "Cache Management",
              value = "${String.format("%.1f", cachedDataSizeMb)} MB",
              subtitle = "Tap to clear cached video loops and image buffers",
              onClick = {
                cachedDataSizeMb = 0.0f
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("Cache cleaned successfully (0.0 MB)")
                }
              }
            )
            SettingsDivider()
            SettingsToggleRow(
              icon = Icons.Outlined.Wifi,
              title = "Download Over Wi-Fi Only",
              subtitle = "Preserve cellular bandwidth for heavy loop cache",
              checked = downloadWifiOnly,
              onCheckedChange = { downloadWifiOnly = it }
            )
          }
        }
      }

      // SECTION 9: CREATOR & BUSINESS
      if (searchQuery.isEmpty() || "creator business analytics monetization tip jar revenue".contains(searchQuery.lowercase())) {
        item {
          SettingsSectionCard(title = "9. Creator & Business", icon = Icons.Outlined.WorkOutline) {
            SettingsToggleRow(
              icon = Icons.Outlined.AutoAwesome,
              title = "Creator Mode",
              subtitle = "Unlock Loop pulses, subscriber perks and tip jar",
              checked = creatorModeEnabled,
              onCheckedChange = { creatorModeEnabled = it }
            )
            SettingsDivider()
            SettingsToggleRow(
              icon = Icons.Outlined.Storefront,
              title = "Business Mode",
              subtitle = "Add commercial links and partner storefronts",
              checked = businessModeEnabled,
              onCheckedChange = { businessModeEnabled = it }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.Insights,
              title = "Analytics Settings",
              value = analyticsPeriod,
              onClick = { activeModalSheet = SettingsModalType.ANALYTICS_SETTINGS }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.Paid,
              title = "Monetization & Tips",
              value = if (tipJarEnabled) "Active ($resparkRevenueShare)" else "Disabled",
              onClick = { activeModalSheet = SettingsModalType.MONETIZATION_SETTINGS }
            )
          }
        }
      }

      // SECTION 10: HELP & SAFETY
      if (searchQuery.isEmpty() || "help safety center report problem guidelines community".contains(searchQuery.lowercase())) {
        item {
          SettingsSectionCard(title = "10. Help & Safety", icon = Icons.Outlined.HelpOutline) {
            SettingsNavigationRow(
              icon = Icons.Outlined.HelpCenter,
              title = "Help Center",
              subtitle = "FAQs on Vault, Whisper encryption & loops",
              onClick = { activeModalSheet = SettingsModalType.HELP_CENTER }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.BugReport,
              title = "Report a Problem",
              subtitle = "Send diagnostics or feedback directly to SNIXLY team",
              onClick = { activeModalSheet = SettingsModalType.REPORT_PROBLEM }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.MenuBook,
              title = "Community Guidelines",
              subtitle = "Respect, creative integrity & zero harassment",
              onClick = { activeModalSheet = SettingsModalType.COMMUNITY_GUIDELINES }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.HealthAndSafety,
              title = "Safety Center",
              subtitle = "Anti-abuse tools, safety guides & emergency lockdown",
              onClick = { activeModalSheet = SettingsModalType.SAFETY_CENTER }
            )
          }
        }
      }

      // SECTION 11: ABOUT SNIXLY
      if (searchQuery.isEmpty() || "about snixly version terms privacy policy licenses".contains(searchQuery.lowercase())) {
        item {
          SettingsSectionCard(title = "11. About SNIXLY", icon = Icons.Outlined.Info) {
            SettingsNavigationRow(
              icon = Icons.Outlined.MobileFriendly,
              title = "App Version",
              value = "v2.4.0 (Build 4129)",
              subtitle = "Production Gold • Up to date",
              onClick = {
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("SNIXLY is on the latest release v2.4.0")
                }
              }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.Description,
              title = "Terms of Service",
              subtitle = "Legal agreements & creator rights",
              onClick = { activeModalSheet = SettingsModalType.TERMS }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.Policy,
              title = "Privacy Policy",
              subtitle = "Zero-knowledge architecture guarantee",
              onClick = { activeModalSheet = SettingsModalType.PRIVACY_POLICY }
            )
            SettingsDivider()
            SettingsNavigationRow(
              icon = Icons.Outlined.Code,
              title = "Open-Source Licenses",
              subtitle = "Third-party libraries and attributions",
              onClick = { activeModalSheet = SettingsModalType.LICENSES }
            )
          }
        }
      }

      // SECTION 12: ACCOUNT ACTIONS
      if (searchQuery.isEmpty() || "account actions logout log out deactivate delete".contains(searchQuery.lowercase())) {
        item {
          SettingsSectionCard(title = "12. Account Actions", icon = Icons.Outlined.WarningAmber, isDanger = true) {
            SettingsDangerRow(
              icon = Icons.Outlined.Logout,
              title = "Log Out",
              subtitle = "Log out from this device",
              color = SnixlyGoldDeep,
              onClick = { showLogoutConfirmDialog = true }
            )
            SettingsDivider()
            SettingsDangerRow(
              icon = Icons.Outlined.PauseCircleOutline,
              title = "Deactivate Account",
              subtitle = "Temporarily hide your space, posts and loops",
              color = SnixlyGoldDeep,
              onClick = { showDeactivateConfirmDialog = true }
            )
            SettingsDivider()
            SettingsDangerRow(
              icon = Icons.Outlined.DeleteForever,
              title = "Delete Account",
              subtitle = "Permanently remove your account and all vaulted content",
              color = SnixlyCrimsonAlert,
              onClick = { showDeleteConfirmDialog = true }
            )
          }
        }
      }

      // Bottom Branding Footnote
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "SNIXLY",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
            color = colors.accentGold
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Designed for intentional connection & creative sovereignty",
            style = MaterialTheme.typography.labelSmall,
            color = colors.secondaryText
          )
          Text(
            text = "Zero-Knowledge Encryption • Decentralized Aura",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = colors.accentGold.copy(alpha = 0.7f)
          )
        }
      }
    }
  }

  // ==========================================
  // MODAL BOTTOM SHEETS & INTERACTIVE DIALOGS
  // ==========================================

  when (activeModalSheet) {
    SettingsModalType.EDIT_PROFILE -> {
      EditProfileModal(
        initialName = userName,
        initialBio = userBio,
        initialLocation = userLocation,
        initialAura = userAura,
        onDismiss = { activeModalSheet = null },
        onSave = { newName, newBio, newLoc, newAura ->
          userName = newName
          userBio = newBio
          userLocation = newLoc
          userAura = newAura
          onUserUpdated(
            currentUser.copy(
              name = newName,
              bio = newBio,
              location = newLoc,
              auraStatus = newAura
            )
          )
          activeModalSheet = null
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Profile updated successfully")
          }
        }
      )
    }

    SettingsModalType.CHANGE_USERNAME -> {
      SingleValueEditModal(
        title = "Change Username",
        currentValue = userHandle,
        prefix = "@",
        helperText = "Your username can contain letters, numbers, and underscores.",
        onDismiss = { activeModalSheet = null },
        onSave = { newHandle ->
          userHandle = newHandle.removePrefix("@")
          onUserUpdated(currentUser.copy(username = userHandle))
          activeModalSheet = null
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Username updated to @$userHandle")
          }
        }
      )
    }

    SettingsModalType.CHANGE_EMAIL -> {
      SingleValueEditModal(
        title = "Email Address",
        currentValue = userEmail,
        helperText = "A verification code will be sent if you update your address.",
        keyboardType = KeyboardType.Email,
        onDismiss = { activeModalSheet = null },
        onSave = { newEmail ->
          userEmail = newEmail
          activeModalSheet = null
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Verification sent to $newEmail")
          }
        }
      )
    }

    SettingsModalType.CHANGE_PHONE -> {
      SingleValueEditModal(
        title = "Phone Number",
        currentValue = userPhone,
        helperText = "Used for two-factor authentication and SMS security alerts.",
        keyboardType = KeyboardType.Phone,
        onDismiss = { activeModalSheet = null },
        onSave = { newPhone ->
          userPhone = newPhone
          activeModalSheet = null
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Phone number updated")
          }
        }
      )
    }

    SettingsModalType.CHANGE_PASSWORD -> {
      ChangePasswordModal(
        onDismiss = { activeModalSheet = null },
        onSuccess = {
          activeModalSheet = null
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Password changed successfully")
          }
        }
      )
    }

    SettingsModalType.ACCOUNT_TYPE -> {
      OptionsPickerModal(
        title = "Account Type",
        subtitle = "Select how you express yourself on SNIXLY",
        options = listOf(
          "Personal Space" to "Standard social experience with encrypted vaults",
          "Creator Space" to "Access to Loop analytics, Aura badges & Monetization",
          "Business Mode" to "Brand storefronts, commercial links & verified metrics"
        ),
        selectedOption = userAccountType,
        onDismiss = { activeModalSheet = null },
        onOptionSelected = { selected ->
          userAccountType = selected
          activeModalSheet = null
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Account type switched to $selected")
          }
        }
      )
    }

    SettingsModalType.WHO_CAN_MESSAGE -> {
      OptionsPickerModal(
        title = "Who Can Message Me",
        subtitle = "Control who can initiate encrypted Whisper channels",
        options = listOf(
          "Everyone" to "Anyone on SNIXLY can request a whisper",
          "Followers Only" to "Only accounts you approve or follow back",
          "Mutual Follows" to "Only when you both follow each other",
          "Off" to "No one can send you new whisper requests"
        ),
        selectedOption = whoCanMessageMe,
        onDismiss = { activeModalSheet = null },
        onOptionSelected = {
          whoCanMessageMe = it
          activeModalSheet = null
        }
      )
    }

    SettingsModalType.MENTIONS_PRIVACY -> {
      OptionsPickerModal(
        title = "Mentions",
        subtitle = "Choose who can mention @$userHandle in posts and loops",
        options = listOf(
          "Everyone" to "Allow all users to mention your handle",
          "People You Follow" to "Only allow people in your circle",
          "No One" to "Prevent all @mentions"
        ),
        selectedOption = mentionsSetting,
        onDismiss = { activeModalSheet = null },
        onOptionSelected = {
          mentionsSetting = it
          activeModalSheet = null
        }
      )
    }

    SettingsModalType.TAGS_PRIVACY -> {
      OptionsPickerModal(
        title = "Tags Approval",
        subtitle = "Manage how photos and moments can tag your profile",
        options = listOf(
          "Allow from Everyone" to "Tags appear automatically on your space",
          "Review Required" to "You manually approve every tag before it appears",
          "Disable Tagging" to "No one can tag your profile"
        ),
        selectedOption = tagsSetting,
        onDismiss = { activeModalSheet = null },
        onOptionSelected = {
          tagsSetting = it
          activeModalSheet = null
        }
      )
    }

    SettingsModalType.STORY_PRIVACY -> {
      OptionsPickerModal(
        title = "Story / Flash Privacy",
        subtitle = "Who sees your 24h ephemeral Flash Moments",
        options = listOf(
          "Public" to "Visible to everyone discovering moments",
          "Close Circle" to "Visible only to your curated inner circle",
          "Encrypted Only" to "Zero-knowledge share with select peers"
        ),
        selectedOption = storyPrivacy,
        onDismiss = { activeModalSheet = null },
        onOptionSelected = {
          storyPrivacy = it
          activeModalSheet = null
        }
      )
    }

    SettingsModalType.LOOP_PRIVACY -> {
      OptionsPickerModal(
        title = "Loop Privacy",
        subtitle = "Default visibility for your video loops & audio remixes",
        options = listOf(
          "Public Stream" to "Recommended in global pulse waves",
          "Followers Only" to "Restricted to your followers",
          "Private Vault" to "Saved in your encrypted vault only"
        ),
        selectedOption = loopPrivacy,
        onDismiss = { activeModalSheet = null },
        onOptionSelected = {
          loopPrivacy = it
          activeModalSheet = null
        }
      )
    }

    SettingsModalType.LOGIN_ACTIVITY -> {
      LoginActivityModal(
        onDismiss = { activeModalSheet = null },
        onTerminateOtherSessions = {
          activeSessionsCount = 1
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Terminated 2 other authorized sessions")
          }
        }
      )
    }

    SettingsModalType.TRUSTED_DEVICES -> {
      TrustedDevicesModal(
        onDismiss = { activeModalSheet = null }
      )
    }

    SettingsModalType.SENSITIVE_CONTENT -> {
      OptionsPickerModal(
        title = "Sensitive Content Control",
        subtitle = "Adjust filtering intensity across public feed",
        options = listOf(
          "Standard" to "Default balance of open creative expression",
          "Less" to "Filter out potentially sensitive or intense visuals",
          "Strict Filter" to "Maximum filtration across all categories"
        ),
        selectedOption = sensitiveContentLevel,
        onDismiss = { activeModalSheet = null },
        onOptionSelected = {
          sensitiveContentLevel = it
          activeModalSheet = null
        }
      )
    }

    SettingsModalType.HIDDEN_WORDS -> {
      HiddenWordsModal(
        words = hiddenWordsList,
        onDismiss = { activeModalSheet = null },
        onAddWord = { hiddenWordsList = hiddenWordsList + it },
        onRemoveWord = { hiddenWordsList = hiddenWordsList - it }
      )
    }

    SettingsModalType.MUTED_ACCOUNTS -> {
      UsersManagementModal(
        title = "Muted Accounts",
        subtitle = "Accounts whose posts and loops are hidden from your feed",
        users = mutedAccountsList,
        actionLabel = "Unmute",
        onDismiss = { activeModalSheet = null },
        onAction = { user ->
          mutedAccountsList = mutedAccountsList.filter { it.first != user }
          coroutineScope.launch { snackbarHostState.showSnackbar("Unmuted @$user") }
        }
      )
    }

    SettingsModalType.BLOCKED_ACCOUNTS -> {
      UsersManagementModal(
        title = "Blocked Accounts",
        subtitle = "Blocked accounts cannot view your space, message, or find your profile",
        users = blockedUsersList,
        actionLabel = "Unblock",
        onDismiss = { activeModalSheet = null },
        onAction = { user ->
          blockedUsersList = blockedUsersList.filter { it.first != user }
          coroutineScope.launch { snackbarHostState.showSnackbar("Unblocked @$user") }
        }
      )
    }

    SettingsModalType.RESTRICTED_ACCOUNTS -> {
      UsersManagementModal(
        title = "Restricted Accounts",
        subtitle = "Their comments on your posts are only visible to them",
        users = restrictedUsersList,
        actionLabel = "Unrestrict",
        onDismiss = { activeModalSheet = null },
        onAction = { user ->
          restrictedUsersList = restrictedUsersList.filter { it.first != user }
          coroutineScope.launch { snackbarHostState.showSnackbar("Unrestricted @$user") }
        }
      )
    }

    SettingsModalType.THEME_SELECT -> {
      OptionsPickerModal(
        title = "Theme Mode",
        subtitle = "Select your preferred visual ambience",
        options = listOf(
          "Light (Warm Pearl)" to "Clean, warm off-white canvas with gold accents",
          "Dark (Obsidian Gold)" to "Deep obsidian surface with radiant golden highlights",
          "System Default" to "Automatically adapts to your Android system theme"
        ),
        selectedOption = selectedTheme,
        onDismiss = { activeModalSheet = null },
        onOptionSelected = {
          selectedTheme = it
          activeModalSheet = null
          coroutineScope.launch { snackbarHostState.showSnackbar("Theme set to $it") }
        }
      )
    }

    SettingsModalType.ACCENT_SELECT -> {
      OptionsPickerModal(
        title = "SNIXLY Gold Accent",
        subtitle = "Personalize your interactive aura color",
        options = listOf(
          "Classic Gold" to "Signature luxury champagne & amber (#C8953E)",
          "Rose Gold" to "Soft warm blush with golden undertones",
          "Champagne Gold" to "Muted delicate sparkling sheen",
          "Royal Bronze" to "Deep rich metallic luster"
        ),
        selectedOption = accentTone,
        onDismiss = { activeModalSheet = null },
        onOptionSelected = {
          accentTone = it
          activeModalSheet = null
          coroutineScope.launch { snackbarHostState.showSnackbar("Accent tone set to $it") }
        }
      )
    }

    SettingsModalType.FEED_PREFERENCES -> {
      OptionsPickerModal(
        title = "Feed Preferences",
        subtitle = "How you explore posts and loops",
        options = listOf(
          "Curated Aura" to "Algorithm tailored to your aesthetic tastes & interests",
          "Chronological Stream" to "Strict time-ordered posts from followed creators",
          "Minimal Quiet Stream" to "Distraction-free feed with hidden counters"
        ),
        selectedOption = feedCurationMode,
        onDismiss = { activeModalSheet = null },
        onOptionSelected = {
          feedCurationMode = it
          activeModalSheet = null
        }
      )
    }

    SettingsModalType.MEDIA_QUALITY -> {
      OptionsPickerModal(
        title = "Media Upload Quality",
        subtitle = "Control resolution and compression for loops and photos",
        options = listOf(
          "Ultra HD (Lossless)" to "Preserves 4K/60fps master quality",
          "High (1080p Standard)" to "Balanced compression for fast loading",
          "Data Optimized (720p)" to "Fast uploads on lower connectivity"
        ),
        selectedOption = mediaQuality,
        onDismiss = { activeModalSheet = null },
        onOptionSelected = {
          mediaQuality = it
          activeModalSheet = null
        }
      )
    }

    SettingsModalType.ANALYTICS_SETTINGS -> {
      OptionsPickerModal(
        title = "Creator Analytics Scope",
        subtitle = "Measurement timeframes and metrics reporting",
        options = listOf(
          "7 Days" to "Fast weekly pulse review",
          "30 Days" to "Monthly creator engagement overview",
          "90 Days" to "Quarterly macro audience trends"
        ),
        selectedOption = analyticsPeriod,
        onDismiss = { activeModalSheet = null },
        onOptionSelected = {
          analyticsPeriod = it
          activeModalSheet = null
        }
      )
    }

    SettingsModalType.MONETIZATION_SETTINGS -> {
      MonetizationModal(
        tipJarActive = tipJarEnabled,
        revenueSplit = resparkRevenueShare,
        onDismiss = { activeModalSheet = null },
        onSave = { tipsOn, split ->
          tipJarEnabled = tipsOn
          resparkRevenueShare = split
          activeModalSheet = null
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Monetization settings updated")
          }
        }
      )
    }

    SettingsModalType.HELP_CENTER -> {
      HelpCenterModal(onDismiss = { activeModalSheet = null })
    }

    SettingsModalType.REPORT_PROBLEM -> {
      ReportProblemModal(
        onDismiss = { activeModalSheet = null },
        onSubmit = {
          activeModalSheet = null
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Report received. Our safety team will review it.")
          }
        }
      )
    }

    SettingsModalType.COMMUNITY_GUIDELINES -> {
      ContentTextModal(
        title = "Community Guidelines",
        content = """
          Welcome to SNIXLY. We are dedicated to building an intentional, privacy-first community of creators and thinkers.
          
          1. Respect and Creative Integrity: Treat every member with dignity. Plagiarism, harassment, hate speech, and impersonation are strictly forbidden.
          
          2. Zero-Tolerance for Abuse: Threats, stalking, non-consensual imagery, and harmful content will result in immediate permanent account termination.
          
          3. Privacy by Design: Respect confidential whisper channels. Do not screenshot or share ephemeral drops without consent.
          
          4. Authentic Engagement: Coordinated spam, automated engagement manipulation, and deception are barred.
        """.trimIndent(),
        onDismiss = { activeModalSheet = null }
      )
    }

    SettingsModalType.SAFETY_CENTER -> {
      ContentTextModal(
        title = "SNIXLY Safety Center",
        content = """
          Your wellbeing and autonomy are our highest priorities.
          
          • Zero-Knowledge Security: Only you and your recipients hold keys to whisper direct messages.
          • Emergency Lockdown: Toggle Private Account anytime to halt discovery.
          • Restrict & Block: Restricting limits unwanted interactions silently.
          • 24/7 Security Operations: Our trust & safety team investigates reports within hours.
        """.trimIndent(),
        onDismiss = { activeModalSheet = null }
      )
    }

    SettingsModalType.TERMS -> {
      ContentTextModal(
        title = "Terms of Service",
        content = """
          Effective Date: August 2026
          
          By accessing or using SNIXLY, you agree to these Terms of Service.
          
          • Ownership of Content: You retain 100% intellectual property ownership of your posts, loops, and curated moments.
          • Service Availability: SNIXLY is provided on an 'as-is' and 'as-available' basis with encrypted data redundancy.
          • Creator Monetization: Tip payouts and respark revenue splits are executed under transparent smart contracts.
        """.trimIndent(),
        onDismiss = { activeModalSheet = null }
      )
    }

    SettingsModalType.PRIVACY_POLICY -> {
      ContentTextModal(
        title = "Privacy Policy",
        content = """
          SNIXLY is architected around privacy sovereignty.
          
          • Zero Tracking: We do not sell your personal data, browser history, or contacts to third-party ad networks.
          • End-to-End Encryption: Direct Whisper messages and Private Vault content use client-side cryptography.
          • Data Portability: You can export all your posts, followers, and loop metrics at any time.
        """.trimIndent(),
        onDismiss = { activeModalSheet = null }
      )
    }

    SettingsModalType.LICENSES -> {
      ContentTextModal(
        title = "Open-Source Licenses",
        content = """
          SNIXLY Android builds upon exceptional open-source software:
          
          • Jetpack Compose & AndroidX (Apache License 2.0)
          • Kotlin Coroutines & Serialization (Apache License 2.0)
          • Coil Image Loader (Apache License 2.0)
          • Material Design 3 Components (Apache License 2.0)
          • Google DeepMind Antigravity Platform Tools
        """.trimIndent(),
        onDismiss = { activeModalSheet = null }
      )
    }

    null -> {}
  }

  // ==========================================
  // CONFIRMATION DIALOGS
  // ==========================================

  // Log Out Dialog
  if (showLogoutConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showLogoutConfirmDialog = false },
      title = {
        Text("Log out of @$userHandle?", fontWeight = FontWeight.Bold, color = colors.primaryText)
      },
      text = {
        Text("You can always log back in anytime with your password or passkey.", color = colors.secondaryText)
      },
      confirmButton = {
        Button(
          onClick = {
            showLogoutConfirmDialog = false
            onLogOut()
          },
          colors = ButtonDefaults.buttonColors(containerColor = SnixlyCrimsonAlert)
        ) {
          Text("Log Out", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutConfirmDialog = false }) {
          Text("Cancel", color = colors.primaryText)
        }
      },
      containerColor = colors.surface,
      shape = RoundedCornerShape(20.dp)
    )
  }

  // Deactivate Dialog
  if (showDeactivateConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showDeactivateConfirmDialog = false },
      title = {
        Text("Deactivate Space?", fontWeight = FontWeight.Bold, color = colors.primaryText)
      },
      text = {
        Text(
          "Your profile, posts, loops, and whisper history will be hidden until you log back in. Your data remains safe in the vault.",
          color = colors.secondaryText
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showDeactivateConfirmDialog = false
            onLogOut()
          },
          colors = ButtonDefaults.buttonColors(containerColor = SnixlyGoldDeep)
        ) {
          Text("Deactivate", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeactivateConfirmDialog = false }) {
          Text("Cancel", color = colors.primaryText)
        }
      },
      containerColor = colors.surface,
      shape = RoundedCornerShape(20.dp)
    )
  }

  // Delete Dialog
  if (showDeleteConfirmDialog) {
    var deleteConfirmText by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showDeleteConfirmDialog = false },
      title = {
        Text("Permanently Delete Account?", fontWeight = FontWeight.Bold, color = SnixlyCrimsonAlert)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "This action is permanent and irreversible. All your posts, followers, loops, and encrypted vaults will be permanently erased.",
            color = colors.secondaryText,
            fontSize = 13.sp
          )
          Text(
            "Type \"DELETE\" below to confirm:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.primaryText
          )
          OutlinedTextField(
            value = deleteConfirmText,
            onValueChange = { deleteConfirmText = it },
            placeholder = { Text("DELETE", color = colors.secondaryText) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (deleteConfirmText.trim() == "DELETE") {
              showDeleteConfirmDialog = false
              onLogOut()
            }
          },
          enabled = deleteConfirmText.trim() == "DELETE",
          colors = ButtonDefaults.buttonColors(containerColor = SnixlyCrimsonAlert)
        ) {
          Text("Delete Forever", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirmDialog = false }) {
          Text("Cancel", color = colors.primaryText)
        }
      },
      containerColor = colors.surface,
      shape = RoundedCornerShape(20.dp)
    )
  }
}

// ==========================================
// ENUMS & REUSABLE ROW COMPONENTS
// ==========================================

enum class SettingsModalType {
  EDIT_PROFILE,
  CHANGE_USERNAME,
  CHANGE_EMAIL,
  CHANGE_PHONE,
  CHANGE_PASSWORD,
  ACCOUNT_TYPE,
  WHO_CAN_MESSAGE,
  MENTIONS_PRIVACY,
  TAGS_PRIVACY,
  STORY_PRIVACY,
  LOOP_PRIVACY,
  LOGIN_ACTIVITY,
  TRUSTED_DEVICES,
  SENSITIVE_CONTENT,
  HIDDEN_WORDS,
  MUTED_ACCOUNTS,
  BLOCKED_ACCOUNTS,
  RESTRICTED_ACCOUNTS,
  THEME_SELECT,
  ACCENT_SELECT,
  FEED_PREFERENCES,
  MEDIA_QUALITY,
  ANALYTICS_SETTINGS,
  MONETIZATION_SETTINGS,
  HELP_CENTER,
  REPORT_PROBLEM,
  COMMUNITY_GUIDELINES,
  SAFETY_CENTER,
  TERMS,
  PRIVACY_POLICY,
  LICENSES
}

@Composable
fun SettingsSectionCard(
  title: String,
  icon: ImageVector,
  isDanger: Boolean = false,
  content: @Composable ColumnScope.() -> Unit
) {
  val colors = MaterialTheme.snixly
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = colors.surface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        if (isDanger) listOf(SnixlyCrimsonAlert.copy(alpha = 0.3f), SnixlyCrimsonAlert.copy(alpha = 0.3f))
        else listOf(colors.border, colors.border)
      )
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = if (isDanger) SnixlyCrimsonAlert else colors.accentGold,
          modifier = Modifier.size(20.dp)
        )
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
          color = if (isDanger) SnixlyCrimsonAlert else colors.primaryText
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      content()
    }
  }
}

@Composable
fun SettingsNavigationRow(
  icon: ImageVector,
  title: String,
  subtitle: String? = null,
  value: String? = null,
  badgeText: String? = null,
  badgeColor: Color = SnixlyEmeraldActive,
  onClick: () -> Unit
) {
  val colors = MaterialTheme.snixly
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = title,
      tint = colors.accentGold,
      modifier = Modifier.size(20.dp)
    )

    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = colors.primaryText
        )
        if (badgeText != null) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = badgeColor.copy(alpha = 0.15f)
          ) {
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
              color = badgeColor,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
            )
          }
        }
      }
      if (subtitle != null) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = colors.secondaryText
        )
      }
    }

    if (value != null) {
      Text(
        text = value,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = colors.secondaryText,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.widthIn(max = 140.dp)
      )
    }

    Icon(
      imageVector = Icons.Outlined.ChevronRight,
      contentDescription = "Open",
      tint = colors.secondaryText.copy(alpha = 0.6f),
      modifier = Modifier.size(18.dp)
    )
  }
}

@Composable
fun SettingsToggleRow(
  icon: ImageVector,
  title: String,
  subtitle: String? = null,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  val colors = MaterialTheme.snixly
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!checked) }
      .padding(horizontal = 16.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = title,
      tint = colors.accentGold,
      modifier = Modifier.size(20.dp)
    )

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = colors.primaryText
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = colors.secondaryText
        )
      }
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = colors.accentGold,
        uncheckedTrackColor = colors.surfaceVariant,
        uncheckedBorderColor = colors.border
      )
    )
  }
}

@Composable
fun SettingsDangerRow(
  icon: ImageVector,
  title: String,
  subtitle: String? = null,
  color: Color = SnixlyCrimsonAlert,
  onClick: () -> Unit
) {
  val colors = MaterialTheme.snixly
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = title,
      tint = color,
      modifier = Modifier.size(20.dp)
    )

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = color
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = colors.secondaryText
        )
      }
    }

    Icon(
      imageVector = Icons.Outlined.ChevronRight,
      contentDescription = "Open",
      tint = color.copy(alpha = 0.5f),
      modifier = Modifier.size(18.dp)
    )
  }
}

@Composable
fun SettingsDivider() {
  val colors = MaterialTheme.snixly
  HorizontalDivider(
    color = colors.border.copy(alpha = 0.6f),
    thickness = 0.8.dp,
    modifier = Modifier.padding(horizontal = 16.dp)
  )
}

// ==========================================
// DETAILED MODAL BOTTOM SHEETS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileModal(
  initialName: String,
  initialBio: String,
  initialLocation: String,
  initialAura: String,
  onDismiss: () -> Unit,
  onSave: (name: String, bio: String, location: String, aura: String) -> Unit
) {
  val colors = MaterialTheme.snixly
  var name by remember { mutableStateOf(initialName) }
  var bio by remember { mutableStateOf(initialBio) }
  var location by remember { mutableStateOf(initialLocation) }
  var aura by remember { mutableStateOf(initialAura) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Edit Space & Profile", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
        IconButton(onClick = onDismiss) {
          Icon(Icons.Outlined.Close, contentDescription = "Close", tint = colors.secondaryText)
        }
      }

      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Display Name") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText,
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border
        )
      )

      OutlinedTextField(
        value = aura,
        onValueChange = { aura = it },
        label = { Text("Aura Status (e.g. In the zone ✨)") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText,
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border
        )
      )

      OutlinedTextField(
        value = location,
        onValueChange = { location = it },
        label = { Text("Location (e.g. San Francisco, CA)") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText,
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border
        )
      )

      OutlinedTextField(
        value = bio,
        onValueChange = { bio = it },
        label = { Text("Bio & Creator Statement") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText,
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border
        )
      )

      Button(
        onClick = { onSave(name, bio, location, aura) },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
      ) {
        Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleValueEditModal(
  title: String,
  currentValue: String,
  prefix: String = "",
  helperText: String = "",
  keyboardType: KeyboardType = KeyboardType.Text,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit
) {
  val colors = MaterialTheme.snixly
  var textValue by remember { mutableStateOf(currentValue) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)

      if (helperText.isNotEmpty()) {
        Text(helperText, style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
      }

      OutlinedTextField(
        value = textValue,
        onValueChange = { textValue = it },
        prefix = if (prefix.isNotEmpty()) { { Text(prefix, color = colors.accentGold, fontWeight = FontWeight.Bold) } } else null,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText,
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border
        )
      )

      Button(
        onClick = { onSave(textValue) },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
      ) {
        Text("Update $title", fontWeight = FontWeight.Bold, color = Color.White)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordModal(
  onDismiss: () -> Unit,
  onSuccess: () -> Unit
) {
  val colors = MaterialTheme.snixly
  var currentPassword by remember { mutableStateOf("") }
  var newPassword by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text("Change Password", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
      Text("Choose a strong passphrase with at least 8 characters.", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)

      OutlinedTextField(
        value = currentPassword,
        onValueChange = { currentPassword = it },
        label = { Text("Current Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText,
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border
        )
      )

      OutlinedTextField(
        value = newPassword,
        onValueChange = { newPassword = it },
        label = { Text("New Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText,
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border
        )
      )

      OutlinedTextField(
        value = confirmPassword,
        onValueChange = { confirmPassword = it },
        label = { Text("Confirm New Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText,
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border
        )
      )

      if (errorMessage != null) {
        Text(errorMessage!!, color = SnixlyCrimsonAlert, style = MaterialTheme.typography.labelSmall)
      }

      Button(
        onClick = {
          if (newPassword.length < 6) {
            errorMessage = "New password must be at least 6 characters"
          } else if (newPassword != confirmPassword) {
            errorMessage = "Passwords do not match"
          } else {
            errorMessage = null
            onSuccess()
          }
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
      ) {
        Text("Save New Password", fontWeight = FontWeight.Bold, color = Color.White)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsPickerModal(
  title: String,
  subtitle: String,
  options: List<Pair<String, String>>,
  selectedOption: String,
  onDismiss: () -> Unit,
  onOptionSelected: (String) -> Unit
) {
  val colors = MaterialTheme.snixly
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)

      Spacer(modifier = Modifier.height(4.dp))

      options.forEach { (optionTitle, optionDesc) ->
        val isSelected = selectedOption.startsWith(optionTitle) || optionTitle == selectedOption
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.accentGold.copy(alpha = 0.15f) else colors.surfaceVariant
          ),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
              if (isSelected) listOf(colors.accentGold, colors.accentGold) else listOf(colors.border, colors.border)
            )
          ),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOptionSelected(optionTitle) }
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            RadioButton(
              selected = isSelected,
              onClick = { onOptionSelected(optionTitle) },
              colors = RadioButtonDefaults.colors(selectedColor = colors.accentGold)
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = optionTitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primaryText
              )
              Text(
                text = optionDesc,
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginActivityModal(
  onDismiss: () -> Unit,
  onTerminateOtherSessions: () -> Unit
) {
  val colors = MaterialTheme.snixly
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text("Where You're Logged In", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
      Text("Review devices currently holding authenticated cryptographic keys.", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)

      // Active Current Device
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.accentGold.copy(alpha = 0.15f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(colors.accentGold, colors.accentGold)))
      ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Icon(Icons.Outlined.Smartphone, contentDescription = "Device", tint = colors.accentGold)
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("Pixel 8 Pro • San Francisco, CA", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
              Text("Active Now", style = MaterialTheme.typography.labelSmall.copy(color = SnixlyEmeraldActive, fontWeight = FontWeight.Bold))
            }
            Text("This Android device • SNIXLY v2.4", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
          }
        }
      }

      // Secondary Session
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
      ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Icon(Icons.Outlined.Laptop, contentDescription = "Laptop", tint = colors.secondaryText)
          Column(modifier = Modifier.weight(1f)) {
            Text("MacBook Pro 16\" • Chrome OS", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.primaryText)
            Text("New York, NY • 3 hours ago", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
          }
        }
      }

      // Tertiary Session
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
      ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Icon(Icons.Outlined.TabletMac, contentDescription = "Tablet", tint = colors.secondaryText)
          Column(modifier = Modifier.weight(1f)) {
            Text("iPad Pro • Safari", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.primaryText)
            Text("London, UK • Yesterday", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
          }
        }
      }

      Button(
        onClick = onTerminateOtherSessions,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SnixlyCrimsonAlert),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Log Out All Other Sessions", color = Color.White, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustedDevicesModal(
  onDismiss: () -> Unit
) {
  val colors = MaterialTheme.snixly
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text("Trusted Hardware & Passkeys", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
      Text("Devices verified with biometric fingerprint or hardware security keys.", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
      ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Icon(Icons.Outlined.Fingerprint, contentDescription = "Biometric", tint = colors.accentGold)
          Column(modifier = Modifier.weight(1f)) {
            Text("Pixel Biometric Passkey", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
            Text("Added Aug 2026 • Verified", style = MaterialTheme.typography.bodySmall, color = SnixlyEmeraldActive)
          }
        }
      }

      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenWordsModal(
  words: List<String>,
  onDismiss: () -> Unit,
  onAddWord: (String) -> Unit,
  onRemoveWord: (String) -> Unit
) {
  val colors = MaterialTheme.snixly
  var newWordInput by remember { mutableStateOf("") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text("Hidden Words & Filters", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
      Text("Comments, replies, and whispers containing these phrases will be automatically filtered.", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = newWordInput,
          onValueChange = { newWordInput = it },
          placeholder = { Text("Add word or phrase...", color = colors.secondaryText) },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.primaryText,
            unfocusedTextColor = colors.primaryText,
            focusedBorderColor = colors.accentGold,
            unfocusedBorderColor = colors.border
          )
        )
        Button(
          onClick = {
            if (newWordInput.isNotBlank()) {
              onAddWord(newWordInput.trim().lowercase())
              newWordInput = ""
            }
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
        ) {
          Text("Add", color = Color.White)
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        words.forEach { word ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(colors.surfaceVariant, RoundedCornerShape(10.dp))
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(word, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = colors.primaryText)
            IconButton(onClick = { onRemoveWord(word) }, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Outlined.Close, contentDescription = "Remove", tint = SnixlyCrimsonAlert, modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersManagementModal(
  title: String,
  subtitle: String,
  users: List<Pair<String, String>>,
  actionLabel: String,
  onDismiss: () -> Unit,
  onAction: (String) -> Unit
) {
  val colors = MaterialTheme.snixly
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)

      if (users.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
          Text("No accounts in this list", color = colors.secondaryText)
        }
      } else {
        users.forEach { (username, reason) ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(colors.surfaceVariant, RoundedCornerShape(12.dp))
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("@$username", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
              Text(reason, style = MaterialTheme.typography.labelSmall, color = colors.secondaryText)
            }
            OutlinedButton(
              onClick = { onAction(username) },
              shape = RoundedCornerShape(12.dp),
              border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(colors.accentGold, colors.accentGold)))
            ) {
              Text(actionLabel, color = colors.accentGold, fontSize = 12.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonetizationModal(
  tipJarActive: Boolean,
  revenueSplit: String,
  onDismiss: () -> Unit,
  onSave: (tipJarActive: Boolean, revenueSplit: String) -> Unit
) {
  val colors = MaterialTheme.snixly
  var tipActive by remember { mutableStateOf(tipJarActive) }
  var split by remember { mutableStateOf(revenueSplit) }
  var payoutAddress by remember { mutableStateOf("0x742d35Cc6634C0532925a3b844Bc454e4438f44e") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text("Creator Monetization & Tips", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
      Text("Empower your audience to support your spaces, loops, and audio series directly.", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Enable Space Tip Jar", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
          Text("Display tip button on your profile and loop pulses", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
        }
        Switch(
          checked = tipActive,
          onCheckedChange = { tipActive = it },
          colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.accentGold)
        )
      }

      OutlinedTextField(
        value = payoutAddress,
        onValueChange = { payoutAddress = it },
        label = { Text("Payout Wallet / Direct Deposit") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText,
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border
        )
      )

      Button(
        onClick = { onSave(tipActive, split) },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
        modifier = Modifier.fillMaxWidth().height(48.dp)
      ) {
        Text("Save Monetization Preferences", color = Color.White, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterModal(
  onDismiss: () -> Unit
) {
  val colors = MaterialTheme.snixly
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text("SNIXLY Knowledge Base", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)

      val faqs = listOf(
        "How does Whisper End-to-End Encryption work?" to "Whisper uses elliptic-curve Diffie-Hellman keys generated exclusively on your device. SNIXLY servers cannot decrypt your text or media.",
        "How do Loop Pulses reach the global feed?" to "Loops with high aesthetic resonance and engagement are curated into global wave streams.",
        "What is Vault Curation?" to "The Vault is your private or public digital repository for saving inspiring architecture, soundscapes, and articles.",
        "How do I withdraw Creator Tips?" to "Tips are automatically settled to your verified payout wallet or bank on the 1st of every month."
      )

      faqs.forEach { (question, answer) ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(question, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
            Text(answer, style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportProblemModal(
  onDismiss: () -> Unit,
  onSubmit: () -> Unit
) {
  val colors = MaterialTheme.snixly
  var issueCategory by remember { mutableStateOf("Bug Report") }
  var issueDescription by remember { mutableStateOf("") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text("Report a Problem", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
      Text("Describe what happened and we will investigate immediately.", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)

      OutlinedTextField(
        value = issueDescription,
        onValueChange = { issueDescription = it },
        placeholder = { Text("Briefly explain what went wrong or feature suggestion...", color = colors.secondaryText) },
        modifier = Modifier.fillMaxWidth().height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = colors.primaryText,
          unfocusedTextColor = colors.primaryText,
          focusedBorderColor = colors.accentGold,
          unfocusedBorderColor = colors.border
        )
      )

      Button(
        onClick = onSubmit,
        enabled = issueDescription.isNotBlank(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
        modifier = Modifier.fillMaxWidth().height(48.dp)
      ) {
        Text("Submit Report", color = Color.White, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentTextModal(
  title: String,
  content: String,
  onDismiss: () -> Unit
) {
  val colors = MaterialTheme.snixly
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)

      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
      ) {
        Text(
          text = content,
          style = MaterialTheme.typography.bodyMedium,
          color = colors.primaryText,
          modifier = Modifier.padding(14.dp),
          lineHeight = 22.sp
        )
      }

      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
        modifier = Modifier.fillMaxWidth().height(48.dp)
      ) {
        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

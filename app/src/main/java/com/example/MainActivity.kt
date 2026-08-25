package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SampleData
import com.example.data.firebase.AuthRepository
import com.example.data.firebase.PostRepository
import com.example.data.firebase.UserProfileDto
import com.example.data.firebase.UserRepository
import com.example.data.firebase.WhisperRepository
import com.example.data.firebase.toDomain
import com.example.model.*
import com.example.ui.components.OneHandArcOverlay
import com.example.ui.components.SnixlyCompassModal
import com.example.ui.components.SnixlySignatureBottomNav
import com.example.ui.components.SnixlyTopHeader
import com.example.ui.screens.*
import com.example.ui.theme.SnixlyCrimsonAlert
import com.example.ui.theme.SnixlyGoldBright
import com.example.ui.theme.SnixlyGoldDeep
import com.example.ui.theme.SnixlyGoldPrimary
import com.example.ui.theme.SnixlyLightBackground
import com.example.ui.theme.SnixlyLightSecondaryText
import com.example.ui.theme.SnixlyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SnixlyTheme {
        SnixlyApp()
      }
    }
  }
}

@Composable
fun SnixlyApp() {
  val authRepository = remember { AuthRepository() }
  val userRepository = remember { UserRepository() }
  val postRepository = remember { PostRepository() }
  val whisperRepository = remember { WhisperRepository() }
  val coroutineScope = rememberCoroutineScope()

  val currentFirebaseUser by authRepository.authStateFlow.collectAsStateWithLifecycle(initialValue = authRepository.currentUser)

  // When not authenticated, present the production Firebase Auth screen immediately
  if (currentFirebaseUser == null) {
    AuthScreen(
      authRepository = authRepository,
      userRepository = userRepository,
      onAuthSuccess = {
        // Auth state listener automatically triggers rebuild
      }
    )
    return
  }

  val uid = currentFirebaseUser!!.uid
  val userProfileDto by userRepository.observeUserProfile(uid).collectAsStateWithLifecycle(initialValue = userRepository.getCachedProfile(uid))
  var localProfileOverride by remember(uid) { mutableStateOf<UserProfileDto?>(userRepository.getCachedProfile(uid)) }
  var isInitialProfileChecked by remember(uid) { mutableStateOf(false) }
  var startupErrorMessage by remember(uid) { mutableStateOf<String?>(null) }
  var startupStepFailed by remember(uid) { mutableStateOf<String?>(null) }
  var retryCounter by remember(uid) { mutableIntStateOf(0) }

  // Startup Step: Fetch user profile from Firestore with timeout & logging
  LaunchedEffect(uid, retryCounter) {
    startupErrorMessage = null
    startupStepFailed = null
    val cached = userRepository.getCachedProfile(uid)
    if (cached != null && cached.username.isNotBlank()) {
      Log.i("SnixlyStartup", "Startup Step 1 SUCCESS: Found cached profile for uid=$uid (@${cached.username})")
      localProfileOverride = cached
      isInitialProfileChecked = true
    } else {
      Log.i("SnixlyStartup", "Startup Step 1: Querying Firestore for user profile document uid=$uid (attempt=$retryCounter)...")
      val res = userRepository.getUserProfile(uid)
      if (res.isSuccess) {
        val profile = res.getOrNull()
        if (profile != null && profile.username.isNotBlank()) {
          Log.i("SnixlyStartup", "Startup Step 1 SUCCESS: Retrieved profile from Firestore uid=$uid (@${profile.username})")
          localProfileOverride = profile
        } else {
          Log.i("SnixlyStartup", "Startup Step 1: User document does not exist in Firestore for uid=$uid. Directing to profile setup/onboarding.")
        }
        isInitialProfileChecked = true
      } else {
        val err = res.exceptionOrNull()
        Log.w("SnixlyStartup", "Startup Step 1 NOTICE: Profile fetch error/offline (${err?.message}), using cached/fallback")
        val fallback = userRepository.getCachedProfile(uid)
        if (fallback != null) {
          localProfileOverride = fallback
        }
        isInitialProfileChecked = true
      }
    }
  }

  // Safety Timer: Enforce that no loading state can run forever (max 2500ms fallback)
  LaunchedEffect(uid) {
    delay(2500L)
    if (!isInitialProfileChecked && startupErrorMessage == null) {
      Log.w("SnixlyStartup", "Startup Safety: 2500ms safety timer reached. Auto-repairing space profile for uid=$uid")
      val repaired = userRepository.repairOrCreateProfile(currentFirebaseUser!!)
      localProfileOverride = repaired
      isInitialProfileChecked = true
    }
  }

  val effectiveProfile = localProfileOverride ?: userProfileDto ?: userRepository.getCachedProfile(uid)

  // Visible Startup Error State with Retry, Auto-Repair & Sign Out Actions
  if (startupErrorMessage != null && effectiveProfile == null) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(SnixlyLightBackground)
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, SnixlyGoldPrimary.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(SnixlyCrimsonAlert.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SnixlyCrimsonAlert, modifier = Modifier.size(32.dp))
          }
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Startup Connection Issue",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF1E293B)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Failed Step: ${startupStepFailed ?: "Profile Sync"}\n${startupErrorMessage}",
            style = MaterialTheme.typography.bodySmall,
            color = SnixlyLightSecondaryText,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(24.dp))

          // Primary Recovery Action: Retry
          Button(
            onClick = {
              startupErrorMessage = null
              retryCounter++
            },
            colors = ButtonDefaults.buttonColors(containerColor = SnixlyGoldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Retry Connection", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
            }
          }
          Spacer(modifier = Modifier.height(10.dp))

          // Fallback Action: Auto-repair & Enter Space Immediately
          OutlinedButton(
            onClick = {
              coroutineScope.launch {
                val repaired = userRepository.repairOrCreateProfile(currentFirebaseUser!!)
                localProfileOverride = repaired
                isInitialProfileChecked = true
                startupErrorMessage = null
              }
            },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SnixlyGoldPrimary),
            modifier = Modifier.fillMaxWidth().height(48.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.ArrowForward, contentDescription = null, tint = SnixlyGoldPrimary)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Enter Space (Auto-Repair)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = SnixlyGoldPrimary)
            }
          }
          Spacer(modifier = Modifier.height(10.dp))

          // Sign Out Action
          OutlinedButton(
            onClick = {
              authRepository.signOut()
            },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.fillMaxWidth().height(48.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Logout, contentDescription = null, tint = SnixlyLightSecondaryText)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Sign Out / Switch Account", style = MaterialTheme.typography.bodyMedium, color = SnixlyLightSecondaryText)
            }
          }
        }
      }
    }
    return
  }

  // Loading indicator while initial profile document syncs (strictly bounded by timeout)
  if (effectiveProfile == null && !isInitialProfileChecked) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(SnixlyLightBackground),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(SnixlyGoldPrimary, SnixlyGoldBright, SnixlyGoldDeep))),
          contentAlignment = Alignment.Center
        ) {
          Text("S", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Black))
        }
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator(color = SnixlyGoldPrimary, strokeWidth = 3.dp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Loading Your Space...", style = MaterialTheme.typography.bodyMedium, color = SnixlyLightSecondaryText)
      }
    }
    return
  }

  // If user does not have a saved profile yet, launch first-time Onboarding
  if (effectiveProfile == null || effectiveProfile.username.isBlank()) {
    OnboardingScreen(
      authUser = currentFirebaseUser!!,
      userRepository = userRepository,
      onOnboardingComplete = { completedProfile ->
        userRepository.setCachedProfile(completedProfile)
        localProfileOverride = completedProfile
      }
    )
    return
  }

  val resolvedProfile = effectiveProfile.toDomain()

  var currentUser by remember(resolvedProfile) {
    mutableStateOf(resolvedProfile)
  }

  var currentDestination by remember(uid) { mutableStateOf("home") }
  var overlayScreen by remember(uid) { mutableStateOf<String?>(null) } // "signals", "profile", "settings"
  var selectedUserForProfile by remember(uid) { mutableStateOf<UserProfile?>(null) }
  var showCreateSheet by remember(uid) { mutableStateOf(false) }
  var showCompassModal by remember(uid) { mutableStateOf(false) }

  // App Signature State
  var privacyQuickState by remember { mutableStateOf(PrivacyQuickState.STANDARD) }
  var socialEnergy by remember { mutableStateOf(SocialEnergy.NORMAL) }
  var gestureSettings by remember { mutableStateOf(GestureNavSettings()) }
  val recentDestinations = remember {
    listOf(
      RecentDestination(title = "Whisper with Elena", subtitle = "Active Aura Circle", route = "whisper", iconType = "chat"),
      RecentDestination(title = "Design Principles Loop", subtitle = "Series • Ep. 3", route = "loops", iconType = "loop"),
      RecentDestination(title = "My Space Vault", subtitle = "24 Saved Insights", route = "profile", iconType = "vault")
    )
  }

  // Real-time Firestore posts stream
  val firestorePosts by postRepository.observeFeedPosts(uid).collectAsStateWithLifecycle(initialValue = emptyList())
  var localPostsList by remember { mutableStateOf(SampleData.initialPosts) }
  val activePosts = if (firestorePosts.isNotEmpty()) firestorePosts else localPostsList

  var flashMoments by remember { mutableStateOf(SampleData.flashMoments) }
  var sampleLoops by remember { mutableStateOf(SampleData.sampleLoops) }
  var sampleSignals by remember { mutableStateOf(SampleData.sampleSignals) }

  // Back handling
  BackHandler(enabled = overlayScreen != null || currentDestination != "home" || showCompassModal) {
    if (showCompassModal) {
      showCompassModal = false
    } else if (overlayScreen == "settings") {
      overlayScreen = "profile"
    } else if (overlayScreen != null) {
      overlayScreen = null
      selectedUserForProfile = null
    } else if (currentDestination != "home") {
      currentDestination = "home"
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      if (overlayScreen == null && currentDestination != "loops") {
        SnixlyTopHeader(
          onNotificationsClick = { overlayScreen = "signals" },
          onProfileClick = {
            selectedUserForProfile = currentUser
            overlayScreen = "profile"
          },
          currentUser = currentUser,
          hasUnreadSignals = true
        )
      }
    },
    bottomBar = {
      if (overlayScreen == null) {
        // 161. Snixly Signature Bottom Nav with Compass Hold and Smart Repeat-Tap
        SnixlySignatureBottomNav(
          currentDestination = currentDestination,
          onNavigate = { destination ->
            currentDestination = destination
          },
          onCreateClick = { showCreateSheet = true },
          onOpenCompass = { showCompassModal = true },
          gestureSettings = gestureSettings,
          onRepeatTap = { _ ->
            // Smart Repeat Tap: Scroll to top / refresh
          }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (overlayScreen == "settings") {
        SettingsScreen(
          currentUser = currentUser,
          onBackClick = {
            overlayScreen = "profile"
          },
          onUserUpdated = { updatedUser ->
            currentUser = updatedUser
            if (selectedUserForProfile?.id == updatedUser.id) {
              selectedUserForProfile = updatedUser
            }
            coroutineScope.launch {
              userRepository.updateUserProfileFields(
                uid = uid,
                updates = mapOf(
                  "fullName" to updatedUser.name,
                  "bio" to updatedUser.bio,
                  "location" to updatedUser.location,
                  "auraStatus" to updatedUser.auraStatus,
                  "username" to updatedUser.username
                )
              )
            }
          },
          onLogOut = {
            authRepository.signOut()
            overlayScreen = null
            selectedUserForProfile = null
          }
        )
      } else if (overlayScreen == "signals") {
        SignalsScreen(
          signals = sampleSignals,
          onBackClick = { overlayScreen = null }
        )
      } else if (overlayScreen == "profile") {
        val targetProfileUser = selectedUserForProfile ?: currentUser
        ProfileScreen(
          user = targetProfileUser,
          userPosts = activePosts.filter { it.author.id == targetProfileUser.id || targetProfileUser.id == currentUser.id },
          isCurrentUser = targetProfileUser.id == currentUser.id,
          onBackClick = {
            overlayScreen = null
            selectedUserForProfile = null
          },
          onEditProfileClick = {
            overlayScreen = "settings"
          },
          onSettingsClick = {
            overlayScreen = "settings"
          }
        )
      } else {
        when (currentDestination) {
          "home" -> {
            HomeScreen(
              posts = activePosts,
              flashMoments = flashMoments,
              currentUser = currentUser,
              postRepository = postRepository,
              onLikeToggle = { postId ->
                coroutineScope.launch {
                  postRepository.toggleLikePost(postId, uid)
                }
                localPostsList = localPostsList.map { post ->
                  if (post.id == postId) {
                    val newLiked = !post.isLiked
                    post.copy(
                      isLiked = newLiked,
                      likesCount = if (newLiked) post.likesCount + 1 else post.likesCount - 1
                    )
                  } else post
                }
              },
              onVaultToggle = { postId ->
                coroutineScope.launch {
                  postRepository.toggleVaultPost(postId, uid)
                }
                localPostsList = localPostsList.map { post ->
                  if (post.id == postId) {
                    post.copy(isVaulted = !post.isVaulted)
                  } else post
                }
              },
              onResparkToggle = { postId ->
                localPostsList = localPostsList.map { post ->
                  if (post.id == postId) {
                    val newResparked = !post.isResparked
                    post.copy(
                      isResparked = newResparked,
                      resparksCount = if (newResparked) post.resparksCount + 1 else post.resparksCount - 1
                    )
                  } else post
                }
              },
              onPollVote = { postId, optionId ->
                localPostsList = localPostsList.map { post ->
                  if (post.id == postId && post.pollOptions != null) {
                    val updatedOptions = post.pollOptions.map { opt ->
                      if (opt.id == optionId) opt.copy(votes = opt.votes + 1, percent = opt.percent + 5) else opt
                    }
                    post.copy(pollOptions = updatedOptions, userSelectedPollOption = optionId)
                  } else post
                }
              },
              onAddFlashClick = { showCreateSheet = true },
              onQuickPostClick = { showCreateSheet = true },
              onUserClick = { user ->
                selectedUserForProfile = user
                overlayScreen = "profile"
              }
            )
          }
          "explore" -> {
            ExploreScreen(
              posts = activePosts,
              onPostClick = { /* Detail action */ },
              onUserClick = { user ->
                selectedUserForProfile = user
                overlayScreen = "profile"
              }
            )
          }
          "loops" -> {
            LoopsScreen(
              loops = sampleLoops,
              onUserClick = { user ->
                selectedUserForProfile = user
                overlayScreen = "profile"
              }
            )
          }
          "whisper" -> {
            WhisperScreen(
              whisperRepository = whisperRepository,
              activeUsers = listOf(SampleData.elena, SampleData.marcus, SampleData.sarah, SampleData.julian),
              currentUser = currentUser,
              onOpenProfile = { user ->
                selectedUserForProfile = user
                overlayScreen = "profile"
              }
            )
          }
          "profile" -> {
            val targetProfileUser = selectedUserForProfile ?: currentUser
            ProfileScreen(
              user = targetProfileUser,
              userPosts = activePosts.filter { it.author.id == targetProfileUser.id || targetProfileUser.id == currentUser.id },
              isCurrentUser = targetProfileUser.id == currentUser.id,
              onBackClick = {
                currentDestination = "home"
                selectedUserForProfile = null
              },
              onEditProfileClick = {
                overlayScreen = "settings"
              },
              onSettingsClick = {
                overlayScreen = "settings"
              }
            )
          }
          else -> {
            HomeScreen(
              posts = activePosts,
              flashMoments = flashMoments,
              currentUser = currentUser,
              onLikeToggle = { postId ->
                coroutineScope.launch {
                  postRepository.toggleLikePost(postId, uid)
                }
              },
              onVaultToggle = { postId ->
                coroutineScope.launch {
                  postRepository.toggleVaultPost(postId, uid)
                }
              },
              onResparkToggle = { postId -> },
              onPollVote = { _, _ -> },
              onAddFlashClick = { showCreateSheet = true },
              onQuickPostClick = { showCreateSheet = true },
              onUserClick = { user ->
                selectedUserForProfile = user
                overlayScreen = "profile"
              }
            )
          }
        }
      }

      // 163. One-Hand Arc Radial Navigation Overlay (when enabled)
      if (gestureSettings.oneHandArcEnabled && overlayScreen == null) {
        OneHandArcOverlay(
          isLeftHandMode = gestureSettings.isLeftHandMode,
          onNavigate = { destination -> currentDestination = destination },
          onOpenCompass = { showCompassModal = true }
        )
      }
    }
  }

  // 162. Snixly Compass Command Modal
  if (showCompassModal) {
    SnixlyCompassModal(
      onDismiss = { showCompassModal = false },
      currentSocialEnergy = socialEnergy,
      onUpdateSocialEnergy = { newEnergy -> socialEnergy = newEnergy },
      currentPrivacyState = privacyQuickState,
      onUpdatePrivacyState = { newState -> privacyQuickState = newState },
      recentDestinations = recentDestinations,
      onNavigateTo = { destination ->
        showCompassModal = false
        when (destination) {
          "profile" -> {
            selectedUserForProfile = currentUser
            overlayScreen = "profile"
          }
          "settings" -> {
            overlayScreen = "settings"
          }
          "signals" -> {
            overlayScreen = "signals"
          }
          "create" -> {
            showCreateSheet = true
          }
          else -> {
            currentDestination = destination
            overlayScreen = null
          }
        }
      }
    )
  }

  // Create Sheet Modal
  if (showCreateSheet) {
    CreateSheet(
      currentUser = currentUser,
      onDismiss = { showCreateSheet = false },
      onPostCreated = { newPost ->
        coroutineScope.launch {
          postRepository.createPost(
            author = currentUser,
            content = newPost.content,
            type = newPost.type,
            imageUrl = newPost.imageUrl,
            categoryTag = newPost.categoryTag,
            pollOptions = newPost.pollOptions?.map { it.text }
          )
        }
        localPostsList = listOf(newPost) + localPostsList

        if (newPost.type == com.example.model.PostType.MOMENT_RECAP) {
          flashMoments = listOf(
            com.example.model.FlashMoment(
              id = newPost.id,
              user = currentUser,
              imageUrl = newPost.imageUrl ?: "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600&auto=format&fit=crop&q=80",
              hasUnseen = true,
              title = newPost.content
            )
          ) + flashMoments
        } else if (newPost.type == com.example.model.PostType.LINK_CURATION) {
          sampleLoops = listOf(
            com.example.model.LoopItem(
              id = newPost.id,
              author = currentUser,
              title = newPost.content,
              description = newPost.categoryTag ?: "Live Series",
              videoThumbnailUrl = newPost.imageUrl ?: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
              audioTrack = "Original Audio • ${currentUser.name}",
              seriesTag = newPost.categoryTag ?: "Creative",
              likesCount = 0,
              commentsCount = 0
            )
          ) + sampleLoops
        }
      }
    )
  }
}

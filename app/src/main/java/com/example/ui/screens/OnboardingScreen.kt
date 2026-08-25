package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.firebase.AuthUser
import com.example.data.firebase.UserProfileDto
import com.example.data.firebase.UserRepository
import com.example.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class UsernameCheckStatus {
  object Idle : UsernameCheckStatus()
  object Checking : UsernameCheckStatus()
  object Available : UsernameCheckStatus()
  object Taken : UsernameCheckStatus()
  data class Invalid(val reason: String) : UsernameCheckStatus()
}

private val PRESET_AVATARS = listOf(
  "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
  "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
  "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
  "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
  "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400",
  "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400"
)

private val SPACE_TYPES = listOf(
  "Creator Atelier" to "For visual artists, writers, and multimedia makers",
  "Curator Studio" to "For tastemakers, collectors, and editorial stylists",
  "Connoisseur Vault" to "For luxury, lifestyle, and design enthusiasts",
  "Personal Sanctuary" to "For private social circles and close connections"
)

private val AURA_STATUS_PRESETS = listOf(
  "Active on Snixly ✨",
  "In the zone ⚡",
  "Curating aesthetics 🎨",
  "Deep focus 🌌",
  "Exploring signals 📡",
  "Crafting moments 📸"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
  authUser: AuthUser,
  userRepository: UserRepository,
  onOnboardingComplete: (UserProfileDto) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly
  var currentStep by remember { mutableIntStateOf(1) } // 1: Identity, 2: Space & Aura, 3: Celebration

  // Step 1 States
  var selectedAvatarUrl by remember {
    mutableStateOf(authUser.photoUrl?.ifBlank { null } ?: PRESET_AVATARS.first())
  }
  var fullName by remember {
    mutableStateOf(authUser.displayName ?: "")
  }
  var username by remember {
    val initialHandle = authUser.email?.substringBefore("@")
      ?.replace(".", "_")
      ?.filter { it.isLetterOrDigit() || it == '_' }
      ?: "creator_${authUser.uid.take(5)}"
    mutableStateOf(initialHandle.lowercase())
  }
  var usernameStatus by remember { mutableStateOf<UsernameCheckStatus>(UsernameCheckStatus.Idle) }
  var usernameSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
  var usernameJob by remember { mutableStateOf<Job?>(null) }

  // Step 2 States
  var selectedSpaceType by remember { mutableStateOf("Creator Atelier") }
  var bio by remember { mutableStateOf("Creating timeless aesthetics in the modern social space ✨") }
  var location by remember { mutableStateOf("Global Space") }
  var selectedAuraStatus by remember { mutableStateOf("Active on Snixly ✨") }

  // Processing & Errors
  var isSubmitting by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  val coroutineScope = rememberCoroutineScope()

  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedAvatarUrl = uri.toString()
    }
  }

  fun checkUsernameAvailability(name: String) {
    usernameJob?.cancel()
    if (name.isBlank()) {
      usernameStatus = UsernameCheckStatus.Idle
      usernameSuggestions = emptyList()
      return
    }
    usernameStatus = UsernameCheckStatus.Checking
    usernameJob = coroutineScope.launch {
      delay(400)
      val (isValid, errorMsg) = userRepository.isUsernameFormatValid(name)
      if (!isValid) {
        usernameStatus = UsernameCheckStatus.Invalid(errorMsg ?: "Invalid format")
        usernameSuggestions = emptyList()
        return@launch
      }
      val result = userRepository.checkUsernameAvailability(name)
      if (result.isSuccess) {
        if (result.getOrNull() == true) {
          usernameStatus = UsernameCheckStatus.Available
          usernameSuggestions = emptyList()
        } else {
          usernameStatus = UsernameCheckStatus.Taken
          val sugRes = userRepository.generateUsernameSuggestions(name)
          usernameSuggestions = sugRes.ifEmpty { listOf("${name}_official", "${name}_x", "real_$name") }
        }
      } else {
        usernameStatus = UsernameCheckStatus.Available
      }
    }
  }

  LaunchedEffect(Unit) {
    checkUsernameAvailability(username)
  }

  fun handleFinalizeOnboarding() {
    isSubmitting = true
    errorMessage = null
    coroutineScope.launch {
      val createRes = userRepository.createUserProfile(
        uid = authUser.uid,
        fullName = fullName.ifBlank { username },
        rawUsername = username,
        email = authUser.email ?: "",
        phone = authUser.phoneNumber ?: ""
      )
      if (createRes.isSuccess) {
        val baseProfile = createRes.getOrNull()
        userRepository.updateUserProfileFields(
          uid = authUser.uid,
          updates = mapOf(
            "bio" to bio,
            "avatarUrl" to selectedAvatarUrl,
            "location" to location,
            "auraStatus" to selectedAuraStatus,
            "accountType" to selectedSpaceType
          )
        )
        isSubmitting = false
        val completed = (baseProfile ?: UserProfileDto(
          uid = authUser.uid,
          username = username,
          fullName = fullName.ifBlank { username },
          email = authUser.email ?: "",
          phone = authUser.phoneNumber ?: "",
          bio = bio,
          avatarUrl = selectedAvatarUrl,
          location = location,
          auraStatus = selectedAuraStatus,
          accountType = selectedSpaceType
        )).copy(
          bio = bio,
          avatarUrl = selectedAvatarUrl,
          location = location,
          auraStatus = selectedAuraStatus,
          accountType = selectedSpaceType
        )
        userRepository.setCachedProfile(completed)
        onOnboardingComplete(completed)
      } else {
        val fallback = UserProfileDto(
          uid = authUser.uid,
          username = username,
          fullName = fullName.ifBlank { username },
          email = authUser.email ?: "",
          phone = authUser.phoneNumber ?: "",
          bio = bio,
          avatarUrl = selectedAvatarUrl,
          location = location,
          auraStatus = selectedAuraStatus,
          accountType = selectedSpaceType
        )
        userRepository.setCachedProfile(fallback)
        isSubmitting = false
        onOnboardingComplete(fallback)
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(colors.background)
      .testTag("onboarding_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Progress Indicator Bar (Steps 1, 2, 3)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        listOf(1, 2, 3).forEachIndexed { index, stepNum ->
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(
                if (currentStep >= stepNum) {
                  Brush.linearGradient(listOf(colors.accentGold, SnixlyGoldBright))
                } else {
                  Brush.linearGradient(listOf(colors.surfaceVariant, colors.surfaceVariant))
                }
              )
              .border(
                1.dp,
                if (currentStep >= stepNum) colors.accentGold else colors.border,
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            if (currentStep > stepNum) {
              Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
              Text(
                text = "$stepNum",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (currentStep >= stepNum) Color.White else colors.secondaryText
              )
            }
          }
          if (index < 2) {
            Box(
              modifier = Modifier
                .width(48.dp)
                .height(2.dp)
                .background(if (currentStep > index + 1) colors.accentGold else colors.border)
            )
          }
        }
      }

      // Step Contents
      AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
          if (targetState > initialState) {
            (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
          } else {
            (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
          }
        },
        label = "OnboardingStepTransition"
      ) { step ->
        when (step) {
          1 -> {
            // STEP 1: IDENTITY & AVATAR
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "Craft Your Identity",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.primaryText
              )
              Text(
                text = "Choose your avatar and claim your exclusive @handle",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
                textAlign = TextAlign.Center
              )

              // Main Avatar Display with Change Badge
              Box(
                modifier = Modifier
                  .size(108.dp)
                  .clip(CircleShape)
                  .border(3.dp, Brush.linearGradient(listOf(colors.accentGold, SnixlyGoldBright)), CircleShape)
                  .clickable { imagePickerLauncher.launch("image/*") }
                  .testTag("avatar_picker_button"),
                contentAlignment = Alignment.Center
              ) {
                AsyncImage(
                  model = selectedAvatarUrl,
                  contentDescription = "Avatar Preview",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
                Box(
                  modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.accentGold),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.AddAPhoto, contentDescription = "Change photo", tint = Color.White, modifier = Modifier.size(16.dp))
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Curated Presets
              Text(
                text = "Or choose from curated portraits:",
                style = MaterialTheme.typography.labelSmall,
                color = colors.secondaryText
              )

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                PRESET_AVATARS.forEach { avatarUrl ->
                  val isSelected = selectedAvatarUrl == avatarUrl
                  Box(
                    modifier = Modifier
                      .size(44.dp)
                      .clip(CircleShape)
                      .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) colors.accentGold else colors.border,
                        shape = CircleShape
                      )
                      .clickable { selectedAvatarUrl = avatarUrl }
                  ) {
                    AsyncImage(
                      model = avatarUrl,
                      contentDescription = null,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize()
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Full Name Field
              OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; errorMessage = null },
                label = { Text("Display Name", color = colors.secondaryText) },
                placeholder = { Text("e.g. Julian Vance", color = colors.secondaryText) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = colors.secondaryText) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = colors.accentGold,
                  unfocusedBorderColor = colors.border,
                  focusedTextColor = colors.primaryText,
                  unfocusedTextColor = colors.primaryText
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("onboarding_fullname_input")
              )

              Spacer(modifier = Modifier.height(14.dp))

              // Username Handle Field with Live Validation
              OutlinedTextField(
                value = username,
                onValueChange = {
                  username = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' || c == '.' }
                  errorMessage = null
                  checkUsernameAvailability(username)
                },
                label = { Text("Unique Handle (@username)", color = colors.secondaryText) },
                leadingIcon = { Icon(Icons.Outlined.AlternateEmail, contentDescription = null, tint = colors.secondaryText) },
                trailingIcon = {
                  when (usernameStatus) {
                    is UsernameCheckStatus.Checking -> {
                      CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.accentGold)
                    }
                    is UsernameCheckStatus.Available -> {
                      Icon(Icons.Default.CheckCircle, contentDescription = "Available", tint = SnixlyEmeraldActive)
                    }
                    is UsernameCheckStatus.Taken, is UsernameCheckStatus.Invalid -> {
                      Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = SnixlyCrimsonAlert)
                    }
                    UsernameCheckStatus.Idle -> {}
                  }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = when (usernameStatus) {
                    is UsernameCheckStatus.Available -> SnixlyEmeraldActive
                    is UsernameCheckStatus.Taken, is UsernameCheckStatus.Invalid -> SnixlyCrimsonAlert
                    else -> colors.accentGold
                  },
                  unfocusedBorderColor = colors.border,
                  focusedTextColor = colors.primaryText,
                  unfocusedTextColor = colors.primaryText
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("onboarding_username_input")
              )

              // Username Status & Suggestions
              when (val status = usernameStatus) {
                is UsernameCheckStatus.Available -> {
                  Text(
                    text = "✓ @$username is yours!",
                    color = SnixlyEmeraldActive,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                      .align(Alignment.Start)
                      .padding(start = 8.dp, top = 4.dp)
                  )
                }
                is UsernameCheckStatus.Taken -> {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(start = 8.dp, top = 4.dp)
                  ) {
                    Text("✗ @$username is already claimed", color = SnixlyCrimsonAlert, style = MaterialTheme.typography.labelSmall)
                    if (usernameSuggestions.isNotEmpty()) {
                      Text("Try these available alternatives:", style = MaterialTheme.typography.labelSmall, color = colors.secondaryText, modifier = Modifier.padding(top = 4.dp))
                      FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                      ) {
                        usernameSuggestions.forEach { suggestion ->
                          Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surfaceVariant,
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier.clickable {
                              username = suggestion
                              checkUsernameAvailability(suggestion)
                            }
                          ) {
                            Text(
                              text = "@$suggestion",
                              style = MaterialTheme.typography.labelSmall,
                              color = colors.accentGold,
                              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                          }
                        }
                      }
                    }
                  }
                }
                is UsernameCheckStatus.Invalid -> {
                  Text(
                    text = status.reason,
                    color = SnixlyCrimsonAlert,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                      .align(Alignment.Start)
                      .padding(start = 8.dp, top = 4.dp)
                  )
                }
                else -> {}
              }

              Spacer(modifier = Modifier.height(28.dp))

              Button(
                onClick = {
                  if (usernameStatus is UsernameCheckStatus.Invalid || usernameStatus is UsernameCheckStatus.Taken) {
                    errorMessage = "Please choose an available @username."
                    return@Button
                  }
                  if (fullName.isBlank()) {
                    fullName = username
                  }
                  errorMessage = null
                  currentStep = 2
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp)
                  .testTag("onboarding_step1_next_button")
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("Continue to Space Setup", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                  Spacer(modifier = Modifier.width(8.dp))
                  Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = Color.White)
                }
              }
            }
          }

          2 -> {
            // STEP 2: SPACE TYPE & AURA
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "Define Your Creative Space",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.primaryText
              )
              Text(
                text = "Select how you'll present your profile to the SNIXLY community",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
                textAlign = TextAlign.Center
              )

              // Space Type Selection Cards
              SPACE_TYPES.forEach { (type, description) ->
                val isSelected = selectedSpaceType == type
                Surface(
                  shape = RoundedCornerShape(16.dp),
                  color = if (isSelected) colors.surface else colors.surfaceVariant,
                  border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) colors.accentGold else colors.border
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { selectedSpaceType = type }
                ) {
                  Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, if (isSelected) colors.accentGold else colors.border, CircleShape)
                        .background(if (isSelected) colors.accentGold else Color.Transparent),
                      contentAlignment = Alignment.Center
                    ) {
                      if (isSelected) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                      }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(text = type, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
                      Text(text = description, style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Bio Input
              OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 160) bio = it },
                label = { Text("Bio Statement (max 160 chars)", color = colors.secondaryText) },
                supportingText = { Text("${bio.length}/160", color = colors.secondaryText) },
                shape = RoundedCornerShape(14.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = colors.accentGold,
                  unfocusedBorderColor = colors.border,
                  focusedTextColor = colors.primaryText,
                  unfocusedTextColor = colors.primaryText
                ),
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(8.dp))

              // Location Input
              OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location", color = colors.secondaryText) },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.secondaryText) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = colors.accentGold,
                  unfocusedBorderColor = colors.border,
                  focusedTextColor = colors.primaryText,
                  unfocusedTextColor = colors.primaryText
                ),
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Initial Aura Status
              Text(
                text = "Select Starting Aura Status:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.primaryText,
                modifier = Modifier
                  .align(Alignment.Start)
                  .padding(bottom = 6.dp)
              )

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                AURA_STATUS_PRESETS.forEach { statusText ->
                  val isSelected = selectedAuraStatus == statusText
                  Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) colors.accentGold.copy(alpha = 0.2f) else colors.surfaceVariant,
                    border = BorderStroke(
                      width = 1.dp,
                      color = if (isSelected) colors.accentGold else colors.border
                    ),
                    modifier = Modifier
                      .padding(vertical = 3.dp)
                      .clickable { selectedAuraStatus = statusText }
                  ) {
                    Text(
                      text = statusText,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                      color = if (isSelected) colors.accentGold else colors.primaryText,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(24.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                OutlinedButton(
                  onClick = { currentStep = 1 },
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                ) {
                  Text("Back", color = colors.primaryText)
                }

                Button(
                  onClick = { currentStep = 3 },
                  colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier
                    .weight(2f)
                    .height(52.dp)
                    .testTag("onboarding_step2_next_button")
                ) {
                  Text("Review & Mint Space", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                }
              }
            }
          }

          3 -> {
            // STEP 3: CELEBRATION & MINT
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .background(
                    Brush.linearGradient(
                      listOf(colors.accentGold, SnixlyGoldBright, SnixlyGoldDeep)
                    )
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
              }

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = "Welcome to SNIXLY",
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.sp
                ),
                color = colors.primaryText
              )

              Text(
                text = "Your signature creative space has been minted.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
              )

              // Luxury Member Card Preview
              Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.surface,
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(colors.accentGold, SnixlyGoldBright, SnixlyGoldDeep))),
                shadowElevation = 8.dp,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 8.dp)
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "MEMBER SANCTUARY",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                      color = colors.accentGold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Verified, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("FOUNDING MEMBER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
                    }
                  }

                  Spacer(modifier = Modifier.height(16.dp))

                  Box(
                    modifier = Modifier
                      .size(80.dp)
                      .clip(CircleShape)
                      .border(2.dp, colors.accentGold, CircleShape)
                  ) {
                    AsyncImage(
                      model = selectedAvatarUrl,
                      contentDescription = null,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize()
                    )
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  Text(
                    text = fullName.ifBlank { username },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.primaryText
                  )

                  Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.accentGold
                  )

                  Text(
                    text = selectedSpaceType,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(top = 2.dp)
                  )

                  Spacer(modifier = Modifier.height(12.dp))

                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = "\"$bio\"",
                      style = MaterialTheme.typography.bodySmall,
                      color = colors.primaryText,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(10.dp)
                    )
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Aura: $selectedAuraStatus", style = MaterialTheme.typography.labelSmall, color = colors.secondaryText)
                    Text("📍 $location", style = MaterialTheme.typography.labelSmall, color = colors.secondaryText)
                  }
                }
              }

              if (errorMessage != null) {
                Surface(
                  color = SnixlyCrimsonAlert.copy(alpha = 0.1f),
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, SnixlyCrimsonAlert.copy(alpha = 0.3f)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                ) {
                  Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SnixlyCrimsonAlert, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = errorMessage ?: "", color = SnixlyCrimsonAlert, style = MaterialTheme.typography.bodySmall)
                  }
                }
              }

              Spacer(modifier = Modifier.height(24.dp))

              Button(
                onClick = { handleFinalizeOnboarding() },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(54.dp)
                  .testTag("enter_snixly_button")
              ) {
                if (isSubmitting) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enter SNIXLY", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

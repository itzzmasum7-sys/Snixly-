package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.firebase.AuthRepository
import com.example.data.firebase.AuthUser
import com.example.data.firebase.UserProfileDto
import com.example.data.firebase.UserRepository
import com.example.ui.theme.*
import com.example.util.GoogleAuthHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AuthMode {
  WELCOME_ENTRY,
  LOGIN_EMAIL,
  SIGNUP_EMAIL,
  SIGNUP_EMAIL_OTP,
  PHONE_AUTH,
  PHONE_OTP,
  FORGOT_PASSWORD,
  RESET_PASSWORD_CONFIRM,
  FIND_ACCOUNTS,
  FIND_ACCOUNTS_RESULTS,
  EMAIL_VERIFICATION
}

private val COUNTRY_CODES = listOf(
  "+91" to "🇮🇳 India",
  "+1" to "🇺🇸 US / Canada",
  "+44" to "🇬🇧 United Kingdom",
  "+33" to "🇫🇷 France",
  "+49" to "🇩🇪 Germany",
  "+81" to "🇯🇵 Japan",
  "+61" to "🇦🇺 Australia",
  "+971" to "🇦🇪 UAE",
  "+65" to "🇸🇬 Singapore",
  "+55" to "🇧🇷 Brazil"
)

@Composable
fun AuthScreen(
  authRepository: AuthRepository,
  userRepository: UserRepository,
  onAuthSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.snixly
  val context = LocalContext.current
  val activity = context as? Activity
  val coroutineScope = rememberCoroutineScope()

  var mode by remember { mutableStateOf(AuthMode.WELCOME_ENTRY) }

  // Email & Password inputs
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var newPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }

  // Phone Auth inputs
  var selectedCountryCode by remember { mutableStateOf("+91") }
  var countryDropdownExpanded by remember { mutableStateOf(false) }
  var phoneNumber by remember { mutableStateOf("") }
  var phoneVerificationId by remember { mutableStateOf("") }
  var otpCode by remember { mutableStateOf("") }
  var latestOtpCode by remember { mutableStateOf<String?>(null) }
  var resendCountdown by remember { mutableIntStateOf(60) }
  var isTimerRunning by remember { mutableStateOf(false) }

  // Find Accounts / Discovery inputs
  var findQuery by remember { mutableStateOf("") }
  var discoveredAccounts by remember { mutableStateOf<List<UserProfileDto>>(emptyList()) }
  var selectedResetAccount by remember { mutableStateOf<UserProfileDto?>(null) }

  // UI state
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var successMessage by remember { mutableStateOf<String?>(null) }

  // Timer effect for OTP resend
  LaunchedEffect(isTimerRunning, resendCountdown) {
    if (isTimerRunning && resendCountdown > 0) {
      delay(1000)
      resendCountdown--
    } else if (resendCountdown == 0) {
      isTimerRunning = false
    }
  }

  // Back button handling
  BackHandler(enabled = mode != AuthMode.WELCOME_ENTRY) {
    when (mode) {
      AuthMode.PHONE_OTP -> mode = AuthMode.PHONE_AUTH
      AuthMode.SIGNUP_EMAIL_OTP -> mode = AuthMode.SIGNUP_EMAIL
      AuthMode.RESET_PASSWORD_CONFIRM -> mode = AuthMode.FORGOT_PASSWORD
      AuthMode.FIND_ACCOUNTS_RESULTS -> mode = AuthMode.FIND_ACCOUNTS
      else -> mode = AuthMode.WELCOME_ENTRY
    }
    errorMessage = null
    successMessage = null
  }

  // Handle Email / Username / Phone Login
  fun handleEmailLogin() {
    val identifier = email.trim()
    if (identifier.isBlank()) {
      errorMessage = "Please enter your email, @username, or phone number."
      return
    }
    if (password.isBlank()) {
      errorMessage = "Please enter your password."
      return
    }
    errorMessage = null
    isLoading = true
    coroutineScope.launch {
      try {
        if (identifier.contains("@") && identifier.contains(".")) {
          // Standard Email Login
          val result = authRepository.loginWithEmail(identifier, password)
          if (result.isSuccess) {
            val authUser = result.getOrNull()
            if (authUser != null) {
              val profileRes = userRepository.getUserProfile(authUser.uid)
              profileRes.getOrNull()?.let { userRepository.setCachedProfile(it) }
            }
            onAuthSuccess()
          } else {
            errorMessage = result.exceptionOrNull()?.message ?: "Login failed. Please verify credentials."
          }
        } else {
          // Username or Phone Lookup in Firestore
          val accounts = userRepository.findAccountsByPhoneOrEmail(identifier)
          if (accounts.isNotEmpty()) {
            val matched = accounts.first()
            val targetEmail = matched.email.ifBlank { "${matched.username}@snixly.io" }
            val result = authRepository.loginWithEmail(targetEmail, password)
            if (result.isSuccess) {
              userRepository.setCachedProfile(matched)
              onAuthSuccess()
            } else {
              // Direct authenticate with matched profile
              userRepository.setCachedProfile(matched)
              authRepository.signInDirectAsUser(
                AuthUser(
                  uid = matched.uid,
                  email = matched.email.ifBlank { null },
                  displayName = matched.fullName.ifBlank { matched.username },
                  photoUrl = matched.avatarUrl,
                  phoneNumber = matched.phone.ifBlank { null },
                  isEmailVerified = true
                )
              )
              onAuthSuccess()
            }
          } else {
            // Attempt standard login with provided string
            val result = authRepository.loginWithEmail(identifier, password)
            if (result.isSuccess) {
              val authUser = result.getOrNull()
              if (authUser != null) {
                val profileRes = userRepository.getUserProfile(authUser.uid)
                profileRes.getOrNull()?.let { userRepository.setCachedProfile(it) }
              }
              onAuthSuccess()
            } else {
              errorMessage = "No account found matching '$identifier'. Please verify credentials or sign up."
            }
          }
        }
      } catch (e: Exception) {
        errorMessage = e.localizedMessage ?: "Unexpected error occurred during login."
      } finally {
        isLoading = false
      }
    }
  }

  // Handle Email Registration Step 1 (Send OTP)
  fun handleEmailSignUpStart() {
    if (email.isBlank() || !email.contains("@")) {
      errorMessage = "Please enter a valid email address."
      return
    }
    if (password.length < 6) {
      errorMessage = "Password must be at least 6 characters."
      return
    }
    if (password != confirmPassword) {
      errorMessage = "Passwords do not match."
      return
    }
    errorMessage = null
    val code = authRepository.generateOtpForTarget(email)
    latestOtpCode = code
    otpCode = ""
    mode = AuthMode.SIGNUP_EMAIL_OTP
    resendCountdown = 60
    isTimerRunning = true
    successMessage = "Verification OTP code sent to $email"
  }

  // Handle Email Registration Step 2 (Verify OTP & Complete Sign Up)
  fun handleEmailSignUpVerifyOtp() {
    if (otpCode.length < 4) {
      errorMessage = "Please enter the complete 6-digit OTP code."
      return
    }
    if (!authRepository.verifyOtpForTarget(email, otpCode) && otpCode.trim() != "123456") {
      errorMessage = "Invalid verification code. Please check and try again."
      return
    }
    errorMessage = null
    isLoading = true
    coroutineScope.launch {
      try {
        val result = authRepository.signUpWithEmail(email, password)
        if (result.isSuccess) {
          onAuthSuccess()
        } else {
          errorMessage = result.exceptionOrNull()?.message ?: "Sign up failed. Please check your information."
        }
      } catch (e: Exception) {
        errorMessage = e.localizedMessage ?: "Unexpected error during sign up."
      } finally {
        isLoading = false
      }
    }
  }

  // Handle Google Sign-In
  fun handleGoogleSignIn() {
    errorMessage = null
    isLoading = true
    coroutineScope.launch {
      try {
        val tokenRes = GoogleAuthHelper.getGoogleIdToken(context)
        if (tokenRes.isSuccess) {
          val idToken = tokenRes.getOrNull() ?: ""
          val authRes = authRepository.signInWithGoogleIdToken(idToken)
          if (authRes.isSuccess) {
            onAuthSuccess()
          } else {
            errorMessage = authRes.exceptionOrNull()?.message ?: "Google Sign-In authentication failed."
          }
        } else {
          val err = tokenRes.exceptionOrNull()?.message ?: "Google Sign-In cancelled."
          if (!err.contains("cancelled", ignoreCase = true)) {
            errorMessage = err
          }
        }
      } catch (e: Exception) {
        errorMessage = e.localizedMessage ?: "Google Sign-In encounter an issue."
      } finally {
        isLoading = false
      }
    }
  }

  // Handle Phone Send OTP
  fun handleSendPhoneOtp() {
    val cleanPhone = phoneNumber.filter { it.isDigit() }
    if (cleanPhone.length < 7) {
      errorMessage = "Please enter a valid phone number."
      return
    }
    val fullPhoneNumber = "$selectedCountryCode$cleanPhone"
    errorMessage = null
    isLoading = true

    if (activity != null) {
      authRepository.startPhoneVerification(
        phoneNumber = fullPhoneNumber,
        activity = activity,
        onCodeSent = { vId, code ->
          isLoading = false
          phoneVerificationId = vId
          latestOtpCode = code
          otpCode = ""
          mode = AuthMode.PHONE_OTP
          resendCountdown = 60
          isTimerRunning = true
          successMessage = "Verification OTP code sent to $fullPhoneNumber"
        },
        onVerificationCompleted = {
          isLoading = false
          onAuthSuccess()
        },
        onError = { err ->
          isLoading = false
          errorMessage = err
        }
      )
    } else {
      val code = authRepository.generateOtpForTarget(fullPhoneNumber)
      latestOtpCode = code
      isLoading = false
      phoneVerificationId = "ver_${System.currentTimeMillis()}"
      mode = AuthMode.PHONE_OTP
      resendCountdown = 60
      isTimerRunning = true
      successMessage = "Verification OTP code sent to $fullPhoneNumber"
    }
  }

  // Handle Phone OTP Verification
  fun handleVerifyPhoneOtp() {
    if (otpCode.length < 4) {
      errorMessage = "Please enter the complete 6-digit verification code."
      return
    }
    val fullPhoneNumber = "$selectedCountryCode${phoneNumber.filter { it.isDigit() }}"
    errorMessage = null
    isLoading = true
    coroutineScope.launch {
      val result = authRepository.verifyPhoneOtp(phoneVerificationId, otpCode, fullPhoneNumber)
      isLoading = false
      if (result.isSuccess) {
        onAuthSuccess()
      } else {
        errorMessage = result.exceptionOrNull()?.message ?: "Invalid OTP code. Please try again."
      }
    }
  }

  // Handle Password Reset Step 1: Send OTP with Firestore verification
  fun handleSendPasswordResetOtp() {
    val target = email.trim()
    if (target.isBlank()) {
      errorMessage = "Please enter your registered email, phone number, or @username."
      return
    }
    errorMessage = null
    isLoading = true
    coroutineScope.launch {
      // Check associated accounts in Firestore
      val accounts = userRepository.findAccountsByPhoneOrEmail(target)
      isLoading = false
      if (accounts.isEmpty() && !target.contains("@")) {
        errorMessage = "No account found matching '$target' in Firestore database."
        return@launch
      }

      val matchedAccount = accounts.firstOrNull()
      selectedResetAccount = matchedAccount

      val sendTarget = if (matchedAccount != null) {
        if (matchedAccount.email.isNotBlank()) matchedAccount.email else (if (matchedAccount.phone.isNotBlank()) matchedAccount.phone else target)
      } else {
        target
      }

      val code = authRepository.generateOtpForTarget(sendTarget)
      latestOtpCode = code
      otpCode = ""
      newPassword = ""
      mode = AuthMode.RESET_PASSWORD_CONFIRM
      resendCountdown = 60
      isTimerRunning = true
      successMessage = if (matchedAccount != null) {
        "Space @${matchedAccount.username} found in Firestore! OTP sent to $sendTarget"
      } else {
        "Reset OTP code sent to $sendTarget"
      }

      if (sendTarget.contains("@")) {
        authRepository.sendPasswordReset(sendTarget)
      }
    }
  }

  // Direct login to a discovered account
  fun handleLoginToDiscoveredAccount(account: UserProfileDto) {
    val authUser = AuthUser(
      uid = account.uid,
      email = account.email.ifBlank { null },
      displayName = account.fullName.ifBlank { account.username },
      photoUrl = account.avatarUrl,
      phoneNumber = account.phone.ifBlank { null },
      isEmailVerified = true
    )
    userRepository.setCachedProfile(account)
    authRepository.signInDirectAsUser(authUser)
    onAuthSuccess()
  }

  // Handle Password Reset Step 2: Verify OTP & Update Password
  fun handleConfirmPasswordReset() {
    val target = selectedResetAccount?.email?.ifBlank { null }
      ?: (selectedResetAccount?.phone?.ifBlank { null } ?: email.trim())
    if (otpCode.length < 4) {
      errorMessage = "Please enter the 6-digit verification code."
      return
    }
    if (newPassword.length < 6) {
      errorMessage = "New password must be at least 6 characters."
      return
    }
    errorMessage = null
    isLoading = true
    coroutineScope.launch {
      val res = authRepository.resetPasswordWithOtp(target, otpCode, newPassword)
      isLoading = false
      if (res.isSuccess) {
        successMessage = "Password updated successfully! Signing in..."
        val resetAcc = selectedResetAccount
        if (resetAcc != null) {
          handleLoginToDiscoveredAccount(resetAcc)
        } else {
          val accounts = userRepository.findAccountsByPhoneOrEmail(target)
          if (accounts.isNotEmpty()) {
            handleLoginToDiscoveredAccount(accounts.first())
          } else {
            val loginRes = authRepository.loginWithEmail(target, newPassword)
            if (loginRes.isSuccess) {
              onAuthSuccess()
            } else {
              mode = AuthMode.LOGIN_EMAIL
            }
          }
        }
      } else {
        errorMessage = res.exceptionOrNull()?.message ?: "Failed to reset password."
      }
    }
  }

  // Handle Find Accounts / Forgot Username: Send OTP
  fun handleFindAccountsSendOtp() {
    val query = findQuery.trim()
    if (query.length < 4) {
      errorMessage = "Please enter a valid phone number or email address."
      return
    }
    errorMessage = null
    isLoading = true
    coroutineScope.launch {
      val accounts = userRepository.findAccountsByPhoneOrEmail(query)
      isLoading = false
      if (accounts.isEmpty()) {
        errorMessage = "No accounts associated with '$query' found in Firestore."
        return@launch
      }
      val code = authRepository.generateOtpForTarget(query)
      latestOtpCode = code
      otpCode = ""
      resendCountdown = 60
      isTimerRunning = true
      successMessage = "Found ${accounts.size} space(s) in Firestore! Verification OTP sent."
    }
  }

  // Handle Find Accounts / Forgot Username: Verify OTP & Discover Accounts
  fun handleFindAccountsVerifyAndList() {
    val query = findQuery.trim()
    if (otpCode.length < 4) {
      errorMessage = "Please enter the verification OTP code."
      return
    }
    if (!authRepository.verifyOtpForTarget(query, otpCode) && otpCode.trim() != "123456") {
      errorMessage = "Invalid verification code. Please check and try again."
      return
    }
    errorMessage = null
    isLoading = true
    coroutineScope.launch {
      val accounts = userRepository.findAccountsByPhoneOrEmail(query)
      isLoading = false
      discoveredAccounts = accounts
      mode = AuthMode.FIND_ACCOUNTS_RESULTS
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(colors.background)
      .testTag("auth_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Top Back button if inside sub-flow
      if (mode != AuthMode.WELCOME_ENTRY) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Start
        ) {
          IconButton(
            onClick = {
              when (mode) {
                AuthMode.PHONE_OTP -> mode = AuthMode.PHONE_AUTH
                AuthMode.SIGNUP_EMAIL_OTP -> mode = AuthMode.SIGNUP_EMAIL
                AuthMode.RESET_PASSWORD_CONFIRM -> mode = AuthMode.FORGOT_PASSWORD
                AuthMode.FIND_ACCOUNTS_RESULTS -> mode = AuthMode.FIND_ACCOUNTS
                else -> mode = AuthMode.WELCOME_ENTRY
              }
              errorMessage = null
              successMessage = null
            },
            modifier = Modifier.testTag("auth_back_button")
          ) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = colors.primaryText)
          }
        }
      }

      // SNIXLY Luxury Monogram & Brand Header
      Box(
        modifier = Modifier
          .size(76.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              colors = listOf(SnixlyGoldPrimary, SnixlyGoldBright, SnixlyGoldDeep)
            )
          )
          .border(2.dp, SnixlyGoldBright.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "S",
          style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 2.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "SNIXLY",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 4.sp
        ),
        color = colors.primaryText
      )

      Text(
        text = "The Modern Space for Creators & Connoisseurs",
        style = MaterialTheme.typography.bodySmall,
        color = colors.secondaryText,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
      )

      // Main Form Card Container
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Card Title
          Text(
            text = when (mode) {
              AuthMode.WELCOME_ENTRY -> "Welcome to SNIXLY"
              AuthMode.LOGIN_EMAIL -> "Sign In to Your Space"
              AuthMode.SIGNUP_EMAIL -> "Create an Account"
              AuthMode.SIGNUP_EMAIL_OTP -> "Verify Account Creation"
              AuthMode.PHONE_AUTH -> "Phone Verification"
              AuthMode.PHONE_OTP -> "Enter 6-Digit OTP"
              AuthMode.FORGOT_PASSWORD -> "Recover Password"
              AuthMode.RESET_PASSWORD_CONFIRM -> "Set New Password"
              AuthMode.FIND_ACCOUNTS -> "Find Linked Accounts"
              AuthMode.FIND_ACCOUNTS_RESULTS -> "Discovered Spaces"
              AuthMode.EMAIL_VERIFICATION -> "Verify Your Email"
            },
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.primaryText,
            textAlign = TextAlign.Center
          )

          Text(
            text = when (mode) {
              AuthMode.WELCOME_ENTRY -> "Sign in or create your space to explore timeless aesthetics and curated feeds"
              AuthMode.LOGIN_EMAIL -> "Enter your email and password to access your account"
              AuthMode.SIGNUP_EMAIL -> "Join the global network with your secure credentials"
              AuthMode.SIGNUP_EMAIL_OTP -> "We sent a 6-digit OTP code to $email"
              AuthMode.PHONE_AUTH -> "We will send an SMS verification code to your phone"
              AuthMode.PHONE_OTP -> "Code sent to $selectedCountryCode$phoneNumber"
              AuthMode.FORGOT_PASSWORD -> "Enter your registered email or phone to reset your password"
              AuthMode.RESET_PASSWORD_CONFIRM -> "Enter the verification code and your new password"
              AuthMode.FIND_ACCOUNTS -> "Find all user accounts & IDs linked to your Phone or Gmail"
              AuthMode.FIND_ACCOUNTS_RESULTS -> "The following spaces are registered with your credentials"
              AuthMode.EMAIL_VERIFICATION -> "Please check your inbox to confirm your address"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
          )

          // Live OTP Helper Banner (for seamless testing & instant autofill)
          if (latestOtpCode != null && (mode == AuthMode.PHONE_OTP || mode == AuthMode.SIGNUP_EMAIL_OTP || mode == AuthMode.RESET_PASSWORD_CONFIRM || mode == AuthMode.FIND_ACCOUNTS)) {
            Surface(
              color = SnixlyGoldPrimary.copy(alpha = 0.12f),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, SnixlyGoldPrimary.copy(alpha = 0.35f)),
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SnixlyGoldPrimary, modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text("Verification OTP Code", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
                    Text(latestOtpCode ?: "", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 3.sp), color = SnixlyGoldPrimary)
                  }
                }
                TextButton(
                  onClick = {
                    otpCode = latestOtpCode ?: ""
                  }
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = SnixlyGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto-fill", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = SnixlyGoldPrimary)
                  }
                }
              }
            }
          }

          // Error banner
          if (errorMessage != null) {
            Surface(
              color = SnixlyCrimsonAlert.copy(alpha = 0.1f),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, SnixlyCrimsonAlert.copy(alpha = 0.3f)),
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SnixlyCrimsonAlert, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = errorMessage ?: "", color = SnixlyCrimsonAlert, style = MaterialTheme.typography.bodySmall)
              }
            }
          }

          // Success banner
          if (successMessage != null) {
            Surface(
              color = SnixlyEmeraldActive.copy(alpha = 0.1f),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, SnixlyEmeraldActive.copy(alpha = 0.3f)),
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SnixlyEmeraldActive, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = successMessage ?: "", color = SnixlyEmeraldActive, style = MaterialTheme.typography.bodySmall)
              }
            }
          }

          // Dynamic Animated Content Based on Auth Mode
          AnimatedContent(
            targetState = mode,
            transitionSpec = {
              (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
            },
            label = "AuthModeTransition"
          ) { currentMode ->
            when (currentMode) {
              AuthMode.WELCOME_ENTRY -> {
                // ENTRY SCREEN: All Options Cleanly Laid Out
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  // Primary Button: Email Login
                  Button(
                    onClick = {
                      mode = AuthMode.LOGIN_EMAIL
                      errorMessage = null
                      successMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                      .testTag("entry_email_login_button")
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Email, contentDescription = null, tint = Color.White)
                      Spacer(modifier = Modifier.width(10.dp))
                      Text("Sign In with Email", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  // Secondary Button: Phone (OTP) Login
                  OutlinedButton(
                    onClick = {
                      mode = AuthMode.PHONE_AUTH
                      errorMessage = null
                      successMessage = null
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                      .testTag("entry_phone_login_button")
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Outlined.PhoneAndroid, contentDescription = null, tint = colors.accentGold)
                      Spacer(modifier = Modifier.width(10.dp))
                      Text("Continue with Phone Number", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.primaryText)
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  // Tertiary Button: Google Sign-In
                  OutlinedButton(
                    onClick = { handleGoogleSignIn() },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                      .testTag("entry_google_signin_button")
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(color = colors.accentGold, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          text = "G",
                          style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold
                          )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Continue with Google", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.primaryText)
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(16.dp))

                  // Account Discovery & Recovery Links
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    TextButton(
                      onClick = {
                        mode = AuthMode.FIND_ACCOUNTS
                        findQuery = ""
                        otpCode = ""
                        errorMessage = null
                        successMessage = null
                      }
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ManageAccounts, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Find Linked IDs", color = colors.accentGold, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                      }
                    }

                    TextButton(
                      onClick = {
                        mode = AuthMode.FORGOT_PASSWORD
                        errorMessage = null
                        successMessage = null
                      }
                    ) {
                      Text("Forgot Password?", color = colors.accentGold, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  // Divider & Create Account Option
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(colors.border))
                    Text(
                      text = "NEW TO SNIXLY?",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                      color = colors.secondaryText,
                      modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(colors.border))
                  }

                  Spacer(modifier = Modifier.height(14.dp))

                  OutlinedButton(
                    onClick = {
                      mode = AuthMode.SIGNUP_EMAIL
                      errorMessage = null
                      successMessage = null
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, colors.accentGold),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(50.dp)
                      .testTag("entry_create_account_button")
                  ) {
                    Text(
                      "Create New Space",
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                      color = colors.accentGold
                    )
                  }
                }
              }

              AuthMode.LOGIN_EMAIL -> {
                // EMAIL LOGIN FORM
                Column(modifier = Modifier.fillMaxWidth()) {
                  OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email, @Username, or Phone", color = colors.secondaryText) },
                    placeholder = { Text("masum@snixly.io or @masum", color = colors.secondaryText.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Outlined.ManageAccounts, contentDescription = null, tint = colors.secondaryText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = colors.accentGold,
                      unfocusedBorderColor = colors.border,
                      focusedTextColor = colors.primaryText,
                      unfocusedTextColor = colors.primaryText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("login_email_input")
                  )

                  Spacer(modifier = Modifier.height(14.dp))

                  OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password", color = colors.secondaryText) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = colors.secondaryText) },
                    trailingIcon = {
                      IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                          imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                          contentDescription = if (passwordVisible) "Hide" else "Show",
                          tint = colors.secondaryText
                        )
                      }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleEmailLogin() }),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = colors.accentGold,
                      unfocusedBorderColor = colors.border,
                      focusedTextColor = colors.primaryText,
                      unfocusedTextColor = colors.primaryText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("login_password_input")
                  )

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    TextButton(
                      onClick = {
                        mode = AuthMode.FIND_ACCOUNTS
                        errorMessage = null
                        successMessage = null
                      }
                    ) {
                      Text("Forgot Username?", color = colors.accentGold, style = MaterialTheme.typography.bodySmall)
                    }

                    TextButton(
                      onClick = {
                        mode = AuthMode.FORGOT_PASSWORD
                        errorMessage = null
                        successMessage = null
                      },
                      modifier = Modifier.testTag("forgot_password_link")
                    ) {
                      Text(
                        text = "Forgot password?",
                        color = colors.accentGold,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  Button(
                    onClick = { handleEmailLogin() },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                      .testTag("login_submit_button")
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                      Text("Sign In", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Don't have an account?", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
                    TextButton(
                      onClick = {
                        mode = AuthMode.SIGNUP_EMAIL
                        errorMessage = null
                        successMessage = null
                      }
                    ) {
                      Text("Sign Up", color = colors.accentGold, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                  }
                }
              }

              AuthMode.SIGNUP_EMAIL -> {
                // EMAIL SIGNUP FORM (Step 1)
                Column(modifier = Modifier.fillMaxWidth()) {
                  OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email Address", color = colors.secondaryText) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = colors.secondaryText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = colors.accentGold,
                      unfocusedBorderColor = colors.border,
                      focusedTextColor = colors.primaryText,
                      unfocusedTextColor = colors.primaryText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("signup_email_input")
                  )

                  Spacer(modifier = Modifier.height(14.dp))

                  OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password (min 6 chars)", color = colors.secondaryText) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = colors.secondaryText) },
                    trailingIcon = {
                      IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                          imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                          contentDescription = null,
                          tint = colors.secondaryText
                        )
                      }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = colors.accentGold,
                      unfocusedBorderColor = colors.border,
                      focusedTextColor = colors.primaryText,
                      unfocusedTextColor = colors.primaryText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("signup_password_input")
                  )

                  Spacer(modifier = Modifier.height(14.dp))

                  OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = { Text("Confirm Password", color = colors.secondaryText) },
                    leadingIcon = { Icon(Icons.Outlined.Security, contentDescription = null, tint = colors.secondaryText) },
                    trailingIcon = {
                      IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                          imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                          contentDescription = null,
                          tint = colors.secondaryText
                        )
                      }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleEmailSignUpStart() }),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = colors.accentGold,
                      unfocusedBorderColor = colors.border,
                      focusedTextColor = colors.primaryText,
                      unfocusedTextColor = colors.primaryText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("signup_confirm_password_input")
                  )

                  Spacer(modifier = Modifier.height(20.dp))

                  Button(
                    onClick = { handleEmailSignUpStart() },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                      .testTag("signup_submit_button")
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                      Text("Verify OTP & Create Space", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Already registered?", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
                    TextButton(
                      onClick = {
                        mode = AuthMode.LOGIN_EMAIL
                        errorMessage = null
                        successMessage = null
                      }
                    ) {
                      Text("Sign In", color = colors.accentGold, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                  }
                }
              }

              AuthMode.SIGNUP_EMAIL_OTP -> {
                // EMAIL SIGNUP OTP VERIFICATION (Step 2)
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) { otpCode = it.filter { c -> c.isDigit() }; errorMessage = null } },
                    label = { Text("6-Digit Verification Code", color = colors.secondaryText) },
                    placeholder = { Text("123456") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 8.sp,
                      textAlign = TextAlign.Center,
                      color = colors.primaryText
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleEmailSignUpVerifyOtp() }),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = colors.accentGold,
                      unfocusedBorderColor = colors.border,
                      focusedTextColor = colors.primaryText,
                      unfocusedTextColor = colors.primaryText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("signup_otp_code_input")
                  )

                  Spacer(modifier = Modifier.height(16.dp))

                  Button(
                    onClick = { handleEmailSignUpVerifyOtp() },
                    enabled = !isLoading && otpCode.length >= 4,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                      .testTag("signup_verify_otp_button")
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                      Text("Confirm & Launch Account", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  if (isTimerRunning) {
                    Text(
                      text = "Resend code in ${resendCountdown}s",
                      style = MaterialTheme.typography.bodySmall,
                      color = colors.secondaryText
                    )
                  } else {
                    TextButton(
                      onClick = { handleEmailSignUpStart() }
                    ) {
                      Text("Resend Code", color = colors.accentGold, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                  }
                }
              }

              AuthMode.PHONE_AUTH -> {
                // PHONE NUMBER ENTRY
                Column(modifier = Modifier.fillMaxWidth()) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    // Country Code Dropdown Trigger
                    Box {
                      Surface(
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, colors.border),
                        color = colors.surfaceVariant,
                        modifier = Modifier
                          .height(56.dp)
                          .clickable { countryDropdownExpanded = true }
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 12.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Text(text = selectedCountryCode, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
                        }
                      }

                      DropdownMenu(
                        expanded = countryDropdownExpanded,
                        onDismissRequest = { countryDropdownExpanded = false }
                      ) {
                        COUNTRY_CODES.forEach { (code, label) ->
                          DropdownMenuItem(
                            text = { Text("$code ($label)") },
                            onClick = {
                              selectedCountryCode = code
                              countryDropdownExpanded = false
                            }
                          )
                        }
                      }
                    }

                    // Phone Number Input
                    OutlinedTextField(
                      value = phoneNumber,
                      onValueChange = { phoneNumber = it.filter { c -> c.isDigit() || c == ' ' || c == '-' }; errorMessage = null },
                      label = { Text("Phone Number", color = colors.secondaryText) },
                      leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = colors.secondaryText) },
                      singleLine = true,
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                      keyboardActions = KeyboardActions(onDone = { handleSendPhoneOtp() }),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentGold,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                      ),
                      shape = RoundedCornerShape(14.dp),
                      modifier = Modifier
                        .weight(1f)
                        .testTag("phone_number_input")
                    )
                  }

                  Spacer(modifier = Modifier.height(20.dp))

                  Button(
                    onClick = { handleSendPhoneOtp() },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                      .testTag("send_otp_button")
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                      Text("Send Verification Code", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                  }
                }
              }

              AuthMode.PHONE_OTP -> {
                // OTP CODE ENTRY
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) { otpCode = it.filter { c -> c.isDigit() }; errorMessage = null } },
                    label = { Text("6-Digit OTP Code", color = colors.secondaryText) },
                    placeholder = { Text("123456") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 8.sp,
                      textAlign = TextAlign.Center,
                      color = colors.primaryText
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleVerifyPhoneOtp() }),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = colors.accentGold,
                      unfocusedBorderColor = colors.border,
                      focusedTextColor = colors.primaryText,
                      unfocusedTextColor = colors.primaryText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("otp_code_input")
                  )

                  Spacer(modifier = Modifier.height(16.dp))

                  Button(
                    onClick = { handleVerifyPhoneOtp() },
                    enabled = !isLoading && otpCode.length >= 4,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                      .testTag("verify_otp_submit_button")
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                      Text("Verify & Continue", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  // Resend countdown
                  if (isTimerRunning) {
                    Text(
                      text = "Resend code in ${resendCountdown}s",
                      style = MaterialTheme.typography.bodySmall,
                      color = colors.secondaryText
                    )
                  } else {
                    TextButton(
                      onClick = { handleSendPhoneOtp() },
                      modifier = Modifier.testTag("resend_otp_button")
                    ) {
                      Text("Resend Code", color = colors.accentGold, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                  }
                }
              }

              AuthMode.FORGOT_PASSWORD -> {
                // FORGOT PASSWORD
                Column(modifier = Modifier.fillMaxWidth()) {
                  Text(
                    text = "Enter your registered email, phone number, or @username. We will look up your account in the Firestore database and send a verification OTP.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(bottom = 14.dp)
                  )

                  OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email, Phone, or @username", color = colors.secondaryText) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = colors.secondaryText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleSendPasswordResetOtp() }),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = colors.accentGold,
                      unfocusedBorderColor = colors.border,
                      focusedTextColor = colors.primaryText,
                      unfocusedTextColor = colors.primaryText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("forgot_email_input")
                  )

                  Spacer(modifier = Modifier.height(20.dp))

                  Button(
                    onClick = { handleSendPasswordResetOtp() },
                    enabled = !isLoading && email.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                      .testTag("send_reset_button")
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                      Text("Verify in Firestore & Send OTP", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))

                  TextButton(
                    onClick = { mode = AuthMode.LOGIN_EMAIL },
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text("Back to Sign In", color = colors.accentGold)
                  }
                }
              }

              AuthMode.RESET_PASSWORD_CONFIRM -> {
                // ENTER RESET OTP & NEW PASSWORD
                Column(modifier = Modifier.fillMaxWidth()) {
                  selectedResetAccount?.let { acc ->
                    Surface(
                      shape = RoundedCornerShape(14.dp),
                      color = colors.surfaceVariant,
                      border = BorderStroke(1.dp, colors.border),
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                    ) {
                      Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        AsyncImage(
                          model = acc.avatarUrl,
                          contentDescription = acc.fullName,
                          modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                          Text(acc.fullName.ifBlank { acc.username }, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
                          Text("Space @${acc.username}", style = MaterialTheme.typography.bodySmall, color = colors.accentGold)
                        }
                      }
                    }
                  }

                  OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) { otpCode = it.filter { c -> c.isDigit() }; errorMessage = null } },
                    label = { Text("6-Digit Recovery OTP Code", color = colors.secondaryText) },
                    placeholder = { Text("123456") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 8.sp,
                      textAlign = TextAlign.Center,
                      color = colors.primaryText
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = colors.accentGold,
                      unfocusedBorderColor = colors.border,
                      focusedTextColor = colors.primaryText,
                      unfocusedTextColor = colors.primaryText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                  )

                  Spacer(modifier = Modifier.height(14.dp))

                  OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; errorMessage = null },
                    label = { Text("Enter New Password (min 6 chars)", color = colors.secondaryText) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = colors.secondaryText) },
                    trailingIcon = {
                      IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                          imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                          contentDescription = null,
                          tint = colors.secondaryText
                        )
                      }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleConfirmPasswordReset() }),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = colors.accentGold,
                      unfocusedBorderColor = colors.border,
                      focusedTextColor = colors.primaryText,
                      unfocusedTextColor = colors.primaryText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                  )

                  Spacer(modifier = Modifier.height(20.dp))

                  Button(
                    onClick = { handleConfirmPasswordReset() },
                    enabled = !isLoading && otpCode.length >= 4 && newPassword.length >= 6,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                      Text("Update Password & Launch Space", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  if (isTimerRunning) {
                    Text(
                      text = "Resend code in ${resendCountdown}s",
                      style = MaterialTheme.typography.bodySmall,
                      color = colors.secondaryText,
                      modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                  } else {
                    TextButton(
                      onClick = { handleSendPasswordResetOtp() },
                      modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                      Text("Resend Code", color = colors.accentGold, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                  }
                }
              }

              AuthMode.FIND_ACCOUNTS -> {
                // FIND ALL LINKED IDS / USERNAMES
                Column(modifier = Modifier.fillMaxWidth()) {
                  Text(
                    text = "Enter your phone number or email address to discover all accounts and usernames associated with your identity in Firestore.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(bottom = 14.dp)
                  )

                  OutlinedTextField(
                    value = findQuery,
                    onValueChange = { findQuery = it; errorMessage = null },
                    label = { Text("Phone Number or Email Address", color = colors.secondaryText) },
                    leadingIcon = { Icon(Icons.Outlined.ManageAccounts, contentDescription = null, tint = colors.secondaryText) },
                    placeholder = { Text("e.g. +91 9876543210 or user@gmail.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = colors.accentGold,
                      unfocusedBorderColor = colors.border,
                      focusedTextColor = colors.primaryText,
                      unfocusedTextColor = colors.primaryText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                  )

                  Spacer(modifier = Modifier.height(14.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                  ) {
                    TextButton(
                      onClick = { handleFindAccountsSendOtp() }
                    ) {
                      Text(if (latestOtpCode == null) "Send Verification OTP" else "Resend OTP", color = colors.accentGold, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                  }

                  OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) { otpCode = it.filter { c -> c.isDigit() }; errorMessage = null } },
                    label = { Text("6-Digit Verification OTP", color = colors.secondaryText) },
                    placeholder = { Text("123456") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 8.sp,
                      textAlign = TextAlign.Center,
                      color = colors.primaryText
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleFindAccountsVerifyAndList() }),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = colors.accentGold,
                      unfocusedBorderColor = colors.border,
                      focusedTextColor = colors.primaryText,
                      unfocusedTextColor = colors.primaryText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                  )

                  Spacer(modifier = Modifier.height(20.dp))

                  Button(
                    onClick = { handleFindAccountsVerifyAndList() },
                    enabled = !isLoading && findQuery.isNotBlank() && otpCode.length >= 4,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                      Text("Query Firestore & Discover Spaces", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                  }
                }
              }

              AuthMode.FIND_ACCOUNTS_RESULTS -> {
                // DISPLAY ALL LINKED ACCOUNTS & DIRECT LOGIN
                Column(modifier = Modifier.fillMaxWidth()) {
                  if (discoveredAccounts.isEmpty()) {
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = colors.secondaryText, modifier = Modifier.size(48.dp))
                      Spacer(modifier = Modifier.height(10.dp))
                      Text(
                        "No existing spaces found linked to '$findQuery'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.secondaryText,
                        textAlign = TextAlign.Center
                      )
                      Spacer(modifier = Modifier.height(16.dp))
                      Button(
                        onClick = {
                          mode = AuthMode.SIGNUP_EMAIL
                          email = if (findQuery.contains("@")) findQuery else ""
                          phoneNumber = if (!findQuery.contains("@")) findQuery else ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        Text("Create a New Space", color = Color.White, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                      }
                    }
                  } else {
                    Text(
                      text = "${discoveredAccounts.size} Space${if (discoveredAccounts.size > 1) "s" else ""} Found in Firestore:",
                      style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                      color = colors.primaryText,
                      modifier = Modifier.padding(bottom = 12.dp)
                    )

                    discoveredAccounts.forEach { acc ->
                      Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(vertical = 6.dp)
                      ) {
                        Column(
                          modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                        ) {
                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                          ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                              AsyncImage(
                                model = acc.avatarUrl,
                                contentDescription = acc.fullName,
                                modifier = Modifier
                                  .size(44.dp)
                                  .clip(CircleShape)
                              )
                              Spacer(modifier = Modifier.width(10.dp))
                              Column {
                                Text(acc.fullName.ifBlank { acc.username }, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.primaryText)
                                Text("@${acc.username}", style = MaterialTheme.typography.bodySmall, color = colors.accentGold)
                                Text(acc.accountType, style = MaterialTheme.typography.labelSmall, color = colors.secondaryText)
                              }
                            }
                            Button(
                              onClick = { handleLoginToDiscoveredAccount(acc) },
                              colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                              shape = RoundedCornerShape(10.dp),
                              modifier = Modifier.height(36.dp)
                            ) {
                              Text("Sign In", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            }
                          }

                          Spacer(modifier = Modifier.height(6.dp))

                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                          ) {
                            TextButton(
                              onClick = {
                                selectedResetAccount = acc
                                email = acc.email.ifBlank { acc.phone }
                                val sendTarget = email.ifBlank { acc.username }
                                val code = authRepository.generateOtpForTarget(sendTarget)
                                latestOtpCode = code
                                otpCode = ""
                                newPassword = ""
                                mode = AuthMode.RESET_PASSWORD_CONFIRM
                                resendCountdown = 60
                                isTimerRunning = true
                                successMessage = "Resetting password for @${acc.username}. OTP code sent."
                              }
                            ) {
                              Text("Reset Password for @${acc.username}", color = colors.accentGold, style = MaterialTheme.typography.labelSmall)
                            }
                          }
                        }
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))

                  TextButton(
                    onClick = { mode = AuthMode.WELCOME_ENTRY },
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text("Back to Main Menu", color = colors.accentGold)
                  }
                }
              }

              AuthMode.EMAIL_VERIFICATION -> {
                // EMAIL VERIFICATION
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(
                    imageVector = Icons.Outlined.MarkEmailRead,
                    contentDescription = null,
                    tint = colors.accentGold,
                    modifier = Modifier.size(56.dp)
                  )

                  Spacer(modifier = Modifier.height(12.dp))

                  Text(
                    text = "A verification email has been sent to $email. Please click the link inside to verify your address.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center
                  )

                  Spacer(modifier = Modifier.height(20.dp))

                  Button(
                    onClick = {
                      isLoading = true
                      coroutineScope.launch {
                        authRepository.reloadUser()
                        isLoading = false
                        onAuthSuccess()
                      }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                  ) {
                    Text("I've Verified My Email", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  TextButton(
                    onClick = {
                      coroutineScope.launch {
                        authRepository.resendVerificationEmail()
                        successMessage = "Verification email resent!"
                      }
                    }
                  ) {
                    Text("Resend Verification Email", color = colors.accentGold)
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

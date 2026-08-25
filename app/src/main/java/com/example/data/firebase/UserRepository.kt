package com.example.data.firebase

import android.util.Log
import com.example.model.UserProfile
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class UserRepository {
  private val TAG = "SnixlyStartup"
  private val firestore: FirebaseFirestore? by lazy {
    try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      null
    }
  }
  private val usersCollection get() = firestore?.collection("users")
  private val usernamesCollection get() = firestore?.collection("usernames")

  // In-memory cache for ultra-responsive UI
  private val _cachedProfiles = mutableMapOf<String, UserProfileDto>()
  private val _profileStateFlows = mutableMapOf<String, MutableStateFlow<UserProfileDto?>>()

  private val reservedUsernames = setOf(
    "admin", "administrator", "snixly", "official", "support", "help", "security",
    "root", "system", "mod", "moderator", "explore", "settings", "whisper", "signals",
    "loops", "vault", "auth", "login", "signup", "terms", "privacy"
  )

  fun isUsernameFormatValid(rawUsername: String): Pair<Boolean, String?> {
    val username = rawUsername.trim().lowercase()
    if (username.length < 3) return Pair(false, "Username must be at least 3 characters")
    if (username.length > 20) return Pair(false, "Username must not exceed 20 characters")
    if (!username.matches(Regex("^[a-z0-9_.]+$"))) {
      return Pair(false, "Only letters, numbers, underscores, and periods are allowed")
    }
    if (username.startsWith(".") || username.endsWith(".") || username.startsWith("_") || username.endsWith("_")) {
      return Pair(false, "Username cannot start or end with a period or underscore")
    }
    if (reservedUsernames.contains(username)) {
      return Pair(false, "This username is reserved by SNIXLY")
    }
    return Pair(true, null)
  }

  suspend fun checkUsernameAvailability(rawUsername: String): Result<Boolean> {
    val username = rawUsername.trim().lowercase()
    val (isValid, error) = isUsernameFormatValid(username)
    if (!isValid) {
      return Result.failure(Exception(error ?: "Invalid username format"))
    }
    // Check in local cache
    if (_cachedProfiles.values.any { it.username.equals(username, ignoreCase = true) }) {
      return Result.success(false)
    }
    val col = usernamesCollection ?: return Result.success(true)
    return try {
      val doc = col.document(username).get().await()
      val isTaken = doc.exists()
      Result.success(!isTaken)
    } catch (e: Exception) {
      Result.success(true) // Fallback to allowed in offline/local mode
    }
  }

  suspend fun generateUsernameSuggestions(rawUsername: String): List<String> {
    val base = rawUsername.trim().lowercase().filter { it.isLetterOrDigit() || it == '_' }.take(14)
    if (base.isEmpty()) return listOf("snix_${System.currentTimeMillis() % 10000}")

    val candidates = listOf(
      "${base}_",
      "${base}_x",
      "${base}${((10..99).random())}",
      "the_${base}",
      "${base}_aura"
    )

    val available = mutableListOf<String>()
    val col = usernamesCollection
    for (candidate in candidates) {
      if (_cachedProfiles.values.any { it.username.equals(candidate, ignoreCase = true) }) {
        continue
      }
      if (col == null) {
        available.add(candidate)
        if (available.size >= 3) break
        continue
      }
      try {
        val doc = col.document(candidate).get().await()
        if (!doc.exists()) {
          available.add(candidate)
          if (available.size >= 3) break
        }
      } catch (_: Exception) {
        available.add(candidate)
        if (available.size >= 3) break
      }
    }
    return available
  }

  fun setCachedProfile(profile: UserProfileDto) {
    _cachedProfiles[profile.uid] = profile
    val flow = _profileStateFlows.getOrPut(profile.uid) { MutableStateFlow(null) }
    flow.value = profile
  }

  fun getCachedProfile(uid: String): UserProfileDto? {
    return _cachedProfiles[uid]
  }

  suspend fun findAccountsByPhoneOrEmail(query: String): List<UserProfileDto> {
    val clean = query.trim()
    if (clean.isBlank()) return emptyList()
    val cleanLower = clean.lowercase()
    val cleanDigits = clean.filter { it.isDigit() }

    val results = mutableListOf<UserProfileDto>()
    val uCol = usersCollection

    if (uCol != null) {
      try {
        // 1. Search by exact email
        if (cleanLower.contains("@")) {
          val emailQuery = uCol.whereEqualTo("email", cleanLower).get().await()
          for (doc in emailQuery.documents) {
            doc.toObject(UserProfileDto::class.java)?.let { results.add(it) }
          }
          if (clean != cleanLower) {
            val emailCaseQuery = uCol.whereEqualTo("email", clean).get().await()
            for (doc in emailCaseQuery.documents) {
              doc.toObject(UserProfileDto::class.java)?.let { results.add(it) }
            }
          }
        }

        // 2. Search by phone number (exact, with +, and raw digits)
        if (cleanDigits.length >= 6) {
          val phoneQuery1 = uCol.whereEqualTo("phone", clean).get().await()
          for (doc in phoneQuery1.documents) {
            doc.toObject(UserProfileDto::class.java)?.let { results.add(it) }
          }
          val phoneQuery2 = uCol.whereEqualTo("phone", "+$cleanDigits").get().await()
          for (doc in phoneQuery2.documents) {
            doc.toObject(UserProfileDto::class.java)?.let { results.add(it) }
          }
          val phoneQuery3 = uCol.whereEqualTo("phone", cleanDigits).get().await()
          for (doc in phoneQuery3.documents) {
            doc.toObject(UserProfileDto::class.java)?.let { results.add(it) }
          }
        }

        // 3. Search by username directly
        val rawUser = cleanLower.removePrefix("@")
        val usernameQuery = uCol.whereEqualTo("username", rawUser).get().await()
        for (doc in usernameQuery.documents) {
          doc.toObject(UserProfileDto::class.java)?.let { results.add(it) }
        }

        // 4. Check usernames mapping collection if still empty
        if (results.isEmpty()) {
          val unCol = usernamesCollection
          if (unCol != null) {
            val uDoc = unCol.document(rawUser).get().await()
            val uid = uDoc.getString("uid")
            if (uid != null) {
              val userDoc = uCol.document(uid).get().await()
              userDoc.toObject(UserProfileDto::class.java)?.let { results.add(it) }
            }
          }
        }
      } catch (_: Exception) {
        // Fallback gracefully on query failures or offline
      }
    }

    // Cache any newly found profiles from Firestore
    for (profile in results) {
      setCachedProfile(profile)
    }

    // Merge with in-memory / local cached profiles matching query
    val cachedMatches = _cachedProfiles.values.filter { profile ->
      val pEmail = profile.email.lowercase()
      val pPhone = profile.phone.filter { it.isDigit() }
      val pUser = profile.username.lowercase()
      val rawUser = cleanLower.removePrefix("@")
      (cleanLower.isNotBlank() && pEmail.contains(cleanLower)) ||
      (cleanDigits.isNotBlank() && cleanDigits.length >= 6 && pPhone.contains(cleanDigits)) ||
      pUser == rawUser ||
      (cleanLower.isNotBlank() && profile.fullName.lowercase().contains(cleanLower))
    }

    for (p in cachedMatches) {
      if (results.none { it.uid == p.uid }) {
        results.add(p)
      }
    }

    return results.distinctBy { it.uid }
  }

  suspend fun createUserProfile(
    uid: String,
    fullName: String,
    rawUsername: String,
    email: String,
    phone: String = ""
  ): Result<UserProfileDto> {
    val username = rawUsername.trim().lowercase()
    val (isValid, error) = isUsernameFormatValid(username)
    if (!isValid) {
      return Result.failure(Exception(error ?: "Invalid username format"))
    }

    val defaultProfile = UserProfileDto(
      uid = uid,
      fullName = fullName.trim(),
      username = username,
      email = email.trim(),
      phone = phone.trim(),
      bio = "Creating in the modern space ✨",
      location = "Global",
      avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
      coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200",
      accountType = "Personal Space",
      isVerified = false,
      isPrivate = false,
      auraStatus = "Active on Snixly ✨",
      followersCount = 0,
      followingCount = 0,
      loopsCount = 0,
      privacySettings = defaultPrivacySettings(),
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis()
    )

    // Instantly update memory cache and notify observers
    setCachedProfile(defaultProfile)

    val fs = firestore
    val uCol = usersCollection
    val unCol = usernamesCollection
    if (fs == null || uCol == null || unCol == null) {
      return Result.success(defaultProfile)
    }

    return try {
      fs.runTransaction { transaction ->
        val usernameRef = unCol.document(username)
        val usernameDoc = transaction.get(usernameRef)
        if (usernameDoc.exists()) {
          val existingUid = usernameDoc.getString("uid")
          if (existingUid != null && existingUid != uid) {
            throw IllegalStateException("Username @$username is already taken. Please choose another.")
          }
        }

        val userRef = uCol.document(uid)
        transaction.set(usernameRef, mapOf("uid" to uid, "username" to username, "claimedAt" to System.currentTimeMillis()))
        transaction.set(userRef, defaultProfile)
        defaultProfile
      }.await().let { profile ->
        setCachedProfile(profile)
        Result.success(profile)
      }
    } catch (e: Exception) {
      setCachedProfile(defaultProfile)
      Result.success(defaultProfile)
    }
  }

  suspend fun getUserProfile(uid: String): Result<UserProfileDto?> {
    val cached = _cachedProfiles[uid]
    if (cached != null) {
      Log.d(TAG, "Startup: Resolved profile from in-memory cache for uid=$uid (@${cached.username})")
      return Result.success(cached)
    }
    val col = usersCollection
    if (col == null) {
      Log.w(TAG, "Startup: Firestore is uninitialized, returning null for uid=$uid")
      return Result.success(null)
    }
    return try {
      // 2.5 second timeout on Firestore profile document fetch to prevent infinite hanging
      val doc = withTimeoutOrNull(2500L) {
        col.document(uid).get().await()
      }
      if (doc == null) {
        Log.w(TAG, "Startup Step NOTICE: Firestore document fetch timed out after 2500ms for uid=$uid, using cached/fallback")
        val fallback = _cachedProfiles[uid]
        return Result.success(fallback)
      }
      if (doc.exists()) {
        val p = doc.toObject(UserProfileDto::class.java)
        if (p != null) {
          Log.i(TAG, "Startup Step SUCCESS: Fetched profile doc for uid=$uid (@${p.username})")
          setCachedProfile(p)
          Result.success(p)
        } else {
          Log.w(TAG, "Startup Step: Document exists but could not deserialize to UserProfileDto for uid=$uid")
          Result.success(_cachedProfiles[uid])
        }
      } else {
        Log.i(TAG, "Startup Step: No Firestore user profile document found for uid=$uid (Requires setup/onboarding)")
        Result.success(_cachedProfiles[uid])
      }
    } catch (e: FirebaseFirestoreException) {
      Log.w(TAG, "Startup Step OFFLINE/UNAVAILABLE: ${e.message} (Code=${e.code}). Falling back to local/cached profile.")
      val fallback = _cachedProfiles[uid]
      Result.success(fallback)
    } catch (e: FirebaseException) {
      Log.w(TAG, "Startup Step Firebase fallback: ${e.message}")
      Result.success(_cachedProfiles[uid])
    } catch (e: Exception) {
      Log.w(TAG, "Startup Step fetch exception fallback: ${e.message}")
      Result.success(_cachedProfiles[uid])
    }
  }

  suspend fun repairOrCreateProfile(authUser: AuthUser): UserProfileDto {
    val uid = authUser.uid
    val cached = _cachedProfiles[uid]
    if (cached != null && cached.username.isNotBlank()) {
      return cached
    }

    val fallbackHandle = (authUser.email?.substringBefore("@")
      ?: authUser.displayName?.filter { it.isLetterOrDigit() || it == '_' }
      ?: "creator_${uid.take(5)}").lowercase().ifBlank { "creator_${System.currentTimeMillis() % 10000}" }

    val repaired = UserProfileDto(
      uid = uid,
      username = fallbackHandle,
      fullName = authUser.displayName?.ifBlank { null } ?: fallbackHandle,
      email = authUser.email ?: "",
      phone = authUser.phoneNumber ?: "",
      bio = "Creating timeless aesthetics in the modern social space ✨",
      avatarUrl = authUser.photoUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
      location = "Global Space",
      auraStatus = "Active on Snixly ✨",
      accountType = "Creator Atelier",
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis()
    )

    setCachedProfile(repaired)

    // Asynchronously try to persist to Firestore without blocking the UI
    try {
      usersCollection?.document(uid)?.set(repaired, SetOptions.merge())
    } catch (e: Exception) {
      Log.w(TAG, "Could not persist repaired profile to Firestore: ${e.message}")
    }

    return repaired
  }

  fun observeUserProfile(uid: String): Flow<UserProfileDto?> {
    val flow = _profileStateFlows.getOrPut(uid) { MutableStateFlow(_cachedProfiles[uid]) }
    val col = usersCollection
    if (col != null) {
      try {
        col.document(uid).addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "observeUserProfile error for uid=$uid | code=${error.code} | msg=${error.message}")
            return@addSnapshotListener
          }
          if (snapshot != null && snapshot.exists()) {
            val profile = snapshot.toObject(UserProfileDto::class.java)
            if (profile != null) {
              setCachedProfile(profile)
            }
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "observeUserProfile setup exception: ${e.message}", e)
      }
    }
    return flow.asStateFlow()
  }

  suspend fun updateUserProfileFields(uid: String, updates: Map<String, Any>): Result<Unit> {
    val current = _cachedProfiles[uid]
    if (current != null) {
      val updated = current.copy(
        fullName = updates["fullName"] as? String ?: current.fullName,
        bio = updates["bio"] as? String ?: current.bio,
        avatarUrl = updates["avatarUrl"] as? String ?: current.avatarUrl,
        location = updates["location"] as? String ?: current.location,
        auraStatus = updates["auraStatus"] as? String ?: current.auraStatus,
        accountType = updates["accountType"] as? String ?: current.accountType,
        username = updates["username"] as? String ?: current.username,
        updatedAt = System.currentTimeMillis()
      )
      setCachedProfile(updated)
    }

    val col = usersCollection ?: return Result.success(Unit)
    return try {
      val mutableUpdates = updates.toMutableMap()
      mutableUpdates["updatedAt"] = System.currentTimeMillis()
      col.document(uid).set(mutableUpdates, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.success(Unit)
    }
  }

  suspend fun updateSettings(uid: String, settingsKey: String, value: Any): Result<Unit> {
    val col = usersCollection ?: return Result.success(Unit)
    return try {
      val updates = mapOf(
        "privacySettings.$settingsKey" to value,
        "updatedAt" to System.currentTimeMillis()
      )
      col.document(uid).update(updates).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.success(Unit)
    }
  }

  suspend fun changeUsername(uid: String, oldUsername: String, newRawUsername: String): Result<String> {
    val newUsername = newRawUsername.trim().lowercase()
    val (isValid, error) = isUsernameFormatValid(newUsername)
    if (!isValid) return Result.failure(Exception(error ?: "Invalid username format"))
    if (oldUsername.equals(newUsername, ignoreCase = true)) return Result.success(newUsername)

    val current = _cachedProfiles[uid]
    if (current != null) {
      setCachedProfile(current.copy(username = newUsername))
    }

    val fs = firestore
    val uCol = usersCollection
    val unCol = usernamesCollection
    if (fs == null || uCol == null || unCol == null) {
      return Result.success(newUsername)
    }

    return try {
      fs.runTransaction { transaction ->
        val newRef = unCol.document(newUsername)
        val newDoc = transaction.get(newRef)
        if (newDoc.exists()) {
          throw IllegalStateException("Username @$newUsername is already taken.")
        }

        val oldRef = unCol.document(oldUsername.lowercase())
        transaction.delete(oldRef)
        transaction.set(newRef, mapOf("uid" to uid, "username" to newUsername, "claimedAt" to System.currentTimeMillis()))
        transaction.update(uCol.document(uid), mapOf(
          "username" to newUsername,
          "updatedAt" to System.currentTimeMillis()
        ))
        newUsername
      }.await().let { Result.success(it) }
    } catch (e: Exception) {
      Result.success(newUsername)
    }
  }
}

fun UserProfileDto.toDomain(): UserProfile {
  return UserProfile(
    id = this.uid,
    name = if (this.fullName.isNotBlank()) this.fullName else (this.username.ifBlank { "Snixly User" }),
    username = this.username,
    avatarUrl = if (this.avatarUrl.isNotBlank()) this.avatarUrl else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
    bio = this.bio,
    location = this.location,
    isVerified = this.isVerified,
    followersCount = this.followersCount.toInt(),
    followingCount = this.followingCount.toInt(),
    loopsCount = this.loopsCount.toInt(),
    isOnline = true,
    auraStatus = this.auraStatus
  )
}

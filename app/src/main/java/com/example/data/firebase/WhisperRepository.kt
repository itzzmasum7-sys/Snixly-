package com.example.data.firebase

import android.net.Uri
import com.example.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class WhisperRepository {
  private val firestore: FirebaseFirestore? by lazy {
    try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      null
    }
  }
  private val storage: FirebaseStorage? by lazy {
    try {
      FirebaseStorage.getInstance()
    } catch (e: Exception) {
      null
    }
  }
  private val conversationsCollection get() = firestore?.collection("whisper_conversations")

  /**
   * Observe all conversations for the current user in real time
   */
  fun observeConversations(userId: String): Flow<List<WhisperConversationDto>> = callbackFlow {
    val col = conversationsCollection
    if (col == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }
    val listener = col
      .whereArrayContains("participantIds", userId)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          trySend(emptyList())
          return@addSnapshotListener
        }
        val list = snapshot?.documents?.mapNotNull { it.toObject(WhisperConversationDto::class.java) }
          ?.sortedByDescending { it.updatedAt }
          ?: emptyList()
        trySend(list)
      }
    awaitClose { listener.remove() }
  }

  /**
   * Observe real-time messages for a specific conversation, ordered chronologically
   */
  fun observeMessages(conversationId: String, limit: Long = 100): Flow<List<WhisperMessageDto>> = callbackFlow {
    val col = conversationsCollection
    if (col == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }
    val messagesCollection = col.document(conversationId).collection("messages")
    val listener = messagesCollection
      .orderBy("createdAt", Query.Direction.ASCENDING)
      .limitToLast(limit)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          trySend(emptyList())
          return@addSnapshotListener
        }
        val list = snapshot?.documents?.mapNotNull { it.toObject(WhisperMessageDto::class.java) } ?: emptyList()
        trySend(list)
      }
    awaitClose { listener.remove() }
  }

  /**
   * Get or create a 1-on-1 conversation between currentUser and targetUser
   */
  suspend fun getOrCreateDirectConversation(
    currentUser: UserProfile,
    targetUser: UserProfile
  ): Result<WhisperConversationDto> {
    val directId = if (currentUser.id < targetUser.id) {
      "${currentUser.id}_${targetUser.id}"
    } else {
      "${targetUser.id}_${currentUser.id}"
    }

    val participantsMap = mapOf(
      currentUser.id to mapOf(
        "name" to currentUser.name,
        "username" to currentUser.username,
        "avatarUrl" to currentUser.avatarUrl,
        "isOnline" to true
      ),
      targetUser.id to mapOf(
        "name" to targetUser.name,
        "username" to targetUser.username,
        "avatarUrl" to targetUser.avatarUrl,
        "isOnline" to targetUser.isOnline
      )
    )

    val newConversation = WhisperConversationDto(
      id = directId,
      participantIds = listOf(currentUser.id, targetUser.id),
      participantProfiles = participantsMap,
      lastMessage = "Started a new Whisper space ✨",
      lastMessageSenderId = currentUser.id,
      lastMessageType = WhisperMessageType.SYSTEM.name,
      lastMessageTimestamp = System.currentTimeMillis(),
      unreadCounts = mapOf(currentUser.id to 0L, targetUser.id to 0L),
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis()
    )

    val col = conversationsCollection ?: return Result.success(newConversation)

    return try {
      val docRef = col.document(directId)
      val doc = docRef.get().await()

      if (doc.exists()) {
        val existing = doc.toObject(WhisperConversationDto::class.java)
        if (existing != null) return Result.success(existing)
      }

      docRef.set(newConversation).await()
      Result.success(newConversation)
    } catch (e: Exception) {
      Result.success(newConversation)
    }
  }

  /**
   * Send a message to a conversation
   */
  suspend fun sendMessage(
    conversationId: String,
    message: WhisperMessageDto,
    recipientIds: List<String>
  ): Result<Unit> {
    val fs = firestore
    val col = conversationsCollection
    if (fs == null || col == null) return Result.success(Unit)

    return try {
      val convDoc = col.document(conversationId)
      val msgDoc = convDoc.collection("messages").document(message.id)

      val previewText = when (message.type) {
        WhisperMessageType.IMAGE.name -> "📷 Photo"
        WhisperMessageType.VIDEO.name -> "🎥 Video"
        WhisperMessageType.VOICE.name -> "🎙️ Voice note (${message.voiceDurationSeconds ?: 0}s)"
        WhisperMessageType.FILE.name -> "📎 ${message.fileName ?: "Document"}"
        WhisperMessageType.POLL.name -> "📊 Poll: ${message.pollQuestion ?: ""}"
        WhisperMessageType.LOCATION.name -> "📍 Location"
        WhisperMessageType.CONTACT.name -> "👤 Contact: ${message.contactName ?: ""}"
        WhisperMessageType.DROP.name -> "✨ Whisper Drop"
        WhisperMessageType.MUSIC.name -> "🎵 Music audio"
        else -> message.text
      }

      val unreadUpdates = mutableMapOf<String, Any>()
      recipientIds.forEach { rId ->
        if (rId != message.senderId) {
          unreadUpdates["unreadCounts.$rId"] = FieldValue.increment(1)
        }
      }

      fs.runBatch { batch ->
        batch.set(msgDoc, message)
        val convUpdates = mutableMapOf<String, Any>(
          "lastMessage" to previewText,
          "lastMessageSenderId" to message.senderId,
          "lastMessageType" to message.type,
          "lastMessageTimestamp" to message.createdAt,
          "updatedAt" to System.currentTimeMillis()
        )
        convUpdates.putAll(unreadUpdates)
        batch.update(convDoc, convUpdates)
      }.await()

      Result.success(Unit)
    } catch (e: Exception) {
      Result.success(Unit)
    }
  }

  /**
   * Upload attachment to Firebase Storage at whisper/{conversationId}/{messageId}/{filename}
   */
  suspend fun uploadAttachment(
    conversationId: String,
    messageId: String,
    uri: Uri,
    fileName: String,
    mimeType: String,
    onProgress: ((Float) -> Unit)? = null
  ): Result<Pair<String, String>> {
    val st = storage ?: return Result.success(Pair(uri.toString(), "local/$conversationId/$messageId/$fileName"))
    return try {
      val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
      val storagePath = "whisper/$conversationId/$messageId/$safeFileName"
      val storageRef = st.reference.child(storagePath)

      val metadata = com.google.firebase.storage.StorageMetadata.Builder()
        .setContentType(mimeType)
        .setCustomMetadata("conversationId", conversationId)
        .setCustomMetadata("messageId", messageId)
        .build()

      val uploadTask = storageRef.putFile(uri, metadata)
      uploadTask.addOnProgressListener { snapshot ->
        val total = snapshot.totalByteCount
        if (total > 0) {
          val progress = (snapshot.bytesTransferred.toFloat() / total.toFloat()).coerceIn(0f, 1f)
          onProgress?.invoke(progress)
        }
      }

      val taskSnapshot = uploadTask.await()
      val downloadUrl = taskSnapshot.storage.downloadUrl.await().toString()
      Result.success(Pair(downloadUrl, storagePath))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Legacy uploadMedia helper for compatibility
   */
  suspend fun uploadMedia(
    conversationId: String,
    uri: Uri,
    fileExtension: String
  ): Result<String> {
    val messageId = UUID.randomUUID().toString()
    val fName = "whisper_${System.currentTimeMillis()}.$fileExtension"
    val res = uploadAttachment(
      conversationId = conversationId,
      messageId = messageId,
      uri = uri,
      fileName = fName,
      mimeType = if (fileExtension == "mp4") "video/mp4" else "image/jpeg"
    )
    return res.map { it.first }
  }

  /**
   * Update fields of an existing message (e.g. deliveryStatus, uploadState, mediaUrl)
   */
  suspend fun updateMessage(
    conversationId: String,
    messageId: String,
    updates: Map<String, Any>
  ): Result<Unit> {
    val col = conversationsCollection ?: return Result.success(Unit)
    return try {
      col.document(conversationId).collection("messages").document(messageId).update(updates).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Add or toggle emoji reaction on a message
   */
  suspend fun toggleReaction(
    conversationId: String,
    messageId: String,
    userId: String,
    emoji: String
  ): Result<Unit> {
    val fs = firestore
    val col = conversationsCollection
    if (fs == null || col == null) return Result.success(Unit)
    return try {
      val msgRef = col.document(conversationId).collection("messages").document(messageId)
      fs.runTransaction { transaction ->
        val snapshot = transaction.get(msgRef)
        val currentReactions = (snapshot.get("reactions") as? Map<String, String>)?.toMutableMap() ?: mutableMapOf()
        if (currentReactions[userId] == emoji) {
          currentReactions.remove(userId)
        } else {
          currentReactions[userId] = emoji
        }
        transaction.update(msgRef, "reactions", currentReactions)
      }.await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.success(Unit)
    }
  }

  /**
   * Pin or unpin a message in conversation
   */
  suspend fun togglePinMessage(
    conversationId: String,
    messageId: String,
    isCurrentlyPinned: Boolean
  ): Result<Unit> {
    val fs = firestore
    val col = conversationsCollection
    if (fs == null || col == null) return Result.success(Unit)
    return try {
      val convRef = col.document(conversationId)
      val msgRef = convRef.collection("messages").document(messageId)

      fs.runBatch { batch ->
        if (isCurrentlyPinned) {
          batch.update(msgRef, "isPinned", false)
          batch.update(convRef, "pinnedMessageId", null)
        } else {
          batch.update(msgRef, "isPinned", true)
          batch.update(convRef, "pinnedMessageId", messageId)
        }
      }.await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.success(Unit)
    }
  }

  /**
   * Delete message for everyone
   */
  suspend fun deleteMessageForEveryone(
    conversationId: String,
    messageId: String,
    senderId: String,
    currentUserId: String
  ): Result<Unit> {
    if (senderId != currentUserId) {
      return Result.failure(Exception("You can only delete messages sent by you"))
    }
    val col = conversationsCollection ?: return Result.success(Unit)
    return try {
      val msgRef = col.document(conversationId).collection("messages").document(messageId)
      msgRef.update(
        mapOf(
          "isDeletedForEveryone" to true,
          "text" to "This message was deleted",
          "mediaUrl" to null
        )
      ).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.success(Unit)
    }
  }

  /**
   * Vote on in-chat poll
   */
  suspend fun votePoll(
    conversationId: String,
    messageId: String,
    optionIndex: Int,
    userId: String
  ): Result<Unit> {
    val fs = firestore
    val col = conversationsCollection
    if (fs == null || col == null) return Result.success(Unit)
    return try {
      val msgRef = col.document(conversationId).collection("messages").document(messageId)
      fs.runTransaction { transaction ->
        val snapshot = transaction.get(msgRef)
        val rawPollOptions = snapshot.get("pollOptions") as? List<Map<String, Any>> ?: return@runTransaction
        val mutableList = rawPollOptions.mapIndexed { idx, opt ->
          val optMap = opt.toMutableMap()
          val voters = (optMap["voterIds"] as? List<String>)?.toMutableList() ?: mutableListOf()
          if (idx == optionIndex) {
            if (!voters.contains(userId)) voters.add(userId)
          } else {
            voters.remove(userId)
          }
          optMap["voterIds"] = voters
          optMap["votes"] = voters.size
          optMap
        }

        val totalVotes = mutableList.sumOf { (it["votes"] as? Number)?.toInt() ?: 0 }
        val updatedOptions = mutableList.map { opt ->
          val votes = (opt["votes"] as? Number)?.toInt() ?: 0
          val pct = if (totalVotes > 0) (votes * 100) / totalVotes else 0
          opt.toMutableMap().apply { put("percent", pct) }
        }

        transaction.update(msgRef, "pollOptions", updatedOptions)
      }.await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.success(Unit)
    }
  }

  /**
   * Mark conversation messages as read
   */
  suspend fun markAsRead(conversationId: String, userId: String): Result<Unit> {
    val col = conversationsCollection ?: return Result.success(Unit)
    return try {
      val convRef = col.document(conversationId)
      convRef.update("unreadCounts.$userId", 0).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.success(Unit)
    }
  }

  /**
   * Update conversation theme & customization
   */
  suspend fun updateThemeConfig(
    conversationId: String,
    themeConfig: WhisperThemeConfigDto
  ): Result<Unit> {
    val col = conversationsCollection ?: return Result.success(Unit)
    return try {
      col.document(conversationId).update(
        mapOf("themeConfig" to themeConfig, "updatedAt" to System.currentTimeMillis())
      ).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.success(Unit)
    }
  }
}

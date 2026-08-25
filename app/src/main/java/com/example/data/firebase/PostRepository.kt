package com.example.data.firebase

import com.example.model.PollOption
import com.example.model.Post
import com.example.model.PostType
import com.example.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class PostDto(
  val id: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val authorUsername: String = "",
  val authorAvatarUrl: String = "",
  val content: String = "",
  val type: String = "STANDARD",
  val imageUrl: String? = null,
  val categoryTag: String? = null,
  val pollOptions: List<Map<String, Any>>? = null,
  val likesCount: Long = 0,
  val commentsCount: Long = 0,
  val resparksCount: Long = 0,
  val privacyScope: String = "Public",
  val createdAt: Long = System.currentTimeMillis()
)

data class CommentDto(
  val id: String = UUID.randomUUID().toString(),
  val postId: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val authorUsername: String = "",
  val authorAvatarUrl: String = "",
  val text: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

class PostRepository {
  private val firestore: FirebaseFirestore? by lazy {
    try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      null
    }
  }
  private val postsCollection get() = firestore?.collection("posts")
  private val usersCollection get() = firestore?.collection("users")

  fun observeFeedPosts(currentUserId: String): Flow<List<Post>> = callbackFlow {
    val col = postsCollection
    if (col == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }
    val listener = col
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .limit(50)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          trySend(emptyList())
          return@addSnapshotListener
        }
        val dtos = snapshot?.documents?.mapNotNull { it.toObject(PostDto::class.java) } ?: emptyList()
        val posts = dtos.map { it.toDomain(currentUserId) }
        trySend(posts)
      }
    awaitClose { listener.remove() }
  }

  suspend fun createPost(
    author: UserProfile,
    content: String,
    type: PostType = PostType.STANDARD,
    imageUrl: String? = null,
    categoryTag: String? = null,
    pollOptions: List<String>? = null
  ): Result<Post> {
    val postId = UUID.randomUUID().toString()
    val pollOptionsDto = pollOptions?.mapIndexed { index, optionText ->
      mapOf("id" to index, "text" to optionText, "votes" to 0, "percent" to 0)
    }

    val postDto = PostDto(
      id = postId,
      authorId = author.id,
      authorName = author.name,
      authorUsername = author.username,
      authorAvatarUrl = author.avatarUrl,
      content = content.trim(),
      type = type.name,
      imageUrl = imageUrl,
      categoryTag = categoryTag,
      pollOptions = pollOptionsDto,
      likesCount = 0,
      commentsCount = 0,
      resparksCount = 0,
      privacyScope = "Public",
      createdAt = System.currentTimeMillis()
    )

    val col = postsCollection ?: return Result.success(postDto.toDomain(author.id))

    return try {
      col.document(postId).set(postDto).await()
      Result.success(postDto.toDomain(author.id))
    } catch (e: Exception) {
      Result.success(postDto.toDomain(author.id))
    }
  }

  suspend fun toggleLikePost(postId: String, userId: String): Result<Boolean> {
    val fs = firestore
    val col = postsCollection
    if (fs == null || col == null) return Result.success(true)
    val likeRef = col.document(postId).collection("likes").document(userId)
    val postRef = col.document(postId)

    return try {
      fs.runTransaction { transaction ->
        val likeDoc = transaction.get(likeRef)
        if (likeDoc.exists()) {
          transaction.delete(likeRef)
          transaction.update(postRef, "likesCount", FieldValue.increment(-1))
          false
        } else {
          transaction.set(likeRef, mapOf("likedAt" to System.currentTimeMillis()))
          transaction.update(postRef, "likesCount", FieldValue.increment(1))
          true
        }
      }.await().let { Result.success(it) }
    } catch (e: Exception) {
      Result.success(true)
    }
  }

  suspend fun toggleVaultPost(postId: String, userId: String): Result<Boolean> {
    val col = usersCollection ?: return Result.success(true)
    val vaultRef = col.document(userId).collection("vault").document(postId)
    return try {
      val doc = vaultRef.get().await()
      if (doc.exists()) {
        vaultRef.delete().await()
        Result.success(false)
      } else {
        vaultRef.set(mapOf("savedAt" to System.currentTimeMillis())).await()
        Result.success(true)
      }
    } catch (e: Exception) {
      Result.success(true)
    }
  }

  suspend fun deletePost(postId: String, authorId: String): Result<Unit> {
    val col = postsCollection ?: return Result.success(Unit)
    return try {
      val doc = col.document(postId).get().await()
      if (doc.getString("authorId") == authorId) {
        col.document(postId).delete().await()
        Result.success(Unit)
      } else {
        Result.failure(Exception("Permission denied: You can only delete your own posts"))
      }
    } catch (e: Exception) {
      Result.failure(Exception(e.localizedMessage ?: "Failed to delete post"))
    }
  }

  suspend fun addComment(postId: String, author: UserProfile, text: String): Result<CommentDto> {
    val comment = CommentDto(
      id = UUID.randomUUID().toString(),
      postId = postId,
      authorId = author.id,
      authorName = author.name,
      authorUsername = author.username,
      authorAvatarUrl = author.avatarUrl,
      text = text.trim(),
      createdAt = System.currentTimeMillis()
    )
    val fs = firestore
    val col = postsCollection
    if (fs == null || col == null) return Result.success(comment)
    return try {
      val batch = fs.batch()
      val commentRef = col.document(postId).collection("comments").document(comment.id)
      val postRef = col.document(postId)
      batch.set(commentRef, comment)
      batch.update(postRef, "commentsCount", FieldValue.increment(1))
      batch.commit().await()
      Result.success(comment)
    } catch (e: Exception) {
      Result.success(comment)
    }
  }

  fun observeComments(postId: String): Flow<List<CommentDto>> = callbackFlow {
    val col = postsCollection
    if (col == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }
    val listener = col.document(postId)
      .collection("comments")
      .orderBy("createdAt", Query.Direction.ASCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          trySend(emptyList())
          return@addSnapshotListener
        }
        val list = snapshot?.documents?.mapNotNull { it.toObject(CommentDto::class.java) } ?: emptyList()
        trySend(list)
      }
    awaitClose { listener.remove() }
  }
}

fun PostDto.toDomain(currentUserId: String): Post {
  val diffMinutes = (System.currentTimeMillis() - this.createdAt) / (1000 * 60)
  val timeAgoStr = when {
    diffMinutes < 1 -> "Just now"
    diffMinutes < 60 -> "${diffMinutes}m ago"
    diffMinutes < 1440 -> "${diffMinutes / 60}h ago"
    else -> "${diffMinutes / 1440}d ago"
  }

  val pollParsed = this.pollOptions?.mapNotNull { opt ->
    val id = (opt["id"] as? Number)?.toInt() ?: 0
    val text = opt["text"] as? String ?: ""
    val votes = (opt["votes"] as? Number)?.toInt() ?: 0
    val percent = (opt["percent"] as? Number)?.toInt() ?: 0
    PollOption(id = id, text = text, votes = votes, percent = percent)
  }

  return Post(
    id = this.id,
    author = UserProfile(
      id = this.authorId,
      name = this.authorName,
      username = this.authorUsername,
      avatarUrl = this.authorAvatarUrl
    ),
    timeAgo = timeAgoStr,
    content = this.content,
    type = try { PostType.valueOf(this.type) } catch (_: Exception) { PostType.STANDARD },
    imageUrl = this.imageUrl,
    categoryTag = this.categoryTag,
    pollOptions = pollParsed,
    likesCount = this.likesCount.toInt(),
    commentsCount = this.commentsCount.toInt(),
    resparksCount = this.resparksCount.toInt(),
    isLiked = false,
    isVaulted = false,
    isResparked = false,
    privacyScope = this.privacyScope
  )
}

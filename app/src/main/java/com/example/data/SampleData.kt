package com.example.data

import com.example.model.*

object SampleData {
  val currentUser = UserProfile(
    id = "user_me",
    name = "Aria Montgomery",
    username = "ariadesign",
    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=300&h=300",
    bio = "Architectural curation & human-first digital spaces. Founder @ Studio SNIXLY.",
    location = "San Francisco, CA",
    isVerified = true,
    followersCount = 4820,
    followingCount = 612,
    loopsCount = 38,
    isOnline = true,
    auraStatus = "Designing quietly ✨"
  )

  val elena = UserProfile(
    id = "user_elena",
    name = "Elena Rostova",
    username = "elena.vision",
    avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&q=80&w=300&h=300",
    bio = "Editorial photography & Scandinavian textures.",
    location = "Stockholm, SE",
    isVerified = true,
    followersCount = 12900,
    followingCount = 420,
    loopsCount = 64,
    auraStatus = "Coffee & aperture ☕"
  )

  val marcus = UserProfile(
    id = "user_marcus",
    name = "Marcus Vance",
    username = "marcusv",
    avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&q=80&w=300&h=300",
    bio = "Sound designer & generative ambient audio artist.",
    location = "Berlin, DE",
    isVerified = false,
    followersCount = 3100,
    followingCount = 190,
    loopsCount = 19,
    auraStatus = "Mixing analog synths 🎹"
  )

  val sarah = UserProfile(
    id = "user_sarah",
    name = "Sarah Lin",
    username = "sarahlin.craft",
    avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&q=80&w=300&h=300",
    bio = "Material scientist & minimalist ceramicist.",
    location = "Kyoto, JP",
    isVerified = true,
    followersCount = 8950,
    followingCount = 310,
    loopsCount = 42,
    auraStatus = "In the pottery studio 🏺"
  )

  val julian = UserProfile(
    id = "user_julian",
    name = "Julian Vance",
    username = "julian.arch",
    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=300&h=300",
    bio = "Modernist monolithic structures & ambient light.",
    location = "New York, NY",
    isVerified = true,
    followersCount = 18400,
    followingCount = 520,
    loopsCount = 78,
    auraStatus = "Exploring Brutalism 🏛️"
  )

  val flashMoments = listOf(
    FlashMoment(
      id = "flash_own",
      user = currentUser,
      imageUrl = "",
      hasUnseen = false,
      isOwnAdd = true,
      title = "Flash"
    ),
    FlashMoment(
      id = "flash_elena",
      user = elena,
      imageUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&q=80&w=300&h=300",
      hasUnseen = true,
      title = "Elena"
    ),
    FlashMoment(
      id = "flash_marcus",
      user = marcus,
      imageUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&q=80&w=300&h=300",
      hasUnseen = false,
      title = "Marcus"
    ),
    FlashMoment(
      id = "flash_sarah",
      user = sarah,
      imageUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&q=80&w=300&h=300",
      hasUnseen = true,
      title = "Sarah"
    ),
    FlashMoment(
      id = "flash_julian",
      user = julian,
      imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=300&h=300",
      hasUnseen = true,
      title = "Julian"
    )
  )

  val initialPosts = listOf(
    Post(
      id = "post_1",
      author = julian,
      timeAgo = "2 hours ago",
      location = "NYC",
      content = "The interplay between natural cast shadows and raw concrete never fails to inspire. Morning sunlight hitting the high pavilion arches.",
      type = PostType.IMAGE,
      imageUrl = "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?auto=format&fit=crop&q=80&w=1000",
      categoryTag = "Architecture",
      likesCount = 1248,
      commentsCount = 48,
      resparksCount = 89,
      isLiked = true,
      isVaulted = false
    ),
    Post(
      id = "post_2",
      author = sarah,
      timeAgo = "4 hours ago",
      location = "Kyoto Workshop",
      content = "Experimenting with unglazed charcoal clay. Which surface finish speaks to you for the upcoming Autumn collection?",
      type = PostType.POLL,
      pollOptions = listOf(
        PollOption(1, "Raw Sanded Matte", 342, 58),
        PollOption(2, "Silky Gold Kintsugi", 195, 33),
        PollOption(3, "Smoked Obsidian Gloss", 53, 9)
      ),
      userSelectedPollOption = 1,
      categoryTag = "Craft & Design",
      likesCount = 760,
      commentsCount = 82,
      resparksCount = 34,
      isLiked = false,
      isVaulted = true
    ),
    Post(
      id = "post_3",
      author = elena,
      timeAgo = "6 hours ago",
      location = "Gamla Stan",
      content = "Subtle warmth in autumn reflections. The quiet hours before the city wakes up always bring the most sincere photographic frames.",
      type = PostType.IMAGE,
      imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&q=80&w=1000",
      categoryTag = "Photography",
      likesCount = 2190,
      commentsCount = 114,
      resparksCount = 152,
      isLiked = false,
      isVaulted = false
    )
  )

  val sampleLoops = listOf(
    LoopItem(
      id = "loop_1",
      author = julian,
      title = "Monolithic Light Studies #4",
      description = "Shadow progression through geometric skylights at 60fps.",
      videoThumbnailUrl = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&q=80&w=1000",
      audioTrack = "Marcus Vance • Harmonic Echoes [SNIXLY Audio]",
      seriesTag = "Brutalist Spaces",
      likesCount = 4320,
      commentsCount = 210,
      isLiked = true
    ),
    LoopItem(
      id = "loop_2",
      author = sarah,
      title = "Wheel Throwing: The Centering Phase",
      description = "Zero commentary, pure focus and acoustic clay resonance.",
      videoThumbnailUrl = "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?auto=format&fit=crop&q=80&w=1000",
      audioTrack = "Ambient Studio Acoustic Session",
      seriesTag = "Studio Kyoto",
      likesCount = 3890,
      commentsCount = 145,
      isLiked = false
    ),
    LoopItem(
      id = "loop_3",
      author = marcus,
      title = "Granular Synthesis on Moog Sub 37",
      description = "Crafting deep resonant bass pads for quiet thinking spaces.",
      videoThumbnailUrl = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?auto=format&fit=crop&q=80&w=1000",
      audioTrack = "Original Loop Patch • Berlin Modular",
      seriesTag = "Synth Diary",
      likesCount = 5120,
      commentsCount = 330,
      isLiked = false
    )
  )

  val sampleWhispers = listOf(
    WhisperChat(
      id = "chat_1",
      participant = elena,
      lastMessage = "Loved your latest space curation! The golden ratio proportions are perfect.",
      timestamp = "12m ago",
      unreadCount = 2,
      isEncrypted = true,
      isEphemeral = false,
      auraColorHex = 0xFFC8953E
    ),
    WhisperChat(
      id = "chat_2",
      participant = julian,
      lastMessage = "Sent you the CAD sketches for the pavilion installation.",
      timestamp = "1h ago",
      unreadCount = 0,
      isEncrypted = true,
      isEphemeral = true,
      auraColorHex = 0xFF10B981
    ),
    WhisperChat(
      id = "chat_3",
      participant = marcus,
      lastMessage = "Let's collaborate on the soundtrack for the Next Loop Series.",
      timestamp = "Yesterday",
      unreadCount = 0,
      isEncrypted = true,
      isEphemeral = false,
      auraColorHex = 0xFF3B82F6
    ),
    WhisperChat(
      id = "chat_4",
      participant = sarah,
      lastMessage = "The glaze sample arrived safely in Kyoto. Beautiful tone!",
      timestamp = "2d ago",
      unreadCount = 0,
      isEncrypted = true,
      isEphemeral = false,
      auraColorHex = 0xFFC8953E
    )
  )

  val sampleSignals = listOf(
    SignalNotification(
      id = "sig_1",
      user = elena,
      title = "Elena Rostova resparked your thought",
      subtitle = "\"Digital spaces should breathe with intent and negative space...\"",
      timeAgo = "10m ago",
      type = SignalType.RESPARK,
      isRead = false
    ),
    SignalNotification(
      id = "sig_2",
      user = julian,
      title = "Julian Vance added your post to Vault",
      subtitle = "Added to \"Minimalist Architecture Inspiration\"",
      timeAgo = "45m ago",
      type = SignalType.VAULT_SAVE,
      isRead = false
    ),
    SignalNotification(
      id = "sig_3",
      user = sarah,
      title = "Sarah Lin commented on your craft preview",
      subtitle = "\"The proportion balance here is exquisite.\"",
      timeAgo = "3h ago",
      type = SignalType.COMMENT,
      isRead = true
    ),
    SignalNotification(
      id = "sig_4",
      user = marcus,
      title = "Marcus Vance initiated an encrypted Whisper",
      subtitle = "Tap to unlock thread",
      timeAgo = "1d ago",
      type = SignalType.WHISPER_REQUEST,
      isRead = true
    )
  )
}

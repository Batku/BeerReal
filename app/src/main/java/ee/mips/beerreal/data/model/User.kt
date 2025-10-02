package ee.mips.beerreal.data.model

import java.util.Date

data class User(
    val id: String,
    val username: String,
    val email: String,
    val profileImageUrl: String? = null,
    val tasteScore: Int = 0,
    val totalPosts: Int = 0,
    val friendsCount: Int = 0,
    val joinedDate: Date = Date(),
    val bio: String? = null
)
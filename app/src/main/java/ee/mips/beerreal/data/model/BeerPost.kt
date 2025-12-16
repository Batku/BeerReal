package ee.mips.beerreal.data.model

import java.util.Date

data class BeerPost(
    val id: String,
    val userId: String,
    val username: String,
    val userProfileImageData: String? = null,
    val caption: String,
    val imageData: String,
    val location: String? = null,
    val timestamp: Date,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val comments: List<Comment> = emptyList(),
    val hasUserVoted: Boolean = false,
    val userVoteType: VoteType? = null // null = no vote, true = upvote, false = downvote
)

data class Comment(
    val id: String,
    val userId: String,
    val username: String,
    val userProfileImageData: String? = null,
    val text: String,
    val timestamp: Date
)

enum class VoteType {
    UPVOTE, DOWNVOTE
}
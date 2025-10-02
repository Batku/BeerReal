package ee.mips.beerreal.data.repository

import ee.mips.beerreal.data.MockData
import ee.mips.beerreal.data.model.BeerPost
import ee.mips.beerreal.data.model.User
import ee.mips.beerreal.data.model.VoteType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BeerRepository {
    
    fun getBeerPosts(): Flow<List<BeerPost>> = flow {
        // Simulate network delay
        delay(500)
        emit(MockData.getBeerPosts())
    }
    
    fun getCurrentUser(): Flow<User> = flow {
        delay(200)
        emit(MockData.currentUser)
    }
    
    fun getUserPosts(userId: String): Flow<List<BeerPost>> = flow {
        delay(300)
        emit(MockData.getUserPosts(userId))
    }
    
    fun getAllUsers(): Flow<List<User>> = flow {
        delay(400)
        emit(MockData.getAllUsers())
    }
    
    fun getPostById(postId: String): Flow<BeerPost?> = flow {
        delay(300)
        emit(MockData.getBeerPosts().find { it.id == postId })
    }
    
    suspend fun addBeerPost(
        caption: String,
        imageUrl: String
    ): Result<BeerPost> {
        return try {
            // Simulate network call
            delay(1000)
            
            val newPost = BeerPost(
                id = "post_${System.currentTimeMillis()}",
                userId = MockData.currentUser.id,
                username = MockData.currentUser.username,
                userProfileImage = MockData.currentUser.profileImageUrl,
                caption = caption,
                imageUrl = imageUrl,
                location = "Current Location", // In real app, this would come from location service
                timestamp = java.util.Date(),
                upvotes = 0,
                downvotes = 0,
                comments = emptyList()
            )
            
            Result.success(newPost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun voteOnPost(postId: String, voteType: VoteType): Result<BeerPost> {
        return try {
            delay(300)
            // In a real app, this would update the server and return updated post
            val post = MockData.getBeerPosts().find { it.id == postId }
            if (post != null) {
                val updatedPost = when (voteType) {
                    VoteType.UPVOTE -> post.copy(
                        upvotes = post.upvotes + 1,
                        userVoteType = VoteType.UPVOTE
                    )
                    VoteType.DOWNVOTE -> post.copy(
                        downvotes = post.downvotes + 1,
                        userVoteType = VoteType.DOWNVOTE
                    )
                }
                Result.success(updatedPost)
            } else {
                Result.failure(Exception("Post not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
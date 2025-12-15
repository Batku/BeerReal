package ee.mips.beerreal.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import ee.mips.beerreal.data.MockData
import ee.mips.beerreal.data.api.BeerApiService
import ee.mips.beerreal.data.api.CreatePostRequest
import ee.mips.beerreal.data.api.RetrofitClient
import ee.mips.beerreal.data.model.BeerPost
import ee.mips.beerreal.data.model.User
import ee.mips.beerreal.data.model.VoteType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BeerRepository(
    private val apiService: BeerApiService = RetrofitClient.apiService,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    
    fun getBeerPosts(): Flow<List<BeerPost>> = flow {
        try {
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
            val authHeader = if (token != null) "Bearer $token" else null
            val response = apiService.getPosts(token = authHeader)
            if (response.isSuccessful) {
                emit(response.body()?.posts ?: emptyList())
            } else {
                // Fallback to mock data if API fails (e.g. backend not running)
                emit(MockData.getBeerPosts())
            }
        } catch (e: Exception) {
            // Fallback to mock data
            emit(MockData.getBeerPosts())
        }
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
        try {
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
            val authHeader = if (token != null) "Bearer $token" else null
            val response = apiService.getPost(postId, token = authHeader)
            if (response.isSuccessful) {
                emit(response.body())
            } else {
                emit(MockData.getBeerPosts().find { it.id == postId })
            }
        } catch (e: Exception) {
            emit(MockData.getBeerPosts().find { it.id == postId })
        }
    }
    
    suspend fun addBeerPost(
        caption: String,
        imageUri: Uri
    ): Result<BeerPost> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not authenticated")
            val token = user.getIdToken(false).await().token ?: throw Exception("Failed to get token")
            
            // Upload image to Firebase Storage
            val filename = "${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("posts/$filename")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            // Create post via API
            val request = CreatePostRequest(
                caption = caption,
                imageUrl = downloadUrl,
                location = "Unknown" // TODO: Get real location
            )
            
            val response = apiService.createPost(request, "Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create post: ${response.message()}"))
            }
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
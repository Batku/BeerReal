package ee.mips.beerreal.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
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
    private val context: Context? = null,
    private val apiService: BeerApiService = RetrofitClient.apiService,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    
    fun getBeerPosts(): Flow<List<BeerPost>> = flow {
        try {
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
            val authHeader = if (token != null) "Bearer $token" else null
            Log.d("BeerRepository", "Fetching posts with token: ${token?.take(10)}...")
            
            val response = apiService.getPosts(token = authHeader)
            if (response.isSuccessful) {
                Log.d("BeerRepository", "Posts fetched successfully: ${response.body()?.posts?.size} posts")
                emit(response.body()?.posts ?: emptyList())
            } else {
                Log.e("BeerRepository", "Failed to fetch posts: ${response.code()} ${response.message()}")
                // Fallback to mock data if API fails (e.g. backend not running)
                emit(MockData.getBeerPosts())
            }
        } catch (e: Exception) {
            Log.e("BeerRepository", "Error fetching posts", e)
            // Fallback to mock data
            emit(MockData.getBeerPosts())
        }
    }
    
    fun getCurrentUser(): Flow<User?> = flow {
        try {
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
            if (token != null) {
                val response = apiService.getMe("Bearer $token")
                if (response.isSuccessful) {
                    emit(response.body())
                } else {
                    Log.e("BeerRepository", "Failed to fetch user: ${response.code()} ${response.message()}")
                    emit(null)
                }
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("BeerRepository", "Error fetching user", e)
            emit(null)
        }
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
            Log.d("BeerRepository", "Starting post creation...")
            val user = auth.currentUser ?: throw Exception("User not authenticated")
            val token = user.getIdToken(false).await().token ?: throw Exception("Failed to get token")
            
            // Convert image to Base64
            Log.d("BeerRepository", "Converting image to Base64...")
            if (context == null) {
                throw Exception("Context is required for image processing")
            }
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            
            if (bytes == null) {
                throw Exception("Failed to read image data")
            }
            
            val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val imageData = "data:image/jpeg;base64,$base64Image"

            // Create post via API
            val request = CreatePostRequest(
                caption = caption,
                imageData = imageData,
                location = "Unknown" // TODO: Get real location
            )
            
            Log.d("BeerRepository", "Creating post on backend...")
            val response = apiService.createPost(request, "Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                Log.d("BeerRepository", "Post created successfully")
                Result.success(response.body()!!)
            } else {
                Log.e("BeerRepository", "Failed to create post: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to create post: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("BeerRepository", "Error creating post", e)
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
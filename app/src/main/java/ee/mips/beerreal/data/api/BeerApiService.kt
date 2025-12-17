package ee.mips.beerreal.data.api

import ee.mips.beerreal.data.model.BeerPost
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

import ee.mips.beerreal.data.model.User

interface BeerApiService {

    @GET("api/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): Response<User>

    @GET("api/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Header("Authorization") token: String? = null
    ): Response<GetPostsResponse>

    @GET("api/users/{userId}/posts")
    suspend fun getUserPosts(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Header("Authorization") token: String? = null
    ): Response<GetPostsResponse>

    @GET("api/posts/{id}")
    suspend fun getPost(
        @Path("id") id: String,
        @Header("Authorization") token: String? = null
    ): Response<BeerPost>

    @POST("api/posts")
    suspend fun createPost(
        @Body request: CreatePostRequest,
        @Header("Authorization") token: String
    ): Response<BeerPost>

    @POST("api/posts/vote")
    suspend fun votePost(
        @Body request: VoteRequest,
        @Header("Authorization") token: String
    ): Response<VoteResponse>

    @POST("api/posts/comment")
    suspend fun addComment(
        @Body request: AddCommentRequest,
        @Header("Authorization") token: String
    ): Response<ee.mips.beerreal.data.model.Comment>

    @retrofit2.http.PUT("api/me")
    suspend fun updateUser(
        @Body request: UpdateUserRequest,
        @Header("Authorization") token: String
    ): Response<User>
}

data class GetPostsResponse(
    val posts: List<BeerPost>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int
)

data class CreatePostRequest(
    val caption: String,
    val imageData: String,
    val location: String? = null
)

data class VoteRequest(
    val postId: String,
    val voteType: String // "UPVOTE" or "DOWNVOTE"
)

data class VoteResponse(
    val upvotes: Int,
    val downvotes: Int
)

data class AddCommentRequest(
    val postId: String,
    val text: String
)

data class UpdateUserRequest(
    val username: String? = null,
    val profileImageData: String? = null,
    val bio: String? = null
)

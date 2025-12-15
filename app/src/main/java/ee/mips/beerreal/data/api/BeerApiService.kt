package ee.mips.beerreal.data.api

import ee.mips.beerreal.data.model.BeerPost
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BeerApiService {

    @GET("api/posts")
    suspend fun getPosts(
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

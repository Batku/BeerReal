package ee.mips.beerreal.ui.screens.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ee.mips.beerreal.data.model.BeerPost
import ee.mips.beerreal.data.model.VoteType
import ee.mips.beerreal.data.repository.BeerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PostDetailUiState(
    val post: BeerPost? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PostDetailViewModel(
    private val repository: BeerRepository = BeerRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()
    
    fun loadPost(postId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getPostById(postId).collect { post ->
                    _uiState.value = _uiState.value.copy(
                        post = post,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load post"
                )
            }
        }
    }
    
    fun onVote(postId: String, voteType: VoteType) {
        viewModelScope.launch {
            try {
                val result = repository.voteOnPost(postId, voteType)
                result.onSuccess { voteResponse ->
                    val currentPost = _uiState.value.post
                    if (currentPost != null && currentPost.id == postId) {
                        val updatedPost = currentPost.copy(
                            upvotes = voteResponse.upvotes,
                            downvotes = voteResponse.downvotes,
                            userVoteType = if (currentPost.userVoteType == voteType) null else voteType
                        )
                        _uiState.value = _uiState.value.copy(post = updatedPost)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to vote: ${e.message}"
                )
            }
        }
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            try {
                val result = repository.addComment(postId, text)
                result.onSuccess { comment ->
                    val currentPost = _uiState.value.post
                    if (currentPost != null) {
                        val updatedComments = currentPost.comments + comment
                        val updatedPost = currentPost.copy(comments = updatedComments)
                        _uiState.value = _uiState.value.copy(post = updatedPost)
                    }
                }
                result.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to add comment: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add comment: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
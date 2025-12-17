package ee.mips.beerreal.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ee.mips.beerreal.data.model.User
import ee.mips.beerreal.data.repository.BeerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val userPosts: List<ee.mips.beerreal.data.model.BeerPost> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingPosts: Boolean = false,
    val errorMessage: String? = null,
    val showFriendsDialog: Boolean = false
)

class ProfileViewModel(
    private val repository: BeerRepository = BeerRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getCurrentUser().collect { user ->
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoading = false,
                        errorMessage = null
                    )
                    if (user != null) {
                        loadUserPosts(user.id)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load profile"
                )
            }
        }
    }
    
    private fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPosts = true)
            try {
                repository.getUserPosts(userId).collect { posts ->
                    _uiState.value = _uiState.value.copy(
                        userPosts = posts,
                        isLoadingPosts = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingPosts = false,
                    errorMessage = e.message ?: "Failed to load posts"
                )
            }
        }
    }
    
    fun updateProfile(username: String?, profileImageUri: android.net.Uri?, bio: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = repository.updateUser(username, profileImageUri, bio)
                result.onSuccess { updatedUser ->
                    _uiState.value = _uiState.value.copy(
                        user = updatedUser,
                        isLoading = false
                    )
                }
                result.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to update profile: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to update profile: ${e.message}"
                )
            }
        }
    }

    fun refreshProfile() {
        loadCurrentUser()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
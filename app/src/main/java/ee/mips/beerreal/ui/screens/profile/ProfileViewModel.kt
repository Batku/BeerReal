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
        loadUserPosts()
        loadAllUsers()
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
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load profile"
                )
            }
        }
    }
    
    private fun loadUserPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPosts = true)
            try {
                repository.getUserPosts("user1").collect { posts ->
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
    
    private fun loadAllUsers() {
        viewModelScope.launch {
            try {
                repository.getAllUsers().collect { users ->
                    _uiState.value = _uiState.value.copy(
                        allUsers = users.filter { it.id != "user1" } // Exclude current user for friends list (currently hard coded), also will be queried on the backend and wont load ALL users, thats stupid like braindead like damn
                    )
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    fun refreshProfile() {
        loadCurrentUser()
        loadUserPosts()
    }
    
    fun onFindFriendsClick() {
        _uiState.value = _uiState.value.copy(showFriendsDialog = true)
    }
    
    fun onDismissFriendsDialog() {
        _uiState.value = _uiState.value.copy(showFriendsDialog = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
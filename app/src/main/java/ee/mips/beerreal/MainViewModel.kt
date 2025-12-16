package ee.mips.beerreal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import ee.mips.beerreal.data.model.User
import ee.mips.beerreal.data.repository.BeerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AppState {
    object Loading : AppState()
    object Ready : AppState()
    data class Error(val message: String) : AppState()
}

class MainViewModel(
    private val repository: BeerRepository = BeerRepository()
) : ViewModel() {

    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        checkUser()
    }

    fun checkUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                repository.getCurrentUser().collect { user ->
                    if (user != null) {
                        _appState.value = AppState.Ready
                    } else {
                        _appState.value = AppState.Error("Failed to load user profile")
                    }
                }
            }
        } else {
            _appState.value = AppState.Ready
        }
    }
}

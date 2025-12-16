package ee.mips.beerreal.ui.screens.add

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ee.mips.beerreal.data.repository.BeerRepository
import ee.mips.beerreal.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddBeerUiState(
    val caption: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val validationErrors: ValidationErrors = ValidationErrors()
)

data class ValidationErrors(
    val captionError: String? = null,
    val imageError: String? = null
)

class AddBeerViewModel(
    private val repository: BeerRepository = BeerRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddBeerUiState())
    val uiState: StateFlow<AddBeerUiState> = _uiState.asStateFlow()
    
    fun updateCaption(caption: String) {
        _uiState.value = _uiState.value.copy(
            caption = caption,
            validationErrors = _uiState.value.validationErrors.copy(captionError = null)
        )
    }
    
    fun updateImageUri(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            imageUri = uri,
            validationErrors = _uiState.value.validationErrors.copy(imageError = null)
        )
    }
    
    fun submitBeerPost() {
        val currentState = _uiState.value
        val validationErrors = validateInput(currentState)
        
        if (validationErrors.hasErrors()) {
            _uiState.value = currentState.copy(validationErrors = validationErrors)
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)
            
            try {
                val result = repository.addBeerPost(
                    caption = currentState.caption,
                    imageUri = currentState.imageUri!!
                )
                
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to submit post"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    private fun validateInput(state: AddBeerUiState): ValidationErrors {
        return ValidationErrors(
            captionError = ValidationUtils.validateCaption(state.caption),
            imageError = if (state.imageUri == null) "Image is required" else null
        )
    }
    
    fun ValidationErrors.hasErrors(): Boolean {
        return captionError != null || imageError != null
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun resetForm() {
        _uiState.value = AddBeerUiState()
    }
}
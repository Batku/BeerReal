package ee.mips.beerreal.utils

object ValidationUtils {
    
    fun validateCaption(caption: String): String? {
        return when {
            caption.isBlank() -> "Caption is required"
            caption.length < 3 -> "Caption must be at least 3 characters"
            caption.length > 500 -> "Caption must be less than 500 characters"
            else -> null
        }
    }
    
    fun validateImageUrl(url: String): String? {
        return when {
            url.isBlank() -> "Image URL is required"
            !url.startsWith("http://") && !url.startsWith("https://") -> "Please enter a valid URL"
            else -> null
        }
    }
}
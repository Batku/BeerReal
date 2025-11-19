package ee.mips.beerreal.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun validateCaption_empty_returnsError() {
        val err = ValidationUtils.validateCaption("")
        assertEquals("Caption is required", err)
    }

    @Test
    fun validateCaption_tooShort_returnsError() {
        val err = ValidationUtils.validateCaption("ab")
        assertEquals("Caption must be at least 3 characters", err)
    }

    @Test
    fun validateCaption_tooLong_returnsError() {
        val long = "a".repeat(600)
        val err = ValidationUtils.validateCaption(long)
        assertEquals("Caption must be less than 500 characters", err)
    }

    @Test
    fun validateCaption_valid_returnsNull() {
        val err = ValidationUtils.validateCaption("Looks great")
        assertNull(err)
    }
}

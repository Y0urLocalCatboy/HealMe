package com.example.healme.viewmodel

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import com.example.healme.R
import java.text.ParseException
import java.util.Locale

class AuthViewModel : ViewModel() {

    /**
     * Validates a name according to the following rules:
     * - Cannot contain numbers
     * - Cannot contain special characters
     * - Cannot contain spaces
     * - First letter must be uppercase
     * - Must be between 2 and 15 characters
     *
     * @param name Name to validate
     * @return Error message string if validation fails, null if validation passes
     */
    @Composable
    fun nameValidity(name: String): String? {
        val minLength = 2
        val numberCheck = name.any { it.isDigit() }
        val specialCharCheck = name.any { "!@#$%^&*()_+-=[]{}|;:',.<>?".contains(it) }
        val spaceCheck = name.any { it.isWhitespace() }
        val firstUppercaseOnly = name[0].isUpperCase() && name.substring(1).all { it.isLowerCase() }
        val isShort = name.length < minLength
        val isLong = name.length > 15
        return when {
            numberCheck -> stringResource(R.string.name_digit)
            specialCharCheck -> stringResource(R.string.name_special_char)
            spaceCheck -> stringResource(R.string.name_space)
            isShort -> stringResource(R.string.name_short)
            isLong -> stringResource(R.string.name_long)
            !firstUppercaseOnly -> stringResource(R.string.name_uppercase)
            else -> null
        }
    }

    /**
     * Validates if the user is at least 18 years old based on the provided date of birth.
     * The date of birth must be in format "dd-MM-yyyy".
     * The user must be at least 18 years old and at most 110 years old.
     *
     * @param dateOfBirth Date of birth in format "dd-MM-yyyy"
     * @return Error message string if validation fails, null if validation passes
     */
    @Composable
    fun ageValidity(dateOfBirth: String): String? {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        val dob = try {
            dateFormat.parse(dateOfBirth)
        } catch (e: ParseException) {
            return stringResource(R.string.invalid_birthdate)
        }

        val today = Calendar.getInstance()
        val birthdate = Calendar.getInstance()
        birthdate.time = dob

        var age = today.get(Calendar.YEAR) - birthdate.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthdate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return when {
            age < 18 -> stringResource(R.string.invalid_age)
            age > 110 -> stringResource(R.string.invalid_birthdate)

            else -> null
        }
    }

    /**
     * Validates a surname according to the following rules:
     * - Cannot contain numbers
     * - Cannot contain special characters (except hyphen for compound surnames)
     * - Cannot contain spaces (use hyphens instead)
     * - First letter must be uppercase
     * - Must be between 2 and 15 characters
     *
     * @param surname Surname to validate
     * @return Error message string if validation fails, null if validation passes
     */
    @Composable
    fun surnameValidity(surname: String): String? {
        val minLength = 2
        val numberCheck = surname.any { it.isDigit() }
        val specialCharCheck = surname.any { "!@#$%^&*()_+=[]{}|;:',.<>?".contains(it) }
        val spaceCheck = surname.any { it.isWhitespace() }
        val firstUppercaseOnly = surname.split("-").all { part ->
            part.isNotEmpty() && part[0].isUpperCase() && part.drop(1).all { it.isLowerCase() }
        }
        val isShort = surname.length < minLength
        val isLong = surname.length > 15
        return when {
            numberCheck -> stringResource(R.string.surname_digit)
            specialCharCheck -> stringResource(R.string.surname_special_char)
            spaceCheck -> stringResource(R.string.surname_hyphenated)
            isShort -> stringResource(R.string.surname_short)
            isLong -> stringResource(R.string.surname_long)
            !firstUppercaseOnly -> stringResource(R.string.surname_uppercase)
            else -> null
        }
    }
    /**
     * Validates the password entered by the user.
     * The password must:
     * - Be at least 8 characters long
     * - Contain at least one uppercase letter
     * - Contain at least one number
     * - Contain at least one special character
     *
     *
     * @param password Password to validate.
     * @return An error message if the password is invalid, null otherwise.
     */
    @Composable
    fun passwordValidity(password: String): String? {
        val minLength = 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasNumber = password.any { it.isDigit() }
        val hasSpecialChar = password.any { "!@#$%^&*()_+-=[]{}|;:',.<>?".contains(it) }

        return when {
            password.length < minLength -> stringResource(R.string.password_length)
            !hasUpperCase -> stringResource(R.string.password_uppercase)
            !hasNumber -> stringResource(R.string.password_digit)
            !hasSpecialChar -> stringResource(R.string.password_special_char)
            else -> null
        }
    }
}

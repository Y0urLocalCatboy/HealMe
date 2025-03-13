package com.example.healme

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import java.text.ParseException
import java.util.Locale

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    /**
     * Opens a new activity.
     *
     * @param activity The activity to open
     * @parem logOut Boolean indicating if the user should be logged out
     * @param extras Bundle containing extra data to pass to the new activity
     */
    fun openActivity(activity: Class<*>, logOut: Boolean = false, extras: Bundle? = null) {
        startActivity(Intent(this, activity))
        TODO("Dodac logout z firebase i obsluge extra intentu")
    }

    /**
     * Displays a toast message.
     *
     * @param message The text message to be displayed
     */
    fun showToast(message: String) {
        Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }

    /**
     * Displays a customizable Snackbar message.
     * If message is null then nothing is displayed.
     *
     * @param message The text message to display
     * @param isError Boolean indicating if this is an error message (red background) or not (blue background)
     */
    fun snackbarMessage(message: String="Allsgood", isError: Boolean) {
        if(message=="Allsgood"){
            return
        }
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        snackbarView.setBackgroundColor(ContextCompat.getColor(this,
            if (isError) R.color.red else R.color.light_blue))

        snackbar.setTextColor(ContextCompat.getColor(this,
            if (isError) R.color.white else R.color.black))

        snackbar.show()
    }

    /**
     * Validates a name according to the following rules:
     * - Cannot contain numbers
     * - Cannot contain special characters
     * - Cannot contain spaces
     * - Only the first letter can be uppercase
     * - Must be between 2 and 15 characters
     *
     * @param name The name to validate
     * @return Error message string if validation fails, null if validation passes
     */
    fun nameValidity(name: String): String? {
        val minLength = 2
        val numberCheck = name.any { it.isDigit() }
        val specialCharCheck = name.any { "!@#$%^&*()_+-=[]{}|;:',.<>?".contains(it) }
        val spaceCheck = name.any { it.isWhitespace() }
        val firstUppercaseOnly = name[0].isUpperCase() && name.substring(1).all { it.isLowerCase() }
        val isShort = name.length < minLength
        val isLong = name.length > 15
        return when {
            numberCheck -> getString(R.string.name_digit)
            specialCharCheck -> getString(R.string.name_special_char)
            spaceCheck -> getString(R.string.name_space)
            isShort -> getString(R.string.name_short)
            isLong -> getString(R.string.name_long)
            !firstUppercaseOnly -> getString(R.string.name_uppercase)
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
     * @param surname The surname to validate
     * @return Error message string if validation fails, null if validation passes
     */
    fun surnameValidity(surname: String): String? {
        val minLength = 2
        val numberCheck = surname.any { it.isDigit() }
        val specialCharCheck = surname.any { "!@#$%^&*()_+-=[]{}|;:',.<>?".contains(it) }
        val spaceCheck = surname.any { it.isWhitespace() }
        val firstUppercaseOnly = surname[0].isUpperCase() && surname.substring(1).all { it.isLowerCase() }
        val isShort = surname.length < minLength
        val isLong = surname.length > 15
        return when {
            numberCheck -> getString(R.string.surname_digit)
            specialCharCheck -> getString(R.string.surname_special_char)
            spaceCheck -> getString(R.string.surname_hyphenated)
            isShort -> getString(R.string.surname_short)
            isLong -> getString(R.string.surname_long)
            !firstUppercaseOnly -> getString(R.string.surname_uppercase)
            else -> null
        }
    }

    /**
     * Validates if the user is at least 18 years old based on the provided date of birth.
     * The date of birth must be in format "dd-MM-yyyy".
     *
     * @param dateOfBirth Date of birth in format "dd-MM-yyyy"
     * @return Error message string if validation fails, null if validation passes
     */
    fun ageValidity(dateOfBirth: String): String? {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        val dob = try {
            dateFormat.parse(dateOfBirth)
        } catch (e: ParseException) {
            return getString(R.string.invalid_birthdate)
        }

        val today = Calendar.getInstance()
        val birthdate = Calendar.getInstance()
        birthdate.time = dob

        var age = today.get(Calendar.YEAR) - birthdate.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthdate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return when {
            age < 18 -> getString(R.string.invalid_age)
            else -> null
        }
    }
}
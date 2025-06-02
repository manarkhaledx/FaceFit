package com.example.facefit.domain.utils.validators

object SignUpValidator {
    fun validateFirstName(firstName: String): String? {
        return if (firstName.isBlank()) {
            "First name is required"
        } else if (firstName.any { it.isDigit() }) {
            "First name cannot contain numbers"
        } else {
            null
        }
    }

    fun validateLastName(lastName: String): String? {
        return if (lastName.isBlank()) {
            "Last name is required"
        } else if (lastName.any { it.isDigit() }) {
            "Last name cannot contain numbers"
        } else {
            null
        }
    }

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isBlank() -> "Email is required"
            " " in trimmed -> "Email cannot contain spaces"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> "Invalid email format"
            else -> null
        }
    }


    fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() -> "Phone number is required"
            !phone.startsWith("+20") -> "Phone number must start with +20"
            !phone.matches(Regex("^\\+201[0125][0-9]{8}$")) ->
                "Phone number must start with 010, 011, 012, or 015"
            phone.length != 13 -> "Phone number must be 11 digits"
            !phone.substring(3).all { it.isDigit() } -> "Phone number must contain only digits"

            else -> null
        }
    }


    fun validatePassword(password: String): String? {
        return when {
            " " in password -> "Password cannot contain spaces"
            password.isBlank() -> "Password is required"
            password.length < 8 -> "Password should be at least 8 characters long"
            !password.any { it.isUpperCase() } -> "Password must contain at least one uppercase letter"
            !password.any { it.isLowerCase() } -> "Password must contain at least one lowercase letter"
            !password.any { it.isDigit() } -> "Password must contain at least one number"
            !password.any { !it.isLetterOrDigit() } -> "Password must contain at least one special character"
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return if (confirmPassword.isBlank()) {
            "Please confirm your password"
        } else if (password != confirmPassword) {
            "Passwords do not match"
        } else {
            null
        }
    }
}
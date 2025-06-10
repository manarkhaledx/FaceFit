package com.example.facefit.domain.utils.validators

object ProfileValidator {
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

    fun validatePhone(phone: String): String? {
        var number = phone.removePrefix("+20").replace(" ", "")
        if (number.startsWith("0")) {
            number = number.drop(1)
        }
        return when {
            number.isBlank() -> "Phone number is required"
            number.length != 10 -> "Phone number must be 10 digits"
            !number.matches(Regex("^1[0125][0-9]{8}$")) ->
                "Phone number must start with 010, 011, 012, or 015"
            else -> null
        }
    }
}
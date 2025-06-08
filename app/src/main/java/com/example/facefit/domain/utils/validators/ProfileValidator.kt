package com.example.facefit.domain.utils.validators

import com.example.facefit.domain.models.User

object ProfileValidator {

    fun validateUser(
        user: User,
        emailAlreadyExists: Boolean = false
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        SignUpValidator.validateFirstName(user.firstName)?.let {
            errors["firstName"] = it
        }

        SignUpValidator.validateLastName(user.lastName)?.let {
            errors["lastName"] = it
        }

        SignUpValidator.validateEmail(user.email, emailAlreadyExists)?.let {
            errors["email"] = it
        }

        SignUpValidator.validatePhone(user.phone)?.let {
            errors["phone"] = it
        }

        if (!user.address.isNullOrBlank() && user.address.length > 100) {
            errors["address"] = "Address too long"
        }

        return errors
    }
}
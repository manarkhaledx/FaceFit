package com.example.facefit.domain.utils.validators

object PrescriptionValidator {

        fun validateSPH(value: String): String? {
            return when {
                value.isBlank() -> "SPH is required"
                !value.matches(Regex("^-?\\d{1,2}(\\.\\d{1,2})?$")) -> "Only numbers allowed"
                value.toFloat() !in -20f..20f -> "Value must be between -20 and 20"
                else -> null
            }
        }

        fun validateCYL(value: String): String? {
            return when {
                value.isBlank() -> "CYL is required"
                !value.matches(Regex("^-?\\d{1,2}(\\.\\d{1,2})?$")) -> "Only numbers allowed"
                value.toFloat() !in -6f..6f -> "Value must be between -6 and 6"
                else -> null
            }
        }

        fun validateAXIS(value: String): String? {
            return when {
                value.isBlank() -> "AXIS is required"
                !value.matches(Regex("^\\d{1,3}$")) -> "Only whole numbers allowed"
                value.toInt() !in 1..180 -> "Value must be between 1 and 180"
                else -> null
            }
        }

fun validatePD(value: String): String? {
    return when {
        value.isBlank() -> "PD is required"
        !value.matches(Regex("^\\d{2}(\\.\\d{1,2})?$")) -> "Only numbers allowed"
        value.toFloat() !in 20f..80f -> "Value must be between 20 and 80"
        else -> null
    }
}

fun validateLeftPD(value: String): String? {
    return when {
        value.isBlank() -> "Left PD is required"
        !value.matches(Regex("^\\d{2}(\\.\\d{1,2})?$")) -> "Only numbers allowed"
        value.toFloat() !in 20f..80f -> "Value must be between 20 and 80"
        else -> null
    }
}

fun validateRightPD(value: String): String? {
    return when {
        value.isBlank() -> "Right PD is required"
        !value.matches(Regex("^\\d{2}(\\.\\d{1,2})?$")) -> "Only numbers allowed"
        value.toFloat() !in 20f..80f -> "Value must be between 20 and 80"
        else -> null
    }
}
    }





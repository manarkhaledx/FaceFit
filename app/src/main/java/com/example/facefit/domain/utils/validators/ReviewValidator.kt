package com.example.facefit.domain.utils.validators



object ReviewValidator {

    fun validateGlassesId(glassesId: String): String? {
        return if (glassesId.isBlank()) "Glasses ID is required" else null
    }

    fun validateRating(rating: Int): String? {
        return if (rating !in 1..5) "Rating must be between 1 and 5" else null
    }

    fun validateComment(comment: String): String? {
        return when {
            comment.isBlank() -> "Comment cannot be empty"
            comment.length > 500 -> "Comment is too long"
            else -> null
        }
    }

    fun validateReview(glassesId: String, rating: Int, comment: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        validateGlassesId(glassesId)?.let { errors["glassesId"] = it }
        validateRating(rating)?.let { errors["rating"] = it }
        validateComment(comment)?.let { errors["comment"] = it }
        return errors
    }
}




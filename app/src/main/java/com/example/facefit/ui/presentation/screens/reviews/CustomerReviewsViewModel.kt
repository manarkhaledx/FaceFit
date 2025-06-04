package com.example.facefit.ui.presentation.screens.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.domain.models.Review
import com.example.facefit.domain.usecases.reviews.GetReviewsUseCase
import com.example.facefit.domain.usecases.reviews.SubmitReviewUseCase
import com.example.facefit.domain.utils.Resource
import com.example.facefit.domain.utils.validators.ReviewValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerReviewsViewModel @Inject constructor(
    private val getReviewsUseCase: GetReviewsUseCase,
    private val submitReviewUseCase: SubmitReviewUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerReviewsUiState())
    val uiState: StateFlow<CustomerReviewsUiState> = _uiState.asStateFlow()

    fun loadReviews(glassesId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val token = tokenManager.getToken() ?: run {
                _uiState.update { it.copy(isLoading = false, error = "Not authenticated") }
                return@launch
            }

            when (val result = getReviewsUseCase(token, glassesId)) {
                is Resource.Success -> {
                    val reviews = result.data ?: emptyList()
                    val averageRating = if (reviews.isNotEmpty()) {
                        reviews.map { it.rating?.toDouble() ?: 0.0 }.average()
                    } else {
                        0.0
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reviews = reviews,
                            averageRating = averageRating,
                            error = null
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load reviews"
                        )
                    }
                }

                is Resource.Loading -> TODO()
            }
        }
    }


    fun submitReview(glassesId: String, rating: Int, comment: String) {
        val errors = ReviewValidator.validateReview(glassesId, rating, comment)
        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    isSubmittingReview = false,
                    error = errors.values.firstOrNull()
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSubmittingReview = true, error = null) }

            val token = tokenManager.getToken()
            if (token == null) {
                _uiState.update {
                    it.copy(
                        isSubmittingReview = false,
                        error = "Authentication required"
                    )
                }
                return@launch
            }

            when (val result = submitReviewUseCase(token, glassesId, rating, comment)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingReview = false,
                            reviewSubmitted = true,
                            error = null
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingReview = false,
                            error = result.message ?: "An error occurred"
                        )
                    }
                }

                else -> Unit
            }
        }
    }


}
data class CustomerReviewsUiState(
    val reviews: List<Review> = emptyList(),
    val averageRating: Double = 0.0,
    val isLoading: Boolean = false,
    val isSubmittingReview: Boolean = false,
    val reviewSubmitted: Boolean = false,
    val error: String? = null
)
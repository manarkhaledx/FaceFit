package com.example.facefit.ui.presentation.screens.prescription

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.models.requests.AddToCartRequest
import com.example.facefit.data.models.requests.CreatePrescriptionRequest
import com.example.facefit.domain.models.CartData
import com.example.facefit.domain.models.DualPd
import com.example.facefit.domain.models.EyePrescription
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.models.PdMeasurement
import com.example.facefit.domain.usecases.cart.AddToCartUseCase
import com.example.facefit.domain.usecases.glasses.GetGlassesByIdUseCase
import com.example.facefit.domain.usecases.prescription.CreatePrescriptionUseCase
import com.example.facefit.domain.utils.Resource
import com.example.facefit.domain.utils.validators.PrescriptionValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrescriptionLensViewModel @Inject constructor(
    private val createPrescriptionUseCase: CreatePrescriptionUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val tokenManager: TokenManager,
    private val getGlassesByIdUseCase: GetGlassesByIdUseCase,
) : ViewModel() {

    var prescriptionState by mutableStateOf(PrescriptionUiState())
        private set

    var fieldErrors by mutableStateOf<Map<PrescriptionField, String>>(emptyMap())
        private set

    private var _isSinglePD by mutableStateOf(true)
    val isSinglePD: Boolean get() = _isSinglePD

    fun setSinglePD(value: Boolean) {
        _isSinglePD = value
    }

    suspend fun getGlassesById(id: String): Resource<Glasses> {
        return getGlassesByIdUseCase(id)
    }

    suspend fun createPrescription(onComplete: (String?) -> Unit) {
        val token = tokenManager.getToken() ?: run {
            onComplete(null)
            return
        }

        val state = prescriptionState
        val request = CreatePrescriptionRequest(
            rightEye = EyePrescription(
                sphere = state.odSph.toDoubleOrNull() ?: 0.0,
                cylinder = state.odCyl.toDoubleOrNull() ?: 0.0,
                axis = state.odAxis.toIntOrNull() ?: 0
            ),
            leftEye = EyePrescription(
                sphere = state.osSph.toDoubleOrNull() ?: 0.0,
                cylinder = state.osCyl.toDoubleOrNull() ?: 0.0,
                axis = state.osAxis.toIntOrNull() ?: 0
            ),
            pd = if (isSinglePD) {
                PdMeasurement(singlePd = state.singlePD.toDoubleOrNull() ?: 0.0)
            } else {
                PdMeasurement(
                    dualPd = DualPd(
                        left = state.leftPD.toDoubleOrNull() ?: 0.0,
                        right = state.rightPD.toDoubleOrNull() ?: 0.0
                    )
                )
            },
            add = 0.0
        )

        when (val result = createPrescriptionUseCase(request)) {
            is Resource.Success -> {
                val prescriptionId = result.data?.data?._id
                prescriptionState = prescriptionState.copy(prescriptionId = prescriptionId)
                onComplete(prescriptionId)
            }

            else -> onComplete(null)
        }
    }

    fun addToCart(
        productId: String,
        color: String,
        lensType: String,
        lensSpecification: String,
        size: String = "standard",
        prescriptionId: String? = null,
        onComplete: (Resource<CartData>) -> Unit
    ) {
        val lensPrice = when (lensSpecification) {
            LensOptions.STANDARD -> 50.0
            LensOptions.BLUE_LIGHT -> 75.0
            LensOptions.DRIVING -> 100.0
            else -> 0.0
        }

        val request = AddToCartRequest(
            glassesId = productId,
            color = color,
            size = size,
            lenseType = lensType,
            lensSpecification = lensSpecification,
            lensPrice = lensPrice,
            prescriptionId = prescriptionId
        )

        viewModelScope.launch {
            val result = addToCartUseCase(request)
            onComplete(result)
        }
    }


    fun updateField(field: PrescriptionField, value: String) {
        prescriptionState = when (field) {
            PrescriptionField.OD_SPH -> prescriptionState.copy(odSph = value)
            PrescriptionField.OD_CYL -> prescriptionState.copy(odCyl = value)
            PrescriptionField.OD_AXIS -> prescriptionState.copy(odAxis = value)
            PrescriptionField.OS_SPH -> prescriptionState.copy(osSph = value)
            PrescriptionField.OS_CYL -> prescriptionState.copy(osCyl = value)
            PrescriptionField.OS_AXIS -> prescriptionState.copy(osAxis = value)
            PrescriptionField.SINGLE_PD -> prescriptionState.copy(singlePD = value)
            PrescriptionField.LEFT_PD -> prescriptionState.copy(leftPD = value)
            PrescriptionField.RIGHT_PD -> prescriptionState.copy(rightPD = value)
        }
    }

    fun validate(): Boolean {
        val errors = mutableMapOf<PrescriptionField, String?>()
        val isSingle = isSinglePD

        with(prescriptionState) {
            errors[PrescriptionField.OD_SPH] = PrescriptionValidator.validateSPH(odSph)
            errors[PrescriptionField.OD_CYL] = PrescriptionValidator.validateCYL(odCyl)
            errors[PrescriptionField.OD_AXIS] = PrescriptionValidator.validateAXIS(odAxis)
            errors[PrescriptionField.OS_SPH] = PrescriptionValidator.validateSPH(osSph)
            errors[PrescriptionField.OS_CYL] = PrescriptionValidator.validateCYL(osCyl)
            errors[PrescriptionField.OS_AXIS] = PrescriptionValidator.validateAXIS(osAxis)

            if (isSingle) {
                errors[PrescriptionField.SINGLE_PD] = PrescriptionValidator.validatePD(singlePD)
            } else {
                errors[PrescriptionField.LEFT_PD] = PrescriptionValidator.validateLeftPD(leftPD)
                errors[PrescriptionField.RIGHT_PD] = PrescriptionValidator.validateRightPD(rightPD)
            }
        }

        fieldErrors = errors.filterValues { it != null } as Map<PrescriptionField, String>
        return fieldErrors.isEmpty()
    }

    fun resetPrescriptionState() {
        prescriptionState = PrescriptionUiState()
        fieldErrors = emptyMap()
        _isSinglePD = true
    }
}

enum class PrescriptionField {
    OD_SPH, OD_CYL, OD_AXIS, OS_SPH, OS_CYL, OS_AXIS, SINGLE_PD, LEFT_PD, RIGHT_PD
}

data class PrescriptionUiState(
    val prescriptionId: String? = null,
    val odSph: String = "",
    val odCyl: String = "",
    val odAxis: String = "",
    val osSph: String = "",
    val osCyl: String = "",
    val osAxis: String = "",
    val isSinglePD: Boolean = true,
    val singlePD: String = "",
    val leftPD: String = "",
    val rightPD: String = ""
)

object LensOptions {
    const val SINGLE_VISION = "Single Vision"
    const val NON_PRESCRIPTION = "Non-Prescription"

    const val STANDARD = "Standard Eyeglass Lenses"
    const val BLUE_LIGHT = "Blue Light Blocking"
    const val DRIVING = "Driving Lenses"
}
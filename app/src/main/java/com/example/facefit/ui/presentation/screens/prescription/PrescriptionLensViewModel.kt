package com.example.facefit.ui.presentation.screens.prescription

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.facefit.domain.utils.validators.PrescriptionValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PrescriptionLensViewModel @Inject constructor() : ViewModel() {

    var prescriptionState by mutableStateOf(PrescriptionUiState())
        private set

    var fieldErrors by mutableStateOf<Map<PrescriptionField, String>>(emptyMap())
        private set
    var isSinglePD: MutableState<Boolean> = mutableStateOf(true)
        private set


    fun setSinglePD(value: Boolean) {
        isSinglePD.value = value
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
        val isSingle = isSinglePD.value

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


}

enum class PrescriptionField {
    OD_SPH, OD_CYL, OD_AXIS, OS_SPH, OS_CYL, OS_AXIS, SINGLE_PD, LEFT_PD, RIGHT_PD
}

data class PrescriptionUiState(
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

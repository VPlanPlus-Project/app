package plus.vplan.app.feature.settings.page.security.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.feature.grades.common.domain.model.GradeProtectLevel
import plus.vplan.app.feature.grades.common.domain.usecase.GetGradeProtectionLevelUseCase
import plus.vplan.app.feature.grades.common.domain.usecase.SetGradeProtectionLevelUseCase
import plus.vplan.app.feature.settings.page.security.domain.usecase.BiometricDeviceState
import plus.vplan.app.feature.settings.page.security.domain.usecase.GetBiometricDeviceStateUseCase

class SecuritySettingsViewModel(
    private val getGradeProtectionLevelUseCase: GetGradeProtectionLevelUseCase,
    private val getBiometricDeviceStateUseCase: GetBiometricDeviceStateUseCase,
    private val setGradeProtectionLevelUseCase: SetGradeProtectionLevelUseCase
) : ViewModel() {
    var state by mutableStateOf(SecuritySettingsState())
        private set

    init {
        viewModelScope.launch { getGradeProtectionLevelUseCase().collectLatest { state = state.copy(gradeProtectLevel = it) } }
        viewModelScope.launch { getBiometricDeviceStateUseCase().collectLatest { state = state.copy(biometricDeviceState = it) } }
    }

    fun onEvent(event: SecuritySettingsEvent) {
        viewModelScope.launch {
            when (event) {
                is SecuritySettingsEvent.ToggleGradeProtection -> {
                    if (state.biometricDeviceState == null) return@launch
                    if (state.gradeProtectLevel == GradeProtectLevel.None) {
                        if (state.biometricDeviceState == BiometricDeviceState.Ready) setGradeProtectionLevelUseCase(GradeProtectLevel.Biometric)
                        else setGradeProtectionLevelUseCase(GradeProtectLevel.None)
                    }
                    else setGradeProtectionLevelUseCase(GradeProtectLevel.None)
                }
            }
        }
    }
}

data class SecuritySettingsState(
    val gradeProtectLevel: GradeProtectLevel? = null,
    val biometricDeviceState: BiometricDeviceState? = null
)

sealed class SecuritySettingsEvent {
    data object ToggleGradeProtection: SecuritySettingsEvent()
}
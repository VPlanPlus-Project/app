package plus.vplan.app.feature.onboarding.domain.model

import plus.vplan.app.feature.onboarding.stage.school_credentials.ui.Sp24CredentialsState

data class OnboardingSp24State(
    val sp24Id: Int? = null,
    val schoolName: String? = null,
    val username: String? = null,
    val password: String? = null,
    val sp24CredentialsState: Sp24CredentialsState = Sp24CredentialsState.NOT_CHECKED,
    val profileOptions: List<OnboardingProfile> = emptyList(),
    val selectedItem: OnboardingProfile? = null,
)
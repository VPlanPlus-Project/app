package plus.vplan.app.feature.onboarding.domain.model

import plus.vplan.app.domain.model.ProfileType
import plus.vplan.app.feature.onboarding.domain.repository.Sp24CredentialsState

data class OnboardingSp24State(
    val sp24Id: Int? = null,
    val schoolName: String? = null,
    val username: String? = null,
    val password: String? = null,
    val sp24CredentialsState: Sp24CredentialsState = Sp24CredentialsState.NOT_CHECKED,
    val groupOptions: List<String> = emptyList(),
    val teacherOptions: List<String> = emptyList(),
    val selectedItem: String? = null,
    val selectedItemType: ProfileType? = null,
    val courseOptionsOptions: Map<String, Boolean> = emptyMap(),
    val subjectInstanceOptions: Map<String, Boolean> = emptyMap(),
)
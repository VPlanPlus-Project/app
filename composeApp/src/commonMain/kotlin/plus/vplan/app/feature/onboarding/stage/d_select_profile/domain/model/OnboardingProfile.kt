package plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model

import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.ProfileType

sealed interface OnboardingProfile {
    val name: String
    val type: ProfileType
    val alias: Alias

    data class StudentProfile(
        override val name: String,
        override val alias: Alias,
        val subjectInstances: List<SubjectInstance>
    ) : OnboardingProfile {
        override val type: ProfileType = ProfileType.STUDENT
    }

    data class TeacherProfile(
        override val name: String,
        override val alias: Alias,
    ) : OnboardingProfile {
        override val type: ProfileType = ProfileType.TEACHER
    }
}
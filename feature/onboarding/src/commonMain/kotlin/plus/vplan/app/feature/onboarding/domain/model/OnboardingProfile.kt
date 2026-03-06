package plus.vplan.app.feature.onboarding.domain.model

import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.ProfileType
import plus.vplan.app.core.model.SubjectInstance

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
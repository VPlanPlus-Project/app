package plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model

import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.ProfileType

sealed interface OnboardingProfile {
    val name: String
    val type: ProfileType

    data class StudentProfile(
        override val name: String,
        val subjectInstances: List<SubjectInstance>
    ) : OnboardingProfile {
        override val type: ProfileType = ProfileType.STUDENT
    }

    data class TeacherProfile(
        override val name: String
    ) : OnboardingProfile {
        override val type: ProfileType = ProfileType.TEACHER
    }

    data class RoomProfile(
        override val name: String
        ) : OnboardingProfile {
        override val type: ProfileType = ProfileType.ROOM
    }
}
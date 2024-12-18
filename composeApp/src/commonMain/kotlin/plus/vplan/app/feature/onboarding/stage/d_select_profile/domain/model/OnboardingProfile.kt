package plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model

import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.ProfileType

sealed interface OnboardingProfile {
    val id: Int
    val name: String
    val type: ProfileType

    data class StudentProfile(
        override val id: Int,
        override val name: String,
        val defaultLessons: List<DefaultLesson>
    ) : OnboardingProfile {
        override val type: ProfileType = ProfileType.STUDENT
    }

    data class TeacherProfile(
        override val id: Int,
        override val name: String
    ) : OnboardingProfile {
        override val type: ProfileType = ProfileType.TEACHER
    }

    data class RoomProfile(
        override val id: Int,
        override val name: String
        ) : OnboardingProfile {
        override val type: ProfileType = ProfileType.ROOM
    }
}
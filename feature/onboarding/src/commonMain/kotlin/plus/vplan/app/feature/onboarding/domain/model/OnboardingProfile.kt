package plus.vplan.app.feature.onboarding.domain.model

import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.ProfileType
import plus.vplan.app.core.model.SubjectInstance

sealed interface OnboardingProfile {
    val name: String
    val type: ProfileType
    val alias: Alias

    /**
     * Since SP24.kt returns _everything_ there may be some items that aren't actual options.
     * These will be hidden by default to remove clutter and only be shown if the user explicitly
     * requests so.
     */
    val isTrustedName: Boolean

    data class StudentProfile(
        override val name: String,
        override val alias: Alias,
        override val isTrustedName: Boolean,
        val subjectInstances: List<SubjectInstance>
    ) : OnboardingProfile {
        override val type: ProfileType = ProfileType.STUDENT
    }

    data class TeacherProfile(
        override val name: String,
        override val alias: Alias,
        override val isTrustedName: Boolean,
    ) : OnboardingProfile {
        override val type: ProfileType = ProfileType.TEACHER
    }
}
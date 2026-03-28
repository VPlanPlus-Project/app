package plus.vplan.app.core.data.populator

sealed class PopulationContext {
    abstract val school: plus.vplan.app.core.model.School.AppSchool

    data class School(override val school: plus.vplan.app.core.model.School.AppSchool): PopulationContext()
    data class Profile(val profile: plus.vplan.app.core.model.Profile): PopulationContext() {
        override val school: plus.vplan.app.core.model.School.AppSchool = profile.school
    }
}
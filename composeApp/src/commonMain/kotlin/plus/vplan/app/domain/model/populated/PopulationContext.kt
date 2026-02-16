package plus.vplan.app.domain.model.populated

sealed class PopulationContext {
    data class School(val school: plus.vplan.app.core.model.School): PopulationContext()
    data class Profile(val profile: plus.vplan.app.core.model.Profile): PopulationContext()
}
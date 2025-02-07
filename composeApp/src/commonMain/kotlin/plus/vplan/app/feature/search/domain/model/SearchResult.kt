package plus.vplan.app.feature.search.domain.model

sealed class SearchResult {
    sealed class SchoolEntity : SearchResult() {
        data class Group(val group: plus.vplan.app.domain.model.Group) : SchoolEntity()
        data class Teacher(val teacher: plus.vplan.app.domain.model.Teacher) : SchoolEntity()
        data class Room(val room: plus.vplan.app.domain.model.Room) : SchoolEntity()
    }
}
package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Teacher

data class EmbeddedTeacher(
    @Embedded val teacher: DbTeacher,
    @Relation(
        parentColumn = "school_id",
        entityColumn = "id",
        entity = DbSchool::class
    ) val school: EmbeddedSchool,
) {
    fun toModel(): Teacher {
        return Teacher(
            id = teacher.id,
            school = Cacheable.Loaded(school.toModel()),
            name = teacher.name
        )
    }
}
package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbSchoolAlias
import plus.vplan.app.data.source.database.model.database.DbSchoolSp24Acess
import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.domain.model.School

data class EmbeddedSchool(
    @Embedded val school: DbSchool,
    @Relation(
        parentColumn = "id",
        entityColumn = "school_id",
        entity = DbSchoolSp24Acess::class
    ) val sp24SchoolDetails: DbSchoolSp24Acess?,
    @Relation(
        parentColumn = "id",
        entityColumn = "school_id",
        entity = DbGroup::class
    ) val groups: List<DbGroup>,
    @Relation(
        parentColumn = "id",
        entityColumn = "school_id",
        entity = DbSchoolAlias::class
    ) val aliases: List<DbSchoolAlias>
) {
    fun toModel(): School {
        val aliases = aliases.map { it.toModel() }.toSet()
        return when (school.creationReason) {
            CreationReason.Cached -> School.CachedSchool(
                id = school.id,
                name = school.name,
                aliases = aliases,
                cachedAt = school.cachedAt
            )
            CreationReason.Persisted -> School.AppSchool(
                id = school.id,
                name = school.name,
                groupIds = groups.map { it.id },
                sp24Id = sp24SchoolDetails!!.sp24SchoolId,
                username = sp24SchoolDetails.username,
                password = sp24SchoolDetails.password,
                daysPerWeek = sp24SchoolDetails.daysPerWeek,
                credentialsValid = sp24SchoolDetails.credentialsValid,
                cachedAt = school.cachedAt,
                aliases = aliases
            )
        }
    }
}
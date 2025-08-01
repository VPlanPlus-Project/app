package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbSchoolAlias
import plus.vplan.app.data.source.database.model.database.DbSchoolIndiwareAccess
import plus.vplan.app.domain.model.School

data class EmbeddedSchool(
    @Embedded val school: DbSchool,
    @Relation(
        parentColumn = "id",
        entityColumn = "school_id",
        entity = DbSchoolIndiwareAccess::class
    ) val sp24SchoolDetails: DbSchoolIndiwareAccess?,
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
        if (sp24SchoolDetails != null) {
            return School.Sp24School(
                id = school.id,
                name = school.name,
                groups = groups.map { it.id },
                sp24Id = sp24SchoolDetails.sp24SchoolId,
                username = sp24SchoolDetails.username,
                password = sp24SchoolDetails.password,
                daysPerWeek = sp24SchoolDetails.daysPerWeek,
                credentialsValid = sp24SchoolDetails.credentialsValid,
                cachedAt = school.cachedAt,
                aliases = aliases.map { it.toModel() }.toSet()
            )
        }

        return School.DefaultSchool(
            id = school.id,
            name = school.name,
            groups = groups.map { it.id },
            cachedAt = school.cachedAt,
            aliases = aliases.map { it.toModel() }.toSet()
        )
    }
}
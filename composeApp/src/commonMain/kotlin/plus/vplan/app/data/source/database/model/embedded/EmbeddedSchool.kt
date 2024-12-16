package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbSp24SchoolDetails
import plus.vplan.app.domain.model.School

data class EmbeddedSchool(
    @Embedded val school: DbSchool,
    @Relation(
        parentColumn = "id",
        entityColumn = "school_id",
        entity = DbSp24SchoolDetails::class
    ) val sp24SchoolDetails: DbSp24SchoolDetails?
) {
    fun toModel(): School {
        if (sp24SchoolDetails != null) {
            return School.IndiwareSchool(
                id = school.id,
                name = school.name,
                sp24Id = sp24SchoolDetails.sp24SchoolId,
                username = sp24SchoolDetails.username,
                password = sp24SchoolDetails.password,
                daysPerWeek = sp24SchoolDetails.daysPerWeek,
                studentsHaveFullAccess = sp24SchoolDetails.studentsHaveFullAccess,
                schoolDownloadMode = sp24SchoolDetails.downloadMode,
            )
        }

        return School.DefaultSchool(
            id = school.id,
            name = school.name
        )
    }
}
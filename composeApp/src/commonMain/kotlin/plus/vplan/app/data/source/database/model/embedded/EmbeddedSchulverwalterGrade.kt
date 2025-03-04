package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterGrade
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterCollection
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterTeacher
import plus.vplan.app.domain.model.schulverwalter.Grade

data class EmbeddedSchulverwalterGrade(
    @Embedded val grade: DbSchulverwalterGrade,
    @Relation(
        parentColumn = "id",
        entityColumn = "grade_id",
        entity = FKSchulverwalterGradeSchulverwalterCollection::class
    ) val collections: FKSchulverwalterGradeSchulverwalterCollection,
    @Relation(
        parentColumn = "id",
        entityColumn = "grade_id",
        entity = FKSchulverwalterGradeSchulverwalterSubject::class
    ) val subject: FKSchulverwalterGradeSchulverwalterSubject,
    @Relation(
        parentColumn = "id",
        entityColumn = "grade_id",
        entity = FKSchulverwalterGradeSchulverwalterTeacher::class
    ) val teacher: FKSchulverwalterGradeSchulverwalterTeacher
) {
    fun toModel() = Grade(
        id = grade.id,
        value = grade.value,
        isOptional = grade.isOptional,
        isSelectedForFinalGrade = grade.isSelectedForFinalGrade,
        subjectId = subject.subjectId,
        teacherId = teacher.teacherId,
        collectionId = collections.collectionId,
        vppIdId = grade.vppId,
        givenAt = grade.givenAt,
        cachedAt = grade.cachedAt
    )
}
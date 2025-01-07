package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile

data class EmbeddedHomework(
    @Embedded val homework: DbHomework,
    @Relation(
        parentColumn = "created_by_profile_id",
        entityColumn = "id",
        entity = DbProfile::class
    ) val createdByProfile: EmbeddedProfile?,
    @Relation(
        parentColumn = "id",
        entityColumn = "homework_id",
        entity = DbHomeworkTask::class
    ) val tasks: List<EmbeddedHomeworkTask>
) {
    fun toModel(): Homework {
        if (homework.id < 0) {
            return Homework.LocalHomework(
                id = homework.id,
                dueTo = homework.dueTo,
                createdAt = homework.createdAt,
                createdByProfile = Cacheable.Loaded(createdByProfile!!.toModel() as Profile.StudentProfile),
                defaultLesson = homework.defaultLessonId?.let { Cacheable.Uninitialized(it) },
                tasks = tasks.map { Cacheable.Loaded(it.toModel()) }
            )
        }
        return Homework.CloudHomework(
            id = homework.id,
            dueTo = homework.dueTo,
            createdAt = homework.createdAt,
            createdBy = Cacheable.Uninitialized(homework.createdBy!!.toString()),
            defaultLesson = homework.defaultLessonId?.let { Cacheable.Uninitialized(it) },
            group = homework.groupId?.let { Cacheable.Uninitialized(it.toString()) },
            isPublic = homework.isPublic,
            tasks = tasks.map { Cacheable.Loaded(it.toModel()) }
        )
    }
}
package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile

data class EmbeddedHomework(
    @Embedded val homework: DbHomework,
    @Relation(
        parentColumn = "group_id",
        entityColumn = "id",
        entity = DbGroup::class
    ) val group: EmbeddedGroup?,
    @Relation(
        parentColumn = "created_by_profile_id",
        entityColumn = "id",
        entity = DbProfile::class
    ) val createdByProfile: EmbeddedProfile?,
    @Relation(
        parentColumn = "default_lesson_id",
        entityColumn = "id",
        entity = DbDefaultLesson::class
    ) val defaultLesson: EmbeddedDefaultLesson?,
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
                defaultLesson = defaultLesson?.toModel()?.let { Cacheable.Loaded(it) },
                tasks = tasks.map { Cacheable.Loaded(it.toModel()) }
            )
        }
        return Homework.CloudHomework(
            id = homework.id,
            dueTo = homework.dueTo,
            createdAt = homework.createdAt,
            createdBy = Cacheable.Uninitialized(homework.createdBy!!.toString()),
            defaultLesson = defaultLesson?.toModel()?.let { Cacheable.Loaded(it) },
            group = group?.toModel()?.let { Cacheable.Loaded(it) },
            isPublic = homework.isPublic,
            tasks = tasks.map { Cacheable.Loaded(it.toModel()) }
        )
    }
}
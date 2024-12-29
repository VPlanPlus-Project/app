package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.DbGroupProfileDisabledDefaultLessons
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbVppId
import plus.vplan.app.data.source.database.model.database.crossovers.DbDefaultLessonGroupCrossover

data class EmbeddedGroupProfile(
    @Embedded val profileLink: DbGroupProfile,
    @Relation(
        parentColumn = "profile_id",
        entityColumn = "id",
        entity = DbProfile::class
    ) val profile: DbProfile,
    @Relation(
        parentColumn = "group_id",
        entityColumn = "id",
        entity = DbGroup::class
    ) val group: EmbeddedGroup,
    @Relation(
        parentColumn = "group_id",
        entityColumn = "id",
        associateBy = Junction(
            value = DbDefaultLessonGroupCrossover::class,
            parentColumn = "group_id",
            entityColumn = "default_lesson_id"
        ),
        entity = DbDefaultLesson::class
    ) val defaultLessons: List<EmbeddedDefaultLesson>,
    @Relation(
        parentColumn = "profile_id",
        entityColumn = "id",
        associateBy = Junction(
            value = DbGroupProfileDisabledDefaultLessons::class,
            parentColumn = "profile_id",
            entityColumn = "default_lesson_id"
        ),
        entity = DbDefaultLesson::class
    ) val disabledDefaultLesson: List<EmbeddedDefaultLesson>,
    @Relation(
        parentColumn = "vpp_id",
        entityColumn = "id",
        entity = DbVppId::class
    ) val vppId: EmbeddedVppId?
)

package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
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
        entityColumn = "group_id",
        entity = DbDefaultLessonGroupCrossover::class
    ) val defaultLessons: List<DbDefaultLessonGroupCrossover>,
    @Relation(
        parentColumn = "profile_id",
        entityColumn = "profile_id",
        entity = DbGroupProfileDisabledDefaultLessons::class
    ) val disabledDefaultLesson: List<DbGroupProfileDisabledDefaultLessons>,
    @Relation(
        parentColumn = "vpp_id",
        entityColumn = "id",
        entity = DbVppId::class
    ) val vppId: EmbeddedVppId?
)

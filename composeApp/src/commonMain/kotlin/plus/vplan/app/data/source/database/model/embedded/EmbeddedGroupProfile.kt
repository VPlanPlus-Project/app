package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.foreign_key.FKGroupProfileDisabledSubjectInstances
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbVppId
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSubjectInstanceGroup

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
        entity = FKSubjectInstanceGroup::class
    ) val subjectInstances: List<FKSubjectInstanceGroup>,
    @Relation(
        parentColumn = "profile_id",
        entityColumn = "profile_id",
        entity = FKGroupProfileDisabledSubjectInstances::class
    ) val disabledSubjectInstances: List<FKGroupProfileDisabledSubjectInstances>,
    @Relation(
        parentColumn = "vpp_id",
        entityColumn = "id",
        entity = DbVppId::class
    ) val vppId: EmbeddedVppId?
)

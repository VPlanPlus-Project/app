package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbGroup
import plus.vplan.app.core.database.model.database.DbGroupProfile
import plus.vplan.app.core.database.model.database.foreign_key.FKGroupProfileDisabledSubjectInstances
import plus.vplan.app.core.database.model.database.DbProfile
import plus.vplan.app.core.database.model.database.DbSubjectInstance
import plus.vplan.app.core.database.model.database.DbVppId
import plus.vplan.app.core.database.model.database.foreign_key.FKSubjectInstanceGroup

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
        entity = DbSubjectInstance::class,
        associateBy = Junction(
            value = FKSubjectInstanceGroup::class,
            parentColumn = "group_id",
            entityColumn = "subject_instance_id"
        )
    ) val subjectInstances: List<EmbeddedSubjectInstance>,
    @Relation(
        parentColumn = "profile_id",
        entityColumn = "id",
        associateBy = Junction(
            value = FKGroupProfileDisabledSubjectInstances::class,
            parentColumn = "profile_id",
            entityColumn = "subject_instance_id"
        ),
        entity = DbSubjectInstance::class
    ) val disabledSubjectInstances: List<EmbeddedSubjectInstance>,
    @Relation(
        parentColumn = "vpp_id",
        entityColumn = "id",
        entity = DbVppId::class
    ) val vppId: EmbeddedVppId?
)

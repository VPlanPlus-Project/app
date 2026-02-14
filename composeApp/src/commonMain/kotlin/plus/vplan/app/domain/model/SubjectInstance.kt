@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.domain.model

import kotlinx.coroutines.flow.flowOf
import plus.vplan.app.App
import plus.vplan.app.core.model.DataTag
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.AliasedItem
import plus.vplan.app.core.model.Group
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class SubjectInstance(
    override val id: Uuid,
    val subject: String,
    val courseId: Uuid?,
    val teacher: Uuid?,
    val groupIds: List<Uuid>,
    val cachedAt: Instant,
    override val aliases: Set<Alias>
) : AliasedItem<DataTag> {
    override val tags: Set<DataTag> = emptySet()

    var courseItem: Course? = null
        private set

    var teacherItem: Teacher? = null
        private set

    var groupItems: List<Group>? = null
        private set

    val groups by lazy {
        if (groupIds.isEmpty()) flowOf(emptyList())
        else kotlinx.coroutines.flow.combine(groupIds.map { App.groupSource.getById(it) }) { it.toList() }
    }

    suspend fun getCourseItem(): Course? {
        if (courseId == null) return null
        return courseItem ?: App.courseSource.getById(courseId).getFirstValue().also { courseItem = it }
    }

    val course by lazy {
        if (courseId == null) null
        else App.courseSource.getById(courseId)
    }

    suspend fun getTeacherItem(): Teacher? {
        if (this.teacher == null) return null
        return teacherItem ?: App.teacherSource.getById(this.teacher).getFirstValue().also { teacherItem = it }
    }

    suspend fun getGroupItems(): List<Group> {
        return groupItems ?: this.groupIds.mapNotNull { App.groupSource.getById(it).getFirstValue() }.also { groupItems = it }
    }

    companion object {
        fun buildSp24Alias(sp24SchoolId: Int, sp24VpId: Int): Alias {
            return Alias(
                provider = AliasProvider.Sp24,
                value = "$sp24SchoolId/${sp24VpId}",
                version = 1
            )
        }
    }
}
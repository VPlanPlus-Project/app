package plus.vplan.app.domain.model

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue
import kotlin.uuid.Uuid

abstract class Profile : Item<DataTag> {
    abstract val id: Uuid
    abstract val profileType: ProfileType
    abstract val name: String

    override fun getEntityId(): String = this.id.toHexString()
    override val tags: Set<DataTag> = emptySet()
    abstract fun getSchool(): Flow<CacheState<School>>

    @Deprecated("Use getSchool() instead")
    var schoolItem: School? = null
        private set

    @Deprecated("Use getSchool() instead")
    suspend fun getSchoolItem(): School {
        return schoolItem ?: getSchool().getFirstValue()!!.also { schoolItem = it }
    }

    data class StudentProfile(
        override val id: Uuid,
        override val name: String,
        val groupId: Int,
        val subjectInstanceConfiguration: Map<Int, Boolean>,
        val vppIdId: Int?
    ) : Profile() {
        override val profileType = ProfileType.STUDENT

        var groupItem: Group? = null
            private set

        var vppIdItem: VppId.Active? = null
            private set

        val vppId by lazy { vppIdId?.let { App.vppIdSource.getById(it) } }
        val subjectInstances by lazy { combine(subjectInstanceConfiguration.keys.map { App.subjectInstanceSource.getById(it).filterIsInstance<CacheState.Done<SubjectInstance>>().map { cacheState -> cacheState.data } }) { it.toList() } }

        private val subjectInstanceCache = hashMapOf<Int, SubjectInstance>()
        val subjectInstanceItems: List<SubjectInstance>
            get() = this.subjectInstanceCache.values.toList()
        suspend fun getSubjectInstance(id: Int): SubjectInstance {
            return subjectInstanceCache.getOrPut(id) { App.subjectInstanceSource.getById(id).filterIsInstance<CacheState.Done<SubjectInstance>>().first().data }
        }

        suspend fun getGroupItem(): Group {
            return groupItem ?: App.groupSource.getById(groupId).getFirstValue()!!.also { groupItem = it }
        }

        suspend fun getVppIdItem(): VppId.Active? {
            if (this.vppIdId == null) return null
            return vppIdItem ?: App.vppIdSource.getById(vppIdId).getFirstValue().let { it as? VppId.Active }.also { this.vppIdItem = it }
        }

        suspend fun getSubjectInstances(): List<SubjectInstance> {
            return this.subjectInstanceConfiguration.keys.map { getSubjectInstance(it) }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun getSchool(): Flow<CacheState<School>> {
            return App.groupSource.getById(groupId).filterIsInstance<CacheState.Done<Group>>().flatMapLatest { App.schoolSource.getById(it.data.schoolId) }
        }

        val group by lazy { App.groupSource.getById(groupId) }

        override fun copyBase(id: Uuid, name: String, profileType: ProfileType): Profile {
            if (profileType != ProfileType.STUDENT) throw IllegalArgumentException("Cannot change type of profile")
            return this.copy(
                id = id,
                name = name
            )
        }
    }

    data class TeacherProfile(
        override val id: Uuid,
        override val name: String,
        val teacher: Int
    ) : Profile() {
        override val profileType = ProfileType.TEACHER

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun getSchool(): Flow<CacheState<School>> {
            return App.teacherSource.getById(teacher).filterIsInstance<CacheState.Done<Teacher>>().flatMapLatest { App.schoolSource.getById(it.data.schoolId) }
        }

        override fun copyBase(id: Uuid, name: String, profileType: ProfileType): Profile {
            if (profileType != ProfileType.TEACHER) throw IllegalStateException("Cannot change type of profile")
            return this.copy(
                id = id,
                name = name
            )
        }
    }

    data class RoomProfile(
        override val id: Uuid,
        override val name: String,
        val room: Int
    ) : Profile() {
        override val profileType = ProfileType.ROOM

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun getSchool(): Flow<CacheState<School>> {
            return App.roomSource.getById(room).filterIsInstance<CacheState.Done<Room>>().flatMapLatest { App.schoolSource.getById(it.data.schoolId) }
        }

        override fun copyBase(id: Uuid, name: String, profileType: ProfileType): Profile {
            if (profileType != ProfileType.ROOM) throw IllegalArgumentException("Cannot change type of profile")
            return this.copy(
                id = id,
                name = name
            )
        }
    }

    abstract fun copyBase(
        id: Uuid = this.id,
        name: String = this.name,
        profileType: ProfileType = this.profileType,
    ): Profile
}

enum class ProfileType {
    STUDENT, TEACHER, ROOM
}
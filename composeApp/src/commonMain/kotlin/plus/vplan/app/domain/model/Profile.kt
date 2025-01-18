package plus.vplan.app.domain.model

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue
import kotlin.uuid.Uuid

abstract class Profile : Item {
    abstract val id: Uuid
    abstract val profileType: ProfileType
    abstract val name: String

    override fun getEntityId(): String = this.id.toHexString()
    abstract fun getSchool(): Flow<CacheState<School>>

    var schoolItem: School? = null
        private set

    suspend fun getSchoolItem(): School {
        return schoolItem ?: getSchool().getFirstValue()!!.also { schoolItem = it }
    }

    data class StudentProfile(
        override val id: Uuid,
        override val name: String,
        val group: Int,
        val defaultLessons: Map<String, Boolean>,
        val vppId: Int?
    ) : Profile() {
        override val profileType = ProfileType.STUDENT

        var groupItem: Group? = null
            private set

        var vppIdItem: VppId.Active? = null
            private set

        private val defaultLessonCache = hashMapOf<String, DefaultLesson>()
        val defaultLessonItems: List<DefaultLesson>
            get() = this.defaultLessonCache.values.toList()
        suspend fun getDefaultLesson(id: String): DefaultLesson {
            return defaultLessonCache.getOrPut(id) { App.defaultLessonSource.getById(id).filterIsInstance<CacheState.Done<DefaultLesson>>().first().data }
        }

        suspend fun getGroupItem(): Group {
            return groupItem ?: App.groupSource.getById(group).getFirstValue()!!.also { groupItem = it }
        }

        suspend fun getVppIdItem(): VppId.Active? {
            if (this.vppId == null) return null
            return vppIdItem ?: App.vppIdSource.getById(vppId).getFirstValue().let { it as? VppId.Active }.also { this.vppIdItem = it }
        }

        suspend fun getDefaultLessons(): List<DefaultLesson> {
            return this.defaultLessons.keys.map { getDefaultLesson(it) }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun getSchool(): Flow<CacheState<School>> {
            return App.groupSource.getById(group).filterIsInstance<CacheState.Done<Group>>().flatMapLatest { App.schoolSource.getById(it.data.school) }
        }

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
            return App.teacherSource.getById(teacher).filterIsInstance<CacheState.Done<Teacher>>().flatMapLatest { App.schoolSource.getById(it.data.school) }
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
            return App.roomSource.getById(room).filterIsInstance<CacheState.Done<Room>>().flatMapLatest { App.schoolSource.getById(it.data.school) }
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
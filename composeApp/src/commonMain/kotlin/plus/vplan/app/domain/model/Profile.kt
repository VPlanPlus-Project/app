package plus.vplan.app.domain.model

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import plus.vplan.app.App
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.data.Item
import kotlin.uuid.Uuid

abstract class Profile : Item<Uuid, DataTag> {
    abstract val profileType: ProfileType
    abstract val name: String

    override val tags: Set<DataTag> = emptySet()
    abstract fun getSchool(): Flow<AliasState<School.AppSchool>>

    data class StudentProfile(
        override val id: Uuid,
        override val name: String,
        val groupId: Uuid,
        val subjectInstanceConfiguration: Map<Uuid, Boolean>,
        val vppIdId: Int?
    ) : Profile() {
        override val profileType = ProfileType.STUDENT

        var groupItem: Group? = null
            private set

        var vppIdItem: VppId.Active? = null
            private set

        val vppId by lazy { vppIdId?.let { App.vppIdSource.getById(it) } }
        val subjectInstances by lazy {
            if (subjectInstanceConfiguration.isEmpty()) flowOf(emptyList())
            else combine(subjectInstanceConfiguration.keys.map { App.subjectInstanceSource.getById(it).filterIsInstance<AliasState.Done<SubjectInstance>>().map { cacheState -> cacheState.data } }) { it.toList() }
        }

        private val subjectInstanceCache = hashMapOf<Uuid, SubjectInstance>()
        val subjectInstanceItems: List<SubjectInstance>
            get() = this.subjectInstanceCache.values.toList()
        suspend fun getSubjectInstance(id: Uuid): SubjectInstance {
            return subjectInstanceCache.getOrPut(id) { App.subjectInstanceSource.getById(id).filterIsInstance<AliasState.Done<SubjectInstance>>().first().data }
        }

        suspend fun getGroupItem(): Group {
            return groupItem ?: App.groupSource.getById(groupId).getFirstValue()!!.also { groupItem = it }
        }

        suspend fun getVppIdItem(): VppId.Active? {
            if (this.vppIdId == null) return null
            return vppIdItem ?: App.vppIdSource.getById(vppIdId).getFirstValueOld().let { it as? VppId.Active }.also { this.vppIdItem = it }
        }

        suspend fun getSubjectInstances(): List<SubjectInstance> {
            return this.subjectInstanceConfiguration.keys.map { getSubjectInstance(it) }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun getSchool(): Flow<AliasState<School.AppSchool>> {
            return App.groupSource.getById(groupId).filterIsInstance<AliasState.Done<Group>>().flatMapLatest { App.schoolSource.getAppSchoolById(it.data.schoolId) }
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
        val teacher: Uuid
    ) : Profile() {
        override val profileType = ProfileType.TEACHER

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun getSchool(): Flow<AliasState<School.AppSchool>> {
            return App.teacherSource.getById(teacher).filterIsInstance<AliasState.Done<Teacher>>().flatMapLatest { App.schoolSource.getAppSchoolById(it.data.schoolId) }
        }

        override fun copyBase(id: Uuid, name: String, profileType: ProfileType): Profile {
            if (profileType != ProfileType.TEACHER) throw IllegalStateException("Cannot change type of profile")
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
    STUDENT, TEACHER
}
package plus.vplan.app.domain.model

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import plus.vplan.app.App
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Item
import kotlin.uuid.Uuid

abstract class Profile : Item<Uuid, DataTag>, KoinComponent {
    abstract val profileType: ProfileType
    abstract val name: String

    override val tags: Set<DataTag> = emptySet()
    abstract val school: School.AppSchool

    data class StudentProfile(
        override val id: Uuid,
        override val name: String,
        val group: Group,
        val subjectInstanceConfiguration: Map<Uuid, Boolean>,
        val vppId: VppId.Active?
    ) : Profile() {
        override val profileType = ProfileType.STUDENT

        override val school: School.AppSchool = group.school

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

        suspend fun getSubjectInstances(): List<SubjectInstance> {
            return this.subjectInstanceConfiguration.keys.map { getSubjectInstance(it) }
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
        val teacher: Teacher
    ) : Profile() {
        override val profileType = ProfileType.TEACHER

        override val school: School.AppSchool = teacher.school

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
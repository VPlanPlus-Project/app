package plus.vplan.app.data.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.SubjectInstanceDbDto
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.service.SubjectInstanceService
import plus.vplan.app.utils.sendAll

class SubjectInstanceServiceImpl(
    private val schoolRepository: SchoolRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository
) : SubjectInstanceService {
    override fun getSubjectInstanceFromAlias(alias: Alias, forceUpdate: Boolean): Flow<AliasState<SubjectInstance>> {
        return channelFlow {
            val localId = subjectInstanceRepository.resolveAliasToLocalId(alias)
            if (localId == null || forceUpdate) {
                send(AliasState.Loading(alias.toString()))
                val schoolId = subjectInstanceRepository.downloadSchoolIdById(alias.toString())

                val schoolAuthentication: VppSchoolAuthentication

                when (schoolId) {
                    is Response.Error -> {
                        send(AliasState.Error(alias.toString(), schoolId))
                        return@channelFlow
                    }
                    is Response.Success -> {
                        val school = schoolRepository.getByLocalId(schoolRepository.resolveAliasToLocalId(
                            Alias(AliasProvider.Vpp, schoolId.data.toString(), 1)
                        )!!).first()!! as School.AppSchool

                        schoolAuthentication = school.buildSp24AppAuthentication()
                    }
                    else -> throw IllegalStateException("Unexpected response type: $schoolId")
                }

                val item = subjectInstanceRepository.downloadById(schoolAuthentication, alias.toString())
                when (item) {
                    is Response.Error -> {
                        send(AliasState.Error(alias.toString(), item))
                        return@channelFlow
                    }
                    is Response.Success -> {
                        val subjectInstance = item.data
                        val existingId = subjectInstanceRepository.resolveAliasesToLocalId(subjectInstance.aliases)
                        if (existingId == null) {
                            send(AliasState.NotExisting(alias.toString()))
                            return@channelFlow
                        }
                        val existing = subjectInstanceRepository.getByLocalId(existingId).first()!!.let { existing ->
                            existing.copy(
                                aliases = (existing.aliases + subjectInstance.aliases).distinctBy { it.toString() }.toSet(),
                            )
                        }
                        subjectInstanceRepository.upsert(
                            SubjectInstanceDbDto(
                                subject = existing.subject,
                                aliases = existing.aliases.toList(),
                                course = existing.courseId,
                                teacher = existing.teacher,
                                groups = existing.groups
                            )
                        )

                        return@channelFlow send(AliasState.Done(existing))
                    }
                    else -> throw IllegalStateException("Unexpected response type: $item")
                }
            } else sendAll(subjectInstanceRepository.getByLocalId(localId).map {
                if (it == null) AliasState.NotExisting(alias.toString())
                else AliasState.Done(it)
            })
        }
    }

    override suspend fun findAliasForSubjectInstance(subjectInstance: SubjectInstance, aliasProvider: AliasProvider): Alias? {
        val existing = subjectInstance.aliases.firstOrNull { it.provider == aliasProvider }
        if (existing != null) return existing
        return getSubjectInstanceFromAlias(subjectInstance.aliases.first(), true).getFirstValue()
            ?.aliases
            ?.firstOrNull { it.provider == aliasProvider }
    }
}
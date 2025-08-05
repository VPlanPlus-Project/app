package plus.vplan.app.data.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.GroupDbDto
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.service.GroupService
import plus.vplan.app.utils.sendAll
import kotlin.uuid.Uuid

class GroupServiceImpl(
    private val groupRepository: GroupRepository,
    private val schoolRepository: SchoolRepository
): GroupService {
    override fun getGroupFromAlias(alias: Alias): Flow<AliasState<Group>> {
        return channelFlow {
            var localId = groupRepository.resolveAliasToLocalId(alias)
            if (localId == null) {
                send(AliasState.Loading(alias.toString()))
                val schoolId = groupRepository.downloadSchoolIdById(alias.toString())

                val appSchoolId: Uuid
                val schoolAuthentication: VppSchoolAuthentication

                when(schoolId) {
                    is Response.Error -> {
                        send(AliasState.Error(alias.toString(), schoolId))
                        return@channelFlow
                    }
                    is Response.Success -> {
                        val school = schoolRepository.getByLocalId(schoolRepository.resolveAliasToLocalId(
                            Alias(AliasProvider.Vpp, schoolId.data.toString(), 1)
                        )!!).first()!! as School.AppSchool
                        appSchoolId = school.id
                        schoolAuthentication = school.buildSp24AppAuthentication()
                    }
                    else -> throw IllegalStateException("Unexpected response type: $schoolId")
                }

                val item = groupRepository.downloadById(schoolAuthentication, alias.toString())
                when (item) {
                    is Response.Error -> {
                        send(AliasState.Error(alias.toString(), item))
                        return@channelFlow
                    }
                    is Response.Success -> {
                        localId = groupRepository.upsert(
                            GroupDbDto(
                                name = item.data.name,
                                schoolId = appSchoolId,
                                aliases = item.data.aliases,
                                creationReason = CreationReason.Cached
                            )
                        )
                    }
                    else -> throw IllegalStateException("Unexpected response type: $item")
                }
            }
            sendAll(groupRepository.getByLocalId(localId).map {
                if (it == null) AliasState.NotExisting(alias.toString())
                else AliasState.Done(it)
            })
        }
    }
}
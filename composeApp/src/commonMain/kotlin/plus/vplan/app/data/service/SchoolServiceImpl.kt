package plus.vplan.app.data.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.School
import plus.vplan.app.domain.repository.SchoolDbDto
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.service.SchoolService
import plus.vplan.app.utils.sendAll

class SchoolServiceImpl(
    private val schoolRepository: SchoolRepository
) : SchoolService {
    override fun getSchoolFromAlias(alias: Alias): Flow<AliasState<School>> {
        return channelFlow {
            var localId = schoolRepository.resolveAliasToLocalId(alias)
            if (localId == null) {
                send(AliasState.Loading(alias.toString()))
                val item = schoolRepository.downloadById(alias.toString())
                when (item) {
                    is Response.Error -> {
                        send(AliasState.Error(alias.toString(), item))
                        return@channelFlow
                    }
                    is Response.Success -> {
                        localId = schoolRepository.upsert(
                            SchoolDbDto(
                                name = item.data.name,
                                aliases = item.data.aliases,
                                creationReason = CreationReason.Cached
                            )
                        )
                    }
                    else -> throw IllegalStateException("Unexpected response type: $item")
                }
            }
            sendAll(schoolRepository.getByLocalId(localId).map {
                if (it == null) AliasState.NotExisting(alias.toString())
                else AliasState.Done(it)
            })
        }
    }
}
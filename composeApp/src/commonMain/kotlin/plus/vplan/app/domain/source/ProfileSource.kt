package plus.vplan.app.domain.source

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.ProfileRepository
import kotlin.uuid.Uuid

class ProfileSource(
    private val profileRepository: ProfileRepository
) : CacheableItemSource<Profile>() {

    override fun getAll(configuration: FetchConfiguration<Profile>): Flow<List<Cacheable<Profile>>> = channelFlow {
        profileRepository.getAll().collectLatest { flowEmission ->
            combine(
                flowEmission.map { getById(it.getItemId(), configuration) }
            ) { it }.collect { send(it.toList()) }
        }
    }

    override fun getById(
        id: String,
        configuration: FetchConfiguration<Profile>
    ): Flow<Cacheable<Profile>> {
        Logger.d { "ProfileC: $configuration" }
        return configuredCache.getOrPut("${id}_$configuration") { channelFlow {
            cache.getOrPut(id) { profileRepository.getById(Uuid.parseHex(id)).distinctUntilChanged() } .collectLatest { cacheableProfile ->
                if (cacheableProfile == null) return@collectLatest send(Cacheable.NotExisting(id))
                val profile = MutableStateFlow(cacheableProfile)
                launch { profile.collectLatest { send(Cacheable.Loaded(it)) } }

                if (configuration is FetchConfiguration.Ignore) return@collectLatest
                if (configuration !is Profile.Fetch) throw IllegalArgumentException("Expected Profile.Fetch Configuration")

                if (profile.value is Profile.StudentProfile && configuration.studentProfile is Profile.StudentProfile.Fetch) {
                    if (configuration.studentProfile.group is Group.Fetch) { launch {
                        App.groupSource.getById((profile.value as Profile.StudentProfile).group.getItemId(), configuration.studentProfile.group).collect { cacheableGroup ->
                            profile.value = (profile.value as Profile.StudentProfile).copy(group = cacheableGroup)
                        } }
                    }
                    if (configuration.studentProfile.defaultLessons is DefaultLesson.Fetch) {
                        val defaultLessonIds = (profile.value as Profile.StudentProfile).defaultLessons.keys.map { it.getItemId() }.toSet()
                        launch {
                            combine(
                                defaultLessonIds.map { App.defaultLessonSource.getById(it, configuration.studentProfile.defaultLessons) }
                            ) {
                                (profile.value as Profile.StudentProfile).defaultLessons.mapKeys { key ->
                                    it.first { it.getItemId() == key.key.getItemId() }
                                }.toList().sortedBy {
                                    buildString {
                                        if (it.first !is Cacheable.Loaded) return@buildString run { append("_${it.first.getItemId()}") }
                                        it.first.toValueOrNull()?.let {
                                            append(it.subject)
                                            append("_")
                                        }
                                    }
                                }.toMap()
                            }.collect {
                                profile.value = (profile.value as Profile.StudentProfile).copy(defaultLessons = it)
                            }
                        }
                    }
                }
            }
        } }
    }
}
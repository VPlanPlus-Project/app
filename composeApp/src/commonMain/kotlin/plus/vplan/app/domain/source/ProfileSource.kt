package plus.vplan.app.domain.source

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.cache.IdentifiedJob
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
        return configuredCache.getOrPut("${id}_$configuration") { channelFlow {
            val profile = MutableStateFlow<Profile?>(null)
            launch { profile.filterNotNull().onEach { Logger.d { "Profile update $configuration" } }.collectLatest { send(Cacheable.Loaded(it)) } }

            val groupUpdater = IdentifiedJob<Int>()
            val defaultLessonUpdater = IdentifiedJob<String>()

            cache.getOrPut(id) { profileRepository.getById(Uuid.parseHex(id)) }.distinctUntilChanged().collectLatest { cacheableProfile ->
                if (cacheableProfile == null) return@collectLatest send(Cacheable.NotExisting(id))
                if (profile.value == null) profile.value = cacheableProfile
                else if (profile.value!!.customName != cacheableProfile.customName) profile.value = profile.value!!.copyBase(customName = cacheableProfile.customName)

                if (configuration is FetchConfiguration.Ignore) return@collectLatest
                if (configuration !is Profile.Fetch) throw IllegalArgumentException("Expected Profile.Fetch Configuration")

                if (profile.value is Profile.StudentProfile && configuration.studentProfile is Profile.StudentProfile.Fetch) {
                    if (configuration.studentProfile.group is Group.Fetch) {
                        groupUpdater.setOnNewKey((cacheableProfile as Profile.StudentProfile).group.getItemId().toInt()) { key -> launch {
                            App.groupSource.getById(key.toString(), configuration.studentProfile.group).collect { cacheableGroup ->
                                profile.value = (profile.value as Profile.StudentProfile).copy(group = cacheableGroup)
                            }
                        } }
                    }
                    if (configuration.studentProfile.defaultLessons is DefaultLesson.Fetch) {
                        val newDefaultLessons = (profile.value as Profile.StudentProfile).defaultLessons.map { it.key.getItemId() to it.value }.toMap()
                        val cachedDefaultLessons = (cacheableProfile as Profile.StudentProfile).defaultLessons.map { it.key.getItemId() to it.value }.toMap()
                        if (newDefaultLessons.keys.toSet() != cachedDefaultLessons.keys.toSet() || (profile.value as Profile.StudentProfile).defaultLessons.keys.any { it is Cacheable.Uninitialized }) defaultLessonUpdater.set(Uuid.random().toString(), launch {
                            combine(
                                newDefaultLessons.keys.map { App.defaultLessonSource.getById(it, configuration.studentProfile.defaultLessons) }
                            ) {
                                (profile.value as Profile.StudentProfile).defaultLessons.mapKeys { key ->
                                    it.first { it.getItemId() == key.key.getItemId() }
                                }.sort()
                            }.collectLatest {
                                profile.value = (profile.value as Profile.StudentProfile).copy(defaultLessons = it)
                            }
                        })
                        else if (newDefaultLessons.values.toList() != cachedDefaultLessons.values.toList()) {
                            profile.value = (profile.value as Profile.StudentProfile).copy(
                                defaultLessons = cachedDefaultLessons.mapKeys { ndl -> (profile.value as Profile.StudentProfile).defaultLessons.filterKeys { it.getItemId() == ndl.key }.keys.first() }.sort()
                            )
                        }
                    }
                }
            }
        } }
    }
}

private fun Map<Cacheable<DefaultLesson>, Boolean>.sort() = this.toList().sortedBy {
        buildString {
            if (it.first !is Cacheable.Loaded) return@buildString run { append("_${it.first.getItemId()}") }
            it.first.toValueOrNull()?.let {
                append(it.subject)
                append("_")
            }
        }
    }.toMap()

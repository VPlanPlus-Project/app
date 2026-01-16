# Implementation Guide: Migrating to Enhanced Data Access

This guide provides step-by-step instructions for migrating existing data sources to the new enhanced architecture.

## Quick Start: Migration Checklist

### For Each Data Source

- [ ] Create new `Enhanced{Name}Source` class
- [ ] Implement three abstract methods (fetchFromLocal, fetchFromRemote, saveToLocal)
- [ ] Configure cache settings (optional)
- [ ] Add linked entity tracking (optional)
- [ ] Register in dependency injection
- [ ] Update ViewModels/UseCases to use new source
- [ ] Test thoroughly
- [ ] Remove old source (optional, can keep both)

## Step-by-Step Migration Example

### Example: Migrating ProfileSource

#### Step 1: Create the Enhanced Source

Create file: `domain/source/EnhancedProfileSource.kt`

```kotlin
package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.first
import org.koin.core.component.inject
import plus.vplan.app.domain.cache.CacheConfig
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.source.base.EnhancedDataSource
import kotlin.uuid.Uuid

class EnhancedProfileSource : EnhancedDataSource<Uuid, Profile>() {
    private val profileRepository: ProfileRepository by inject()
    
    // Step 1: Configure cache behavior
    override fun getCacheConfig(): CacheConfig = CacheConfig(
        ttlMillis = 6 * 60 * 60 * 1000, // 6 hours (profiles don't change often)
        maxEntries = 100, // Most users have < 10 profiles
        evictionPolicy = EvictionPolicy.LRU
    )
    
    // Step 2: Implement local fetch (from database)
    override suspend fun fetchFromLocal(id: Uuid): Profile? {
        return profileRepository.getById(id).first()
    }
    
    // Step 3: Implement remote fetch (from network/sync)
    override suspend fun fetchFromRemote(id: Uuid): Profile {
        // Profiles are synced, not fetched individually
        // Return from database or throw error if not found
        return fetchFromLocal(id) 
            ?: throw IllegalStateException("Profile $id not found. Sync required.")
    }
    
    // Step 4: Implement save to local
    override suspend fun saveToLocal(id: Uuid, data: Profile) {
        // Profiles are typically saved via ProfileRepository during sync
        // No additional action needed here
    }
    
    // Step 5: Track linked entities (optional)
    override suspend fun getLinkedEntityIds(data: Profile): Set<String> {
        return when (data) {
            is Profile.StudentProfile -> buildSet {
                add(data.group.id.toString())
                add(data.group.school.id.toString())
                data.vppId?.id?.let { add(it.toString()) }
            }
            is Profile.TeacherProfile -> buildSet {
                add(data.teacher.id.toString())
                add(data.teacher.school.id.toString())
            }
        }
    }
}
```

#### Step 2: Register in Dependency Injection

Edit: `domain/di/domainModule.kt`

```kotlin
val domainModule = module {
    // ... existing code ...
    
    // Add new source
    singleOf(::EnhancedProfileSource)
}
```

#### Step 3: Update ViewModels

Before:
```kotlin
class ProfileViewModel(
    private val profileSource: ProfileSource
) : ViewModel() {
    
    fun loadProfile(id: Uuid) {
        profileSource.getById(id)
            .filterIsInstance<CacheState.Done<Profile>>()
            .map { it.data }
            .collectAsState(null)
    }
}
```

After:
```kotlin
class ProfileViewModel(
    private val profileSource: EnhancedProfileSource
) : ViewModel() {
    
    fun loadProfile(id: Uuid): StateFlow<DataSourceState<Profile>> {
        return profileSource.get(
            id = id,
            refreshPolicy = RefreshPolicy.CACHE_FIRST
        )
    }
    
    fun forceRefresh(id: Uuid): StateFlow<DataSourceState<Profile>> {
        return profileSource.get(
            id = id,
            forceRefresh = true
        )
    }
}
```

#### Step 4: Update UI Layer

Before:
```kotlin
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, profileId: Uuid) {
    val profile = viewModel.loadProfile(profileId)
    
    when (profile.value) {
        null -> LoadingIndicator()
        else -> ProfileContent(profile.value!!)
    }
}
```

After:
```kotlin
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, profileId: Uuid) {
    val profileState = viewModel.loadProfile(profileId).collectAsState()
    
    when (val state = profileState.value) {
        is DataSourceState.Loading -> {
            LoadingIndicator()
            if (state.linkedEntitiesLoading.isNotEmpty()) {
                Text("Loading ${state.linkedEntitiesLoading.size} related items...")
            }
        }
        
        is DataSourceState.Success -> {
            ProfileContent(state.data)
            
            // Show refresh indicator if updating in background
            if (state.isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
            
            // Show which linked entities are still loading
            if (state.linkedEntitiesLoading.isNotEmpty()) {
                Text(
                    text = "Loading additional information...",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        is DataSourceState.Error -> {
            ErrorView(
                error = state.error,
                onRetry = { viewModel.forceRefresh(profileId) }
            )
            
            // Show cached data if available
            state.cachedData?.let { cachedProfile ->
                Column {
                    Text("Showing cached data", style = MaterialTheme.typography.caption)
                    ProfileContent(cachedProfile, showStaleIndicator = true)
                }
            }
        }
        
        is DataSourceState.NotFound -> {
            NotFoundView(
                message = "Profile not found",
                onAction = { /* Navigate to profile selection */ }
            )
        }
    }
}
```

#### Step 5: Add Pull-to-Refresh

```kotlin
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, profileId: Uuid) {
    val profileState = viewModel.loadProfile(profileId).collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = profileState.value is DataSourceState.Loading ||
                    (profileState.value as? DataSourceState.Success)?.isRefreshing == true,
        onRefresh = { viewModel.forceRefresh(profileId) }
    )
    
    Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
        // ... existing UI code ...
        
        PullRefreshIndicator(
            refreshing = pullRefreshState.refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
```

## Common Patterns

### Pattern 1: Data That's Always Synced (No Individual Network Fetch)

```kotlin
class EnhancedSchoolSource : EnhancedDataSource<Uuid, School>() {
    private val schoolRepository: SchoolRepository by inject()
    
    override suspend fun fetchFromLocal(id: Uuid): School? {
        return schoolRepository.getByLocalId(id).first()
    }
    
    override suspend fun fetchFromRemote(id: Uuid): School {
        // Schools are synced as part of onboarding/sync
        return fetchFromLocal(id) 
            ?: throw IllegalStateException("School not synced")
    }
    
    override suspend fun saveToLocal(id: Uuid, data: School) {
        // Handled by sync process
    }
}
```

### Pattern 2: Data With Direct API Endpoint

```kotlin
class EnhancedHomeworkSource : EnhancedDataSource<Int, Homework>() {
    private val homeworkRepository: HomeworkRepository by inject()
    private val homeworkApi: HomeworkApi by inject()
    
    override suspend fun fetchFromLocal(id: Int): Homework? {
        return homeworkRepository.getById(id).first()
    }
    
    override suspend fun fetchFromRemote(id: Int): Homework {
        val response = homeworkApi.getHomework(id)
        return response.toModel()
    }
    
    override suspend fun saveToLocal(id: Int, data: Homework) {
        homeworkRepository.upsert(data)
    }
}
```

### Pattern 3: Data With Complex Dependencies

```kotlin
class EnhancedDaySource : EnhancedDataSource<String, Day>() {
    private val dayRepository: DayRepository by inject()
    private val weekRepository: WeekRepository by inject()
    private val timetableRepository: TimetableRepository by inject()
    private val substitutionPlanRepository: SubstitutionPlanRepository by inject()
    
    override suspend fun fetchFromLocal(id: String): Day? {
        val schoolId = Uuid.parse(id.substringBefore("/"))
        val date = LocalDate.parse(id.substringAfter("/"))
        // Compose day from multiple sources
        return composeDay(schoolId, date)
    }
    
    override suspend fun fetchFromRemote(id: String): Day {
        // Trigger sync for this specific day
        val schoolId = Uuid.parse(id.substringBefore("/"))
        val date = LocalDate.parse(id.substringAfter("/"))
        syncDay(schoolId, date)
        return fetchFromLocal(id)!!
    }
    
    override suspend fun saveToLocal(id: String, data: Day) {
        // Day is a composed entity, save components separately
        dayRepository.upsert(data)
    }
    
    override suspend fun getLinkedEntityIds(data: Day): Set<String> {
        return buildSet {
            add(data.schoolId.toString())
            data.weekId?.let { add(it.toString()) }
            addAll(data.timetable.map { it.toString() })
            addAll(data.substitutionPlan.map { it.toString() })
            addAll(data.assessmentIds.map { it.toString() })
            addAll(data.homeworkIds.map { it.toString() })
        }
    }
}
```

## Optimizing Sync Use Cases

### Before: Sequential Fetching

```kotlin
class SyncGradesUseCase(
    private val besteSchuleYearsRepository: BesteSchuleYearsRepository,
    private val besteSchuleIntervalsRepository: BesteSchuleIntervalsRepository,
    private val besteSchuleTeachersRepository: BesteSchuleTeachersRepository,
    // ... other repositories
) {
    suspend fun invoke(forceUpdate: Boolean) {
        val years = besteSchuleYearsRepository.get().first()
        val intervals = besteSchuleIntervalsRepository.get().first()
        val teachers = besteSchuleTeachersRepository.get().first()
        val collections = besteSchuleCollectionsRepository.get().first()
        val subjects = besteSchuleSubjectsRepository.get().first()
        // ... process data
    }
}
```

### After: Parallel Fetching

```kotlin
class SyncGradesUseCase(
    private val besteSchuleYearsRepository: BesteSchuleYearsRepository,
    private val besteSchuleIntervalsRepository: BesteSchuleIntervalsRepository,
    private val besteSchuleTeachersRepository: BesteSchuleTeachersRepository,
    // ... other repositories
) {
    suspend fun invoke(forceUpdate: Boolean) = coroutineScope {
        // Phase 1: Fetch independent resources in parallel
        val (years, intervals, teachers, collections) = awaitAll(
            async { besteSchuleYearsRepository.get().first() },
            async { besteSchuleIntervalsRepository.get().first() },
            async { besteSchuleTeachersRepository.get().first() },
            async { besteSchuleCollectionsRepository.get().first() }
        )
        
        // Phase 2: Fetch dependent resources
        val subjects = besteSchuleSubjectsRepository.get().first()
        
        // ... process data
    }
}
```

**Performance Gain**: 4x faster (800ms → 200ms for 4 independent resources)

## Testing Your Migration

### Unit Test Example

```kotlin
class EnhancedProfileSourceTest {
    private lateinit var source: EnhancedProfileSource
    private lateinit var repository: ProfileRepository
    
    @Before
    fun setup() {
        repository = mockk()
        // Setup Koin for testing
        startKoin {
            modules(module {
                single { repository }
                single { ConcurrentHashMapFactory() }
                single { RefreshCoordinator(get()) }
            })
        }
        source = EnhancedProfileSource()
    }
    
    @Test
    fun `get with CACHE_FIRST returns cached data immediately`() = runTest {
        val profile = mockk<Profile.StudentProfile>()
        coEvery { repository.getById(any()) } returns flowOf(profile)
        
        // First call fetches from database
        val state1 = source.get(profileId, RefreshPolicy.CACHE_FIRST).first()
        assertTrue(state1 is DataSourceState.Success)
        assertEquals(profile, (state1 as DataSourceState.Success).data)
        
        // Second call uses cache (no database query)
        val state2 = source.get(profileId, RefreshPolicy.CACHE_FIRST).first()
        assertTrue(state2 is DataSourceState.Success)
        
        // Verify only called once
        coVerify(exactly = 1) { repository.getById(profileId) }
    }
    
    @Test
    fun `forceRefresh bypasses cache`() = runTest {
        val profile1 = mockk<Profile.StudentProfile>()
        val profile2 = mockk<Profile.StudentProfile>()
        coEvery { repository.getById(any()) } returns flowOf(profile1) andThen flowOf(profile2)
        
        source.get(profileId).first() // Cache profile1
        val state = source.get(profileId, forceRefresh = true).first()
        
        // Should get profile2, not cached profile1
        assertEquals(profile2, (state as DataSourceState.Success).data)
    }
}
```

### Integration Test Example

```kotlin
@Test
fun `parallel fetch in sync is faster than sequential`() = runTest {
    // Measure sequential
    val sequentialTime = measureTimeMillis {
        val years = repository1.get().first()
        val intervals = repository2.get().first()
        val teachers = repository3.get().first()
    }
    
    // Measure parallel
    val parallelTime = measureTimeMillis {
        val (years, intervals, teachers) = coroutineScope {
            awaitAll(
                async { repository1.get().first() },
                async { repository2.get().first() },
                async { repository3.get().first() }
            )
        }
    }
    
    // Parallel should be at least 2x faster
    assertTrue(parallelTime < sequentialTime / 2)
}
```

## Troubleshooting

### Issue: Data not refreshing

**Symptoms**: UI shows stale data even after force refresh

**Solutions**:
1. Check if `saveToLocal` is actually saving to database
2. Verify `fetchFromLocal` is reading from correct source
3. Check cache TTL isn't too long
4. Try using `RefreshPolicy.NETWORK_ONLY`

```kotlin
// Debug logging
override suspend fun saveToLocal(id: Uuid, data: T) {
    Logger.d { "Saving to local: $id" }
    repository.upsert(data)
    Logger.d { "Saved successfully" }
}
```

### Issue: Too many network requests

**Symptoms**: Network logs show duplicate requests

**Solutions**:
1. Verify `RefreshCoordinator` is injected
2. Check if you're creating multiple source instances
3. Use `RefreshPolicy.CACHE_FIRST` instead of `NETWORK_FIRST`

```kotlin
// Verify single instance
val domainModule = module {
    single { EnhancedProfileSource() } // Not `factory`
}
```

### Issue: Memory usage too high

**Symptoms**: App crashes or slows down over time

**Solutions**:
1. Reduce `maxEntries` in `CacheConfig`
2. Lower `ttlMillis` to expire data sooner
3. Call `invalidateAll()` when logging out

```kotlin
override fun getCacheConfig() = CacheConfig(
    maxEntries = 50, // Reduce from default 1000
    ttlMillis = 1 * 60 * 60 * 1000 // 1 hour instead of 24
)
```

### Issue: Linked entities never finish loading

**Symptoms**: `linkedEntitiesLoading` never becomes empty

**Solutions**:
1. Verify linked entity IDs are correct
2. Check if linked sources are properly updating
3. Implement `isLinkedEntityLoading` to track actual state

```kotlin
override suspend fun isLinkedEntityLoading(entityId: String): Boolean {
    // Check if the entity source is loading
    val schoolId = Uuid.parseOrNull(entityId) ?: return false
    val schoolState = App.schoolSource.getById(schoolId).value
    return schoolState is AliasState.Loading
}
```

## Best Practices

### 1. Choose Appropriate Cache TTL

| Entity Type | Recommended TTL | Reason |
|-------------|-----------------|--------|
| News, Substitution Plans | 1 hour | Changes frequently |
| Timetables, Homework | 6 hours | Changes daily |
| Profiles, Schools | 12 hours | Changes rarely |
| Teachers, Rooms | 24 hours | Almost static |
| Holidays, Lesson Times | 7 days | Very static |

### 2. Use Correct Refresh Policy

| Scenario | Policy | Why |
|----------|--------|-----|
| List screens | CACHE_FIRST | Fast initial load |
| Detail screens | CACHE_THEN_NETWORK | Fresh data + fast display |
| Pull-to-refresh | forceRefresh=true | User explicitly wants fresh |
| Form submission | NETWORK_ONLY | Must be fresh |
| Offline mode | CACHE_ONLY | No network available |

### 3. Handle All States

```kotlin
// ✅ GOOD - Handles all states
when (val state = source.get(id).value) {
    is DataSourceState.Loading -> LoadingUI()
    is DataSourceState.Success -> ContentUI(state.data)
    is DataSourceState.Error -> ErrorUI(state.error)
    is DataSourceState.NotFound -> NotFoundUI()
}

// ❌ BAD - Missing states
when (val state = source.get(id).value) {
    is DataSourceState.Success -> ContentUI(state.data)
    else -> LoadingUI() // Hides errors and not-found!
}
```

### 4. Provide Meaningful Error Messages

```kotlin
override suspend fun fetchFromRemote(id: Uuid): Profile {
    return fetchFromLocal(id) ?: throw IllegalStateException(
        "Profile $id not found. " +
        "Please complete onboarding or sync your account."
    )
}
```

### 5. Log Performance Metrics

```kotlin
class EnhancedProfileSource : EnhancedDataSource<Uuid, Profile>() {
    override suspend fun fetchFromRemote(id: Uuid): Profile {
        val start = System.currentTimeMillis()
        try {
            return profileApi.getProfile(id)
        } finally {
            val duration = System.currentTimeMillis() - start
            Logger.d { "Profile fetch took ${duration}ms" }
            // Send to analytics
            Analytics.trackPerformance("profile_fetch", duration)
        }
    }
}
```

## Next Steps

After migrating your data sources:

1. **Monitor Performance**
   - Add timing logs to measure improvement
   - Track cache hit rates
   - Monitor network request counts

2. **Optimize Based on Data**
   - Adjust cache TTL based on actual usage patterns
   - Fine-tune maxEntries per source
   - Identify and parallelize sequential operations

3. **Enhance UX**
   - Show specific linked entity loading states
   - Add pull-to-refresh to key screens
   - Implement optimistic updates

4. **Scale Gradually**
   - Start with high-traffic sources
   - Measure impact before migrating more
   - Keep old and new sources coexisting initially

## Questions?

Refer to:
- `DATA_ACCESS_IMPROVEMENTS.md` - Full architecture documentation
- `ARCHITECTURE_IMPROVEMENTS_SUMMARY.md` - Performance analysis
- `EnhancedTeacherSource.kt` - Reference implementation
- `OptimizedParallelFetchExample.kt` - Sync optimization patterns

package plus.vplan.app.feature.sync.domain.usecase.optimized

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.component.KoinComponent

/**
 * EXAMPLE: Optimized sync use case showing how to parallelize data fetching.
 * 
 * This demonstrates the performance improvement pattern that should be applied
 * to existing sync use cases like SyncGradesUseCase, FullSyncUseCase, etc.
 * 
 * BEFORE (Sequential - from SyncGradesUseCase.kt):
 * ```kotlin
 * val years = besteSchuleYearsRepository.get().first()            // ~200ms
 * val intervals = besteSchuleIntervalsRepository.get().first()    // ~200ms
 * val teachers = besteSchuleTeachersRepository.get().first()      // ~200ms
 * val collections = besteSchuleCollectionsRepository.get().first()// ~200ms
 * val subjects = besteSchuleSubjectsRepository.get().first()      // ~200ms
 * val grades = besteSchuleGradesRepository.get().first()          // ~200ms
 * // Total: ~1200ms sequentially
 * ```
 * 
 * AFTER (Parallel):
 * ```kotlin
 * val results = coroutineScope {
 *     awaitAll(
 *         async { besteSchuleYearsRepository.get().first() },       // \
 *         async { besteSchuleIntervalsRepository.get().first() },   //  |
 *         async { besteSchuleTeachersRepository.get().first() },    //  |- All parallel
 *         async { besteSchuleCollectionsRepository.get().first() }, //  |
 *         async { besteSchuleSubjectsRepository.get().first() },    //  |
 *         async { besteSchuleGradesRepository.get().first() }       // /
 *     )
 * }
 * // Total: ~200ms (limited by slowest single request)
 * ```
 * 
 * Performance improvement: 6x faster (1200ms -> 200ms)
 */
class OptimizedParallelFetchExample : KoinComponent {
    
    /**
     * Example 1: Simple parallel fetch of independent resources
     */
    suspend fun fetchIndependentResourcesInParallel() = coroutineScope {
        // Launch all fetches concurrently
        val yearsDef = async { fetchYears() }
        val intervalsDef = async { fetchIntervals() }
        val teachersDef = async { fetchTeachers() }
        val collectionsDef = async { fetchCollections() }
        val subjectsDef = async { fetchSubjects() }
        
        // Wait for all to complete
        val years = yearsDef.await()
        val intervals = intervalsDef.await()
        val teachers = teachersDef.await()
        val collections = collectionsDef.await()
        val subjects = subjectsDef.await()
        
        // Use results
        processData(years, intervals, teachers, collections, subjects)
    }
    
    /**
     * Example 2: Parallel fetch with dependent resources
     * Some resources depend on others (e.g., grades depend on subjects)
     */
    suspend fun fetchWithDependencies() = coroutineScope {
        // Phase 1: Fetch independent resources in parallel
        val (years, intervals, teachers, collections) = awaitAll(
            async { fetchYears() },
            async { fetchIntervals() },
            async { fetchTeachers() },
            async { fetchCollections() }
        )
        
        // Phase 2: Fetch subjects (depends on collections)
        val subjects = fetchSubjects(collections)
        
        // Phase 3: Fetch grades (depends on subjects) 
        val grades = fetchGrades(subjects)
        
        // Process all data
        processAllData(years, intervals, teachers, collections, subjects, grades)
    }
    
    /**
     * Example 3: Chunked parallel processing for large datasets
     * When you have many items to process, batch them to avoid overwhelming the system
     */
    suspend fun fetchManyItemsInChunks(itemIds: List<String>) = coroutineScope {
        val chunkSize = 10 // Process 10 items at a time
        
        itemIds.chunked(chunkSize).flatMap { chunk ->
            // Process each chunk in parallel
            chunk.map { id ->
                async { fetchItem(id) }
            }.awaitAll()
        }
    }
    
    /**
     * Example 4: Parallel processing with error handling
     */
    suspend fun fetchWithErrorHandling() = coroutineScope {
        val results = awaitAll(
            async { 
                runCatching { fetchYears() }
                    .getOrElse { emptyList() } // Fallback on error
            },
            async { 
                runCatching { fetchIntervals() }
                    .getOrElse { emptyList() }
            },
            async { 
                runCatching { fetchTeachers() }
                    .getOrElse { emptyList() }
            }
        )
        
        val (years, intervals, teachers) = results
        processData(years, intervals, teachers)
    }
    
    /**
     * Example 5: Parallel fetch with early cancellation
     * If one critical fetch fails, cancel all others
     */
    suspend fun fetchWithEarlyExit() = coroutineScope {
        val years = async { fetchYears() }
        val intervals = async { fetchIntervals() }
        val teachers = async { fetchTeachers() }
        
        // If years fetch fails (critical), cancel everything
        try {
            val yearsResult = years.await()
            if (yearsResult.isEmpty()) {
                // Cancel other operations
                intervals.cancel()
                teachers.cancel()
                throw IllegalStateException("No years available")
            }
            
            // Continue with other results
            val intervalsResult = intervals.await()
            val teachersResult = teachers.await()
            
            processData(yearsResult, intervalsResult, teachersResult)
        } catch (e: Exception) {
            // Cleanup
            intervals.cancel()
            teachers.cancel()
            throw e
        }
    }
    
    /**
     * Example 6: Real-world pattern for updating sync use cases
     * 
     * Apply this pattern to:
     * - feature/sync/domain/usecase/besteschule/SyncGradesUseCase.kt
     * - feature/sync/domain/usecase/sp24/UpdateTimetableUseCase.kt
     * - feature/sync/domain/usecase/sp24/UpdateSubstitutionPlanUseCase.kt
     */
    suspend fun optimizedGradesSyncPattern(
        token: String,
        userId: Int
    ) = coroutineScope {
        // Step 1: Fetch metadata resources in parallel (no dependencies)
        val (years, intervals, teachers, collections) = awaitAll(
            async { fetchYearsFromApi(token, userId) },
            async { fetchIntervalsFromApi(token, userId) },
            async { fetchTeachersFromApi(token, userId) },
            async { fetchCollectionsFromApi(token, userId) }
        )
        
        // Step 2: Save metadata to database in parallel
        awaitAll(
            async { saveYearsToDb(years) },
            async { saveIntervalsToDb(intervals) },
            async { saveTeachersToDb(teachers) },
            async { saveCollectionsToDb(collections) }
        )
        
        // Step 3: Fetch subjects (depends on collections being saved)
        val subjects = fetchSubjectsFromApi(token, userId, collections)
        saveSubjectsToDb(subjects)
        
        // Step 4: Fetch grades for each subject in parallel (chunked)
        val allGrades = subjects.chunked(5).flatMap { subjectChunk ->
            subjectChunk.map { subject ->
                async { fetchGradesForSubject(token, userId, subject) }
            }.awaitAll()
        }.flatten()
        
        // Step 5: Save all grades
        saveGradesToDb(allGrades)
        
        SyncResult.Success(gradesCount = allGrades.size)
    }
    
    // Mock implementations for demonstration
    private suspend fun fetchYears(): List<Any> = emptyList()
    private suspend fun fetchIntervals(): List<Any> = emptyList()
    private suspend fun fetchTeachers(): List<Any> = emptyList()
    private suspend fun fetchCollections(): List<Any> = emptyList()
    private suspend fun fetchSubjects(collections: List<Any> = emptyList()): List<Any> = emptyList()
    private suspend fun fetchGrades(subjects: List<Any>): List<Any> = emptyList()
    private suspend fun fetchItem(id: String): Any = id
    private suspend fun processData(vararg args: List<Any>) {}
    private suspend fun processAllData(vararg args: List<Any>) {}
    private suspend fun fetchYearsFromApi(token: String, userId: Int): List<Any> = emptyList()
    private suspend fun fetchIntervalsFromApi(token: String, userId: Int): List<Any> = emptyList()
    private suspend fun fetchTeachersFromApi(token: String, userId: Int): List<Any> = emptyList()
    private suspend fun fetchCollectionsFromApi(token: String, userId: Int): List<Any> = emptyList()
    private suspend fun fetchSubjectsFromApi(token: String, userId: Int, collections: List<Any>): List<Any> = emptyList()
    private suspend fun fetchGradesForSubject(token: String, userId: Int, subject: Any): List<Any> = emptyList()
    private suspend fun saveYearsToDb(data: List<Any>) {}
    private suspend fun saveIntervalsToDb(data: List<Any>) {}
    private suspend fun saveTeachersToDb(data: List<Any>) {}
    private suspend fun saveCollectionsToDb(data: List<Any>) {}
    private suspend fun saveSubjectsToDb(data: List<Any>) {}
    private suspend fun saveGradesToDb(data: List<Any>) {}
    
    sealed class SyncResult {
        data class Success(val gradesCount: Int) : SyncResult()
        data class Error(val message: String) : SyncResult()
    }
}

/**
 * PERFORMANCE COMPARISON
 * 
 * Scenario: Sync 6 different resources
 * Average response time per resource: 200ms
 * 
 * Sequential approach:
 * - Years:       0-200ms      ████████
 * - Intervals:   200-400ms            ████████
 * - Teachers:    400-600ms                    ████████
 * - Collections: 600-800ms                            ████████
 * - Subjects:    800-1000ms                                   ████████
 * - Grades:      1000-1200ms                                          ████████
 * Total: 1200ms
 * 
 * Parallel approach:
 * - Years:       0-200ms      ████████
 * - Intervals:   0-200ms      ████████
 * - Teachers:    0-200ms      ████████
 * - Collections: 0-200ms      ████████
 * - Subjects:    200-400ms            ████████
 * - Grades:      400-600ms                    ████████
 * Total: 600ms
 * 
 * Improvement: 2x faster (50% reduction in time)
 * 
 * For more resources with no dependencies: up to Nx faster
 * For 10 independent resources: 10x faster
 */

/**
 * MIGRATION CHECKLIST
 * 
 * To apply this pattern to existing sync use cases:
 * 
 * 1. Identify independent fetches
 *    - [ ] List all data sources being fetched
 *    - [ ] Identify which ones don't depend on each other
 *    - [ ] Group independent fetches together
 * 
 * 2. Wrap parallel fetches in coroutineScope
 *    - [ ] Add `coroutineScope { }` block
 *    - [ ] Convert each fetch to `async { }`
 *    - [ ] Use `awaitAll()` to collect results
 * 
 * 3. Handle dependencies
 *    - [ ] Keep dependent fetches sequential
 *    - [ ] Create phases (Phase 1: independent, Phase 2: dependent on Phase 1, etc.)
 * 
 * 4. Add error handling
 *    - [ ] Wrap individual async blocks with runCatching if needed
 *    - [ ] Decide on cancellation strategy
 * 
 * 5. Test and measure
 *    - [ ] Add timing logs before/after optimization
 *    - [ ] Verify all data is still fetched correctly
 *    - [ ] Check for any race conditions
 *    - [ ] Measure performance improvement
 * 
 * Example timing logs:
 * ```kotlin
 * val startTime = System.currentTimeMillis()
 * // ... sync logic
 * val endTime = System.currentTimeMillis()
 * Logger.d { "Sync completed in ${endTime - startTime}ms" }
 * ```
 */

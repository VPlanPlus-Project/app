package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.first
import org.koin.core.component.inject
import plus.vplan.app.domain.cache.CacheConfig
import plus.vplan.app.domain.cache.RefreshPolicy
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.source.base.EnhancedDataSource
import kotlin.uuid.Uuid

/**
 * Enhanced Teacher data source demonstrating the new architecture.
 * 
 * This implementation shows:
 * - How to configure cache with custom TTL
 * - How to fetch from local (database) and remote (network)
 * - How to track linked entities (school in this case)
 * - How to use the different refresh policies
 * 
 * MIGRATION GUIDE:
 * Old usage:
 *   teacherSource.getById(id).filterIsInstance<AliasState.Done<Teacher>>().map { it.data }
 * 
 * New usage:
 *   enhancedTeacherSource.get(id, RefreshPolicy.CACHE_FIRST)
 *     .map { state -> 
 *       when(state) {
 *         is DataSourceState.Success -> state.data
 *         is DataSourceState.Loading -> null // or show loading UI
 *         is DataSourceState.Error -> state.cachedData // fallback to cache
 *         is DataSourceState.NotFound -> null
 *       }
 *     }
 * 
 * Force refresh:
 *   enhancedTeacherSource.get(id, forceRefresh = true)
 */
class EnhancedTeacherSource : EnhancedDataSource<Uuid, Teacher>() {
    private val teacherRepository: TeacherRepository by inject()
    // If there was a network API for teachers, inject it here
    // private val teacherApi: TeacherApi by inject()
    
    /**
     * Configure shorter TTL for teachers (12 hours instead of default 24)
     */
    override fun getCacheConfig(): CacheConfig = CacheConfig(
        ttlMillis = 12 * 60 * 60 * 1000, // 12 hours
        maxEntries = 500
    )
    
    /**
     * Fetch teacher from local database
     */
    override suspend fun fetchFromLocal(id: Uuid): Teacher? {
        return teacherRepository.getByLocalId(id).first()
    }
    
    /**
     * Fetch teacher from remote API
     * In this case, teachers are synced as part of school data,
     * so we just return what's in the database or throw an error
     */
    override suspend fun fetchFromRemote(id: Uuid): Teacher {
        // In a real implementation with a teacher API:
        // return teacherApi.getTeacher(id)
        
        // For now, teachers are synced as part of school sync,
        // so we can only return what's in the database
        return fetchFromLocal(id) 
            ?: throw IllegalStateException("Teacher $id not found. Teachers must be synced via school sync.")
    }
    
    /**
     * Save teacher to local database
     * Note: Teachers are typically saved via the repository during sync,
     * but this method allows for caching individual teacher updates
     */
    override suspend fun saveToLocal(id: Uuid, data: Teacher) {
        // Teachers are saved via TeacherRepository.upsert during sync
        // For this implementation, we don't need to do anything as the
        // repository already handles persistence through fetchFromRemote
    }
    
    /**
     * Track the school as a linked entity that might be loading
     */
    override suspend fun getLinkedEntityIds(data: Teacher): Set<String> {
        return setOf(data.schoolId.toString())
    }
    
    /**
     * Check if the linked school entity is currently loading
     * This allows the UI to show "Loading school..." state
     */
    override suspend fun isLinkedEntityLoading(entityId: String): Boolean {
        // Could check if school source is currently loading this school
        // For simplicity, returning false for now
        return false
    }
}

/**
 * Example usage in a ViewModel or UseCase:
 * 
 * // Get teacher with cache-first strategy
 * val teacher = enhancedTeacherSource
 *     .get(teacherId, RefreshPolicy.CACHE_FIRST)
 *     .collectAsState(DataSourceState.Loading(teacherId.toString()))
 * 
 * // Force refresh when user pulls to refresh
 * fun onRefresh() {
 *     enhancedTeacherSource.get(teacherId, forceRefresh = true)
 * }
 * 
 * // Get fresh data from network first
 * val freshTeacher = enhancedTeacherSource
 *     .get(teacherId, RefreshPolicy.NETWORK_FIRST)
 * 
 * // Display in UI based on state
 * when (teacher.value) {
 *     is DataSourceState.Loading -> LoadingIndicator()
 *     is DataSourceState.Success -> {
 *         val data = teacher.value.data
 *         val isRefreshing = teacher.value.isRefreshing
 *         TeacherCard(data, showRefreshIndicator = isRefreshing)
 *         
 *         // Show linked entities loading
 *         if (teacher.value.linkedEntitiesLoading.isNotEmpty()) {
 *             Text("Loading school information...")
 *         }
 *     }
 *     is DataSourceState.Error -> ErrorView(teacher.value.error)
 *     is DataSourceState.NotFound -> NotFoundView()
 * }
 */

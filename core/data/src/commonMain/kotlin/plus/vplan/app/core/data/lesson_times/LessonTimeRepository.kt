package plus.vplan.app.core.data.lesson_times

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.LessonTime
import plus.vplan.app.core.model.School

interface LessonTimeRepository {
    fun getByGroup(group: Group): Flow<List<LessonTime>>
    fun getByGroup(group: Group, lessonNumber: Int): Flow<LessonTime?>
    fun getBySchool(school: School): Flow<List<LessonTime>>
    fun getById(id: String): Flow<LessonTime?>

    suspend fun save(lessonTime: LessonTime) = save(listOf(lessonTime))
    suspend fun save(lessonTimes: List<LessonTime>)
    suspend fun delete(lessonTime: LessonTime) = delete(listOf(lessonTime))
    suspend fun delete(lessonTimes: List<LessonTime>)
}
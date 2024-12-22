package plus.vplan.app.data.source.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import plus.vplan.app.data.source.database.converters.LocalDateConverter
import plus.vplan.app.data.source.database.converters.LocalTimeConverter
import plus.vplan.app.data.source.database.converters.UuidTypeConverter
import plus.vplan.app.data.source.database.dao.CourseDao
import plus.vplan.app.data.source.database.dao.DefaultLessonDao
import plus.vplan.app.data.source.database.dao.GroupDao
import plus.vplan.app.data.source.database.dao.IndiwareDao
import plus.vplan.app.data.source.database.dao.KeyValueDao
import plus.vplan.app.data.source.database.dao.LessonTimeDao
import plus.vplan.app.data.source.database.dao.ProfileDao
import plus.vplan.app.data.source.database.dao.RoomDao
import plus.vplan.app.data.source.database.dao.SchoolDao
import plus.vplan.app.data.source.database.dao.TeacherDao
import plus.vplan.app.data.source.database.dao.TimetableDao
import plus.vplan.app.data.source.database.dao.WeekDao
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.DbGroupProfileDisabledDefaultLessons
import plus.vplan.app.data.source.database.model.database.DbIndiwareHasTimetableInWeek
import plus.vplan.app.data.source.database.model.database.DbKeyValue
import plus.vplan.app.data.source.database.model.database.DbLessonTime
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbRoomProfile
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbSp24SchoolDetails
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTeacherProfile
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.DbWeek
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableGroup
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableRoom
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableTeacher

@Database(
    entities = [
        DbSchool::class,
        DbSp24SchoolDetails::class,
        DbIndiwareHasTimetableInWeek::class,

        DbGroup::class,
        DbTeacher::class,
        DbRoom::class,

        DbProfile::class,
        DbGroupProfile::class,
        DbGroupProfileDisabledDefaultLessons::class,
        DbTeacherProfile::class,
        DbRoomProfile::class,

        DbDefaultLesson::class,
        DbCourse::class,

        DbKeyValue::class,

        DbWeek::class,
        DbLessonTime::class,
        DbTimetableLesson::class,
        DbTimetableGroup::class,
        DbTimetableTeacher::class,
        DbTimetableRoom::class
    ],
    version = 1,
)
@TypeConverters(
    value = [
        UuidTypeConverter::class,
        LocalDateConverter::class,
        LocalTimeConverter::class
    ]
)
abstract class VppDatabase : RoomDatabase() {
    abstract val schoolDao: SchoolDao
    abstract val groupDao: GroupDao
    abstract val teacherDao: TeacherDao
    abstract val roomDao: RoomDao
    abstract val courseDao: CourseDao
    abstract val defaultLessonDao: DefaultLessonDao
    abstract val profileDao: ProfileDao
    abstract val keyValueDao: KeyValueDao
    abstract val weekDao: WeekDao
    abstract val lessonTimeDao: LessonTimeDao
    abstract val timetableDao: TimetableDao
    abstract val indiwareDao: IndiwareDao
}
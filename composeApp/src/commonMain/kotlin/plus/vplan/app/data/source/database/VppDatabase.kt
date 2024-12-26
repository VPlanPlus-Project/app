package plus.vplan.app.data.source.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import plus.vplan.app.data.source.database.converters.LocalDateConverter
import plus.vplan.app.data.source.database.converters.LocalTimeConverter
import plus.vplan.app.data.source.database.converters.UuidTypeConverter
import plus.vplan.app.data.source.database.dao.CourseDao
import plus.vplan.app.data.source.database.dao.DayDao
import plus.vplan.app.data.source.database.dao.DefaultLessonDao
import plus.vplan.app.data.source.database.dao.GroupDao
import plus.vplan.app.data.source.database.dao.HolidayDao
import plus.vplan.app.data.source.database.dao.IndiwareDao
import plus.vplan.app.data.source.database.dao.KeyValueDao
import plus.vplan.app.data.source.database.dao.LessonTimeDao
import plus.vplan.app.data.source.database.dao.ProfileDao
import plus.vplan.app.data.source.database.dao.RoomDao
import plus.vplan.app.data.source.database.dao.SchoolDao
import plus.vplan.app.data.source.database.dao.SubstitutionPlanDao
import plus.vplan.app.data.source.database.dao.TeacherDao
import plus.vplan.app.data.source.database.dao.TimetableDao
import plus.vplan.app.data.source.database.dao.WeekDao
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.DbDay
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.DbGroupProfileDisabledDefaultLessons
import plus.vplan.app.data.source.database.model.database.DbHoliday
import plus.vplan.app.data.source.database.model.database.DbIndiwareHasTimetableInWeek
import plus.vplan.app.data.source.database.model.database.DbKeyValue
import plus.vplan.app.data.source.database.model.database.DbLessonTime
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbRoomProfile
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbSp24SchoolDetails
import plus.vplan.app.data.source.database.model.database.DbSubstitutionPlanLesson
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTeacherProfile
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.DbVppId
import plus.vplan.app.data.source.database.model.database.DbWeek
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanTeacherCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableTeacherCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbVppIdGroupCrossover

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
        DbTimetableGroupCrossover::class,
        DbTimetableTeacherCrossover::class,
        DbTimetableRoomCrossover::class,

        DbSubstitutionPlanLesson::class,
        DbSubstitutionPlanGroupCrossover::class,
        DbSubstitutionPlanRoomCrossover::class,
        DbSubstitutionPlanTeacherCrossover::class,

        DbDay::class,
        DbHoliday::class,

        DbVppId::class,
        DbVppIdGroupCrossover::class,
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
    abstract val dayDao: DayDao
    abstract val holidayDao: HolidayDao
    abstract val substitutionPlanDao: SubstitutionPlanDao
}
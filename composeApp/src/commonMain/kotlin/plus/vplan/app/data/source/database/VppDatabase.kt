package plus.vplan.app.data.source.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import plus.vplan.app.data.source.database.converters.InstantConverter
import plus.vplan.app.data.source.database.converters.LocalDateConverter
import plus.vplan.app.data.source.database.converters.LocalDateTimeConverter
import plus.vplan.app.data.source.database.converters.LocalTimeConverter
import plus.vplan.app.data.source.database.converters.UuidTypeConverter
import plus.vplan.app.data.source.database.dao.AssessmentDao
import plus.vplan.app.data.source.database.dao.CourseDao
import plus.vplan.app.data.source.database.dao.DayDao
import plus.vplan.app.data.source.database.dao.SubjectInstanceDao
import plus.vplan.app.data.source.database.dao.FileDao
import plus.vplan.app.data.source.database.dao.GroupDao
import plus.vplan.app.data.source.database.dao.HolidayDao
import plus.vplan.app.data.source.database.dao.HomeworkDao
import plus.vplan.app.data.source.database.dao.IndiwareDao
import plus.vplan.app.data.source.database.dao.KeyValueDao
import plus.vplan.app.data.source.database.dao.LessonTimeDao
import plus.vplan.app.data.source.database.dao.ProfileDao
import plus.vplan.app.data.source.database.dao.RoomDao
import plus.vplan.app.data.source.database.dao.SchoolDao
import plus.vplan.app.data.source.database.dao.SubstitutionPlanDao
import plus.vplan.app.data.source.database.dao.TeacherDao
import plus.vplan.app.data.source.database.dao.TimetableDao
import plus.vplan.app.data.source.database.dao.VppIdDao
import plus.vplan.app.data.source.database.dao.WeekDao
import plus.vplan.app.data.source.database.dao.schulverwalter.CollectionDao
import plus.vplan.app.data.source.database.dao.schulverwalter.FinalGradeDao
import plus.vplan.app.data.source.database.dao.schulverwalter.GradeDao
import plus.vplan.app.data.source.database.dao.schulverwalter.IntervalDao
import plus.vplan.app.data.source.database.dao.schulverwalter.SubjectDao
import plus.vplan.app.data.source.database.dao.schulverwalter.YearDao
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.DbDay
import plus.vplan.app.data.source.database.model.database.DbSubjectInstance
import plus.vplan.app.data.source.database.model.database.DbAssessment
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.foreign_key.FKGroupProfileDisabledSubjectInstances
import plus.vplan.app.data.source.database.model.database.DbHoliday
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbFile
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneAccount
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneProfile
import plus.vplan.app.data.source.database.model.database.DbIndiwareTimetableMetadata
import plus.vplan.app.data.source.database.model.database.DbKeyValue
import plus.vplan.app.data.source.database.model.database.DbLessonTime
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbRoomProfile
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterCollection
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterFinalGrade
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterGrade
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterTeacher
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterYear
import plus.vplan.app.data.source.database.model.database.DbSchoolIndiwareAccess
import plus.vplan.app.data.source.database.model.database.DbSubstitutionPlanLesson
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTeacherProfile
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.DbVppId
import plus.vplan.app.data.source.database.model.database.DbVppIdAccess
import plus.vplan.app.data.source.database.model.database.DbVppIdSchulverwalter
import plus.vplan.app.data.source.database.model.database.DbWeek
import plus.vplan.app.data.source.database.model.database.crossovers.DbCourseGroupCrossover
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSubjectInstanceGroup
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanTeacherCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableTeacherCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbVppIdGroupCrossover
import plus.vplan.app.data.source.database.model.database.foreign_key.FKAssessmentFile
import plus.vplan.app.data.source.database.model.database.foreign_key.FKHomeworkFile
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchoolGroup
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterCollection
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterTeacher
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterSubjectSchulverwalterFinalGrade
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterYearSchulverwalterInterval
import plus.vplan.app.data.source.database.dao.schulverwalter.TeacherDao as SchulverwalterTeacherDao

@Database(
    entities = [
        DbSchool::class,
        DbSchoolIndiwareAccess::class,
        DbIndiwareTimetableMetadata::class,

        DbGroup::class,
        DbTeacher::class,
        DbRoom::class,

        DbProfile::class,
        DbGroupProfile::class,
        FKGroupProfileDisabledSubjectInstances::class,
        DbTeacherProfile::class,
        DbRoomProfile::class,

        DbSubjectInstance::class,
        FKSubjectInstanceGroup::class,
        DbCourse::class,
        DbCourseGroupCrossover::class,

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
        DbVppIdAccess::class,
        DbVppIdSchulverwalter::class,
        DbVppIdGroupCrossover::class,

        DbHomework::class,
        DbHomeworkTask::class,
        DbHomeworkTaskDoneProfile::class,
        DbHomeworkTaskDoneAccount::class,
        FKHomeworkFile::class,

        DbFile::class,

        FKSchoolGroup::class,

        DbAssessment::class,
        FKAssessmentFile::class,

        DbSchulverwalterCollection::class,
        DbSchulverwalterFinalGrade::class,
        DbSchulverwalterGrade::class,
        DbSchulverwalterInterval::class,
        DbSchulverwalterSubject::class,
        DbSchulverwalterTeacher::class,
        DbSchulverwalterYear::class,

        FKSchulverwalterYearSchulverwalterInterval::class,
        FKSchulverwalterCollectionSchulverwalterInterval::class,
        FKSchulverwalterCollectionSchulverwalterSubject::class,
        FKSchulverwalterGradeSchulverwalterCollection::class,
        FKSchulverwalterGradeSchulverwalterSubject::class,
        FKSchulverwalterGradeSchulverwalterTeacher::class,
        FKSchulverwalterSubjectSchulverwalterFinalGrade::class,
    ],
    version = 1,
)
@TypeConverters(
    value = [
        UuidTypeConverter::class,
        LocalDateConverter::class,
        LocalTimeConverter::class,
        LocalDateTimeConverter::class,
        InstantConverter::class
    ]
)
abstract class VppDatabase : RoomDatabase() {
    abstract val schoolDao: SchoolDao
    abstract val groupDao: GroupDao
    abstract val teacherDao: TeacherDao
    abstract val roomDao: RoomDao
    abstract val courseDao: CourseDao
    abstract val subjectInstanceDao: SubjectInstanceDao
    abstract val profileDao: ProfileDao
    abstract val keyValueDao: KeyValueDao
    abstract val weekDao: WeekDao
    abstract val lessonTimeDao: LessonTimeDao
    abstract val timetableDao: TimetableDao
    abstract val indiwareDao: IndiwareDao
    abstract val dayDao: DayDao
    abstract val holidayDao: HolidayDao
    abstract val substitutionPlanDao: SubstitutionPlanDao
    abstract val vppIdDao: VppIdDao
    abstract val homeworkDao: HomeworkDao
    abstract val fileDao: FileDao
    abstract val assessmentDao: AssessmentDao

    // Schulverwalter
    abstract val yearDao: YearDao
    abstract val intervalDao: IntervalDao
    abstract val collectionDao: CollectionDao
    abstract val subjectDao: SubjectDao
    abstract val schulverwalterTeacherDao: SchulverwalterTeacherDao
    abstract val gradeDao: GradeDao
    abstract val finalGradeDao: FinalGradeDao
}
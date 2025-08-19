package plus.vplan.app.data.source.database

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import plus.vplan.app.data.source.database.converters.AliasPrefixConverter
import plus.vplan.app.data.source.database.converters.CreationReasonConverter
import plus.vplan.app.data.source.database.converters.InstantConverter
import plus.vplan.app.data.source.database.converters.KotlinxInstantConverter
import plus.vplan.app.data.source.database.converters.LocalDateConverter
import plus.vplan.app.data.source.database.converters.LocalDateTimeConverter
import plus.vplan.app.data.source.database.converters.LocalTimeConverter
import plus.vplan.app.data.source.database.converters.UuidTypeConverter
import plus.vplan.app.data.source.database.dao.AssessmentDao
import plus.vplan.app.data.source.database.dao.CourseDao
import plus.vplan.app.data.source.database.dao.DayDao
import plus.vplan.app.data.source.database.dao.FcmDao
import plus.vplan.app.data.source.database.dao.FileDao
import plus.vplan.app.data.source.database.dao.GroupDao
import plus.vplan.app.data.source.database.dao.HolidayDao
import plus.vplan.app.data.source.database.dao.HomeworkDao
import plus.vplan.app.data.source.database.dao.KeyValueDao
import plus.vplan.app.data.source.database.dao.LessonTimeDao
import plus.vplan.app.data.source.database.dao.NewsDao
import plus.vplan.app.data.source.database.dao.ProfileDao
import plus.vplan.app.data.source.database.dao.ProfileTimetableCacheDao
import plus.vplan.app.data.source.database.dao.RoomDao
import plus.vplan.app.data.source.database.dao.SchoolDao
import plus.vplan.app.data.source.database.dao.Stundenplan24Dao
import plus.vplan.app.data.source.database.dao.SubjectInstanceDao
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
import plus.vplan.app.data.source.database.model.database.DbAssessment
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.DbCourseAlias
import plus.vplan.app.data.source.database.model.database.DbDay
import plus.vplan.app.data.source.database.model.database.DbFcmLog
import plus.vplan.app.data.source.database.model.database.DbFile
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupAlias
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.DbHoliday
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneAccount
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneProfile
import plus.vplan.app.data.source.database.model.database.DbKeyValue
import plus.vplan.app.data.source.database.model.database.DbLessonTime
import plus.vplan.app.data.source.database.model.database.DbNews
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbProfileAssessmentIndex
import plus.vplan.app.data.source.database.model.database.DbProfileHomeworkIndex
import plus.vplan.app.data.source.database.model.database.DbProfileSubstitutionPlanCache
import plus.vplan.app.data.source.database.model.database.DbProfileTimetableCache
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbRoomAlias
import plus.vplan.app.data.source.database.model.database.DbRoomProfile
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbSchoolAlias
import plus.vplan.app.data.source.database.model.database.DbSchoolSp24Acess
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterCollection
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterFinalGrade
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterGrade
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterTeacher
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterYear
import plus.vplan.app.data.source.database.model.database.DbStundenplan24TimetableMetadata
import plus.vplan.app.data.source.database.model.database.DbSubjectInstance
import plus.vplan.app.data.source.database.model.database.DbSubjectInstanceAlias
import plus.vplan.app.data.source.database.model.database.DbSubstitutionPlanLesson
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTeacherAlias
import plus.vplan.app.data.source.database.model.database.DbTeacherProfile
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.DbVppId
import plus.vplan.app.data.source.database.model.database.DbVppIdAccess
import plus.vplan.app.data.source.database.model.database.DbVppIdSchulverwalter
import plus.vplan.app.data.source.database.model.database.DbWeek
import plus.vplan.app.data.source.database.model.database.crossovers.DbCourseGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanTeacherCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableTeacherCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbVppIdGroupCrossover
import plus.vplan.app.data.source.database.model.database.foreign_key.FKAssessmentFile
import plus.vplan.app.data.source.database.model.database.foreign_key.FKGroupProfileDisabledSubjectInstances
import plus.vplan.app.data.source.database.model.database.foreign_key.FKHomeworkFile
import plus.vplan.app.data.source.database.model.database.foreign_key.FKNewsSchool
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterCollection
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterTeacher
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterSubjectSchulverwalterFinalGrade
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterYearSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSubjectInstanceGroup
import plus.vplan.app.data.source.database.dao.schulverwalter.TeacherDao as SchulverwalterTeacherDao

@Database(
    entities = [
        DbSchool::class,
        DbSchoolAlias::class,
        DbSchoolSp24Acess::class,
        DbStundenplan24TimetableMetadata::class,

        DbGroup::class,
        DbGroupAlias::class,

        DbTeacher::class,
        DbTeacherAlias::class,

        DbRoom::class,
        DbRoomAlias::class,

        DbProfile::class,
        DbGroupProfile::class,
        FKGroupProfileDisabledSubjectInstances::class,
        DbTeacherProfile::class,
        DbRoomProfile::class,

        DbProfileAssessmentIndex::class,
        DbProfileHomeworkIndex::class,

        DbSubjectInstance::class,
        DbSubjectInstanceAlias::class,
        FKSubjectInstanceGroup::class,

        DbCourse::class,
        DbCourseAlias::class,

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

        DbFcmLog::class,

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

        DbProfileTimetableCache::class,
        DbProfileSubstitutionPlanCache::class,

        DbNews::class,
        FKNewsSchool::class,
    ],
    version = VppDatabase.DATABASE_VERSION,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2,
            spec = VppDatabase.Migration1to2::class
        ),
        AutoMigration(
            from = 2,
            to = 3,
            spec = VppDatabase.Migration2to3::class
        ),
        AutoMigration(
            from = 3,
            to = 4,
            spec = VppDatabase.Migration3to4::class
        )
    ]
)
@TypeConverters(
    value = [
        UuidTypeConverter::class,
        LocalDateConverter::class,
        LocalTimeConverter::class,
        LocalDateTimeConverter::class,
        InstantConverter::class,
        KotlinxInstantConverter::class,
        AliasPrefixConverter::class,
        CreationReasonConverter::class
    ]
)
@ConstructedBy(VppDatabaseConstructor::class)
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
    abstract val stundenplan24Dao: Stundenplan24Dao
    abstract val dayDao: DayDao
    abstract val holidayDao: HolidayDao
    abstract val substitutionPlanDao: SubstitutionPlanDao
    abstract val vppIdDao: VppIdDao
    abstract val homeworkDao: HomeworkDao
    abstract val fileDao: FileDao
    abstract val assessmentDao: AssessmentDao
    abstract val profileTimetableCacheDao: ProfileTimetableCacheDao
    abstract val newsDao: NewsDao

    abstract val fcmDao: FcmDao

    // Schulverwalter
    abstract val yearDao: YearDao
    abstract val intervalDao: IntervalDao
    abstract val collectionDao: CollectionDao
    abstract val subjectDao: SubjectDao
    abstract val schulverwalterTeacherDao: SchulverwalterTeacherDao
    abstract val gradeDao: GradeDao
    abstract val finalGradeDao: FinalGradeDao

    companion object {
        const val DATABASE_VERSION = 4
    }

    @RenameColumn(
        tableName = "indiware_timetable_metadata",
        fromColumnName = "indiware_school_id",
        toColumnName = "stundenplan24_school_id"
    )
    @RenameTable(fromTableName = "school_indiware_access", toTableName = "school_sp24_access")
    @RenameTable(fromTableName = "indiware_timetable_metadata", toTableName = "stundenplan24_timetable_metadata")
    class Migration1to2 : AutoMigrationSpec

    /**
     * Remove not-null constraint for [DbDay]
     */
    class Migration2to3 : AutoMigrationSpec {
        override fun onPostMigrate(connection: SQLiteConnection) {
            connection.execSQL("""
                CREATE TABLE day_dg_tmp
                (
                    id        TEXT NOT NULL
                        PRIMARY KEY,
                    date      TEXT NOT NULL,
                    week_id   TEXT
                        REFERENCES weeks
                            ON UPDATE CASCADE ON DELETE CASCADE,
                    school_id TEXT NOT NULL
                        REFERENCES schools
                            ON UPDATE CASCADE ON DELETE CASCADE,
                    info      TEXT
                );

                INSERT INTO day_dg_tmp(id, date, week_id, school_id, info)
                SELECT id, date, week_id, school_id, info
                FROM day;

                DROP TABLE day;

                ALTER TABLE day_dg_tmp
                    RENAME TO day;

                CREATE INDEX index_day_school_id
                    ON day (school_id);

                CREATE INDEX index_day_week_id
                    ON day (week_id);
            """.trimIndent())
        }
    }

    class Migration3to4 : AutoMigrationSpec {
        override fun onPostMigrate(connection: SQLiteConnection) {
            connection.execSQL("""
                create table homework_dg_tmp
                (
                    id                    INTEGER not null
                        primary key,
                    subject_instance_id   INTEGER,
                    group_id              INTEGER,
                    created_at            INTEGER not null,
                    due_to                TEXT    not null,
                    created_by_vpp_id     INTEGER,
                    created_by_profile_id TEXT
                        references profiles
                            on update cascade on delete cascade,
                    is_public             INTEGER not null,
                    cached_at             INTEGER not null
                );

                drop table homework;

                alter table homework_dg_tmp
                    rename to homework;

                create index index_homework_created_by_profile_id
                    on homework (created_by_profile_id);

                create index index_homework_created_by_vpp_id
                    on homework (created_by_vpp_id);

                create unique index index_homework_id
                    on homework (id);


            """.trimIndent())
        }
    }
}

// Room compiler generates the `actual` implementations
@Suppress(
    "NO_ACTUAL_FOR_EXPECT",
    "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
    "KotlinNoActualForExpect"
)
expect object VppDatabaseConstructor : RoomDatabaseConstructor<VppDatabase> {
    override fun initialize(): VppDatabase
}
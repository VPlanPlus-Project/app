package plus.vplan.app.feature.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.collectAsLoadingState
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.cache.collectAsSingleFlow
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.ProfileType
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.feature.assessment.ui.components.create.NewAssessmentDrawer
import plus.vplan.app.feature.home.ui.components.DayInfoCard
import plus.vplan.app.feature.home.ui.components.FeedTitle
import plus.vplan.app.feature.home.ui.components.Greeting
import plus.vplan.app.feature.home.ui.components.QuickActions
import plus.vplan.app.feature.homework.ui.components.NewHomeworkDrawer
import plus.vplan.app.feature.main.MainScreen
import plus.vplan.app.feature.news.ui.NewsDrawer
import plus.vplan.app.feature.schulverwalter.domain.usecase.InitializeSchulverwalterReauthUseCase
import plus.vplan.app.feature.settings.page.info.ui.components.FeedbackDrawer
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.utils.BrowserIntent
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.longDayOfWeekNames
import plus.vplan.app.utils.longMonthNames
import plus.vplan.app.utils.progressIn
import plus.vplan.app.utils.regularDateFormatWithoutYear
import plus.vplan.app.utils.regularTimeFormat
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.transparent
import plus.vplan.app.utils.untilRelativeText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_right
import vplanplus.composeapp.generated.resources.book_open
import vplanplus.composeapp.generated.resources.chart_no_axes_gantt
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.info
import vplanplus.composeapp.generated.resources.key_round
import vplanplus.composeapp.generated.resources.megaphone
import vplanplus.composeapp.generated.resources.minus
import vplanplus.composeapp.generated.resources.notebook_text
import vplanplus.composeapp.generated.resources.triangle_alert
import kotlin.uuid.ExperimentalUuidApi

private val LESSON_NUMBER_TOP_PADDING = 16.dp
private val LESSON_NUMBER_SIZE = 32.dp

@Composable
fun HomeScreen(
    navHostController: NavHostController,
    contentPadding: PaddingValues,
    homeViewModel: HomeViewModel
) {
    HomeContent(
        state = homeViewModel.state,
        contentPadding = contentPadding,
        onOpenRoomSearch = remember { { navHostController.navigate(MainScreen.RoomSearch) } },
        onOpenSchoolSettings = remember { { navHostController.navigate(MainScreen.SchoolSettings(openIndiwareSettingsSchoolId = it)) } },
        onEvent = homeViewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
private fun HomeContent(
    state: HomeState,
    contentPadding: PaddingValues,
    onOpenRoomSearch: () -> Unit,
    onOpenSchoolSettings: (schoolId: Int) -> Unit,
    onEvent: (event: HomeEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val localDensity = LocalDensity.current
    val pullToRefreshState = rememberPullToRefreshState()
    val initializeSchulverwalterReauthUseCase = koinInject<InitializeSchulverwalterReauthUseCase>()

    var isNewHomeworkDrawerVisible by rememberSaveable { mutableStateOf(false) }
    var isNewAssessmentDrawerVisible by rememberSaveable { mutableStateOf(false) }
    var isFeedbackDrawerVisible by rememberSaveable { mutableStateOf(false) }

    var visibleNews by rememberSaveable { mutableStateOf<Int?>(null) }

    val vppId = remember(state.currentProfile) { (state.currentProfile as? Profile.StudentProfile)?.vppId }?.collectAsResultingFlow()?.value as? VppId.Active

    PullToRefreshBox(
        state = pullToRefreshState,
        onRefresh = { onEvent(HomeEvent.OnRefresh) },
        isRefreshing = state.isUpdating,
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            run greeting@{
                Greeting(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    displayName = vppId?.name?.split(" ")?.first() ?: ""
                )
            }
            Spacer(Modifier.height(4.dp))
            AnimatedContent(
                targetState = state.initDone
            ) { initDone ->
                if (!initDone) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@AnimatedContent
                }

                val school = remember(state.currentProfile) { state.currentProfile?.getSchool() }?.collectAsResultingFlow()?.value

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(Modifier.size(8.dp)) }
                    item quickActions@{
                        QuickActions(
                            onNewHomeworkClicked = { isNewHomeworkDrawerVisible = true },
                            onNewAssessmentClicked = { isNewAssessmentDrawerVisible = true },
                            onRoomSearchClicked = onOpenRoomSearch,
                            onFeedbackClicked = { isFeedbackDrawerVisible = true }
                        )
                    }
                    item schoolAccessInvalid@{
                        androidx.compose.animation.AnimatedVisibility(
                            visible = school is School.IndiwareSchool && !school.credentialsValid,
                            enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            var displaySchool by remember { mutableStateOf<School?>(null) }
                            LaunchedEffect(school) {
                                if (school is School.IndiwareSchool && !school.credentialsValid) displaySchool = school
                            }
                            if (displaySchool != null) InfoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                title = "Schulzugangsdaten abgelaufen",
                                text = "Die Schulzugangsdaten für ${displaySchool!!.name} sind abgelaufen. Bitte aktualisiere sie, um weiterhin auf dem neuesten Stand zu bleiben.",
                                textColor = MaterialTheme.colorScheme.onErrorContainer,
                                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                                shadow = true,
                                buttonAction1 = { onOpenSchoolSettings(displaySchool!!.id) },
                                buttonText1 = "Aktualisieren",
                                imageVector = Res.drawable.key_round
                            )
                        }
                    }
                    item {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = vppId?.schulverwalterConnection?.isValid == false,
                            enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InfoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                title = "beste.schule-Zugangsdaten abgelaufen",
                                text = "Die Verbindung zu beste.schule ist nicht mehr gültig. Bitte melde dich erneut mit beste.schule an.",
                                textColor = MaterialTheme.colorScheme.onErrorContainer,
                                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                                shadow = true,
                                buttonAction1 = { scope.launch {
                                    if (vppId == null) return@launch
                                    val url = initializeSchulverwalterReauthUseCase(vppId) ?: return@launch
                                    BrowserIntent.openUrl(url)
                                } },
                                buttonText1 = "Aktualisieren",
                                imageVector = Res.drawable.key_round
                            )
                        }
                    }
                    item unreadNews@{
                        val unreadNews = state.news.filter { !it.isRead }
                        if (unreadNews.isEmpty()) return@unreadNews
                        Column {
                            FeedTitle(
                                icon = Res.drawable.megaphone,
                                title = "Ungelesene Meldungen"
                            )
                            Spacer(Modifier.size(8.dp))
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp)),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                unreadNews.forEach { news ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { visibleNews = news.id }
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = news.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = news.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 4,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item yourDay@{
                        val isYourDayToday = state.day?.date == state.currentTime.date
                        val weekState = remember(state.day) { state.day?.week }?.collectAsLoadingState("")?.value
                        if (state.day == null || weekState is CacheState.Loading) return@yourDay
                        val week = (weekState as? CacheState.Done)?.data

                        Column {
                            FeedTitle(
                                icon = Res.drawable.chart_no_axes_gantt,
                                title = if (isYourDayToday) "Dein Tag" else "Nächster Schultag",
                                endText = state.day.date.format(LocalDate.Format {
                                    dayOfWeek(longDayOfWeekNames)
                                    chars(", ")
                                    dayOfMonth()
                                    chars(". ")
                                    monthName(longMonthNames)
                                    char(' ')
                                    year()
                                }) + "\n${week?.weekType ?: "Unbekannte"}-Woche (KW ${week?.calendarWeek}, SW ${week?.weekIndex})"
                            )
                            if (state.day.info != null) DayInfoCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), info = state.day.info)
                            if (state.day.substitutionPlan.isEmpty()) InfoCard(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                imageVector = Res.drawable.triangle_alert,
                                title = "Kein Vertretungsplan",
                                text = buildString {
                                    append("Für ")
                                    append((state.currentTime.date untilRelativeText state.day.date) ?: "den ${state.day.date.format(regularDateFormatWithoutYear)}")
                                    append(" ist noch kein Vertretungsplan verfügbar. Es kann noch zu Änderungen kommen.")
                                },
                                backgroundColor = colors[CustomColor.Yellow]!!.getGroup().container,
                                textColor = colors[CustomColor.Yellow]!!.getGroup().onContainer,
                            )
                            val highlightedLessons = HighlightedLessons(state)
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth()
                            ) {
                                val homework by remember(state.day.homeworkIds) { state.day.homework }.collectAsState(emptySet())
                                val assessments by remember(state.day.assessmentIds) { state.day.assessments }.collectAsState(emptySet())
                                if (highlightedLessons.hasLessons) AnimatedContent(
                                    targetState = highlightedLessons
                                ) { highlightConfig ->
                                    Column {
                                        if (highlightConfig.showCurrent) {
                                            Text(
                                                text = "Aktueller Unterricht",
                                                style = MaterialTheme.typography.titleSmall,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                            Spacer(Modifier.size(4.dp))
                                            Column(
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp)
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp)),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer) {
                                                    highlightedLessons.currentLessons.forEach { (currentLesson, continuing) ->
                                                        val homeworkForLesson = homework.filter { it.subjectInstanceId == currentLesson.subjectInstanceId }
                                                        val assessmentsForLesson = assessments.filter { it.subjectInstanceId == currentLesson.subjectInstanceId }
                                                        CurrentOrNextLesson(
                                                            currentTime = state.currentTime,
                                                            currentLesson = currentLesson,
                                                            currentProfileType = state.currentProfile?.profileType,
                                                            continuing = continuing,
                                                            homework = homeworkForLesson,
                                                            assessments = assessmentsForLesson,
                                                            progressType = ProgressType.Regular
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        else {
                                            Text(
                                                text = "Nächster Unterricht",
                                                style = MaterialTheme.typography.titleSmall,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                            Spacer(Modifier.size(4.dp))
                                            Column(
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp)
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp)),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer) {
                                                    state.nextLessons.forEach { nextLesson ->
                                                        CurrentOrNextLesson(
                                                            currentTime = state.currentTime,
                                                            currentLesson = nextLesson,
                                                            currentProfileType = state.currentProfile?.profileType,
                                                            continuing = null,
                                                            homework = emptyList(),
                                                            assessments = emptyList(),
                                                            progressType = ProgressType.Disabled
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if ((state.remainingLessons.values.flatten() - (if (!highlightedLessons.showCurrent) highlightedLessons.nextLesson.toSet() else emptySet())).isNotEmpty()) {
                                    Spacer(Modifier.size(8.dp))
                                    Text(
                                        text = if (isYourDayToday) "Weitere Stunden" else "Stundenplan",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    Column(
                                        modifier = Modifier
                                            .padding(start = 16.dp)
                                            .fillMaxWidth()
                                    ) {
                                        state.remainingLessons.toList().forEachIndexed forEachLessonNumber@{ i, (lessonNumber, lessons) ->
                                            var lessonsHeight by remember { mutableStateOf(0.dp) }
                                            Row {
                                                val colorScheme = MaterialTheme.colorScheme
                                                Box(
                                                    modifier = Modifier
                                                        .width(32.dp)
                                                        .height(lessonsHeight)
                                                        .drawWithContent {
                                                            if (!(state.currentLessons.isEmpty() && state.nextLessons.isEmpty() && i == 0)) drawLine(
                                                                brush = Brush.verticalGradient(0f to if (i == 0) colorScheme.secondaryContainer.transparent() else colorScheme.secondaryContainer, 1f to colorScheme.secondaryContainer, startY = 0f, endY = 16.dp.toPx()),
                                                                start = Offset(size.width/2, 0f),
                                                                end = Offset(size.width/2, LESSON_NUMBER_TOP_PADDING.toPx()),
                                                                strokeWidth = 1.dp.toPx()
                                                            )
                                                            if (i < state.remainingLessons.size - 1) drawLine(
                                                                color = colorScheme.secondaryContainer,
                                                                start = Offset(size.width/2, (LESSON_NUMBER_TOP_PADDING+LESSON_NUMBER_SIZE).toPx()),
                                                                end = Offset(size.width/2, size.height),
                                                                strokeWidth = 1.dp.toPx()
                                                            )
                                                            drawContent()
                                                        }
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopCenter)
                                                            .padding(top = LESSON_NUMBER_TOP_PADDING)
                                                            .size(LESSON_NUMBER_SIZE)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                                    ) {
                                                        Text(
                                                            text = lessonNumber.toString(),
                                                            modifier = Modifier.align(Alignment.Center),
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    }
                                                }
                                                Spacer(Modifier.size(8.dp))
                                                Column(
                                                    modifier = Modifier
                                                        .onSizeChanged { with(localDensity) { lessonsHeight = it.height.toDp() } }
                                                        .padding(end = 16.dp, top = LESSON_NUMBER_TOP_PADDING)
                                                        .fillMaxWidth(),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    val headFont = MaterialTheme.typography.bodyLarge
                                                    lessons.forEach forEachLesson@{ lesson ->
                                                        val lessonTime = remember(lesson) { lesson.lessonTime }.collectAsResultingFlow().value ?: return@forEachLesson
                                                        val rooms = remember(lesson.roomIds) { lesson.rooms }.collectAsSingleFlow().value
                                                        val groups = remember(lesson.groupIds) { lesson.groups }.collectAsSingleFlow().value
                                                        val teachers = remember(lesson.teacherIds) { lesson.teachers }.collectAsSingleFlow().value
                                                        val homeworkForLesson = homework.filter { it.subjectInstanceId == lesson.subjectInstanceId }
                                                        val assessmentsForLesson = assessments.filter { it.subjectInstanceId == lesson.subjectInstanceId }
                                                        val subjectInstance = remember(lesson.subjectInstanceId) { lesson.subjectInstance }?.collectAsResultingFlow()?.value
                                                        Column(Modifier.fillMaxWidth()) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(LESSON_NUMBER_SIZE),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                SubjectIcon(
                                                                    subject = lesson.subject,
                                                                    modifier = Modifier
                                                                        .size(LESSON_NUMBER_SIZE)
                                                                        .padding(2.dp)
                                                                        .clip(RoundedCornerShape(8.dp))
                                                                )
                                                                Text(
                                                                    text = lesson.subject ?: "${subjectInstance?.subject?.plus(" ").orEmpty()}Entfall",
                                                                    style = headFont,
                                                                    color = if (lesson is Lesson.SubstitutionPlanLesson && lesson.isSubjectChanged) MaterialTheme.colorScheme.error else LocalContentColor.current
                                                                )
                                                                if (rooms.isNotEmpty()) Text(
                                                                    text = rooms.joinToString { it.name },
                                                                    style = headFont,
                                                                    color = if (lesson is Lesson.SubstitutionPlanLesson && lesson.isRoomChanged) MaterialTheme.colorScheme.error else LocalContentColor.current
                                                                )
                                                                if (teachers.isNotEmpty() && state.currentProfile?.profileType != ProfileType.TEACHER) Text(
                                                                    text = teachers.joinToString { it.name },
                                                                    style = headFont,
                                                                    color = if (lesson is Lesson.SubstitutionPlanLesson && lesson.isTeacherChanged) MaterialTheme.colorScheme.error else LocalContentColor.current
                                                                )
                                                                if (groups.isNotEmpty() && state.currentProfile?.profileType != ProfileType.STUDENT) Text(
                                                                    text = groups.joinToString { it.name },
                                                                    style = headFont
                                                                )
                                                                Spacer(Modifier.weight(1f))
                                                                Text(
                                                                    text = buildString {
                                                                        append(lessonTime.start.format(regularTimeFormat))
                                                                        append(" - ")
                                                                        append(lessonTime.end.format(regularTimeFormat))
                                                                    },
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            }
                                                            Column(
                                                                modifier = Modifier.padding(top = 4.dp),
                                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                if (lesson is Lesson.SubstitutionPlanLesson && lesson.info != null) Row {
                                                                    Icon(
                                                                        painter = painterResource(Res.drawable.info),
                                                                        contentDescription = "Information",
                                                                        modifier = Modifier
                                                                            .padding(end = 8.dp)
                                                                            .size(MaterialTheme.typography.bodyMedium.lineHeight.toDp())
                                                                    )
                                                                    Text(
                                                                        text = lesson.info,
                                                                        style = MaterialTheme.typography.bodyMedium
                                                                    )
                                                                }
                                                                if (assessmentsForLesson.isNotEmpty()) Row {
                                                                    Icon(
                                                                        painter = painterResource(Res.drawable.notebook_text),
                                                                        contentDescription = "Leistungen",
                                                                        modifier = Modifier
                                                                            .padding(end = 8.dp)
                                                                            .size(MaterialTheme.typography.bodyMedium.lineHeight.toDp())
                                                                    )
                                                                    Text(
                                                                        text = assessmentsForLesson.joinToString { it.description },
                                                                        style = MaterialTheme.typography.bodyMedium
                                                                    )
                                                                }
                                                                if (homeworkForLesson.isNotEmpty()) Row {
                                                                    Icon(
                                                                        painter = painterResource(Res.drawable.book_open),
                                                                        contentDescription = "Aufgaben",
                                                                        modifier = Modifier
                                                                            .padding(end = 8.dp)
                                                                            .size(MaterialTheme.typography.bodyMedium.lineHeight.toDp())
                                                                    )
                                                                    Column {
                                                                        homeworkForLesson.forEachIndexed forEachTask@{ i, homeworkItem ->
                                                                            val tasks = remember(homeworkItem.taskIds) { homeworkItem.tasks }.collectAsState(emptyList()).value.ifEmpty { return@forEachTask }
                                                                            if (i > 0) HorizontalDivider(Modifier.fillMaxWidth())
                                                                            tasks.forEach { task ->
                                                                                Row {
                                                                                    Box(
                                                                                        modifier = Modifier.height(MaterialTheme.typography.bodyMedium.lineHeight.toDp()),
                                                                                        contentAlignment = Alignment.Center
                                                                                    ) {
                                                                                        Icon(
                                                                                            painter = painterResource(
                                                                                                if (state.currentProfile is Profile.StudentProfile && task.isDone(state.currentProfile)) Res.drawable.check
                                                                                                else Res.drawable.minus
                                                                                            ),
                                                                                            contentDescription = null,
                                                                                            modifier = Modifier.size(min(MaterialTheme.typography.bodyMedium.fontSize.toDp(), 12.dp))
                                                                                        )
                                                                                    }
                                                                                    Spacer(Modifier.size(4.dp))
                                                                                    Text(
                                                                                        text = task.content,
                                                                                        style = MaterialTheme.typography.bodyMedium
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item readNews@{
                        val readNews = state.news.filter { it.isRead }
                        if (readNews.isEmpty()) return@readNews
                        Column {
                            FeedTitle(
                                icon = Res.drawable.megaphone,
                                title = "Alle Meldungen"
                            )
                            Spacer(Modifier.size(8.dp))
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp)),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                readNews.forEach { news ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { visibleNews = news.id }
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = news.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = news.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 4,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item bottomSpacer@{  }
                }
            }
        }
    }

    if (isNewHomeworkDrawerVisible) NewHomeworkDrawer { isNewHomeworkDrawerVisible = false }
    if (isNewAssessmentDrawerVisible) NewAssessmentDrawer { isNewAssessmentDrawerVisible = false }
    if (isFeedbackDrawerVisible) FeedbackDrawer { isFeedbackDrawerVisible = false }
    visibleNews?.let { NewsDrawer(it) { visibleNews = null } }
}

@Composable
private fun CurrentOrNextLesson(
    currentTime: LocalDateTime,
    currentLesson: Lesson,
    currentProfileType: ProfileType?,
    homework: List<Homework>,
    assessments: List<Assessment>,
    continuing: Lesson?,
    progressType: ProgressType
) {
    val iconSize = 32.dp
    val subject = remember(currentLesson.subjectInstanceId) { currentLesson.subjectInstance }?.collectAsResultingFlow()?.value
    val rooms by remember(currentLesson.roomIds) { currentLesson.rooms }.collectAsSingleFlow()
    val groups by remember(currentLesson.groupIds) { currentLesson.groups }.collectAsSingleFlow()
    val teachers by remember(currentLesson.teacherIds) { currentLesson.teachers }.collectAsSingleFlow()
    val lessonTime = remember(currentLesson) { currentLesson.lessonTime }.collectAsResultingFlow().value
    if (
        (subject == null && currentLesson.subjectInstanceId != null) ||
        (rooms.isEmpty() && currentLesson.roomIds.orEmpty().isNotEmpty()) ||
        (teachers.isEmpty() && currentLesson.teacherIds.isNotEmpty()) ||
        (groups.isEmpty() && currentLesson.groupIds.isNotEmpty()) ||
        lessonTime == null
    ) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubjectIcon(
                modifier = Modifier
                    .padding(8.dp)
                    .size(iconSize),
                subject = subject?.subject
            )
            val titleFont = MaterialTheme.typography.titleMedium
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentLesson.subject ?: "Entfall",
                        style = titleFont,
                        color = if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.isSubjectChanged) MaterialTheme.colorScheme.error else LocalContentColor.current
                    )
                    if (rooms.isNotEmpty()) Text(
                        text = rooms.joinToString { it.name },
                        style = MaterialTheme.typography.titleSmall,
                        color = if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.isRoomChanged) MaterialTheme.colorScheme.error else LocalContentColor.current
                    )
                    if (teachers.isNotEmpty() && currentProfileType != ProfileType.TEACHER) Text(
                        text = teachers.joinToString { it.name },
                        style = MaterialTheme.typography.titleSmall,
                        color = if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.isTeacherChanged) MaterialTheme.colorScheme.error else LocalContentColor.current
                    )
                    if (groups.isNotEmpty() && currentProfileType != ProfileType.STUDENT) Text(
                        text = groups.joinToString { it.name },
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    text = buildString {
                        append(lessonTime.lessonNumber)
                        append(". Stunde $DOT ")
                        append(lessonTime.start.format(regularTimeFormat))
                        append(" - ")
                        append(lessonTime.end.format(regularTimeFormat))
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        val bodyFont = MaterialTheme.typography.bodyMedium

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (continuing != null) Row {
                Icon(
                    painter = painterResource(Res.drawable.arrow_right),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(bodyFont.lineHeight.toDp())
                )
                Text(
                    text = "Weiter in ${lessonTime.lessonNumber + 1}. Stunde",
                    style = bodyFont
                )
            }
            if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.info != null) Row {
                Icon(
                    painter = painterResource(Res.drawable.info),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(bodyFont.lineHeight.toDp())
                )
                Text(
                    text = currentLesson.info,
                    style = bodyFont
                )
            }
            if (homework.isNotEmpty()) Row {
                Icon(
                    painter = painterResource(Res.drawable.book_open),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(bodyFont.lineHeight.toDp())
                )
                Text(
                    text = when(homework.size) {
                        1 -> "Eine Hausaufgabe"
                        else -> "${homework.size} Hausaufgaben"
                    },
                    style = bodyFont
                )
            }
            if (assessments.isNotEmpty()) Row {
                Icon(
                    painter = painterResource(Res.drawable.notebook_text),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(bodyFont.lineHeight.toDp())
                )
                Text(
                    text = when(assessments.size) {
                        1 -> "Eine Leistung"
                        else -> "${assessments.size} Leistungen"
                    },
                    style = bodyFont
                )
            }
        }
        if (progressType != ProgressType.Disabled) {
            Spacer(Modifier.size(4.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth((currentTime.time progressIn lessonTime.start..lessonTime.end).toFloat())
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp, 3.dp, 0.dp, 0.dp))
                    .background(MaterialTheme.colorScheme.onPrimaryContainer)
            )
        } else {
            Spacer(Modifier.size(8.dp))
        }
    }
}

enum class ProgressType {
    Disabled, Regular
}

private data class HighlightedLessons(
    val currentLessons: List<CurrentLesson>,
    val nextLesson: List<Lesson>,
    val showCurrent: Boolean
) {
    constructor(state: HomeState): this(state.currentLessons, state.nextLessons, state.currentLessons.isNotEmpty())

    val hasLessons = nextLesson.isNotEmpty() || currentLessons.isNotEmpty()
}
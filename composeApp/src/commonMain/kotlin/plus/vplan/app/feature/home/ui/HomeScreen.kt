@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.feature.home.ui


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.ProfileType
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.getByProvider
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.InformativePullToRefresh
import plus.vplan.app.core.ui.theme.CustomColor
import plus.vplan.app.core.ui.theme.colors
import plus.vplan.app.core.ui.theme.displayFontFamily
import plus.vplan.app.core.utils.date.longMonthNames
import plus.vplan.app.core.utils.date.regularDateFormatWithoutYear
import plus.vplan.app.core.utils.date.untilRelativeText
import plus.vplan.app.core.utils.ui.plus
import plus.vplan.app.feature.assessment.ui.components.create.NewAssessmentDrawer
import plus.vplan.app.feature.home.ui.components.DayInfoCard
import plus.vplan.app.feature.home.ui.components.FeedTitle
import plus.vplan.app.feature.home.ui.components.Greeting
import plus.vplan.app.feature.home.ui.components.LastUpdated
import plus.vplan.app.feature.home.ui.components.QuickActions
import plus.vplan.app.feature.homework.ui.components.NewHomeworkDrawer
import plus.vplan.app.feature.main.ui.MainScreen
import plus.vplan.app.feature.news.ui.NewsDrawer
import plus.vplan.app.feature.schulverwalter.domain.usecase.InitializeSchulverwalterReauthUseCase
import plus.vplan.app.feature.settings.page.info.ui.components.FeedbackDrawer
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.longDayOfWeekNames
import plus.vplan.app.utils.openUrl
import plus.vplan.app.utils.progressIn
import plus.vplan.app.utils.regularTimeFormat
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.transparent
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
        state = homeViewModel.state.collectAsStateWithLifecycle().value,
        contentPadding = contentPadding,
        onOpenRoomSearch = remember { { navHostController.navigate(MainScreen.RoomSearch) } },
        onOpenSchoolSettings = remember { { navHostController.navigate(MainScreen.SchoolSettings(openIndiwareSettingsSchool = it.toString())) } },
        onEvent = homeViewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeContent(
    state: HomeState,
    contentPadding: PaddingValues,
    onOpenRoomSearch: () -> Unit,
    onOpenSchoolSettings: (schoolId: Alias) -> Unit,
    onEvent: (event: HomeEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val localDensity = LocalDensity.current
    val initializeSchulverwalterReauthUseCase = koinInject<InitializeSchulverwalterReauthUseCase>()

    var isNewHomeworkDrawerVisible by rememberSaveable { mutableStateOf(false) }
    var isNewAssessmentDrawerVisible by rememberSaveable { mutableStateOf(false) }
    var isFeedbackDrawerVisible by rememberSaveable { mutableStateOf(false) }

    var visibleNews by rememberSaveable { mutableStateOf<Int?>(null) }

    val vppId = (state.currentProfile as? Profile.StudentProfile)?.vppId

    InformativePullToRefresh(
        modifier = Modifier
            .padding(bottom = contentPadding.calculateBottomPadding())
            .fillMaxSize(),
        isRefreshing = state.currentUpdateStage != null,
        onRefresh = { onEvent(HomeEvent.OnRefresh) },
        refreshingContent = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .animateContentSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = state.currentUpdateStage == HomeState.CurrentUpdateStage.Done
                    ) { isDone ->
                        if (isDone) Icon(
                            painter = painterResource(CoreUiRes.drawable.check),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        else CircularProgressIndicator(Modifier.size(24.dp))
                    }
                    Column {
                        Text(
                            text = "Aktualisieren",
                            style = MaterialTheme.typography.titleSmallEmphasized,
                            fontFamily = displayFontFamily(),
                        )
                        AnimatedContent(
                            targetState = state.currentUpdateStage ?: HomeState.CurrentUpdateStage.Done,
                            modifier = Modifier.clipToBounds(),
                            transitionSpec = {
                                fadeIn() + slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up) togetherWith
                                        fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                            }
                        ) { stage ->
                            Text(
                                text = stage.title,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    ) { innerContentPadding ->
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = state.initDone
            ) { initDone ->
                if (!initDone) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularWavyProgressIndicator()
                    }
                    return@AnimatedContent
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = contentPadding + innerContentPadding
                ) {
                    item {
                        Greeting(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            displayName = vppId?.name?.split(" ")?.first() ?: ""
                        )
                    }
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
                            visible = state.currentProfile?.school?.credentialsValid == false,
                            enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            var displaySchool by remember { mutableStateOf<School?>(null) }
                            LaunchedEffect(state.currentProfile?.school) {
                                if (state.currentProfile?.school is School.AppSchool && !state.currentProfile.school.credentialsValid) displaySchool = state.currentProfile.school
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
                                buttonAction1 = { onOpenSchoolSettings(displaySchool!!.aliases.getByProvider(AliasProvider.Sp24)!!) },
                                buttonText1 = "Aktualisieren",
                                imageVector = CoreUiRes.drawable.key_round
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
                                    openUrl(url)
                                } },
                                buttonText1 = "Aktualisieren",
                                imageVector = CoreUiRes.drawable.key_round
                            )
                        }
                    }
                    item unreadNews@{
                        val unreadNews = state.news.filter { !it.isRead }
                        if (unreadNews.isEmpty()) return@unreadNews
                        Column {
                            FeedTitle(
                                icon = CoreUiRes.drawable.megaphone,
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
                        val isYourDayToday = state.day?.day?.date == state.currentTime.date

                        if (state.day == null) return@yourDay

                        val week = state.day.day.week

                        Column {
                            FeedTitle(
                                icon = CoreUiRes.drawable.chart_no_axes_gantt,
                                title = if (isYourDayToday) "Dein Tag" else "Nächster Schultag",
                                endText = buildString {
                                    append(state.day.day.date.format(LocalDate.Format {
                                        dayOfWeek(longDayOfWeekNames)
                                        chars(", ")
                                        day(padding = Padding.ZERO)
                                        chars(". ")
                                        monthName(longMonthNames)
                                        char(' ')
                                        year()
                                    }))

                                    if (week != null) {
                                        append("\n")
                                        if (week.weekType != null) {
                                            append(week.weekType)
                                            append("-Woche ")
                                        }
                                        append("(KW")
                                        append(week.calendarWeek)
                                        append(", SW ")
                                        append(week.weekIndex)
                                        append(")")
                                    }
                                }
                            )
                            val info = state.day.day.info
                            if (info != null) repeat(20) { DayInfoCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), info = info) }
                            if (state.day.substitution.isEmpty()) InfoCard(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                imageVector = CoreUiRes.drawable.triangle_alert,
                                title = "Kein Vertretungsplan",
                                text = buildString {
                                    append("Für ")
                                    append((state.currentTime.date untilRelativeText state.day.day.date) ?: "den ${state.day.day.date.format(regularDateFormatWithoutYear)}")
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
                                                        val homeworkForLesson =
                                                            remember { mutableListOf<Homework>() }
                                                        LaunchedEffect(
                                                            state.day.homework,
                                                            currentLesson.subjectInstance?.id
                                                        ) {
                                                            homeworkForLesson.clear()
                                                            homeworkForLesson.addAll(state.day.homework.filter { homework -> homework.subjectInstance != null && homework.subjectInstance?.id == currentLesson.subjectInstance?.id })
                                                        }

                                                        val assessmentsForLesson =
                                                            remember { mutableListOf<Assessment>() }
                                                        LaunchedEffect(
                                                            state.day.assessments,
                                                            currentLesson.subjectInstance?.id
                                                        ) {
                                                            assessmentsForLesson.clear()
                                                            assessmentsForLesson.addAll(state.day.assessments.filter { assessment -> assessment.subjectInstance.id == currentLesson.subjectInstance?.id })
                                                        }

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
                                        } else {
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
                                                        val homeworkForLesson = remember { mutableListOf<Homework>() }
                                                        LaunchedEffect(state.day.homework, (lesson as? Lesson.SubstitutionPlanLesson)?.subjectInstance?.id) {
                                                            homeworkForLesson.clear()
                                                            homeworkForLesson.addAll(state.day.homework.filter { homework -> homework.subjectInstance != null && homework.subjectInstance?.id == lesson.subjectInstance?.id })
                                                        }

                                                        val assessmentsForLesson = remember { mutableListOf<Assessment>() }
                                                        LaunchedEffect(state.day.assessments, lesson.subjectInstance?.id) {
                                                            assessmentsForLesson.clear()
                                                            assessmentsForLesson.addAll(state.day.assessments.filter { assessment -> assessment.subjectInstance.id == lesson.subjectInstance?.id })
                                                        }

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
                                                                    text = lesson.subject ?: "${lesson.subjectInstance?.subject?.plus(" ").orEmpty()}Entfall",
                                                                    style = headFont,
                                                                    color = if (lesson is Lesson.SubstitutionPlanLesson && lesson.isSubjectChanged) MaterialTheme.colorScheme.error else LocalContentColor.current
                                                                )
                                                                if (lesson.rooms.orEmpty().isNotEmpty()) Text(
                                                                    text = lesson.rooms.orEmpty().joinToString { it.name },
                                                                    style = headFont,
                                                                    color = if (lesson is Lesson.SubstitutionPlanLesson && lesson.isRoomChanged) MaterialTheme.colorScheme.error else LocalContentColor.current
                                                                )
                                                                if (lesson.teachers.isNotEmpty() && state.currentProfile?.profileType != ProfileType.TEACHER) Text(
                                                                    text = lesson.teachers.joinToString { it.name },
                                                                    style = headFont,
                                                                    color = if (lesson is Lesson.SubstitutionPlanLesson && lesson.isTeacherChanged) MaterialTheme.colorScheme.error else LocalContentColor.current
                                                                )
                                                                if (lesson.groups.isNotEmpty() && state.currentProfile?.profileType != ProfileType.STUDENT) Text(
                                                                    text = lesson.groups.joinToString { it.name },
                                                                    style = headFont
                                                                )
                                                                if (lesson.lessonTime != null) {
                                                                    Spacer(Modifier.weight(1f))
                                                                    Text(
                                                                        text = buildString {
                                                                            append(lesson.lessonTime!!.start.format(regularTimeFormat))
                                                                            append(" - ")
                                                                            append(lesson.lessonTime!!.end.format(regularTimeFormat))
                                                                        },
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = if (lesson.lessonTime!!.interpolated) MaterialTheme.colorScheme.primary
                                                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                }
                                                            }
                                                            Column(
                                                                modifier = Modifier.padding(top = 4.dp),
                                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                if (lesson is Lesson.SubstitutionPlanLesson && lesson.info != null) Row {
                                                                    Icon(
                                                                        painter = painterResource(CoreUiRes.drawable.info),
                                                                        contentDescription = "Information",
                                                                        modifier = Modifier
                                                                            .padding(end = 8.dp)
                                                                            .size(MaterialTheme.typography.bodyMedium.lineHeight.toDp())
                                                                    )
                                                                    Text(
                                                                        text = lesson.info!!,
                                                                        style = MaterialTheme.typography.bodyMedium
                                                                    )
                                                                }
                                                                if (lesson is Lesson.TimetableLesson && lesson.limitedToWeeks != null) Row {
                                                                    Icon(
                                                                        painter = painterResource(CoreUiRes.drawable.info),
                                                                        contentDescription = "Information",
                                                                        modifier = Modifier
                                                                            .padding(end = 8.dp)
                                                                            .size(MaterialTheme.typography.bodyMedium.lineHeight.toDp())
                                                                    )
                                                                    if (lesson.limitedToWeeks!!.isNotEmpty()) Text(
                                                                        text = if (lesson.limitedToWeeks!!.size == 1) "Nur in Schulwoche ${lesson.limitedToWeeks!!.first()}"
                                                                                else "Nur in Schulwochen ${lesson.limitedToWeeks!!.map { it.weekIndex }.sorted().dropLast(1).joinToString()} und ${lesson.limitedToWeeks!!.map { it.weekIndex }.maxOf { it }}",
                                                                        style = MaterialTheme.typography.bodyMedium
                                                                    )
                                                                }
                                                                if (lesson.lessonTime?.interpolated == true) {
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        verticalAlignment = Alignment.Top,
                                                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                                    ) info@{
                                                                        Icon(
                                                                            painter = painterResource(CoreUiRes.drawable.clock_fading),
                                                                            modifier = Modifier
                                                                                .padding(end = 2.dp)
                                                                                .size(MaterialTheme.typography.bodyMedium.lineHeight.toDp()),
                                                                            contentDescription = null,
                                                                            tint = MaterialTheme.colorScheme.primary
                                                                        )
                                                                        Text(
                                                                            text = "Stundenzeit automatisch berechnet",
                                                                            style = MaterialTheme.typography.bodyMedium,
                                                                            color = MaterialTheme.colorScheme.primary
                                                                        )
                                                                    }
                                                                }
                                                                if (assessmentsForLesson.isNotEmpty()) Row {
                                                                    Icon(
                                                                        painter = painterResource(CoreUiRes.drawable.notebook_text),
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
                                                                        painter = painterResource(CoreUiRes.drawable.book_open),
                                                                        contentDescription = "Aufgaben",
                                                                        modifier = Modifier
                                                                            .padding(end = 8.dp)
                                                                            .size(MaterialTheme.typography.bodyMedium.lineHeight.toDp())
                                                                    )
                                                                    Column {
                                                                        homeworkForLesson.forEachIndexed forEachTask@{ i, homeworkItem ->
                                                                            if (i > 0) HorizontalDivider(Modifier.fillMaxWidth())
                                                                            homeworkItem.tasks.forEach { task ->
                                                                                Row {
                                                                                    Box(
                                                                                        modifier = Modifier.height(MaterialTheme.typography.bodyMedium.lineHeight.toDp()),
                                                                                        contentAlignment = Alignment.Center
                                                                                    ) {
                                                                                        Icon(
                                                                                            painter = painterResource(
                                                                                                if (state.currentProfile is Profile.StudentProfile && task.isDone(state.currentProfile)) CoreUiRes.drawable.check
                                                                                                else CoreUiRes.drawable.minus
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

                                    if (state.hasInterpolatedLessonTimes) {
                                        Spacer(Modifier.size(8.dp))
                                        InfoCard(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            imageVector = CoreUiRes.drawable.clock_fading,
                                            title = "Ungültige Stundenzeiten",
                                            text = "Deine Schule stellt nicht für alle Stunden gültige Stundenzeiten zur Verfügung. Einige Stundenzeiten wurden anhand der gegebenen berechnet und können daher falsch sein. Bald kannst du selber Stundenzeiten hinzufügen.",
                                            backgroundColor = colors[CustomColor.Yellow]!!.getGroup().container,
                                            textColor = colors[CustomColor.Yellow]!!.getGroup().onContainer
                                        )
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
                                icon = CoreUiRes.drawable.megaphone,
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
                    if (state.lastPlanUpdate != null) item {
                        LastUpdated(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            currentTime = state.currentTime.toInstant(TimeZone.currentSystemDefault()),
                            lastUpdated = state.lastPlanUpdate
                        )
                    }
                    item bottomSpacer@{  }
                }
            }
        }
    }

    if (isNewHomeworkDrawerVisible) NewHomeworkDrawer { isNewHomeworkDrawerVisible = false }
    if (isNewAssessmentDrawerVisible) NewAssessmentDrawer { isNewAssessmentDrawerVisible = false }
    if (isFeedbackDrawerVisible) FeedbackDrawer(null) { isFeedbackDrawerVisible = false }
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
                subject = currentLesson.subjectInstance?.subject
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
                    if (currentLesson.rooms.orEmpty().isNotEmpty()) Text(
                        text = currentLesson.rooms.orEmpty().joinToString { it.name },
                        style = MaterialTheme.typography.titleSmall,
                        color = if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.isRoomChanged) MaterialTheme.colorScheme.error else LocalContentColor.current
                    )
                    if (currentLesson.teachers.isNotEmpty() && currentProfileType != ProfileType.TEACHER) Text(
                        text = currentLesson.teachers.joinToString { it.name },
                        style = MaterialTheme.typography.titleSmall,
                        color = if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.isTeacherChanged) MaterialTheme.colorScheme.error else LocalContentColor.current
                    )
                    if (currentLesson.groups.isNotEmpty() && currentProfileType != ProfileType.STUDENT) Text(
                        text = currentLesson.groups.joinToString { it.name },
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    text = buildString {
                        append(currentLesson.lessonNumber)
                        append(". Stunde")
                        if (currentLesson.lessonTime != null) {
                            append(" $DOT ")
                            append(currentLesson.lessonTime!!.start.format(regularTimeFormat))
                            append(" - ")
                            append(currentLesson.lessonTime!!.end.format(regularTimeFormat))
                        }
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
                    painter = painterResource(CoreUiRes.drawable.arrow_right),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(bodyFont.lineHeight.toDp())
                )
                Text(
                    text = "Weiter in ${currentLesson.lessonNumber + 1}. Stunde",
                    style = bodyFont
                )
            }
            if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.info != null) Row {
                Icon(
                    painter = painterResource(CoreUiRes.drawable.info),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(bodyFont.lineHeight.toDp())
                )
                Text(
                    text = currentLesson.info!!,
                    style = bodyFont
                )
            }
            if (currentLesson.lessonTime?.interpolated == true) Row {
                Icon(
                    painter = painterResource(CoreUiRes.drawable.clock_fading),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(bodyFont.lineHeight.toDp())
                )
                Text(
                    text = "Stundenzeit fehlt und wurde automatisch berechnet. Kann von den tatsächlichen Zeiten abweichen.",
                    style = bodyFont
                )
            }
            if (homework.isNotEmpty()) Row {
                Icon(
                    painter = painterResource(CoreUiRes.drawable.book_open),
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
                    painter = painterResource(CoreUiRes.drawable.notebook_text),
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
        if (progressType != ProgressType.Disabled && currentLesson.lessonTime != null) {
            Spacer(Modifier.size(4.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth((currentTime.time progressIn currentLesson.lessonTime!!.start..currentLesson.lessonTime!!.end).toFloat())
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
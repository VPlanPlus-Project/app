package plus.vplan.app.feature.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.collectAsLoadingState
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.cache.collectAsSingleFlow
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
import plus.vplan.app.feature.schulverwalter.domain.usecase.InitializeSchulverwalterReauthUseCase
import plus.vplan.app.feature.settings.page.info.ui.components.FeedbackDrawer
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.BrowserIntent
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.longDayOfWeekNames
import plus.vplan.app.utils.longMonthNames
import plus.vplan.app.utils.progressIn
import plus.vplan.app.utils.regularDateFormatWithoutYear
import plus.vplan.app.utils.regularTimeFormat
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.untilRelativeText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_right
import vplanplus.composeapp.generated.resources.chart_no_axes_gantt
import vplanplus.composeapp.generated.resources.info
import vplanplus.composeapp.generated.resources.key_round
import vplanplus.composeapp.generated.resources.triangle_alert

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeState,
    contentPadding: PaddingValues,
    onOpenRoomSearch: () -> Unit,
    onOpenSchoolSettings: (schoolId: Int) -> Unit,
    onEvent: (event: HomeEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    val initializeSchulverwalterReauthUseCase = koinInject<InitializeSchulverwalterReauthUseCase>()

    var isNewHomeworkDrawerVisible by rememberSaveable { mutableStateOf(false) }
    var isNewAssessmentDrawerVisible by rememberSaveable { mutableStateOf(false) }
    var isFeedbackDrawerVisible by rememberSaveable { mutableStateOf(false) }

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
                val vppId = (state.currentProfile as? Profile.StudentProfile)?.vppId?.collectAsResultingFlow()?.value
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

                val school = (state.currentProfile)?.getSchool()?.filterIsInstance<CacheState.Done<School>>()?.map { it.data }?.collectAsState(null)?.value

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
                        val vppId = ((state.currentProfile as? Profile.StudentProfile)?.vppId?.collectAsResultingFlow()?.value as? VppId.Active)
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
                    item yourDay@{
                        val isYourDayToday = state.day?.date == state.currentTime.date
                        val lessons = state.day?.lessons?.collectAsState(null)?.value.orEmpty()
                        val weekState = state.day?.week?.collectAsLoadingState("")?.value
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
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth()
                            ) {
                                if (state.currentLessons.isNotEmpty()) {
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
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        val iconSize = 32.dp
                                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer) {
                                            state.currentLessons.forEachIndexed { i, (currentLesson, continuing) ->
                                                val subject = currentLesson.subjectInstance?.collectAsResultingFlow()?.value
                                                val rooms by currentLesson.rooms.collectAsSingleFlow()
                                                val groups by currentLesson.groups.collectAsSingleFlow()
                                                val teachers by currentLesson.teachers.collectAsSingleFlow()
                                                val lessonTime = currentLesson.lessonTime.collectAsResultingFlow().value
                                                if (
                                                    (subject == null && currentLesson.subjectInstanceId != null) ||
                                                    (rooms.isEmpty() && currentLesson.roomIds.orEmpty().isNotEmpty()) ||
                                                    (teachers.isEmpty() && currentLesson.teacherIds.isNotEmpty()) ||
                                                    (groups.isEmpty() && currentLesson.groupIds.isNotEmpty()) ||
                                                    lessonTime == null
                                                ) return@forEachIndexed
                                                Row {
                                                    SubjectIcon(
                                                        modifier = Modifier
                                                            .padding(8.dp)
                                                            .size(iconSize),
                                                        subject = subject?.subject
                                                    )
                                                    val titleFont = MaterialTheme.typography.titleMedium
                                                    Column(
                                                        modifier = Modifier
                                                            .padding(top = (iconSize - titleFont.lineHeight.toDp())/2 + 8.dp)
                                                    ) {
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
                                                            if (teachers.isNotEmpty() && state.currentProfile?.profileType != ProfileType.TEACHER) Text(
                                                                text = teachers.joinToString { it.name },
                                                                style = MaterialTheme.typography.titleSmall,
                                                                color = if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.isTeacherChanged) MaterialTheme.colorScheme.error else LocalContentColor.current
                                                            )
                                                            if (groups.isNotEmpty() && state.currentProfile?.profileType != ProfileType.STUDENT) Text(
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
                                                        val bodyFont = MaterialTheme.typography.bodyMedium

                                                        Column(
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
                                                        }
                                                    }
                                                }
                                                Spacer(Modifier.size(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .padding(horizontal = 8.dp)
                                                        .fillMaxWidth((state.currentTime.time progressIn lessonTime.start..lessonTime.end).toFloat())
                                                        .height(3.dp)
                                                        .thenIf(Modifier.clip(RoundedCornerShape(3.dp, 3.dp, 0.dp, 0.dp))) { i == state.currentLessons.lastIndex }
                                                        .background(MaterialTheme.colorScheme.onPrimaryContainer)
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

    if (isNewHomeworkDrawerVisible) NewHomeworkDrawer { isNewHomeworkDrawerVisible = false }
    if (isNewAssessmentDrawerVisible) NewAssessmentDrawer { isNewAssessmentDrawerVisible = false }
    if (isFeedbackDrawerVisible) FeedbackDrawer { isFeedbackDrawerVisible = false }
}
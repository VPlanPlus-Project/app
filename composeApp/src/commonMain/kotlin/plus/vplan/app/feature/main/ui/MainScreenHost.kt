package plus.vplan.app.feature.main.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutQuint
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.StartTask
import plus.vplan.app.VPP_ID_AUTH_URL
import plus.vplan.app.domain.model.School
import plus.vplan.app.feature.assessment.ui.components.detail.AssessmentDetailDrawer
import plus.vplan.app.feature.calendar.ui.CalendarEvent
import plus.vplan.app.feature.calendar.ui.CalendarScreen
import plus.vplan.app.feature.calendar.ui.CalendarViewModel
import plus.vplan.app.feature.dev.ui.DevScreen
import plus.vplan.app.feature.grades.page.analytics.ui.AnalyticsScreen
import plus.vplan.app.feature.grades.page.detail.ui.GradeDetailDrawer
import plus.vplan.app.feature.grades.page.view.ui.GradesScreen
import plus.vplan.app.feature.home.ui.HomeScreen
import plus.vplan.app.feature.home.ui.HomeViewModel
import plus.vplan.app.feature.homework.ui.components.detail.HomeworkDetailDrawer
import plus.vplan.app.feature.profile.page.ui.ProfileScreen
import plus.vplan.app.feature.profile.page.ui.ProfileScreenEvent
import plus.vplan.app.feature.profile.page.ui.ProfileViewModel
import plus.vplan.app.feature.profile.page.ui.components.ProfileSwitcher
import plus.vplan.app.feature.profile.settings.page.main.ui.ProfileSettingsScreen
import plus.vplan.app.feature.profile.settings.page.subject_instances.ui.components.ProfileSubjectInstanceScreen
import plus.vplan.app.feature.search.subfeature.room_search.ui.RoomSearch
import plus.vplan.app.feature.search.ui.main.SearchScreen
import plus.vplan.app.feature.search.ui.main.SearchViewModel
import plus.vplan.app.feature.settings.page.info.ui.InfoScreen
import plus.vplan.app.feature.settings.page.school.ui.SchoolSettingsScreen
import plus.vplan.app.feature.settings.page.security.ui.SecuritySettingsScreen
import plus.vplan.app.feature.settings.ui.SettingsScreen
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateNewsUseCase
import plus.vplan.app.isDeveloperMode
import plus.vplan.app.utils.BrowserIntent
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.bug_play
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.house
import vplanplus.composeapp.generated.resources.search
import vplanplus.composeapp.generated.resources.user
import kotlin.uuid.Uuid

const val ANIMATION_DURATION = 150
val easingMove = EaseInOut
val easingFade = EaseInOutQuint

val defaultEnterAnimation: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    slideInHorizontally(
        animationSpec = tween(ANIMATION_DURATION, easing = easingMove),
    ) { it/10 } + fadeIn(animationSpec = tween(ANIMATION_DURATION/2, easing = easingFade, delayMillis = ANIMATION_DURATION/2))
}

val defaultPopExitAnimation: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    slideOutHorizontally(
        animationSpec = tween(ANIMATION_DURATION, easing = easingMove),
    ) { it/10 } + fadeOut(animationSpec = tween(ANIMATION_DURATION, easing = easingFade))
}

val defaultPopEnterAnimation: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    slideInHorizontally(
        animationSpec = tween(ANIMATION_DURATION, easing = easingMove),
    ) { -it/10 }
}

val defaultExitAnimation: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    slideOutHorizontally(
        animationSpec = tween(ANIMATION_DURATION, easing = easingMove),
    ) { -it/10 } + fadeOut(animationSpec = tween(ANIMATION_DURATION/2, easing = easingFade, delayMillis = ANIMATION_DURATION/2))
}

val defaultMainEnterAnimation: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    fadeIn(animationSpec = tween(100))
}

val defaultMainExitAnimation: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    fadeOut(animationSpec = tween(100))
}

@Composable
fun MainScreenHost(
    onNavigateToOnboarding: (school: School?) -> Unit,
    contentPaddingDevice: PaddingValues,
    navigationTask: StartTask?
) {
    val navController = rememberNavController()
    var currentDestination by rememberSaveable<MutableState<String?>> { mutableStateOf("Home") }
    navController.addOnDestinationChangedListener { _, destination, _ ->
        currentDestination =
            if (destination.route.orEmpty().startsWith(MainScreen.MainHome::class.qualifiedName ?: "__")) "_Home"
            else if (destination.route.orEmpty().startsWith(MainScreen.MainCalendar::class.qualifiedName ?: "__")) "_Calendar"
            else if (destination.route.orEmpty().startsWith(MainScreen.MainSearch::class.qualifiedName ?: "__")) "_Search"
            else if (destination.route.orEmpty().startsWith(MainScreen.MainDev::class.qualifiedName ?: "__")) "_Dev"
            else if (destination.route.orEmpty().startsWith(MainScreen.MainProfile::class.qualifiedName ?: "__")) "_Profile"
            else null
    }

    val syncNewsUseCase = koinInject<UpdateNewsUseCase>()
    LaunchedEffect(Unit) { syncNewsUseCase() }

    val homeViewModel = koinViewModel<HomeViewModel>()
    val calendarViewModel = koinViewModel<CalendarViewModel>()
    val searchViewModel = koinViewModel<SearchViewModel>()
    val profileViewModel = koinViewModel<ProfileViewModel>()

    val localDensity = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    val contentPadding = PaddingValues(
        start = contentPaddingDevice.calculateStartPadding(layoutDirection),
        top = contentPaddingDevice.calculateTopPadding(),
        end = contentPaddingDevice.calculateEndPadding(layoutDirection),
        bottom = listOf(contentPaddingDevice.calculateBottomPadding(), bottomBarHeight).max()
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = currentDestination?.startsWith("_") == true,
            enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged {
                    with(localDensity) { bottomBarHeight = it.height.toDp() }
                }
        ) {
            NavigationBar(
                modifier = Modifier
                    .shadow(elevation = 4.dp)
            ) {
                NavigationBarItem(
                    selected = currentDestination == "_Home",
                    label = { Text("Home") },
                    icon = { Icon(painter = painterResource(Res.drawable.house), contentDescription = null, modifier = Modifier.size(20.dp)) },
                    onClick = { navController.navigate(MainScreen.MainHome) { popUpTo(0) } }
                )
                NavigationBarItem(
                    selected = currentDestination == "_Calendar",
                    label = { Text("Kalender") },
                    icon = { Icon(painter = painterResource(Res.drawable.calendar), contentDescription = null, modifier = Modifier.size(20.dp)) },
                    onClick = { navController.navigate(MainScreen.MainCalendar) { popUpTo(MainScreen.MainHome) } }
                )
                NavigationBarItem(
                    selected = currentDestination == "_Search",
                    label = { Text("Suche") },
                    icon = { Icon(painter = painterResource(Res.drawable.search), contentDescription = null, modifier = Modifier.size(20.dp)) },
                    onClick = { navController.navigate(MainScreen.MainSearch) { popUpTo(MainScreen.MainHome) } }
                )
                if (isDeveloperMode) NavigationBarItem(
                    selected = currentDestination == "_Dev",
                    label = { Text("Dev") },
                    icon = { Icon(painter = painterResource(Res.drawable.bug_play), contentDescription = null, modifier = Modifier.size(20.dp)) },
                    onClick = { navController.navigate(MainScreen.MainDev) { popUpTo(MainScreen.MainHome) } }
                )
                NavigationBarItem(
                    selected = currentDestination == "_Profile",
                    label = { Text("Profil") },
                    icon = { Icon(painter = painterResource(Res.drawable.user), contentDescription = null, modifier = Modifier.size(20.dp)) },
                    onClick = { navController.navigate(MainScreen.MainProfile) { popUpTo(MainScreen.MainHome) } },
                )
            }
        }

        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = MainScreen.MainHome
        ) {
            composable<MainScreen.MainHome>(
                enterTransition = defaultMainEnterAnimation,
                exitTransition = defaultMainExitAnimation,
                popEnterTransition = defaultMainEnterAnimation,
                popExitTransition = defaultMainExitAnimation
            ) { HomeScreen(navController, contentPadding, homeViewModel) }
            composable<MainScreen.MainCalendar>(
                enterTransition = defaultMainEnterAnimation,
                exitTransition = defaultMainExitAnimation,
                popEnterTransition = defaultMainEnterAnimation,
                popExitTransition = defaultMainExitAnimation
            ) { CalendarScreen(contentPadding, calendarViewModel) }
            composable<MainScreen.MainSearch>(
                enterTransition = defaultMainEnterAnimation,
                exitTransition = defaultMainExitAnimation,
                popEnterTransition = defaultMainEnterAnimation,
                popExitTransition = defaultMainExitAnimation
            ) { SearchScreen(navController, contentPadding, searchViewModel) }
            composable<MainScreen.MainDev>(
                enterTransition = defaultMainEnterAnimation,
                exitTransition = defaultMainExitAnimation,
                popEnterTransition = defaultMainEnterAnimation,
                popExitTransition = defaultMainExitAnimation
            ) { DevScreen(contentPadding) }
            composable<MainScreen.MainProfile>(
                enterTransition = defaultMainEnterAnimation,
                exitTransition = defaultMainExitAnimation,
                popEnterTransition = defaultMainEnterAnimation,
                popExitTransition = defaultMainExitAnimation
            ) { ProfileScreen(navController, contentPadding, profileViewModel) }

            composable<MainScreen.ProfileSettings>(
                enterTransition = defaultEnterAnimation,
                exitTransition = defaultExitAnimation,
                popEnterTransition = defaultPopEnterAnimation,
                popExitTransition = defaultPopExitAnimation
            ) {
                val args = it.toRoute<MainScreen.ProfileSettings>()
                ProfileSettingsScreen(args.profileId, navController)
            }
            composable<MainScreen.ProfileSubjectInstances>(
                enterTransition = defaultEnterAnimation,
                exitTransition = defaultExitAnimation,
                popEnterTransition = defaultPopEnterAnimation,
                popExitTransition = defaultPopExitAnimation
            ) {
                val args = it.toRoute<MainScreen.ProfileSubjectInstances>()
                ProfileSubjectInstanceScreen(Uuid.parse(args.profileId), navController)
            }

            composable<MainScreen.RoomSearch>(
                enterTransition = defaultEnterAnimation,
                exitTransition = defaultExitAnimation,
                popEnterTransition = defaultPopEnterAnimation,
                popExitTransition = defaultPopExitAnimation
            ) { RoomSearch(navController) }

            composable<MainScreen.Settings>(
                enterTransition = defaultEnterAnimation,
                exitTransition = defaultExitAnimation,
                popEnterTransition = defaultPopEnterAnimation,
                popExitTransition = defaultPopExitAnimation
            ) { SettingsScreen(navController) }
            composable<MainScreen.SchoolSettings>(
                enterTransition = defaultEnterAnimation,
                exitTransition = defaultExitAnimation,
                popEnterTransition = defaultPopEnterAnimation,
                popExitTransition = defaultPopExitAnimation
            ) {
                val args = it.toRoute<MainScreen.SchoolSettings>()
                SchoolSettingsScreen(
                    navHostController = navController,
                    openIndiwareSettingsSchoolId = args.openIndiwareSettingsSchoolId
                )
            }
            composable<MainScreen.SecuritySettings>(
                enterTransition = defaultEnterAnimation,
                exitTransition = defaultExitAnimation,
                popEnterTransition = defaultPopEnterAnimation,
                popExitTransition = defaultPopExitAnimation
            ) {
                SecuritySettingsScreen(
                    navHostController = navController
                )
            }
            composable<MainScreen.InfoFeedbackSettings>(
                enterTransition = defaultEnterAnimation,
                exitTransition = defaultExitAnimation,
                popEnterTransition = defaultPopEnterAnimation,
                popExitTransition = defaultPopExitAnimation
            ) {
                InfoScreen(navController)
            }

            composable<MainScreen.Grades>(
                enterTransition = defaultEnterAnimation,
                exitTransition = defaultExitAnimation,
                popEnterTransition = defaultPopEnterAnimation,
                popExitTransition = defaultPopExitAnimation
            ) {
                val args = it.toRoute<MainScreen.Grades>()
                GradesScreen(navController, args.vppId)
            }
            composable<MainScreen.Analytics>(
                enterTransition = defaultEnterAnimation,
                exitTransition = defaultExitAnimation,
                popEnterTransition = defaultPopEnterAnimation,
                popExitTransition = defaultPopExitAnimation
            ) {
                val args = it.toRoute<MainScreen.Analytics>()
                AnalyticsScreen(navController, args.vppId)
            }
        }
    }

    if (profileViewModel.state.isSheetVisible) {
        val activeProfile = profileViewModel.state.currentProfile
        if (activeProfile != null) ProfileSwitcher(
            profiles = profileViewModel.state.profiles,
            showVppIdBanner = profileViewModel.state.showVppIdBanner,
            activeProfile = activeProfile,
            onSelectProfile = { profileViewModel.onEvent(ProfileScreenEvent.SetActiveProfile(it)) },
            onDismiss = { profileViewModel.onEvent(ProfileScreenEvent.SetProfileSwitcherVisibility(false)) },
            onCreateNewProfile = onNavigateToOnboarding,
            onConnectVppId = { BrowserIntent.openUrl(VPP_ID_AUTH_URL) },
            onOpenProfileSettings = {
                navController.navigate(MainScreen.ProfileSettings(activeProfile.id.toString()))
            }
        )
    }

    var homeworkSheetHomeworkId by rememberSaveable { mutableStateOf<Int?>(null) }
    var assessmentSheetAssessmentId by rememberSaveable { mutableStateOf<Int?>(null) }
    var gradeSheetGradeId by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(navigationTask) {
        if (navigationTask == null) return@LaunchedEffect
        when (navigationTask) {
            is StartTask.NavigateTo.Calendar -> {
                calendarViewModel.onEvent(CalendarEvent.SelectDate(navigationTask.date))
                navController.navigate(MainScreen.MainCalendar)
            }
            is StartTask.NavigateTo.SchoolSettings -> {
                navController.navigate(MainScreen.SchoolSettings(navigationTask.openIndiwareSettingsSchoolId))
            }
            is StartTask.NavigateTo.Grades -> navController.navigate(MainScreen.Grades(navigationTask.vppId))
            is StartTask.Open.Homework -> homeworkSheetHomeworkId = navigationTask.homeworkId
            is StartTask.Open.Assessment -> assessmentSheetAssessmentId = navigationTask.assessmentId
            is StartTask.Open.Grade -> gradeSheetGradeId = navigationTask.gradeId

            else -> Unit
        }
    }

    if (homeworkSheetHomeworkId != null) HomeworkDetailDrawer(
        homeworkId = homeworkSheetHomeworkId!!,
        onDismiss = { homeworkSheetHomeworkId = null }
    )

    if (assessmentSheetAssessmentId != null) AssessmentDetailDrawer(
        assessmentId = assessmentSheetAssessmentId!!,
        onDismiss = { assessmentSheetAssessmentId = null }
    )

    if (gradeSheetGradeId != null) GradeDetailDrawer(
        gradeId = gradeSheetGradeId!!,
        onDismiss = { gradeSheetGradeId = null }
    )
}

/**
 * @param name The name of the screen, used for the route. If it starts with an underscore, the bottom bar will be shown.
 */
@Serializable
sealed class MainScreen(val name: String) {
    @Serializable data object MainHome : MainScreen("_Home")
    @Serializable data object MainCalendar : MainScreen("_Calendar")
    @Serializable data object MainSearch : MainScreen("_Search")
    @Serializable data object MainDev : MainScreen("_Dev")
    @Serializable data object MainProfile : MainScreen("_Profile")

    @Serializable data class ProfileSettings(val profileId: String) : MainScreen("ProfileSettings")
    @Serializable data class ProfileSubjectInstances(val profileId: String) : MainScreen("ProfileSubjectInstances")

    @Serializable data object RoomSearch : MainScreen("RoomSearch")

    @Serializable data object Settings : MainScreen("Settings")
    @Serializable data class SchoolSettings(val openIndiwareSettingsSchoolId: Int? = null) : MainScreen("SchoolSettings")
    @Serializable data object SecuritySettings : MainScreen("SecuritySettings")
    @Serializable data object InfoFeedbackSettings : MainScreen("InfoFeedbackSettings")

    @Serializable data class Grades(val vppId: Int) : MainScreen("Grades")
    @Serializable data class Analytics(val vppId: Int) : MainScreen("Analytics")
}
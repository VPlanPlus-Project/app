package plus.vplan.app.feature.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.StartTask
import plus.vplan.app.VPP_ID_AUTH_URL
import plus.vplan.app.domain.model.School
import plus.vplan.app.feature.calendar.ui.CalendarEvent
import plus.vplan.app.feature.calendar.ui.CalendarScreen
import plus.vplan.app.feature.calendar.ui.CalendarViewModel
import plus.vplan.app.feature.dev.ui.DevScreen
import plus.vplan.app.feature.home.ui.HomeScreen
import plus.vplan.app.feature.home.ui.HomeViewModel
import plus.vplan.app.feature.profile.page.ui.ProfileScreen
import plus.vplan.app.feature.profile.page.ui.ProfileScreenEvent
import plus.vplan.app.feature.profile.page.ui.ProfileViewModel
import plus.vplan.app.feature.profile.page.ui.components.ProfileSwitcher
import plus.vplan.app.feature.profile.settings.ui.ProfileSettingsScreen
import plus.vplan.app.feature.search.subfeature.room_search.ui.RoomSearch
import plus.vplan.app.feature.search.ui.main.SearchScreen
import plus.vplan.app.feature.search.ui.main.SearchViewModel
import plus.vplan.app.isDeveloperMode
import plus.vplan.app.utils.BrowserIntent
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.bug_play
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.house
import vplanplus.composeapp.generated.resources.search
import vplanplus.composeapp.generated.resources.user

@Composable
fun MainScreenHost(
    onNavigateToOnboarding: (school: School?) -> Unit,
    navigationTask: StartTask.NavigateTo?
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

    val homeViewModel = koinViewModel<HomeViewModel>()
    val calendarViewModel = koinViewModel<CalendarViewModel>()
    val searchViewModel = koinViewModel<SearchViewModel>()
    val profileViewModel = koinViewModel<ProfileViewModel>()

    val localDensity = LocalDensity.current
    val localLayoutDirection = LocalLayoutDirection.current

    var isBottomBarVisible by rememberSaveable { mutableStateOf(true) }
    val toggleBottomBar = remember<(Boolean) -> Unit> { { isBottomBarVisible = it } }
    var bottomBarHeight by remember { mutableStateOf(0.dp) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
        val left = WindowInsets.systemBars.asPaddingValues().calculateLeftPadding(localLayoutDirection)
        val right = WindowInsets.systemBars.asPaddingValues().calculateRightPadding(localLayoutDirection)
        val bottom = bottomBarHeight.let { if (it == 0.dp) WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() else it }
        val contentPadding = PaddingValues(left, top, right, bottom)
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = MainScreen.MainHome
        ) {
            composable<MainScreen.MainHome> { HomeScreen(contentPadding, homeViewModel) }
            composable<MainScreen.MainCalendar> { CalendarScreen(navController, contentPadding, calendarViewModel) }
            composable<MainScreen.MainSearch> { SearchScreen(navController, contentPadding, searchViewModel, toggleBottomBar) }
            composable<MainScreen.MainDev> { DevScreen(contentPadding, toggleBottomBar) }
            composable<MainScreen.MainProfile> { ProfileScreen(contentPadding, profileViewModel) }

            composable<MainScreen.ProfileSettings> {
                val args = it.toRoute<MainScreen.ProfileSettings>()
                ProfileSettingsScreen(args.profileId, navController)
            }

            composable<MainScreen.RoomSearch> { RoomSearch(navController) }
        }

        AnimatedVisibility(
            visible = currentDestination?.startsWith("_") == true && isBottomBarVisible,
            enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            NavigationBar(
                modifier = Modifier
                    .onSizeChanged {
                        with(localDensity) { bottomBarHeight = it.height.toDp() }
                    }
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

    LaunchedEffect(navigationTask) {
        if (navigationTask == null) return@LaunchedEffect
        when (navigationTask) {
            is StartTask.NavigateTo.Calendar -> {
                calendarViewModel.onEvent(CalendarEvent.SelectDate(navigationTask.date))
                navController.navigate(MainScreen.MainCalendar)
            }
        }
    }
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
    @Serializable data object RoomSearch : MainScreen("RoomSearch")
}
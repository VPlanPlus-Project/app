package plus.vplan.app.feature.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.VPP_ID_AUTH_URL
import plus.vplan.app.domain.model.School
import plus.vplan.app.feature.calendar.ui.CalendarScreen
import plus.vplan.app.feature.calendar.ui.CalendarViewModel
import plus.vplan.app.feature.home.ui.HomeScreen
import plus.vplan.app.feature.home.ui.HomeViewModel
import plus.vplan.app.feature.profile.page.ui.ProfileScreen
import plus.vplan.app.feature.profile.page.ui.ProfileScreenEvent
import plus.vplan.app.feature.profile.page.ui.ProfileViewModel
import plus.vplan.app.feature.profile.page.ui.components.ProfileSwitcher
import plus.vplan.app.feature.profile.settings.ui.ProfileSettingsScreen
import plus.vplan.app.utils.BrowserIntent
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.house
import vplanplus.composeapp.generated.resources.message_square
import vplanplus.composeapp.generated.resources.search
import vplanplus.composeapp.generated.resources.user

@Composable
fun MainScreenHost(
    onNavigateToOnboarding: (school: School?) -> Unit
) {
    val navController = rememberNavController()
    var currentDestination by rememberSaveable<MutableState<String?>> { mutableStateOf("Home") }
    navController.addOnDestinationChangedListener { _, destination, _ ->
        currentDestination =
            if (destination.route.orEmpty().startsWith(MainScreen.MainHome::class.qualifiedName ?: "__")) "_Home"
            else if (destination.route.orEmpty().startsWith(MainScreen.MainCalendar::class.qualifiedName ?: "__")) "_Calendar"
            else if (destination.route.orEmpty().startsWith(MainScreen.MainSearch::class.qualifiedName ?: "__")) "_Search"
            else if (destination.route.orEmpty().startsWith(MainScreen.MainChat::class.qualifiedName ?: "__")) "_Chat"
            else if (destination.route.orEmpty().startsWith(MainScreen.MainProfile::class.qualifiedName ?: "__")) "_Profile"
            else null
    }

    val homeViewModel = koinViewModel<HomeViewModel>()
    val calendarViewModel = koinViewModel<CalendarViewModel>()
    val profileViewModel = koinViewModel<ProfileViewModel>()

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentDestination?.startsWith("_") == true,
                enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut()
            ) {
                NavigationBar {
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
                    NavigationBarItem(
                        selected = currentDestination == "_Chat",
                        label = { Text("Chat") },
                        icon = { Icon(painter = painterResource(Res.drawable.message_square), contentDescription = null, modifier = Modifier.size(20.dp)) },
                        onClick = { navController.navigate(MainScreen.MainChat) { popUpTo(MainScreen.MainHome) } }
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
    ) { contentPadding ->
        Box(modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding())) {
            NavHost(
                navController = navController,
//                startDestination = MainScreen.MainHome
                startDestination = MainScreen.MainCalendar // TODO remove
            ) {
                composable<MainScreen.MainHome> { HomeScreen(homeViewModel) }
                composable<MainScreen.MainCalendar> { CalendarScreen(navController, calendarViewModel) }
                composable<MainScreen.MainSearch> { Text("Search") }
                composable<MainScreen.MainChat> { Text("Chat") }
                composable<MainScreen.MainProfile> { ProfileScreen(profileViewModel) }

                composable<MainScreen.ProfileSettings> {
                    val args = it.toRoute<MainScreen.ProfileSettings>()
                    ProfileSettingsScreen(args.profileId, navController)
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
    @Serializable data object MainChat : MainScreen("_Chat")
    @Serializable data object MainProfile : MainScreen("_Profile")

    @Serializable data class ProfileSettings(val profileId: String) : MainScreen("ProfileSettings")
}
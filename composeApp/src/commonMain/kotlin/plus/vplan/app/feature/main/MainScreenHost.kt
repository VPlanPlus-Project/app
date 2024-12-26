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
import co.touchlab.kermit.Logger
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.VPP_ID_AUTH_URL
import plus.vplan.app.domain.model.School
import plus.vplan.app.feature.home.ui.HomeScreen
import plus.vplan.app.feature.home.ui.HomeViewModel
import plus.vplan.app.feature.profile.ui.ProfileScreen
import plus.vplan.app.feature.profile.ui.ProfileScreenEvent
import plus.vplan.app.feature.profile.ui.ProfileViewModel
import plus.vplan.app.feature.profile.ui.components.ProfileSwitcher
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
            if (destination.route.orEmpty().startsWith(MainScreen.Home::class.qualifiedName ?: "__")) "Home"
            else if (destination.route.orEmpty().startsWith(MainScreen.Calendar::class.qualifiedName ?: "__")) "Calendar"
            else if (destination.route.orEmpty().startsWith(MainScreen.Search::class.qualifiedName ?: "__")) "Search"
            else if (destination.route.orEmpty().startsWith(MainScreen.Chat::class.qualifiedName ?: "__")) "Chat"
            else if (destination.route.orEmpty().startsWith(MainScreen.Profile::class.qualifiedName ?: "__")) "Profile"
            else null
    }

    val homeViewModel = koinViewModel<HomeViewModel>()
    val profileViewModel = koinViewModel<ProfileViewModel>()

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentDestination != null,
                enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut()
            ) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentDestination == "Home",
                        label = { Text("Home") },
                        icon = { Icon(painter = painterResource(Res.drawable.house), contentDescription = null, modifier = Modifier.size(20.dp)) },
                        onClick = { navController.navigate(MainScreen.Home) { popUpTo(0) } }
                    )
                    NavigationBarItem(
                        selected = currentDestination == "Calendar",
                        label = { Text("Kalender") },
                        icon = { Icon(painter = painterResource(Res.drawable.calendar), contentDescription = null, modifier = Modifier.size(20.dp)) },
                        onClick = { navController.navigate(MainScreen.Calendar) { popUpTo(MainScreen.Home) } }
                    )
                    NavigationBarItem(
                        selected = currentDestination == "Search",
                        label = { Text("Suche") },
                        icon = { Icon(painter = painterResource(Res.drawable.search), contentDescription = null, modifier = Modifier.size(20.dp)) },
                        onClick = { navController.navigate(MainScreen.Search) { popUpTo(MainScreen.Home) } }
                    )
                    NavigationBarItem(
                        selected = currentDestination == "Chat",
                        label = { Text("Chat") },
                        icon = { Icon(painter = painterResource(Res.drawable.message_square), contentDescription = null, modifier = Modifier.size(20.dp)) },
                        onClick = { navController.navigate(MainScreen.Chat) { popUpTo(MainScreen.Home) } }
                    )
                    NavigationBarItem(
                        selected = currentDestination == "Profile",
                        label = { Text("Profil") },
                        icon = { Icon(painter = painterResource(Res.drawable.user), contentDescription = null, modifier = Modifier.size(20.dp)) },
                        onClick = { navController.navigate(MainScreen.Profile) { popUpTo(MainScreen.Home) } },
                    )
                }
            }
        }
    ) { contentPadding ->
        Box(modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding())) {
            NavHost(
                navController = navController,
                startDestination = MainScreen.Home
            ) {
                composable<MainScreen.Home> { HomeScreen(homeViewModel) }
                composable<MainScreen.Calendar> { Text("Calendar") }
                composable<MainScreen.Search> { Text("Search") }
                composable<MainScreen.Chat> { Text("Chat") }
                composable<MainScreen.Profile> { ProfileScreen(profileViewModel) }
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
                onConnectVppId = {
                    Logger.d { "Opening vpp.ID auth url: $VPP_ID_AUTH_URL" }
                    BrowserIntent.openUrl(VPP_ID_AUTH_URL)
                }
            )
        }
    }
}

@Serializable
sealed class MainScreen(val name: String) {
    @Serializable data object Home : MainScreen("Home")
    @Serializable data object Calendar : MainScreen("Calendar")
    @Serializable data object Search : MainScreen("Search")
    @Serializable data object Chat : MainScreen("Chat")
    @Serializable data object Profile : MainScreen("Profile")
}
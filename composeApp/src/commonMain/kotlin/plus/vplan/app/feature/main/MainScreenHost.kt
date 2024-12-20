package plus.vplan.app.feature.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.house
import vplanplus.composeapp.generated.resources.user

@Composable
fun MainScreenHost() {
    val navController = rememberNavController()
    var currentDestination by rememberSaveable<MutableState<String?>> { mutableStateOf("Home") }
    navController.addOnDestinationChangedListener { _, destination, _ ->
        currentDestination =
            if (destination.route.orEmpty().startsWith(MainScreen.Home::class.qualifiedName ?: "__")) "Home"
            else if (destination.route.orEmpty().startsWith(MainScreen.Calendar::class.qualifiedName ?: "__")) "Calendar"
            else null
    }
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
                        selected = currentDestination == "Profile",
                        label = { Text("Profil") },
                        icon = { Icon(painter = painterResource(Res.drawable.user), contentDescription = null, modifier = Modifier.size(20.dp)) },
                        onClick = { navController.navigate(MainScreen.Profile) { popUpTo(MainScreen.Home) } }
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = MainScreen.Home
        ) {
            composable<MainScreen.Home> {
                Text("Home")
            }
            composable<MainScreen.Calendar> { Text("Calendar") }
            composable<MainScreen.Profile> { Text("Profile") }
        }
    }
}

@Serializable
sealed class MainScreen(val name: String) {
    @Serializable data object Home : MainScreen("Home")
    @Serializable data object Calendar : MainScreen("Calendar")
    @Serializable data object Profile : MainScreen("Profile")
}
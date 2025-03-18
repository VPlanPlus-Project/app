package plus.vplan.app.feature.vpp_id.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.data.Response
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.circle_user_round
import vplanplus.composeapp.generated.resources.house

@Composable
fun VppIdSetupScreen(
    token: String,
    onGoToHome: () -> Unit
) {
    val viewModel = koinViewModel<VppIdSetupViewModel>()
    val state = viewModel.state
    LaunchedEffect(token) {
        viewModel.init(token)
    }

    Scaffold { contentPadding ->
        Column(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = state.user,
                contentKey = { state.user is Response.Loading },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) { user ->
                if (user is Response.Loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@AnimatedContent
                }

                if (user is Response.Success) Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.circle_user_round),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Hallo ${user.data.name}",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            brush = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
                        )
                    )
                    Text(
                        text = "Du bist nun in VPlanPlus angemeldet und kannst alle Funktionen nutzen.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onGoToHome
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.house),
                                contentDescription = null,
                                modifier = Modifier.size(LocalTextStyle.current.lineHeight.toDp())
                            )
                            Spacer(Modifier.size(4.dp))
                            Text("Zur Startseite")
                        }
                    }
                }
            }
        }
    }
}

/* TODO: Show profile picker if there are multiple profiles that match the vpp.ID target (group)
 * or when the same vpp.ID is already connected, for instance, if it got logged out remotely.
 */
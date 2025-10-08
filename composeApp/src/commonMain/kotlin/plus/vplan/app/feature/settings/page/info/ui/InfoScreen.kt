package plus.vplan.app.feature.settings.page.info.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.BuildConfig
import plus.vplan.app.Platform
import plus.vplan.app.feature.settings.page.info.ui.components.FeedbackDrawer
import plus.vplan.app.feature.settings.ui.components.SettingsRecord
import plus.vplan.app.ui.components.noRippleClickable
import plus.vplan.app.ui.theme.displayFontFamily
import plus.vplan.app.utils.openUrl
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import vplanplus.composeapp.generated.resources.chevron_right
import vplanplus.composeapp.generated.resources.github
import vplanplus.composeapp.generated.resources.globe
import vplanplus.composeapp.generated.resources.google_play
import vplanplus.composeapp.generated.resources.handshake
import vplanplus.composeapp.generated.resources.instagram
import vplanplus.composeapp.generated.resources.logo
import vplanplus.composeapp.generated.resources.mastodon
import vplanplus.composeapp.generated.resources.message_circle_warning
import vplanplus.composeapp.generated.resources.shield_user
import vplanplus.composeapp.generated.resources.threads
import vplanplus.composeapp.generated.resources.whatsapp

expect fun getPlatform(): String

@Composable
fun InfoScreen(
    navHostController: NavHostController
) {

    val viewModel = koinViewModel<InfoViewModel>()

    InfoContent(
        onBack = remember { { navHostController.navigateUp() } },
        onEvent = viewModel::handleEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoContent(
    onBack: () -> Unit,
    onEvent: (InfoEvent) -> Unit
) {
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    var showFeedbackDrawer by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Info & Feedback") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_left),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehaviour
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nestedScroll(scrollBehaviour.nestedScrollConnection)
                .padding(top = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(2.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                )
                Column {
                    Text(
                        text = "VPlanPlus für ${getPlatform()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = displayFontFamily(),
                    )

                    var clickCounter by remember { mutableIntStateOf(0) }
                    LaunchedEffect(clickCounter) {
                        if (clickCounter == 0) return@LaunchedEffect
                        if (clickCounter == 10) onEvent(InfoEvent.EnableDeveloperMode)
                        delay(1000)
                        clickCounter = 0
                    }
                    Text(
                        text = "${BuildConfig.APP_VERSION} (${BuildConfig.APP_VERSION_CODE})",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .noRippleClickable { clickCounter++ }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showFeedbackDrawer = true }
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) feedbackButton@{
                Icon(
                    painter = painterResource(Res.drawable.message_circle_warning),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Feedback zur Version abgeben",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Teile uns Probleme, Verbesserungsvorschläge, Lob und Funktionswünsche direkt in der App mit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    painter = painterResource(Res.drawable.chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Links",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            SettingsRecord(
                title = "Internetseite",
                subtitle = "vplan.plus",
                icon = painterResource(Res.drawable.globe),
                onClick = { openUrl("https://vplan.plus") },
                showArrow = true,
            )
            SettingsRecord(
                title = "Datenschutzerklärung",
                subtitle = "für VPlanPlus und vpp.ID",
                icon = painterResource(Res.drawable.shield_user),
                onClick = { openUrl("https://vplan.plus/about/privacy") },
                showArrow = true,
            )
            SettingsRecord(
                title = "Nutzungsbedingungen",
                subtitle = "für VPlanPlus und vpp.ID",
                icon = painterResource(Res.drawable.handshake),
                onClick = { openUrl("https://vplan.plus/about/tos") },
                showArrow = true,
            )
            when (plus.vplan.app.getPlatform()) {
                Platform.Android -> SettingsRecord(
                    title = "Google Play Store",
                    subtitle = "VPlanPlus für stundenplan24.de",
                    icon = painterResource(Res.drawable.google_play),
                    onClick = { openUrl("https://play.google.com/store/apps/details?id=plus.vplan.app") },
                    showArrow = true,
                )
                else -> Unit
            }
            SettingsRecord(
                title = "GitHub-Repository",
                subtitle = "VPlanPlusProject/app",
                icon = painterResource(Res.drawable.github),
                onClick = { openUrl("https://github.com/VPlanPlus-Project/app") },
                showArrow = true,
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Soziale Netzwerke",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            SettingsRecord(
                title = "Instagram",
                subtitle = "@vplanplus",
                icon = painterResource(Res.drawable.instagram),
                onClick = { openUrl("https://www.instagram.com/vplanplus") },
                showArrow = true,
            )
            SettingsRecord(
                title = "Threads",
                subtitle = "@vplanplus",
                icon = painterResource(Res.drawable.threads),
                onClick = { openUrl("https://www.threads.net/@vplanplus") },
                showArrow = true,
            )
            SettingsRecord(
                title = "Mastodon",
                subtitle = "@vpp_app@mastodon.social",
                icon = painterResource(Res.drawable.mastodon),
                onClick = { openUrl("https://mastodon.social/@vpp_app") },
                showArrow = true,
            )
            SettingsRecord(
                title = "WhatsApp-Kanal",
                subtitle = "Erhalte Statusupdates und Infos in WhatsApp",
                icon = painterResource(Res.drawable.whatsapp),
                onClick = { openUrl("https://whatsapp.com/channel/0029Vagcelf5q08Vjjc7Of1o") },
                showArrow = true,
            )
            Text(
                text = "Made with ❤️ by Julius Babies",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .align(Alignment.End)
            )
        }
    }

    if (showFeedbackDrawer) FeedbackDrawer(null) { showFeedbackDrawer = false }
}
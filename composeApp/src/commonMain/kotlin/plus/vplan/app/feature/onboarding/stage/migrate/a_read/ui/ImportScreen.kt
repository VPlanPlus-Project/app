package plus.vplan.app.feature.onboarding.stage.migrate.a_read.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.migrate.a_read.domain.usecase.MigrationDataReadResult
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.utils.readLatestClipboardValue
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.clipboard_paste
import vplanplus.composeapp.generated.resources.download
import vplanplus.composeapp.generated.resources.smartphone
import vplanplus.composeapp.generated.resources.x

@Composable
fun ImportScreen(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<ImportScreenViewModel>()
    LaunchedEffect(Unit) {
        readLatestClipboardValue()?.let { viewModel.handleEvent(ImportScreenEvent.UpdateText(it)) }
    }

    LaunchedEffect(viewModel.state.isDone) {
        if (viewModel.state.isDone) navHostController.navigate(OnboardingScreen.OnboardingPermission)
    }

    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.smartphone),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Übertrage deine Daten",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            AnimatedContent(
                targetState = viewModel.state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) { isLoading ->
                if (isLoading) Text(
                    text = "Wir richten jetzt VPlanPlus mit deinen alten Einstellungen ein. Das dauert ein wenig, bitte lasse die App geöffnet. Es müssen noch einige Daten heruntergeladen werden.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                else Text(
                    text = "Füge hier den kopierten Text aus der alten App ein. Sobald der korrekte Text eingefügt wurde, geht's weiter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
        AnimatedVisibility(
            visible = !viewModel.state.isLoading,
            enter = expandVertically(expandFrom = Alignment.CenterVertically),
            exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically)
        ) {
            TextField(
                value = viewModel.state.text,
                onValueChange = { viewModel.handleEvent(ImportScreenEvent.UpdateText(it)) },
                label = { Text("Hier Text aus alter VPlanPlus-App einfügen") },
                minLines = 5,
                maxLines = 5,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
                enabled = !viewModel.state.isLoading,
                shape = RoundedCornerShape(8.dp),
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.download),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { viewModel.handleEvent(ImportScreenEvent.UpdateText("")) }) {
                        Icon(
                            painter = painterResource(Res.drawable.x),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                isError = viewModel.state.result is MigrationDataReadResult.InvalidSequence,
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = viewModel.state.result
            ) { error ->
                Text(
                    text = when (error) {
                        MigrationDataReadResult.InvalidSequence -> "Ungültiges Format, stelle sicher, dass du zuletzt den Text aus der alten App kopiert hast."
                        else -> ""
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
            Button(
                text = "Aus Zwischenablage einfügen",
                state = if (viewModel.state.isLoading) ButtonState.Loading else ButtonState.Enabled,
                icon = Res.drawable.clipboard_paste,
                size = ButtonSize.Big,
                onlyEventOnActive = true,
                onClick = {
                    viewModel.handleEvent(event = ImportScreenEvent.UpdateText(readLatestClipboardValue() ?: ""))
                }
            )
        }
    }
}
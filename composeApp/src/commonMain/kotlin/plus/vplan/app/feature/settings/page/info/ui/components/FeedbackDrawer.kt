package plus.vplan.app.feature.settings.page.info.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.ui.components.ButtonType
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.circle_user_round
import vplanplus.composeapp.generated.resources.message_circle_heart
import vplanplus.composeapp.generated.resources.message_circle_warning
import vplanplus.composeapp.generated.resources.send_horizontal
import vplanplus.composeapp.generated.resources.smartphone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackDrawer(
    onDismiss: () -> Unit
) {
    val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val viewModel = koinViewModel<FeedbackDrawerViewModel>()
    val state = viewModel.state

    LaunchedEffect(Unit) { viewModel.init() }
    if (state.currentProfile == null) return

    LaunchedEffect(state.sendDone) {
        if (!state.sendDone) return@LaunchedEffect
        delay(2000)
        modalState.hide()
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalState
    ) {
        FeedbackDrawerContent(
            state = state,
            viewModel::onEvent
        )
    }
}

@Composable
private fun FeedbackDrawerContent(
    state: FeedbackDrawerState,
    onEvent: (FeedbackEvent) -> Unit
) {
    val localKeyboardController = LocalSoftwareKeyboardController.current
    var showSystemInfoDialog by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) {
        if (state.isLoading) localKeyboardController?.hide()
    }
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding().coerceAtLeast(16.dp))
    ) {
        AnimatedContent(
            targetState = state.sendDone
        ) { sendDone ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .defaultMinSize(minHeight = 164.dp)
                    .animateContentSize()
            ) {
                if (sendDone) {
                    var showFirstBubble by remember { mutableStateOf(false) }
                    var showSecondBubble by remember { mutableStateOf(false) }
                    Text(
                        text = "Feedback gesendet",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Text(
                        text = "Vielen Dank für deinen Beitrag",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    AnimatedVisibility(
                        visible = showFirstBubble,
                        enter = scaleIn() + fadeIn()
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.message_circle_warning),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 16.dp, end = 16.dp)
                                .size(32.dp)
                        )
                    }
                    AnimatedVisibility(
                        visible = showSecondBubble,
                        enter = scaleIn() + fadeIn()
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.message_circle_heart),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 16.dp, start = 16.dp)
                                .size(32.dp)
                                .graphicsLayer { rotationY = 180f }
                        )
                    }

                    LaunchedEffect(Unit) {
                        delay(500)
                        showFirstBubble = true
                        delay(1000)
                        showSecondBubble = true
                    }

                    return@AnimatedContent
                }

                Text(
                    text = "Dein Feedback",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    text = "Verbesserungsvorschläge, Fehlerberichte, Lob & Kritik",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = state.message,
                    onValueChange = { onEvent(FeedbackEvent.UpdateMessage(it)) },
                    placeholder = { Text("Deine Nachricht an die VPlanPlus-Entwickler") },
                    minLines = 5,
                    isError = state.showEmptyError,
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedVisibility(
                    visible = state.showEmptyError,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Text(
                        text = "Das Feedback darf nicht leer sein",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(MaterialTheme.typography.bodySmall.toSpanStyle()) {
                            append("Deinem Feedback werden einige Informationen zur App-Version und zu deinem Gerät angefügt. ")
                            withLink(LinkAnnotation.Clickable(
                                tag = "show_details",
                                linkInteractionListener = { showSystemInfoDialog = true }
                            )) {
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                                    append("Informationen einblenden")
                                }
                            }
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
                if ((state.currentProfile as? Profile.StudentProfile)?.vppIdId == null) {
                    TextField(
                        value = state.customEmail,
                        onValueChange = { onEvent(FeedbackEvent.UpdateEmail(it)) },
                        singleLine = true,
                        placeholder = { Text("Deine E-Mail-Adresse (optional)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(
                        visible = state.showEmailError,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Text(
                            text = "Diese E-Mail-Adresse ist nicht gültig",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }
                } else {
                    val vppId = state.currentProfile.vppId?.collectAsState(null)?.value
                    if (vppId != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.circle_user_round),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Verknüpft mit vpp.ID von ${vppId.name}.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Absenden",
                    icon = Res.drawable.send_horizontal,
                    state = if (state.isLoading) ButtonState.Loading else ButtonState.Enabled,
                    size = ButtonSize.Big,
                    type = ButtonType.Primary,
                    onlyEventOnActive = true,
                    center = false,
                    onClick = { onEvent(FeedbackEvent.RequestSend) }
                )
            }
        }
    }

    if (showSystemInfoDialog) AlertDialog(
        onDismissRequest = { showSystemInfoDialog = false },
        icon = {
            Icon(
                painter = painterResource(Res.drawable.smartphone),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        title = { Text("Systeminformationen") },
        text = {
            Text(state.systemInfo.toString())
        },
        confirmButton = {
            TextButton(
                onClick = { showSystemInfoDialog = false }
            ) {
                Text("OK")
            }
        },
        dismissButton = {}
    )
}
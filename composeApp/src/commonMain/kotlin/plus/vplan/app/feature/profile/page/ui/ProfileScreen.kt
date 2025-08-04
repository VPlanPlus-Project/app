package plus.vplan.app.feature.profile.page.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.VPP_ID_AUTH_URL
import plus.vplan.app.domain.cache.collectAsResultingFlowOld
import plus.vplan.app.domain.cache.collectAsSingleFlowOld
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.feature.main.ui.MainScreen
import plus.vplan.app.feature.profile.page.ui.components.ProfileTitle
import plus.vplan.app.ui.components.ShimmerLoader
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.utils.BrowserIntent
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.lock
import vplanplus.composeapp.generated.resources.log_in
import vplanplus.composeapp.generated.resources.settings
import vplanplus.composeapp.generated.resources.undraw_profile
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    navHostController: NavHostController,
    contentPadding: PaddingValues,
    viewModel: ProfileViewModel
) {
    val state = viewModel.state

    ProfileContent(
        state = state,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent,
        onOpenSettings = remember { { navHostController.navigate(MainScreen.Settings) } },
        onOpenGrades = remember(state.currentProfile?.id) { { navHostController.navigate(MainScreen.Grades((state.currentProfile as Profile.StudentProfile).vppIdId!!)) } }
    )
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    contentPadding: PaddingValues,
    onEvent: (event: ProfileScreenEvent) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGrades: () -> Unit
) {
    val localDensity = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition()
    val colorScheme = MaterialTheme.colorScheme

    var openGradesScreenAfterUnlock by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.areGradesLocked) {
        if (!state.areGradesLocked && openGradesScreenAfterUnlock) {
            openGradesScreenAfterUnlock = false
            onOpenGrades()
        }
    }

    LaunchedEffect(openGradesScreenAfterUnlock) {
        if (openGradesScreenAfterUnlock) {
            delay(10000)
            openGradesScreenAfterUnlock = false
        }
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ProfileTitle(state.currentProfile?.name.orEmpty()) {
                onEvent(
                    ProfileScreenEvent.SetProfileSwitcherVisibility(
                        true
                    )
                )
            }
            FilledTonalIconButton(
                onClick = onOpenSettings
            ) {
                Icon(
                    painter = painterResource(Res.drawable.settings),
                    modifier = Modifier.size(24.dp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        if (state.currentProfile is Profile.StudentProfile && state.currentProfile.vppId == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.undraw_profile),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Hier landen deine Noten",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "wenn du eine vpp.ID mit beste.schule hinzufügst."
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { BrowserIntent.openUrl(VPP_ID_AUTH_URL) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.log_in),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Anmelden/Registrieren")
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            if (state.currentProfile is Profile.StudentProfile) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val vppId = state.currentProfile.vppId?.collectAsResultingFlowOld()?.value
                    if (vppId is VppId.Active) {
                        val subjectInstances = state.currentProfile.subjectInstances
                            .map { it.map { subjectInstance -> subjectInstance.subject }.distinct() }
                            .distinctUntilChanged()
                            .collectAsState(emptyList()).value

                        val grades = vppId.grades.collectAsSingleFlowOld().value
                        if (vppId.schulverwalterConnection != null) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        if (state.areGradesLocked) {
                                            openGradesScreenAfterUnlock = true
                                            onEvent(ProfileScreenEvent.RequestGradeUnlock)
                                        }
                                        else onOpenGrades()
                                    }
                            ) {
                                var contentWidth by remember { mutableStateOf(0.dp) }
                                var contentHeight by remember { mutableStateOf(0.dp) }

                                val subjectBackdropIconSize = 24.dp
                                val subjectBackdropIconPadding = 4.dp

                                androidx.compose.animation.AnimatedVisibility(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .height(contentHeight)
                                        .clipToBounds(),
                                    visible = subjectInstances.isNotEmpty() && contentWidth > 0.dp && contentHeight > 0.dp,
                                    enter = fadeIn(tween(2000)),
                                    exit = fadeOut(tween(2000)),
                                ) subjectIconBackdrop@{
                                    val rows = (contentHeight / (subjectBackdropIconSize + subjectBackdropIconPadding)).roundToInt()
                                    val height = rows * (subjectBackdropIconSize + subjectBackdropIconPadding)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(((contentHeight - height) / 2).coerceAtLeast(0.dp))
                                            .drawWithCache {
                                                onDrawWithContent {
                                                    drawContent()
                                                    drawRect(
                                                        brush = Brush.horizontalGradient(
                                                            0f to colorScheme.surfaceVariant,
                                                            .3f to colorScheme.surfaceVariant,
                                                            1f to colorScheme.surfaceVariant.copy(alpha = .8f),
                                                        ),
                                                        size = size,
                                                        topLeft = Offset(0f, 0f),
                                                    )
                                                }
                                            },
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(subjectBackdropIconPadding)
                                    ) {
                                        val subjectMap = remember { mutableMapOf<IntOffset, String>() }
                                        if (subjectInstances.isNotEmpty()) repeat(rows) { i ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(subjectBackdropIconPadding)) {
                                                repeat(((contentWidth.value / 1.5) / (subjectBackdropIconSize + subjectBackdropIconPadding).value).roundToInt()) { j ->
                                                    SubjectIcon(
                                                        subject = subjectMap.getOrPut(IntOffset(i, j)) { subjectInstances.random() },
                                                        modifier = Modifier.size(subjectBackdropIconSize),
                                                        contentColor = MaterialTheme.colorScheme.secondary,
                                                        containerColor = Color.Transparent,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onSizeChanged { with(localDensity) {
                                            contentWidth = it.width.toDp()
                                            contentHeight = it.height.toDp()
                                        } }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clipToBounds(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Noten",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = state.areGradesLocked,
                                                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.CenterHorizontally) + slideInHorizontally { it },
                                                exit = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally) + fadeOut() + slideOutHorizontally { it },
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        painter = painterResource(Res.drawable.lock),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = "Noten gesperrt, tippe zum Entsperren",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Text(
                                                        text = "∅ ",
                                                        style = MaterialTheme.typography.headlineLarge,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    AnimatedContent(
                                                        targetState = state.averageGrade,
                                                    ) { average ->
                                                        if (average == null || state.areGradesLocked) ShimmerLoader(
                                                            modifier = Modifier.size(MaterialTheme.typography.headlineLarge.lineHeight.toDp()).clip(RoundedCornerShape(8.dp)),
                                                            infiniteTransition = infiniteTransition
                                                        ) else Text(
                                                            text = if (average.isNaN()) "-" else ((average * 100).roundToInt() / 100.0).toString(),
                                                            style = MaterialTheme.typography.headlineLarge,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "Gesamtdurchschnitt",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                            VerticalDivider(Modifier.height(48.dp))
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                AnimatedContent(
                                                    targetState = grades.filter { it.value != null }.maxByOrNull { it.givenAt },
                                                ) { latestGrade ->
                                                    if (latestGrade == null || state.areGradesLocked) {
                                                        ShimmerLoader(
                                                            modifier = Modifier.size(MaterialTheme.typography.headlineLarge.lineHeight.toDp()).clip(RoundedCornerShape(8.dp)),
                                                            infiniteTransition = infiniteTransition
                                                        )
                                                        return@AnimatedContent
                                                    }
                                                    Text(
                                                        text = latestGrade.value.toString(),
                                                        style = MaterialTheme.typography.headlineLarge,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                    )
                                                }
                                                Text(
                                                    text = "Neueste Note",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
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
}
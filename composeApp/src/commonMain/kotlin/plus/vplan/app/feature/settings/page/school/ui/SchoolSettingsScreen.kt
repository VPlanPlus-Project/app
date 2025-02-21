package plus.vplan.app.feature.settings.page.school.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.model.School
import plus.vplan.app.feature.settings.page.school.ui.components.IndiwareCredentialSheet
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.cloud_alert
import vplanplus.composeapp.generated.resources.x

@Composable
fun SchoolSettingsScreen(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<SchoolSettingsViewModel>()
    val state = viewModel.state

    SchoolSettingsContent(
        state = state,
        onBack = navHostController::navigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SchoolSettingsContent(
    state: SchoolSettingsState,
    onBack: () -> Unit
) {
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    var visibleIndiwareCredentialSchoolId by rememberSaveable { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schulen") },
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
        AnimatedContent(
            targetState = state.schools == null,
            modifier = Modifier.fillMaxSize()
        ) { schoolsAreLoading ->
            if (schoolsAreLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
                return@AnimatedContent
            }
            val schools = state.schools.orEmpty()
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .nestedScroll(scrollBehaviour.nestedScrollConnection)
                    .padding(top = 4.dp)
            ) {
                schools.forEachIndexed { i, school ->
                    if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { visibleIndiwareCredentialSchoolId = school.school.id }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = school.school.name,
                                style = MaterialTheme.typography.headlineSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (school.school is School.IndiwareSchool) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Stundenplan24.de",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = school.school.username + "@" + school.school.sp24Id,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    AnimatedVisibility(
                                        visible = school.credentialsState == SchoolSettingsCredentialsState.Invalid,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Text(
                                            text = "Deine Zugangsdaten sind nicht mehr gültig. Tippe zum aktualisieren.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                AnimatedContent(
                                    targetState = school.credentialsState,
                                ) { displayCredentialsState ->
                                    Box(
                                        modifier = Modifier.size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (displayCredentialsState) {
                                            SchoolSettingsCredentialsState.Loading -> CircularProgressIndicator()
                                            SchoolSettingsCredentialsState.Valid -> Icon(
                                                painter = painterResource(Res.drawable.check),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            SchoolSettingsCredentialsState.Invalid -> Icon(
                                                painter = painterResource(Res.drawable.x),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            SchoolSettingsCredentialsState.Error -> Icon(
                                                painter = painterResource(Res.drawable.cloud_alert),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.error
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

    visibleIndiwareCredentialSchoolId?.let { selectedSchoolId ->
        IndiwareCredentialSheet(
            schoolId = selectedSchoolId,
            onDismiss = { visibleIndiwareCredentialSchoolId = null }
        )
    }
}
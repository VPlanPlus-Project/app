package plus.vplan.app.feature.grades.page.detail.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState
import plus.vplan.app.feature.grades.page.detail.ui.components.GivenAtRow
import plus.vplan.app.feature.grades.page.detail.ui.components.GivenByRow
import plus.vplan.app.feature.grades.page.detail.ui.components.IntervalRow
import plus.vplan.app.feature.grades.page.detail.ui.components.OptionalRow
import plus.vplan.app.feature.grades.page.detail.ui.components.TypeRow
import plus.vplan.app.feature.grades.page.detail.ui.components.UseForFinalGradeRow
import plus.vplan.app.feature.grades.page.detail.ui.components.UserRow
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState
import plus.vplan.app.feature.homework.ui.components.detail.components.SubjectGroupRow
import plus.vplan.app.utils.safeBottomPadding


@Composable
fun GradeDetailPage(
    state: GradeDetailState,
    onEvent: (event: GradeDetailEvent) -> Unit
) {
    val grade = state.grade ?: return
    val vppId = state.gradeUser

    AnimatedContent(
        targetState = state.lockState!!.canAccess
    ) { canAccess ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (!canAccess) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .padding(bottom = safeBottomPadding())
                ) {
                    Text(
                        text = "Noten entsperren",
                        style = MaterialTheme.typography.headlineLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = { onEvent(GradeDetailEvent.RequestGradesUnlock) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(CoreUiRes.drawable.lock_open),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Entsperren")
                        }
                    }
                }
                return@AnimatedContent
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildString {
                            val value = if (grade.isOptional) "(${grade.value})" else grade.value
                            when (grade.collection.interval.type) {
                                is BesteSchuleInterval.Type.Sek2 -> {
                                    if (grade.value == null) append("Note")
                                    else append("$value Notenpunkte")
                                }
                                else -> {
                                    append("Note")
                                    if (grade.value != null) append(" $value")
                                }
                            }
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.lockState == GradeLockState.Unlocked) FilledTonalIconButton(
                        enabled = state.reloadingState != UnoptimisticTaskState.InProgress,
                        onClick = { onEvent(GradeDetailEvent.LockGrades) }
                    ) {
                        Icon(
                            painter = painterResource(CoreUiRes.drawable.lock),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).padding(2.dp)
                        )
                    }

                    FilledTonalIconButton(
                        enabled = state.reloadingState != UnoptimisticTaskState.InProgress,
                        onClick = { onEvent(GradeDetailEvent.Reload) }
                    ) {
                        AnimatedContent(
                            targetState = state.reloadingState,
                        ) { reloadingState ->
                            when (reloadingState) {
                                UnoptimisticTaskState.InProgress -> CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp).padding(2.dp),
                                    strokeWidth = 2.dp
                                )

                                UnoptimisticTaskState.Error -> Icon(
                                    painter = painterResource(CoreUiRes.drawable.info),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).padding(2.dp)
                                )

                                UnoptimisticTaskState.Success -> Icon(
                                    painter = painterResource(CoreUiRes.drawable.check),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).padding(2.dp)
                                )

                                null -> Icon(
                                    painter = painterResource(CoreUiRes.drawable.rotate_cw),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).padding(2.dp)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = grade.collection.subject.fullName,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(16.dp))
                SubjectGroupRow(
                    canEdit = false,
                    allowGroup = false,
                    subject = grade.collection.subject.shortName,
                    onClick = {}
                )
                TypeRow(type = grade.collection.type)
                IntervalRow(schoolYearName = grade.collection.interval.year.name, intervalName = grade.collection.interval.name)
                GivenAtRow(grade.givenAt)
                GivenByRow("${grade.collection.teacher.forename} ${grade.collection.teacher.surname}")
                if (vppId != null) UserRow(vppId.name)
                OptionalRow(grade.isOptional)
                UseForFinalGradeRow(grade.isSelectedForFinalGrade, grade.value == null) { onEvent(GradeDetailEvent.ToggleConsiderForFinalGrade) }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text(text = grade.collection.name)
            }
        }
    }

}
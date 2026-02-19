package plus.vplan.app.feature.search.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.populated.PopulatedAssessment
import plus.vplan.app.domain.model.populated.PopulatedHomework
import plus.vplan.app.feature.search.ui.main.NewItem
import plus.vplan.app.ui.components.Grid
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.ui.theme.displayFontFamily
import plus.vplan.app.utils.blendColor
import plus.vplan.app.utils.now
import plus.vplan.app.utils.regularDateFormatWithoutYear
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.toName
import plus.vplan.app.utils.untilRelativeText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.badge_check
import vplanplus.composeapp.generated.resources.badge_plus
import vplanplus.composeapp.generated.resources.door_open
import vplanplus.composeapp.generated.resources.search

@Composable
private fun sectionTitleFont() = MaterialTheme.typography.titleMedium.copy(fontFamily = displayFontFamily())

@Composable
fun SearchStart(
    profile: Profile,
    newItems: List<NewItem>,
    onAssessmentClicked: (assessmentId: Int) -> Unit,
    onHomeworkClicked: (homeworkId: Int) -> Unit,
    onOpenRoomSearchClicked: () -> Unit
) {
    val localDensity = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (newItems.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.badge_plus),
                    contentDescription = null,
                    modifier = Modifier.size(sectionTitleFont().lineHeight.toDp())
                )
                Text(
                    text = buildString {
                        append("Neu in deiner Gruppe")
                        if (profile is Profile.StudentProfile) {
                            append(" ")
                            append(profile.group.name)
                        }
                    },
                    style = sectionTitleFont()
                )
            }

            var targetIdentifierColumnWidth by remember { mutableStateOf(0.dp) }
            var createdAtByColumnWidth by remember { mutableStateOf(0.dp) }

            Column(Modifier.fillMaxWidth()) {
                newItems.forEachIndexed { i, item ->
                    if (i != 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                when (item) {
                                    is NewItem.Assessment -> onAssessmentClicked(item.assessment.assessment.id)
                                    is NewItem.Homework -> onHomeworkClicked(item.homework.homework.id)
                                }
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .defaultMinSize(minWidth = targetIdentifierColumnWidth)
                                .onSizeChanged { with(localDensity) { it.width.toDp().let { width -> if (width > targetIdentifierColumnWidth) targetIdentifierColumnWidth = width } } },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (item is NewItem.Homework && item.homework.subjectInstance == null) {
                                Text(
                                    text = item.homework.group?.name ?: "",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 24.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(6.dp)
                                )
                            } else {
                                val subject = when (item) {
                                    is NewItem.Assessment -> item.assessment.subjectInstance.subject
                                    is NewItem.Homework -> item.homework.subjectInstance?.subject
                                }
                                SubjectIcon(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    subject = subject
                                )

                                Text(
                                    text = subject.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = when (item) {
                                    is NewItem.Assessment -> "${item.assessment.assessment.type.toName()} (${item.assessment.assessment.date.format(regularDateFormatWithoutYear)})"
                                    is NewItem.Homework -> "Hausaufgabe (${item.homework.homework.dueTo.format(regularDateFormatWithoutYear)})"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                            when (item) {
                                is NewItem.Assessment -> Text(
                                    text = item.assessment.assessment.description.lines().firstOrNull() ?: "Keine Details",
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodySmall,
                                    overflow = TextOverflow.Ellipsis
                                )
                                is NewItem.Homework -> Row(Modifier.fillMaxWidth()) {
                                    Text(
                                        text = if (item.homework.tasks.isEmpty()) "Keine Aufgaben"
                                        else item.homework.tasks.first().content,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodySmall,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.homework.tasks.size > 1) {
                                        Spacer(Modifier.size(4.dp))
                                        Text(
                                            text = "+" + (item.homework.tasks.size - 1),
                                            maxLines = 1,
                                            style = MaterialTheme.typography.labelMedium,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .defaultMinSize(minWidth = createdAtByColumnWidth)
                                .onSizeChanged { with(localDensity) { it.width.toDp().let { width -> if (width > createdAtByColumnWidth) createdAtByColumnWidth = width } } },
                            horizontalAlignment = Alignment.End
                        ) {
                            val createdByFont = MaterialTheme.typography.labelMedium

                            val creator = when (item) {
                                is NewItem.Assessment -> item.assessment.createdBy
                                is NewItem.Homework -> item.homework.createdBy
                            }

                            Row {
                                when (creator) {
                                    is AppEntity.Profile -> {
                                        val profile = when (item) {
                                            is NewItem.Assessment -> (item.assessment as PopulatedAssessment.LocalAssessment).createdByProfile
                                            is NewItem.Homework -> (item.homework as PopulatedHomework.LocalHomework).createdByProfile
                                        }
                                        Text(
                                            text = "Profil " + profile.name,
                                            style = createdByFont
                                        )
                                    }
                                    is AppEntity.VppId -> {
                                        val vppId = when (item) {
                                            is NewItem.Assessment -> (item.assessment as PopulatedAssessment.CloudAssessment).createdByUser
                                            is NewItem.Homework -> (item.homework as PopulatedHomework.CloudHomework).createdByUser
                                        }
                                        Text(
                                            text = vppId.name,
                                            style = createdByFont
                                        )
                                    }
                                }
                            }
                            Text(
                                text = (LocalDate.now() untilRelativeText item.createdAt) ?: item.createdAt.format(regularDateFormatWithoutYear),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.size(8.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.badge_check),
                contentDescription = null,
                modifier = Modifier.size(sectionTitleFont().lineHeight.toDp())
            )
            Text(
                text = "Vorgeschlagene Suchen",
                style = sectionTitleFont()
            )
        }
        Grid(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            columns = 2,
            cellPadding = 4.dp,
            content = List(1) { { _, _, index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            when (index) {
                                0 -> onOpenRoomSearchClicked()
                            }
                        }
                        .padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(when (index) {
                            0 -> Res.drawable.door_open
                            else -> Res.drawable.search
                        }),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp),
                        tint = blendColor(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceVariant, .7f)
                    )
                    Text(
                        text = when (index) {
                            0 -> "Freie Räume"
                            else -> "-"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } }
        )
    }
}
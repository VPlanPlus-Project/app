package plus.vplan.app.feature.calendar.ui.components.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format
import plus.vplan.app.domain.model.populated.PopulatedAssessment
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.ui.subjectColor
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.toName

@Composable
fun AssessmentCard(
    assessment: PopulatedAssessment,
    onClick: () -> Unit
) {
    val localDensity = LocalDensity.current

    var boxHeight by remember { mutableStateOf(0.dp) }
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .onSizeChanged { with(localDensity) { boxHeight = it.height.toDp() } }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(4.dp)
                .height((boxHeight - 32.dp).coerceAtLeast(0.dp))
                .clip(RoundedCornerShape(0, 50, 50, 0))
                .background(assessment.subjectInstance.subject.subjectColor().getGroup().color)
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row {
                SubjectIcon(
                    modifier = Modifier.size(MaterialTheme.typography.titleLarge.lineHeight.toDp()),
                    subject = assessment.subjectInstance.subject
                )
                Spacer(Modifier.size(8.dp))
                Column {
                    Text(
                        text = buildString {
                            append(assessment.subjectInstance.subject)
                            append(": ")
                            append(assessment.assessment.type.toName())
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = assessment.assessment.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            HorizontalDivider(Modifier.padding(8.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val createdByFont = MaterialTheme.typography.labelMedium

                Row {
                    when (assessment) {
                        is PopulatedAssessment.LocalAssessment -> {
                            Text(
                                text = "Profil " + assessment.createdByProfile.name,
                                style = createdByFont
                            )
                        }
                        is PopulatedAssessment.CloudAssessment -> {
                            Text(
                                text = assessment.createdByUser.name,
                                style = createdByFont
                            )
                        }
                    }
                    Text(
                        text = buildString {
                            append(", am ")
                            append(assessment.assessment.date.format(regularDateFormat))
                            append(" erstellt")
                        },
                        style = createdByFont,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
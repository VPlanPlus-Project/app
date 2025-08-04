package plus.vplan.app.feature.calendar.ui.components.agenda

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.collectAsLoadingStateOld
import plus.vplan.app.domain.cache.collectAsResultingFlowOld
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.ui.subjectColor
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.utils.blendColor
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.toDp

@Composable
fun GradeCard(
    grade: Grade,
    onClick: () -> Unit
) {
    val localDensity = LocalDensity.current

    val subject = grade.subject.collectAsResultingFlowOld().value
    val collection = grade.collection.collectAsResultingFlowOld().value
    val interval = collection?.interval?.collectAsResultingFlowOld()?.value
    val createdBy = grade.teacher.collectAsLoadingStateOld("").value
    if (subject == null || collection == null || interval == null) return
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
                .background(subject.localId.subjectColor().getGroup().color)
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubjectIcon(
                    modifier = Modifier.size(MaterialTheme.typography.titleLarge.lineHeight.toDp()),
                    subject = subject.localId
                )
                Column(Modifier.weight(1f, true)) {
                    Text(
                        text = buildString {
                            append("Note in ")
                            append(subject.localId)
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                val red = colors[CustomColor.Red]!!.getGroup()
                val green = colors[CustomColor.Green]!!.getGroup()
                val backgroundColor by animateColorAsState(
                    if (!grade.isSelectedForFinalGrade || grade.value == null || grade.value.startsWith('+') || grade.value.startsWith('-')) Color.Gray
                    else when (interval.type) {
                        Interval.Type.SEK1 -> blendColor(blendColor(green.container, red.container, ((grade.numericValue?:1)-1)/5f), MaterialTheme.colorScheme.surfaceVariant, .7f)
                        Interval.Type.SEK2 -> blendColor(blendColor(red.container, green.container, (grade.numericValue?:0)/15f), MaterialTheme.colorScheme.surfaceVariant, .7f)
                    }
                )

                val textColor by animateColorAsState(
                    if (!grade.isSelectedForFinalGrade || grade.value == null || grade.value.startsWith('+') || grade.value.startsWith('-')) Color.White
                    else when (interval.type) {
                        Interval.Type.SEK1 -> blendColor(blendColor(green.onContainer, red.onContainer, ((grade.numericValue?:1)-1)/5f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
                        Interval.Type.SEK2 -> blendColor(blendColor(red.onContainer, green.onContainer, (grade.numericValue?:0)/15f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
                    }
                )


                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .fillMaxHeight()
                        .width(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buildString {
                            if (grade.isOptional) append("(")
                            if (grade.value != null) append(grade.value)
                            else append("-")
                            if (grade.isOptional) append(")")
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
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
                if (createdBy is CacheState.Loading) CircularProgressIndicator(Modifier.size(MaterialTheme.typography.labelMedium.lineHeight.toDp()))
                else Text(
                    text = buildString {
                        val creator = (createdBy as? CacheState.Done)?.data ?: return@buildString
                        append("${creator.forename} ${creator.name}")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = grade.givenAt.format(regularDateFormat),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
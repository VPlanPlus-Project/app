package plus.vplan.app.feature.grades.page.view.ui.components.latest

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.feature.grades.page.view.ui.components.GradeValue
import plus.vplan.app.ui.components.ShimmerLoader
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.utils.longMonthNames

@Composable
fun LatestGradeItem(
    modifier: Modifier = Modifier,
    isSelectedForAverage: Boolean,
    grade: BesteSchuleGrade,
    infiniteTransition: InfiniteTransition = rememberInfiniteTransition(),
    subjectColumnMinWidth: Dp = 0.dp,
    onSubjectColumnWidthChange: (Dp) -> Unit = {},
    onClick: () -> Unit
) {
    val localDensity = LocalDensity.current
    val collection = grade.collection.collectAsState(null).value
    val interval = collection?.interval?.collectAsState(null)?.value
    val subject = collection?.subject?.collectAsState(null)?.value

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minWidth = subjectColumnMinWidth)
                .onSizeChanged { with(localDensity) { onSubjectColumnWidthChange(it.width.toDp()) } },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) subject@{
            val subjectIconModifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))

            AnimatedContent(
                targetState = subject?.shortName,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { subjectName ->
                if (subjectName == null) ShimmerLoader(
                    modifier = subjectIconModifier,
                    infiniteTransition = infiniteTransition
                ) else SubjectIcon(
                    modifier = subjectIconModifier,
                    subject = subjectName
                )
            }

            if (subject != null) Text(
                text = subject.shortName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Column(Modifier.weight(1f)) {
            if (collection != null) Text(
                text = collection.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = buildString {
                    append(grade.givenAt.format(LocalDate.Format {
                        day(Padding.ZERO)
                        chars(". ")
                        monthName(longMonthNames)
                        char(' ')
                        year()
                    }))
                },
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column {
            GradeValue(
                grade = grade,
                interval = interval,
                isSelected = isSelectedForAverage,
                onClick = null
            )
        }
    }
}

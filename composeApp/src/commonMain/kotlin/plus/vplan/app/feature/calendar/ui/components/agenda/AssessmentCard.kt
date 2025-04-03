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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.collectAsLoadingState
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.ui.components.ShimmerLoader
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.ui.subjectColor
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.toName

@Composable
fun AssessmentCard(
    assessment: Assessment,
    onClick: () -> Unit
) {
    val localDensity = LocalDensity.current

    val subject = assessment.subjectInstance.collectAsResultingFlow().value
    val createdBy by when (assessment.creator) {
        is AppEntity.VppId -> assessment.creator.vppId.collectAsLoadingState("")
        is AppEntity.Profile -> assessment.creator.profile.collectAsLoadingState("")
    }
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
                .background(subject?.subject.subjectColor().getGroup().color)
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row {
                if (subject == null) ShimmerLoader(Modifier.size(MaterialTheme.typography.titleLarge.lineHeight.toDp()))
                else SubjectIcon(
                    modifier = Modifier.size(MaterialTheme.typography.titleLarge.lineHeight.toDp()),
                    subject = subject.subject
                )
                Spacer(Modifier.size(8.dp))
                Column {
                    Text(
                        text = buildString {
                            append(assessment.type.toName())
                            append(" in ")
                            if (subject != null) append(subject.subject)
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = assessment.description,
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
                if (createdBy is CacheState.Loading) CircularProgressIndicator(Modifier.size(MaterialTheme.typography.labelMedium.lineHeight.toDp()))
                else Text(
                    text = buildString {
                        val creator = (createdBy as? CacheState.Done)?.data
                        append(when (creator) {
                            is VppId -> creator.name
                            is Profile -> creator.name
                            else -> ""
                        })
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = assessment.createdAt.date.format(regularDateFormat),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
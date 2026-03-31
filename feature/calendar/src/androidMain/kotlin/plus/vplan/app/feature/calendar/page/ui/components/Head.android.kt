@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package plus.vplan.app.feature.calendar.page.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.feature.calendar.page.domain.model.DisplayType

@Composable
actual fun Head(
    title: String,
    subtitle: String,
    currentDisplayType: DisplayType,
    showTodayButton: Boolean,
    onTodayClicked: () -> Unit,
    onCreateHomeworkClicked: () -> Unit,
    onCreateAssessmentClicked: () -> Unit,
    onShowAgenda: () -> Unit,
    onShowCalendar: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        subtitle = {
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            Row {
                AnimatedVisibility(
                    visible = showTodayButton
                ) {
                    FilledTonalIconButton(
                        onClick = onTodayClicked
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(CoreUiRes.drawable.calendar),
                                contentDescription = "Heute",
                                modifier = Modifier.size(24.dp)
                            )

                            Text(
                                text = LocalDate.now().day.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(160.dp)
                ) {
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        onClick = { onShowAgenda() },
                        selected = currentDisplayType == DisplayType.Agenda,
                        label = {
                            Icon(
                                painter = painterResource(CoreUiRes.drawable.list_ordered),
                                contentDescription = "Agenda"
                            )
                        }
                    )
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        onClick = { onShowCalendar() },
                        selected = currentDisplayType == DisplayType.Calendar,
                        label = {
                            Icon(
                                painter = painterResource(CoreUiRes.drawable.calendar),
                                contentDescription = "Kalender"
                            )
                        }
                    )
                }
            }
        }
    )
}

@Preview
@Composable
private fun HeadPreview() {
    AppTheme(dynamicColor = false) {
        Head(
            title = "Kalender",
            subtitle = "KW22",
            currentDisplayType = DisplayType.Calendar,
            showTodayButton = true,
            onCreateHomeworkClicked = {},
            onCreateAssessmentClicked = {},
            onShowAgenda = {},
            onShowCalendar = {},
            onTodayClicked = {},
        )
    }
}
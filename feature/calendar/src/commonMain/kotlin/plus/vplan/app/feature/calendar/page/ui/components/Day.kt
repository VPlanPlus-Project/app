package plus.vplan.app.feature.calendar.page.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.core.ui.theme.CustomColor
import plus.vplan.app.core.ui.theme.colors
import plus.vplan.app.core.ui.theme.displayFontFamily
import plus.vplan.app.core.ui.theme.getGroup
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.core.utils.date.shortDayOfWeekNames

@Composable
fun Day(
    modifier: Modifier = Modifier,
    date: LocalDate,
    isGrayedOut: Boolean,
    isHoliday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val defaultColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onBackground

    val grayedOutColor = MaterialTheme.colorScheme.outline
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary
        else Color.Transparent
    )
    Box(
        modifier = modifier
            .width(48.dp)
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .height(48.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val weekDayColor by animateColorAsState(
                targetValue = if (isGrayedOut) grayedOutColor
                else if (isHoliday) colors[CustomColor.Red]!!.getGroup().color
                else defaultColor
            )

            Text(
                text = date.format(LocalDate.Format { dayOfWeek(shortDayOfWeekNames) }),
                fontFamily = displayFontFamily(),
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = weekDayColor,
            )

            val dateColor by animateColorAsState(
                if (isGrayedOut) grayedOutColor
                else defaultColor
            )

            Text(
                text = date.format(LocalDate.Format { day(Padding.NONE) }),
                fontSize = 16.sp,
                lineHeight = 12.sp,
                color = dateColor,
            )
        }
    }
}

@Preview
@Composable
private fun DayPreview() {
    AppTheme(dynamicColor = false) {
        Day(
            date = LocalDate.now(),
            isGrayedOut = false,
            isHoliday = false,
            isSelected = false,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun GrayedOutDayPreview() {
    AppTheme(dynamicColor = false) {
        Day(
            date = LocalDate.now(),
            isGrayedOut = true,
            isHoliday = true,
            isSelected = false,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun HolidayDayPreview() {
    AppTheme(dynamicColor = false) {
        Day(
            date = LocalDate.now(),
            isGrayedOut = false,
            isHoliday = true,
            isSelected = false,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun SelectedHolidayDayPreview() {
    AppTheme(dynamicColor = false) {
        Day(
            date = LocalDate.now(),
            isGrayedOut = false,
            isHoliday = true,
            isSelected = true,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun SelectedDayPreview() {
    AppTheme(dynamicColor = false) {
        Day(
            date = LocalDate.now(),
            isGrayedOut = false,
            isHoliday = false,
            isSelected = false,
            onClick = {},
        )
    }
}
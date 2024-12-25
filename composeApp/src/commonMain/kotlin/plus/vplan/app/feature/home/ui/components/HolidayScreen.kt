package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.undraw_relaxing_at_home_black
import vplanplus.composeapp.generated.resources.undraw_relaxing_at_home_white

@Composable
fun HolidayScreen(
    modifier: Modifier = Modifier,
    nextRegularSchoolDay: LocalDate?
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(
                if (!isSystemInDarkTheme()) Res.drawable.undraw_relaxing_at_home_white
                else Res.drawable.undraw_relaxing_at_home_black
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Inside
        )
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = "Schulfreier Tag",
                style = MaterialTheme.typography.headlineMedium.copy(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
            )
            nextRegularSchoolDay?.let { nextDay ->
                Text(
                    text = "Nächster Schultag am ${nextDay.format(remember { LocalDate.Format {
                        dayOfWeek(DayOfWeekNames("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"))
                        chars(", ")
                        dayOfMonth()
                        chars(". ")
                        monthName(MonthNames("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"))
                        char(' ')
                        year()
                    } })}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
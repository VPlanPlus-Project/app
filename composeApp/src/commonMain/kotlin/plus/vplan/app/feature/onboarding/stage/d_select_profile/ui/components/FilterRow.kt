package plus.vplan.app.feature.onboarding.stage.d_select_profile.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.ProfileType
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.square_user_round
import vplanplus.composeapp.generated.resources.users

@Composable
fun FilterRow(
    currentSelection: ProfileType?,
    onClick: (ProfileType?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = currentSelection == ProfileType.STUDENT,
            label = { Text("Klasse") },
            onClick = {
                if (currentSelection == ProfileType.STUDENT) onClick(null)
                else onClick(ProfileType.STUDENT)
            },
            leadingIcon = {
                AnimatedContent(
                    targetState = currentSelection == ProfileType.STUDENT
                ) { selected ->
                    if (!selected) {
                        Icon(
                            painter = painterResource(Res.drawable.users),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.check),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )
        FilterChip(
            selected = currentSelection == ProfileType.TEACHER,
            label = { Text("Lehrkraft") },
            onClick = {
                if (currentSelection == ProfileType.TEACHER) onClick(null)
                else onClick(ProfileType.TEACHER)
            },
            leadingIcon = {
                AnimatedContent(
                    targetState = currentSelection == ProfileType.TEACHER
                ) { selected ->
                    if (!selected) {
                        Icon(
                            painter = painterResource(Res.drawable.square_user_round),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.check),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )
    }
}
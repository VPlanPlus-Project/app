package plus.vplan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.ui.subjectColor
import plus.vplan.app.ui.subjectIcon

@Composable
fun SubjectIcon(modifier: Modifier = Modifier, subject: String?, containerColor: Color = subject.subjectColor().getGroup().container, contentColor: Color = subject.subjectColor().getGroup().onContainer) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(containerColor)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(subject.subjectIcon()),
            contentDescription = subject,
            modifier = Modifier.fillMaxSize(),
            tint = contentColor
        )
    }
}
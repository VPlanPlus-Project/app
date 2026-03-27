@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package plus.vplan.app.feature.onboarding.stage.teacher_notice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.Button
import plus.vplan.app.core.ui.components.ButtonSize
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.core.ui.theme.displayFontFamily
import plus.vplan.app.feature.onboarding.ui.components.OnboardingHeader

@Composable
fun TeacherNoticeScreen(
    contentPadding: PaddingValues,
    onContinue: () -> Unit,
) {
    TeacherNoticeContent(
        contentPadding = contentPadding,
        onContinue = onContinue,
    )
}

@Composable
private fun TeacherNoticeContent(
    contentPadding: PaddingValues,
    onContinue: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(contentPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            OnboardingHeader(
                title = "Hinweis für Lehrkräfte",
                subtitle = null
            )

            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp + contentPadding.calculateBottomPadding())
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = buildString {
                            appendLine(
                                "VPlanPlus wurde von Schülern für Schüler entwickelt. " +
                                        "Die App verwendet jene Schnittstellen von stundenplan24.de, welche " +
                                        "Plandaten für Schüler bereitstellt, unabhängig davon, welcher " +
                                        "Benutzerzugang verwendet wird."
                            )
                            appendLine()
                            append(
                                "Das liegt daran, dass wir aktuell nur mit der Datenstruktur für " +
                                        "Schülerpläne vertraut sind."
                            )
                        },
                        textAlign = TextAlign.Justify,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                    )

                    Text(
                        text = buildString {
                            append("Was bedeutet das?")
                        },
                        textAlign = TextAlign.Justify,
                        fontFamily = displayFontFamily(),
                        style = MaterialTheme.typography.titleMediumEmphasized.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                    )
                    Text(
                        text = buildString {
                            appendLine(
                                "Stunden, die grundsätzlich nicht für Schüler von Relevanz sind, " +
                                        "werden auch bei Lehrerprofilen nicht angezeigt, beispielsweise " +
                                        "Konferenzen oder Personalgespräche. Schulstunden, die im " +
                                        "Vertretungsplan ausfallen, werden dennoch auch für betroffene " +
                                        "Lehrkräfte angezeigt."
                            )
                        },
                        textAlign = TextAlign.Justify,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                    )
                }

                Button(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    text = "Weiter",
                    size = ButtonSize.Big,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onContinue()
                    },
                    icon = CoreUiRes.drawable.arrow_right,
                )
            }
        }
    }
}

@Preview(name = "Phone", device = "spec:width=411dp,height=891dp")
@Preview(name = "Phone - Landscape", device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420")
@Preview(name = "Unfolded Foldable", device = "spec:width=673dp,height=841dp")
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait")
@Preview(name = "Tablet - Landscape", device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(name = "Desktop", device = "spec:width=1920dp,height=1080dp,dpi=160")
@Composable
private fun TeacherNoticePreview() {
    AppTheme(dynamicColor = false) {
        TeacherNoticeContent(
            onContinue = {},
            contentPadding = PaddingValues()
        )
    }
}
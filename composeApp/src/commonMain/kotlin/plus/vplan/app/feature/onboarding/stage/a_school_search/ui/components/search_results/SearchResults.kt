package plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components.search_results

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.OnboardingSchoolOption
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.OnboardingSchoolSearchEvent

@Composable
fun SearchResults(
    modifier: Modifier = Modifier,
    query: String,
    results: List<OnboardingSchoolOption>,
    onEvent: (OnboardingSchoolSearchEvent) -> Unit
) {
    AnimatedContent(
        targetState = query.isNotBlank() && results.isEmpty(),
        modifier = modifier.fillMaxSize()
    ) searchResult@{ hasNoResults ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (hasNoResults) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Keine Schulen gefunden",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Du kannst dennoch die Stundenplan24.de-Schulnummer verwenden, um eine neue Schule hinzuzufügen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                return@searchResult
            }
            val scrollState = rememberLazyListState()
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .animateContentSize(tween()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(
                    items = results,
                    key = { it.id }
                ) { school ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .defaultMinSize(minHeight = 48.dp)
                            .clickable { onEvent(OnboardingSchoolSearchEvent.OnSchoolSelected(school)) }
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = school.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (school.sp24Id != null) {
                            Text(
                                text = school.sp24Id.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

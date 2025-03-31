package plus.vplan.app.feature.onboarding.stage.a_school_search.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components.SearchBar
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components.search_results.SearchResults

@Composable
fun OnboardingSchoolSearch(
    navController: NavHostController,
    contentPadding: PaddingValues
) {
    val viewModel = koinViewModel<OnboardingSchoolSearchViewModel>()
    LaunchedEffect(Unit) {
        viewModel.init(navController)
    }
    OnboardingSchoolSearchContent(
        state = viewModel.state,
        contentPadding = contentPadding,
        onEvent = viewModel::handleEvent
    )
}

@Composable
private fun OnboardingSchoolSearchContent(
    state: OnboardingSchoolSearchState,
    contentPadding: PaddingValues,
    onEvent: (OnboardingSchoolSearchEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        SearchResults(
            query = state.searchQuery,
            results = state.results,
            onEvent = onEvent
        )
        Text(
            text = buildAnnotatedString {
                withStyle(MaterialTheme.typography.labelMedium.let { it.copy(lineHeight = it.fontSize) }.toSpanStyle()) {
                    append("Wenn du fortfährst, akzeptierst du die ")
                    withLink(LinkAnnotation.Url("https://vplan.plus/privacy", TextLinkStyles(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)))) {
                        append("Datenschutzerklärung")
                    }
                    append(" und die ")
                    withLink(LinkAnnotation.Url("https://vplan.plus/tos", TextLinkStyles(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)))) {
                        append("Nutzungsbedingungen")
                    }
                    append(".")
                }
            },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
        )
        SearchBar(
            query = state.searchQuery, textFieldError = state.textFieldError, onEvent = onEvent
        )
    }
}
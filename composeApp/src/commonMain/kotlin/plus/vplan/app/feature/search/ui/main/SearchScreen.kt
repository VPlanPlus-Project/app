package plus.vplan.app.feature.search.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun SearchScreen(
    navHostController: NavHostController,
    contentPadding: PaddingValues,
) {
    SearchScreenContent(
        contentPadding = contentPadding
    )
}

@Composable
private fun SearchScreenContent(
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
    ) {
        Text("Suche Neu")
    }
}
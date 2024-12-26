package plus.vplan.app.feature.vpp_id.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.data.Response

@Composable
fun VppIdSetupScreen(token: String) {
    val viewModel = koinViewModel<VppIdSetupViewModel>()
    val state = viewModel.state
    LaunchedEffect(token) {
        viewModel.init(token)
    }

    if (state.user is Response.Success) {
        Text(state.user.data.toString())
    }
    if (state.user is Response.Error) {
        Text(state.user.toString())
    }
}
package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.ui.components.ShimmerLoader
import plus.vplan.app.utils.toDp

@Composable
fun CreatedByRow(
    createdBy: AppEntity.VppId
) {
    val vppId = createdBy.vppId.collectAsState(CacheState.Loading(createdBy.id.toString(), source = null)).value
    MetadataRow(
        key = {
            Text(
                text = "Erstellt von",
                style = tableNameStyle()
            )
        },
        value = {
            when (vppId) {
                is CacheState.Loading -> if (vppId.source == CacheState.Loading.Source.Network) ShimmerLoader(modifier = Modifier
                    .fillMaxWidth(.5f)
                    .height(tableValueStyle().lineHeight.toDp())
                    .clip(RoundedCornerShape(8.dp))
                )
                is CacheState.Done -> Text(
                    text = vppId.data.name,
                    style = tableValueStyle()
                )
                is CacheState.Error -> Text(
                    text = "Fehler",
                    color = MaterialTheme.colorScheme.error,
                    style = tableValueStyle()
                )
                else -> Unit
            }
        }
    )
}
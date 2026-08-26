package com.lyf.cmp.core.ui.loadmore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lyf.cmp.core.resources.Res
import com.lyf.cmp.core.resources.core_load_more_failed
import com.lyf.cmp.core.resources.core_no_more
import com.lyf.cmp.core.resources.core_retry
import org.jetbrains.compose.resources.stringResource

/**
 * 加载更多 footer。Idle 时不占内容；Failed 提供重试入口；
 * End 是否显示「没有更多」由 [showEndText] 控制。
 */
@Composable
fun LoadMoreFooter(
    state: LoadMoreState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    showEndText: Boolean = true,
) {
    when (state) {
        LoadMoreState.Idle -> Unit
        LoadMoreState.Loading -> Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        LoadMoreState.Failed -> Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.core_load_more_failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRetry) {
                Text(text = stringResource(Res.string.core_retry))
            }
        }

        LoadMoreState.End -> if (showEndText) {
            Text(
                text = stringResource(Res.string.core_no_more),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            )
        }
    }
}

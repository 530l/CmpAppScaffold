package com.lyf.cmp.feature.cart.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lyf.cmp.core.design.AppTheme
import com.lyf.cmp.core.ui.loadmore.LoadableLazyColumn
import com.lyf.cmp.core.util.formatMoney
import com.lyf.cmp.feature.cart.domain.Article
import com.lyf.cmp.feature.cart.resources.Res
import com.lyf.cmp.feature.cart.resources.cart_article_subtitle
import com.lyf.cmp.feature.cart.resources.cart_checkout
import com.lyf.cmp.feature.cart.resources.cart_empty
import com.lyf.cmp.feature.cart.resources.cart_error_load
import com.lyf.cmp.feature.cart.resources.cart_loading
import com.lyf.cmp.feature.cart.resources.cart_retry
import com.lyf.cmp.feature.cart.resources.cart_select_all
import com.lyf.cmp.feature.cart.resources.cart_title
import com.lyf.cmp.feature.cart.resources.cart_total
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CartScreen(
    onCheckout: (selectedItemIds: List<Long>) -> Unit = {},
    viewModel: CartViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CartContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onCheckout = onCheckout,
    )
}

@Composable
private fun CartContent(
    uiState: CartUiState,
    onIntent: (CartIntent) -> Unit,
    onCheckout: (selectedItemIds: List<Long>) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            SettleBar(
                uiState = uiState,
                onIntent = onIntent,
                onCheckout = onCheckout,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Text(
                text = stringResource(Res.string.cart_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
            )

            when {
                uiState.isInitializing -> LoadingContent()
                uiState.error != null && uiState.dataList.isEmpty() -> ErrorContent(
                    message = errorMessage(uiState.error),
                    onRetry = { onIntent(CartIntent.Retry) },
                )
                uiState.dataList.isEmpty() -> EmptyContent()
                else -> ArticleList(
                    uiState = uiState,
                    onIntent = onIntent,
                )
            }
        }
    }
}

private fun LazyListScope.articleListContent(
    uiState: CartUiState,
    onIntent: (CartIntent) -> Unit,
) {
    uiState.error?.let { error ->
        item(key = "cart_error") {
            ErrorBanner(message = errorMessage(error))
        }
    }
    itemsIndexed(uiState.dataList, key = { _, article -> article.id }) { position, article ->
        ArticleRow(
            article = article,
            position = position,
            onToggle = { onIntent(CartIntent.ToggleItem(article.id)) },
        )
    }
}

@Composable
private fun ArticleList(
    uiState: CartUiState,
    onIntent: (CartIntent) -> Unit,
) {
    LoadableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = uiState.isRefreshing,
        loadMoreState = uiState.loadMoreState,
        onRefresh = { onIntent(CartIntent.Refresh) },
        onLoadMore = { onIntent(CartIntent.LoadMore) },
        content = { articleListContent(uiState, onIntent) },
    )
}

@Composable
private fun ArticleRow(
    article: Article,
    position: Int,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .toggleable(
                value = article.selected,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArticleThumbnail(article)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    Res.string.cart_article_subtitle,
                    formatMoney(demoUnitPrice(position)),
                    article.chapterName,
                    article.niceDate,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Checkbox(
            checked = article.selected,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun ArticleThumbnail(article: Article) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(shape)
            .background(thumbnailColor(article.id)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = article.chapterName.firstOrNull()?.toString() ?: "#",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SettleBar(
    uiState: CartUiState,
    onIntent: (CartIntent) -> Unit,
    onCheckout: (selectedItemIds: List<Long>) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val allState = when {
                uiState.allSelected -> ToggleableState.On
                uiState.selectedCount > 0 -> ToggleableState.Indeterminate
                else -> ToggleableState.Off
            }
            Row(
                modifier = Modifier.triStateToggleable(
                    state = allState,
                    enabled = uiState.dataList.isNotEmpty(),
                    role = Role.Checkbox,
                    onClick = { onIntent(CartIntent.ToggleSelectAll) },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TriStateCheckbox(
                    state = allState,
                    onClick = null,
                    enabled = uiState.dataList.isNotEmpty(),
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                )
                Text(
                    text = stringResource(Res.string.cart_select_all),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.cart_total),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatMoney(uiState.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    onCheckout(uiState.dataList.filter(Article::selected).map(Article::id))
                },
                enabled = uiState.selectedCount > 0,
                modifier = Modifier.height(44.dp),
            ) {
                Text(stringResource(Res.string.cart_checkout, uiState.selectedCount))
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(Res.string.cart_loading),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) {
                Text(stringResource(Res.string.cart_retry))
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(Res.string.cart_empty),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        textAlign = TextAlign.Center,
    )
}

private val thumbnailColors = listOf(
    Color(0xFFF7E8D3),
    Color(0xFFE2EFFA),
    Color(0xFFEFE7FB),
    Color(0xFFE6F4E4),
    Color(0xFFFCEBDC),
    Color(0xFFE3F1EF),
)

private fun thumbnailColor(articleId: Long): Color =
    thumbnailColors[(articleId % thumbnailColors.size).toInt()]

@Composable
private fun errorMessage(error: CartError): String = when (error) {
    CartError.LOAD_FAILED -> stringResource(Res.string.cart_error_load)
}

@Preview
@Composable
private fun CartContentPreview() {
    AppTheme {
        CartContent(
            uiState = CartUiState(
                isInitializing = false,
                dataList = listOf(
                    Article(1, "Kotlin 与 Java，不是简单的高低之分", "化骨龙", "广场Tab", "https://example.com/1", "1天前"),
                    Article(2, "Compose Multiplatform 1.11 发布", "官方", "资讯", "https://example.com/2", "2天前", selected = true),
                ),
            ),
            onIntent = {},
            onCheckout = {},
        )
    }
}

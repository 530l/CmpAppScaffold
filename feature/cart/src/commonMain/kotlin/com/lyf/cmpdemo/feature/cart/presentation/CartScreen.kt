package com.lyf.cmpdemo.feature.cart.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lyf.cmpdemo.core.design.AppTheme
import com.lyf.cmpdemo.core.image.AppImage
import com.lyf.cmpdemo.core.model.Money
import com.lyf.cmpdemo.core.util.formatMoney
import com.lyf.cmpdemo.feature.cart.domain.CartItem
import com.lyf.cmpdemo.feature.cart.resources.Res
import com.lyf.cmpdemo.feature.cart.resources.cart_checkout
import com.lyf.cmpdemo.feature.cart.resources.cart_empty
import com.lyf.cmpdemo.feature.cart.resources.cart_error_load
import com.lyf.cmpdemo.feature.cart.resources.cart_error_update
import com.lyf.cmpdemo.feature.cart.resources.cart_loading
import com.lyf.cmpdemo.feature.cart.resources.cart_price_quantity
import com.lyf.cmpdemo.feature.cart.resources.cart_product_image
import com.lyf.cmpdemo.feature.cart.resources.cart_retry
import com.lyf.cmpdemo.feature.cart.resources.cart_select_all
import com.lyf.cmpdemo.feature.cart.resources.cart_title
import com.lyf.cmpdemo.feature.cart.resources.cart_total
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
                uiState.isLoading -> LoadingContent()
                uiState.error != null && uiState.items.isEmpty() -> ErrorContent(
                    message = errorMessage(uiState.error),
                    onRetry = { onIntent(CartIntent.Retry) },
                )
                uiState.items.isEmpty() -> EmptyContent()
                else -> CartList(
                    uiState = uiState,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun CartList(
    uiState: CartUiState,
    onIntent: (CartIntent) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        uiState.error?.let { error ->
            item(key = "cart_error") {
                ErrorBanner(message = errorMessage(error))
            }
        }
        items(uiState.items, key = CartItem::id) { item ->
            CartItemRow(
                item = item,
                onToggle = { onIntent(CartIntent.ToggleItem(item.id)) },
            )
        }
    }
}

@Composable
private fun CartItemRow(item: CartItem, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .toggleable(
                value = item.selected,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductThumbnail(item)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    Res.string.cart_price_quantity,
                    formatMoney(item.unitPrice),
                    item.count,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Checkbox(
            checked = item.selected,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun ProductThumbnail(item: CartItem) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(shape)
            .background(productColor(item.id)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = item.emoji, fontSize = 26.sp)
        if (!item.imageUrl.isNullOrBlank()) {
            AppImage(
                imageUrl = item.imageUrl,
                contentDescription = stringResource(Res.string.cart_product_image, item.name),
                modifier = Modifier.fillMaxSize(),
            )
        }
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
                uiState.selectedLineCount > 0 -> ToggleableState.Indeterminate
                else -> ToggleableState.Off
            }
            Row(
                modifier = Modifier.triStateToggleable(
                    state = allState,
                    enabled = uiState.items.isNotEmpty(),
                    role = Role.Checkbox,
                    onClick = { onIntent(CartIntent.ToggleSelectAll) },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TriStateCheckbox(
                    state = allState,
                    onClick = null,
                    enabled = uiState.items.isNotEmpty(),
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
                    onCheckout(uiState.items.filter(CartItem::selected).map(CartItem::id))
                },
                enabled = uiState.selectedQuantity > 0,
                modifier = Modifier.height(44.dp),
            ) {
                Text(stringResource(Res.string.cart_checkout, uiState.selectedQuantity))
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

private val productColors = listOf(
    Color(0xFFF7E8D3),
    Color(0xFFE2EFFA),
    Color(0xFFEFE7FB),
    Color(0xFFE6F4E4),
    Color(0xFFFCEBDC),
    Color(0xFFE3F1EF),
)

private fun productColor(itemId: Long): Color =
    productColors[(itemId % productColors.size).toInt()]

@Composable
private fun errorMessage(error: CartError): String = when (error) {
    CartError.LOAD_FAILED -> stringResource(Res.string.cart_error_load)
    CartError.UPDATE_FAILED -> stringResource(Res.string.cart_error_update)
}

@Preview
@Composable
private fun CartContentPreview() {
    AppTheme {
        CartContent(
            uiState = CartUiState(
                isLoading = false,
                items = listOf(
                    CartItem(1, "挂耳咖啡 · 10 包装", Money(1_250), 2, "☕", selected = true),
                    CartItem(2, "陶瓷马克杯 380ml", Money(880), 1, "🍵"),
                ),
            ),
            onIntent = {},
            onCheckout = {},
        )
    }
}

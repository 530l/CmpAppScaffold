package com.lyf.cmpdemo.feature.cart.presentation

import androidx.compose.foundation.background
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyf.cmpdemo.core.design.AppColors
import com.lyf.cmpdemo.core.util.formatPrice
import com.lyf.cmpdemo.feature.cart.domain.CartItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CartScreen(
    // Koin 注入（common 代码 viewModel() 必须带 initializer 的官方约束由 Koin 兜底：
    // koinViewModel 从 Koin 容器按类型解析，不依赖平台反射）
    viewModel: CartViewModel = koinViewModel(),
) {
    val uiState = viewModel.uiState
    Scaffold(
        containerColor = AppColors.PageBg,
        bottomBar = { SettleBar(viewModel) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // 顶栏（单屏 demo，无返回按钮）
            Text(
                text = "购物车",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TitleText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
            )
            // LazyColumn 按需组合可见行；key 用稳定 id，列表变化时做精准增量刷新
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.items, key = { it.id }) { item ->
                    CartItemRow(item = item, onToggle = { viewModel.onIntent(CartIntent.ToggleItem(item.id)) })
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(item: CartItem, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.CardBg)
            .toggleable(
                value = item.selected,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(item.bgColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = item.emoji, fontSize = 26.sp)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(text = item.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.TitleText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatPrice(item.priceCents)} × ${item.count}",
                fontSize = 13.sp,
                color = AppColors.SubText,
            )
        }
        // onCheckedChange = null：勾选交互由父级 toggleable 统一处理，
        // 行与勾选框合并为一个语义节点（无障碍读屏只念一个目标）
        Checkbox(
            checked = item.selected,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = AppColors.AccentRed),
        )
    }
}

@Composable
private fun SettleBar(viewModel: CartViewModel) {
    Surface(color = AppColors.CardBg, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 全选三态：全选 On / 部分选 Indeterminate / 全不选 Off
            val uiState = viewModel.uiState
            val allState = when {
                uiState.allSelected -> ToggleableState.On
                uiState.selectedCount > 0 -> ToggleableState.Indeterminate
                else -> ToggleableState.Off
            }
            Row(
                modifier = Modifier.triStateToggleable(
                    state = allState,
                    onClick = { viewModel.onIntent(CartIntent.ToggleSelectAll) },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TriStateCheckbox(
                    state = allState,
                    onClick = null, // 同上，交互由父级 triStateToggleable 处理
                    colors = CheckboxDefaults.colors(checkedColor = AppColors.AccentRed),
                )
                Text(text = "全选", fontSize = 14.sp, color = AppColors.TitleText)
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "合计", fontSize = 12.sp, color = AppColors.SubText)
                Text(
                    text = formatPrice(uiState.totalCents),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.AccentRed,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // 0 选中时置灰禁用（enabled=false 时 Material3 自动用灰色样式）
            // TODO 结算流程待接
            Button(
                onClick = {},
                enabled = uiState.selectedCount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentRed),
                modifier = Modifier.height(42.dp),
            ) {
                Text(text = "去结算(${uiState.selectedCount})")
            }
        }
    }
}

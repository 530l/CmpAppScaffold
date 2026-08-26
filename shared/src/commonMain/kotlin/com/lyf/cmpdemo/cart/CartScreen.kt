package com.lyf.cmpdemo.cart

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// 视觉常量；接入主题系统时可整体替换为 MaterialTheme.colorScheme 取值
private val AccentRed = Color(0xFFE93B3D)
private val PageBg = Color(0xFFF6F6F8)
private val CardBg = Color.White
private val TitleText = Color(0xFF1B1B1B)
private val SubText = Color(0xFF9A9A9A)

@Composable
fun CartScreen(
    // common 代码里 viewModel() 必须显式传 initializer：
    // 非 JVM 平台（iOS）没有类型反射，框架无法推断零参构造
    viewModel: CartViewModel = viewModel { CartViewModel() },
) {
    Scaffold(
        containerColor = PageBg,
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
                color = TitleText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
            )
            // LazyColumn 按需组合可见行；key 用稳定 id，列表变化时做精准增量刷新
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(viewModel.items, key = { it.id }) { item ->
                    CartItemRow(item = item, onToggle = { viewModel.toggleItem(item) })
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
            .background(CardBg)
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
            Text(text = item.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TitleText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatPrice(item.priceCents)} × ${item.count}",
                fontSize = 13.sp,
                color = SubText,
            )
        }
        // onCheckedChange = null：勾选交互由父级 toggleable 统一处理，
        // 行与勾选框合并为一个语义节点（无障碍读屏只念一个目标）
        Checkbox(
            checked = item.selected,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = AccentRed),
        )
    }
}

@Composable
private fun SettleBar(viewModel: CartViewModel) {
    Surface(color = CardBg, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 全选三态：全选 On / 部分选 Indeterminate / 全不选 Off
            val allState = when {
                viewModel.allSelected -> ToggleableState.On
                viewModel.selectedCount > 0 -> ToggleableState.Indeterminate
                else -> ToggleableState.Off
            }
            Row(
                modifier = Modifier.triStateToggleable(
                    state = allState,
                    onClick = viewModel::toggleSelectAll,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TriStateCheckbox(
                    state = allState,
                    onClick = null, // 同上，交互由父级 triStateToggleable 处理
                    colors = CheckboxDefaults.colors(checkedColor = AccentRed),
                )
                Text(text = "全选", fontSize = 14.sp, color = TitleText)
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "合计", fontSize = 12.sp, color = SubText)
                Text(
                    text = formatPrice(viewModel.totalCents),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentRed,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // 0 选中时置灰禁用（enabled=false 时 Material3 自动用灰色样式）
            // TODO 结算流程待接
            Button(
                onClick = {},
                enabled = viewModel.selectedCount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                modifier = Modifier.height(42.dp),
            ) {
                Text(text = "去结算(${viewModel.selectedCount})")
            }
        }
    }
}

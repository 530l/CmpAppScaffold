package com.lyf.cmp.feature.cart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyf.cmp.core.log.AppLogger
import com.lyf.cmp.core.model.Money
import com.lyf.cmp.feature.cart.data.CartRepository
import com.lyf.cmp.feature.cart.domain.CartItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val isLoading: Boolean = true,
    val items: List<CartItem> = emptyList(),
    val error: CartError? = null,
) {
    val selectedLineCount: Int get() = items.count(CartItem::selected)
    val selectedQuantity: Long
        get() = items.filter(CartItem::selected).sumOf { it.count.toLong() }
    val total: Money
        get() = items
            .filter(CartItem::selected)
            .fold(Money.zero()) { total, item -> total + item.unitPrice * item.count }
    val allSelected: Boolean get() = items.isNotEmpty() && items.all(CartItem::selected)
}

enum class CartError {
    LOAD_FAILED,
    UPDATE_FAILED,
}

sealed interface CartIntent {
    data class ToggleItem(val itemId: Long) : CartIntent
    data object ToggleSelectAll : CartIntent
    data object Retry : CartIntent
}

class CartViewModel(
    private val repository: CartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observeCart()
    }

    fun onIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.ToggleItem -> launchMutation {
                repository.toggleSelection(intent.itemId)
            }
            CartIntent.ToggleSelectAll -> launchMutation {
                repository.setAllSelected(!_uiState.value.allSelected)
            }
            CartIntent.Retry -> observeCart()
        }
    }

    private fun observeCart() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.ensureSeeded()
                repository.observeCartItems().collectLatest { items ->
                    _uiState.value = CartUiState(isLoading = false, items = items)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                AppLogger.error(TAG, error) { "购物车加载失败" }
                _uiState.update {
                    it.copy(isLoading = false, error = CartError.LOAD_FAILED)
                }
            }
        }
    }

    private fun launchMutation(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                AppLogger.error(TAG, error) { "购物车更新失败" }
                _uiState.update { it.copy(error = CartError.UPDATE_FAILED) }
            }
        }
    }

    private companion object {
        const val TAG = "CartViewModel"
    }
}

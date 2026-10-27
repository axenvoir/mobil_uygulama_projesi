package com.axenvoir.sktplus.ui.expiry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axenvoir.sktplus.domain.model.Product
import com.axenvoir.sktplus.domain.model.ExpiryStatus
import com.axenvoir.sktplus.domain.repository.ProductRepository
import com.axenvoir.sktplus.domain.util.expiryStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProductFilter {
    ALL, FRESH, EXPIRING_SOON, EXPIRED
}

data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val visibleProducts: List<Product> = emptyList(),
    val filter: ProductFilter = ProductFilter.ALL,
    val query: String = "",
    val currentDate: java.time.LocalDate = java.time.LocalDate.now(),
    val freshCount: Int = 0,
    val expiringCount: Int = 0,
    val expiredCount: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val filter = MutableStateFlow(ProductFilter.ALL)
    private val searchQuery = MutableStateFlow("")
    private val loaded = MutableStateFlow(false)

    val query: StateFlow<String> = searchQuery.asStateFlow()

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error.asStateFlow()

    val uiState: StateFlow<ProductListUiState> = combine(
        productRepository.observeAll().onEach { loaded.value = true },
        filter,
        searchQuery,
        com.axenvoir.sktplus.domain.util.todayFlow(),
        loaded,
    ) { products, selectedFilter, queryText, today, isLoaded ->
        val searched = if (queryText.isBlank()) products else products.filter {
            it.name.contains(queryText, ignoreCase = true) ||
                it.categoryName.contains(queryText, ignoreCase = true)
        }
        val freshCount = searched.count { expiryStatus(it.expiryDate, today) == ExpiryStatus.FRESH }
        val expiringCount = searched.count { expiryStatus(it.expiryDate, today) == ExpiryStatus.EXPIRING_SOON }
        val expiredCount = searched.count { expiryStatus(it.expiryDate, today) == ExpiryStatus.EXPIRED }
        val visible = when (selectedFilter) {
            ProductFilter.ALL -> searched
            ProductFilter.FRESH -> searched.filter { expiryStatus(it.expiryDate, today) == ExpiryStatus.FRESH }
            ProductFilter.EXPIRING_SOON -> searched.filter { expiryStatus(it.expiryDate, today) == ExpiryStatus.EXPIRING_SOON }
            ProductFilter.EXPIRED -> searched.filter { expiryStatus(it.expiryDate, today) == ExpiryStatus.EXPIRED }
        }
        ProductListUiState(
            products = searched,
            visibleProducts = visible,
            filter = selectedFilter,
            query = queryText,
            currentDate = today,
            freshCount = freshCount,
            expiringCount = expiringCount,
            expiredCount = expiredCount,
            isLoading = !isLoaded,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductListUiState(),
    )

    fun onFilterChange(newFilter: ProductFilter) {
        filter.value = newFilter
    }

    fun onQueryChange(query: String) {
        searchQuery.value = query
    }

    fun deleteProduct(product: Product, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { productRepository.deleteById(product.id) }
                .onSuccess { onDeleted() }
                .onFailure { if (it is kotlinx.coroutines.CancellationException) throw it else _error.value = true }
        }
    }

    fun restoreProduct(product: Product) {
        viewModelScope.launch {
            runCatching { productRepository.upsert(product) }
                .onFailure { _error.value = true }
        }
    }

    fun consumeError() {
        _error.value = false
    }
}

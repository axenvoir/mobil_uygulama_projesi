package com.axenvoir.sktplus.ui.expiry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axenvoir.sktplus.domain.model.Product
import com.axenvoir.sktplus.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProductFilter {
    ALL, FRESH, EXPIRING_SOON, EXPIRED
}

data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val filter: ProductFilter = ProductFilter.ALL,
    val query: String = "",
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
        productRepository.observeAll(),
        filter,
        searchQuery,
        loaded,
    ) { products, selectedFilter, queryText, isLoaded ->
        val filtered = if (queryText.isBlank()) products else products.filter {
            it.name.contains(queryText, ignoreCase = true) ||
                it.categoryName.contains(queryText, ignoreCase = true)
        }
        ProductListUiState(
            products = filtered,
            filter = selectedFilter,
            query = queryText,
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
                .onFailure { _error.value = true }
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

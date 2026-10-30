package com.axenvoir.expira.ui.expiry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axenvoir.expira.domain.model.ExpiryStatus
import com.axenvoir.expira.domain.model.Product
import com.axenvoir.expira.domain.repository.ProductRepository
import com.axenvoir.expira.domain.util.expiryStatus
import com.axenvoir.expira.domain.util.normalizeSearchText
import com.axenvoir.expira.domain.util.todayFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProductFilter {
    ALL,
    FRESH,
    EXPIRING_SOON,
    EXPIRED,
}

data class ProductListUiState(
    /** Products after search, used for the summary and the selected filter. */
    val products: List<Product> = emptyList(),
    /** The complete repository result, needed to distinguish a real empty state. */
    val allProducts: List<Product> = emptyList(),
    val visibleProducts: List<Product> = emptyList(),
    val filter: ProductFilter = ProductFilter.ALL,
    val query: String = "",
    val hasActiveSearch: Boolean = false,
    val currentDate: LocalDate = LocalDate.now(),
    val freshCount: Int = 0,
    val expiringCount: Int = 0,
    val expiredCount: Int = 0,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
)

private data class ProductSourceState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
)

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val filter = MutableStateFlow(ProductFilter.ALL)
    private val searchQuery = MutableStateFlow("")
    val query: StateFlow<String> = searchQuery.asStateFlow()

    private val sourceState = MutableStateFlow(ProductSourceState())
    private var observationJob: Job? = null

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error.asStateFlow()

    val uiState: StateFlow<ProductListUiState> = combine(
        sourceState,
        filter,
        searchQuery,
        todayFlow(),
    ) { source, selectedFilter, queryText, today ->
        val allProducts = source.products
        val normalizedQuery = normalizeSearchText(queryText)
        val searched = if (normalizedQuery.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { product ->
                normalizeSearchText(product.name).contains(normalizedQuery) ||
                    normalizeSearchText(product.categoryName).contains(normalizedQuery)
            }
        }

        val freshCount = searched.count {
            expiryStatus(it.expiryDate, today) == ExpiryStatus.FRESH
        }
        val expiringCount = searched.count {
            expiryStatus(it.expiryDate, today) == ExpiryStatus.EXPIRING_SOON
        }
        val expiredCount = searched.count {
            expiryStatus(it.expiryDate, today) == ExpiryStatus.EXPIRED
        }
        val visible = when (selectedFilter) {
            ProductFilter.ALL -> searched
            ProductFilter.FRESH -> searched.filter {
                expiryStatus(it.expiryDate, today) == ExpiryStatus.FRESH
            }
            ProductFilter.EXPIRING_SOON -> searched.filter {
                expiryStatus(it.expiryDate, today) == ExpiryStatus.EXPIRING_SOON
            }
            ProductFilter.EXPIRED -> searched.filter {
                expiryStatus(it.expiryDate, today) == ExpiryStatus.EXPIRED
            }
        }

        ProductListUiState(
            products = searched,
            allProducts = allProducts,
            visibleProducts = visible,
            filter = selectedFilter,
            query = queryText,
            hasActiveSearch = normalizedQuery.isNotEmpty(),
            currentDate = today,
            freshCount = freshCount,
            expiringCount = expiringCount,
            expiredCount = expiredCount,
            isLoading = source.isLoading,
            loadFailed = source.loadFailed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductListUiState(),
    )

    init {
        observeProducts()
    }

    fun retryLoad() {
        observeProducts()
    }

    private fun observeProducts() {
        observationJob?.cancel()
        sourceState.value = sourceState.value.copy(isLoading = true, loadFailed = false)
        observationJob = viewModelScope.launch {
            var hasEmitted = false
            try {
                productRepository.observeAll().collect { products ->
                    hasEmitted = true
                    sourceState.value = ProductSourceState(
                        products = products,
                        isLoading = false,
                        loadFailed = false,
                    )
                }
                if (!hasEmitted) {
                    sourceState.value = sourceState.value.copy(isLoading = false, loadFailed = true)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                sourceState.value = sourceState.value.copy(isLoading = false, loadFailed = true)
            }
        }
    }

    fun onFilterChange(newFilter: ProductFilter) {
        filter.value = newFilter
    }

    fun onQueryChange(query: String) {
        // Keep exactly what the user entered; only the matching key is normalized.
        searchQuery.value = query
    }

    fun deleteProduct(product: Product, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { productRepository.deleteById(product.id) }
                .onSuccess { onDeleted() }
                .onFailure {
                    if (it is CancellationException) {
                        throw it
                    }
                    _error.value = true
                }
        }
    }

    fun restoreProduct(product: Product) {
        viewModelScope.launch {
            runCatching { productRepository.upsert(product) }
                .onFailure {
                    if (it is CancellationException) {
                        throw it
                    }
                    _error.value = true
                }
        }
    }

    fun consumeError() {
        _error.value = false
    }
}

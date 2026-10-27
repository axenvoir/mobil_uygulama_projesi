package com.axenvoir.sktplus.ui.expiry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axenvoir.sktplus.domain.model.Category
import com.axenvoir.sktplus.domain.model.Product
import com.axenvoir.sktplus.domain.repository.ProductRepository
import com.axenvoir.sktplus.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ProductEditUiState(
    val name: String = "",
    val quantityText: String = "1",
    val selectedCategoryId: Long? = null,
    val newCategoryName: String = "",
    val expiryDate: LocalDate? = null,
    val storageLocation: String = "",
    val categories: List<Category> = emptyList(),
    val error: ProductEditError? = null,
)

private data class SelectionState(
    val categoryId: Long?,
    val newCategoryName: String,
    val expiryDate: LocalDate?,
    val storageLocation: String,
)

sealed interface ProductEditError {
    data object EmptyName : ProductEditError
    data object MissingExpiryDate : ProductEditError
    data object MissingCategory : ProductEditError
    data object InvalidQuantity : ProductEditError
    data object Generic : ProductEditError
}

@HiltViewModel
class ProductEditViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<Long>(Screen.EditProduct.ARG_PRODUCT_ID) ?: 0L
    val isEditMode: Boolean = productId != 0L

    private val name = MutableStateFlow("")
    private val quantityText = MutableStateFlow("1")
    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val newCategoryName = MutableStateFlow("")
    private val expiryDate = MutableStateFlow<LocalDate?>(null)
    private val storageLocation = MutableStateFlow("")
    private val error = MutableStateFlow<ProductEditError?>(null)

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    val uiState: StateFlow<ProductEditUiState> = combine(
        productRepository.observeCategories(),
        name,
        quantityText,
        combine(selectedCategoryId, newCategoryName, expiryDate, storageLocation) { cat, newCat, date, loc ->
            SelectionState(categoryId = cat, newCategoryName = newCat, expiryDate = date, storageLocation = loc)
        },
        error,
    ) { categories, nameValue, quantityValue, selection, errorValue ->
        ProductEditUiState(
            name = nameValue,
            quantityText = quantityValue,
            selectedCategoryId = selection.categoryId,
            newCategoryName = selection.newCategoryName,
            expiryDate = selection.expiryDate,
            storageLocation = selection.storageLocation,
            categories = categories,
            error = errorValue,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductEditUiState(),
    )

    private var originalName: String = ""
    private var originalQuantityText: String = ""
    private var originalCategoryId: Long? = null
    private var originalExpiryDate: LocalDate? = null
    private var originalNewCategoryName: String = ""
    private var originalStorageLocation: String = ""
    private val loaded = MutableStateFlow(false)

    val isDirty: StateFlow<Boolean> = combine(
        uiState,
        loaded,
    ) { state, isLoaded ->
        isLoaded && (
            state.name != originalName ||
                state.quantityText != originalQuantityText ||
                state.selectedCategoryId != originalCategoryId ||
                state.expiryDate != originalExpiryDate ||
                state.newCategoryName != originalNewCategoryName ||
                state.storageLocation != originalStorageLocation
            )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    init {
        if (isEditMode) {
            viewModelScope.launch {
                val product = productRepository.observeById(productId).first()
                if (product != null) {
                    originalName = product.name
                    originalQuantityText = product.quantity.toString()
                    originalCategoryId = product.categoryId
                    originalExpiryDate = product.expiryDate
                    originalNewCategoryName = ""
                    originalStorageLocation = product.storageLocation
                    name.value = product.name
                    quantityText.value = product.quantity.toString()
                    selectedCategoryId.value = product.categoryId
                    expiryDate.value = product.expiryDate
                    storageLocation.value = product.storageLocation
                    loaded.value = true
                }
            }
        } else {
            loaded.value = true
        }
    }

    fun onNameChange(value: String) {
        name.value = value
    }

    fun onQuantityChange(value: String) {
        quantityText.value = value.filter { it.isDigit() }.take(4)
    }

    fun onCategorySelect(categoryId: Long) {
        selectedCategoryId.value = categoryId
        newCategoryName.value = ""
    }

    fun onNewCategoryNameChange(value: String) {
        newCategoryName.value = value
        selectedCategoryId.value = null
    }

    fun onExpiryDateChange(date: LocalDate) {
        expiryDate.value = date
    }

    fun onStorageLocationChange(location: String) {
        storageLocation.value = location
    }

    fun save() {
        val trimmedName = name.value.trim()
        val quantity = quantityText.value.toIntOrNull() ?: 0
        val date = expiryDate.value
        val categoryId = selectedCategoryId.value

        when {
            trimmedName.isEmpty() -> {
                error.value = ProductEditError.EmptyName
                return
            }
            date == null -> {
                error.value = ProductEditError.MissingExpiryDate
                return
            }
            quantity < 1 -> {
                error.value = ProductEditError.InvalidQuantity
                return
            }
        }

        viewModelScope.launch {
            try {
                val resolvedCategoryId = categoryId ?: run {
                    val customName = newCategoryName.value.trim()
                    if (customName.isEmpty()) throw IllegalArgumentException()
                    productRepository.addCategory(customName)
                }
                val existing = if (isEditMode) productRepository.observeById(productId).first() else null
                val product = Product(
                    id = existing?.id ?: 0L,
                    name = trimmedName,
                    categoryId = resolvedCategoryId,
                    categoryName = "",
                    expiryDate = date,
                    quantity = quantity,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    storageLocation = storageLocation.value,
                )
                productRepository.upsert(product)
                _saved.value = true
            } catch (e: IllegalArgumentException) {
                error.value = ProductEditError.MissingCategory
            } catch (e: Exception) {
                error.value = ProductEditError.Generic
            }
        }
    }

    fun consumeError() {
        error.value = null
    }
}

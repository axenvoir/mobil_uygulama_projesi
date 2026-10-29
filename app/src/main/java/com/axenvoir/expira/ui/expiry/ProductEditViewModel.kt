package com.axenvoir.expira.ui.expiry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axenvoir.expira.domain.model.Category
import com.axenvoir.expira.domain.model.Product
import com.axenvoir.expira.domain.repository.ProductRepository
import com.axenvoir.expira.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    val isHydrated: Boolean = false,
    val editRecordMissing: Boolean = false,
    val loadFailed: Boolean = false,
)

private data class SelectionState(
    val categoryId: Long?,
    val newCategoryName: String,
    val expiryDate: LocalDate?,
    val storageLocation: String,
)

private data class FormStatus(
    val isHydrated: Boolean,
    val editRecordMissing: Boolean,
    val loadFailed: Boolean,
)

private data class FormInput(
    val name: String,
    val quantityText: String,
    val selection: SelectionState,
    val error: ProductEditError?,
    val status: FormStatus,
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
    private val productId: Long =
        savedStateHandle.get<Long>(Screen.EditProduct.ARG_PRODUCT_ID) ?: 0L
    val isEditMode: Boolean = productId != 0L

    private val name = MutableStateFlow("")
    private val quantityText = MutableStateFlow("1")
    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val newCategoryName = MutableStateFlow("")
    private val expiryDate = MutableStateFlow<LocalDate?>(null)
    private val storageLocation = MutableStateFlow("")
    private val error = MutableStateFlow<ProductEditError?>(null)

    private val loaded = MutableStateFlow(false)
    private val categoriesLoaded = MutableStateFlow(false)
    private val _editRecordMissing = MutableStateFlow(false)
    private val _loadFailed = MutableStateFlow(false)
    private val _saved = MutableStateFlow(false)
    private val _isSaving = MutableStateFlow(false)

    val saved: StateFlow<Boolean> = _saved.asStateFlow()
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val categories = productRepository.observeCategories().onEach {
        categoriesLoaded.value = true
    }
    private val formStatus = combine(
        loaded,
        categoriesLoaded,
        _editRecordMissing,
        _loadFailed,
    ) { isLoaded, areCategoriesLoaded, recordMissing, loadFailed ->
        FormStatus(
            isHydrated = isLoaded && areCategoriesLoaded && !loadFailed,
            editRecordMissing = recordMissing,
            loadFailed = loadFailed,
        )
    }
    private val formInput = combine(
        name,
        quantityText,
        combine(selectedCategoryId, newCategoryName, expiryDate, storageLocation) {
                categoryId, newCategory, date, location ->
            SelectionState(
                categoryId = categoryId,
                newCategoryName = newCategory,
                expiryDate = date,
                storageLocation = location,
            )
        },
        error,
        formStatus,
    ) { nameValue, quantityValue, selection, errorValue, status ->
        FormInput(
            name = nameValue,
            quantityText = quantityValue,
            selection = selection,
            error = errorValue,
            status = status,
        )
    }

    val uiState: StateFlow<ProductEditUiState> = combine(categories, formInput) {
        categoriesValue, input ->
        ProductEditUiState(
            name = input.name,
            quantityText = input.quantityText,
            selectedCategoryId = input.selection.categoryId,
            newCategoryName = input.selection.newCategoryName,
            expiryDate = input.selection.expiryDate,
            storageLocation = input.selection.storageLocation,
            categories = categoriesValue,
            error = input.error,
            isHydrated = input.status.isHydrated,
            editRecordMissing = input.status.editRecordMissing,
            loadFailed = input.status.loadFailed,
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

    val isDirty: StateFlow<Boolean> = combine(uiState, loaded) { state, isLoaded ->
        isLoaded && state.isHydrated && !state.editRecordMissing && (
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
                try {
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
                        newCategoryName.value = ""
                        expiryDate.value = product.expiryDate
                        storageLocation.value = product.storageLocation
                    } else {
                        _editRecordMissing.value = true
                    }
                } catch (exception: Exception) {
                    if (exception is kotlinx.coroutines.CancellationException) {
                        throw exception
                    }
                    _loadFailed.value = true
                    error.value = ProductEditError.Generic
                }
                loaded.value = true
            }
        } else {
            originalQuantityText = "1"
            loaded.value = true
        }
    }

    fun onNameChange(value: String) {
        name.value = value
        clearError()
    }

    fun onQuantityChange(value: String) {
        // Do not filter or take a prefix: invalid pasted input remains visible and is validated on save.
        quantityText.value = value
        clearError()
    }

    fun onCategorySelect(categoryId: Long) {
        selectedCategoryId.value = categoryId
        newCategoryName.value = ""
        clearError()
    }

    fun onNewCategoryNameChange(value: String) {
        newCategoryName.value = value
        selectedCategoryId.value = null
        clearError()
    }

    fun onExpiryDateChange(date: LocalDate) {
        expiryDate.value = date
        clearError()
    }

    fun onStorageLocationChange(location: String) {
        storageLocation.value = location
        clearError()
    }

    fun save() {
        if (
            _isSaving.value ||
            _saved.value ||
            !loaded.value ||
            !categoriesLoaded.value ||
            _loadFailed.value ||
            _editRecordMissing.value
        ) {
            return
        }

        error.value = null
        val trimmedName = name.value.trim()
        val quantity = quantityText.value.toIntOrNull()
        val date = expiryDate.value
        val categoryId = selectedCategoryId.value
        val customCategoryName = newCategoryName.value.trim()
        when {
            trimmedName.isEmpty() -> {
                error.value = ProductEditError.EmptyName
                return
            }
            date == null -> {
                error.value = ProductEditError.MissingExpiryDate
                return
            }
            quantity == null || quantity < 1 -> {
                error.value = ProductEditError.InvalidQuantity
                return
            }
            categoryId == null && customCategoryName.isEmpty() -> {
                error.value = ProductEditError.MissingCategory
                return
            }
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val existing = if (isEditMode) {
                    productRepository.observeById(productId).first()
                } else {
                    null
                }
                if (isEditMode && existing == null) {
                    _editRecordMissing.value = true
                    return@launch
                }

                val resolvedCategoryId = categoryId ?: productRepository.addCategory(customCategoryName)
                val product = Product(
                    id = existing?.id ?: 0L,
                    name = trimmedName,
                    categoryId = resolvedCategoryId,
                    categoryName = existing?.categoryName.orEmpty(),
                    expiryDate = date,
                    quantity = quantity,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    storageLocation = storageLocation.value,
                )
                productRepository.upsert(product)
                _saved.value = true
            } catch (exception: Exception) {
                if (exception is kotlinx.coroutines.CancellationException) {
                    throw exception
                }
                error.value = ProductEditError.Generic
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun consumeError() {
        error.value = null
    }

    /** The missing-record state is persistent so it can never reopen a blank edit form. */
    fun consumeEditRecordMissing() = Unit

    private fun clearError() {
        error.value = null
    }
}

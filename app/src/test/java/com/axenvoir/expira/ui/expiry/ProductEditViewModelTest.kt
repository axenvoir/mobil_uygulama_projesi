package com.axenvoir.expira.ui.expiry

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.axenvoir.expira.domain.model.Category
import com.axenvoir.expira.domain.model.Product
import com.axenvoir.expira.domain.repository.ProductRepository
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductEditViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpMainDispatcher() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun categoryLoadFailureCanBeRetriedBeforeFormBecomesReady() = runTest {
        val repository = RetryableCategoryRepository()
        val viewModel = ProductEditViewModel(repository, SavedStateHandle())
        val store = ViewModelStore().apply { put("product-edit", viewModel) }
        try {
            val collection = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.loadFailed).isTrue()
            assertThat(viewModel.uiState.value.isHydrated).isFalse()

            viewModel.retryLoad()
            advanceUntilIdle()

            assertThat(repository.categoryObservationCount).isAtLeast(2)
            assertThat(viewModel.uiState.value.loadFailed).isFalse()
            assertThat(viewModel.uiState.value.isHydrated).isTrue()
            assertThat(viewModel.uiState.value.categories).containsExactly(Category(id = 1L, name = "Food", isDefault = true))
            collection.cancel()
        } finally {
            store.clear()
        }
    }

    private class RetryableCategoryRepository : ProductRepository {
        var categoryObservationCount = 0
            private set

        override fun observeAll(): Flow<List<Product>> = flowOf(emptyList())

        override fun observeById(id: Long): Flow<Product?> = flowOf(null)

        override suspend fun upsert(product: Product): Long = product.id

        override suspend fun deleteById(id: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = flow {
            categoryObservationCount += 1
            if (categoryObservationCount == 1) throw IOException("Simulated category read failure")
            emit(listOf(Category(id = 1L, name = "Food", isDefault = true)))
            awaitCancellation()
        }

        override suspend fun addCategory(name: String): Long = 1L

        override suspend fun seedDefaultCategoriesIfEmpty() = Unit
    }
}

package com.axenvoir.expira.ui.expiry

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductListViewModelTest {
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
    fun observationFailureCanBeRetried() = runTest {
        val repository = RetryableProductRepository()
        val viewModel = ProductListViewModel(repository)
        val store = ViewModelStore().apply { put("product-list", viewModel) }
        try {
            val collection = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            assertThat(viewModel.uiState.value.loadFailed).isTrue()
            assertThat(viewModel.uiState.value.isLoading).isFalse()

            viewModel.retryLoad()
            runCurrent()

            assertThat(repository.observationCount).isAtLeast(2)
            assertThat(viewModel.uiState.value.loadFailed).isFalse()
            assertThat(viewModel.uiState.value.isLoading).isFalse()
            collection.cancel()
        } finally {
            store.clear()
        }
    }

    private class RetryableProductRepository : ProductRepository {
        var observationCount = 0
            private set

        override fun observeAll(): Flow<List<Product>> = flow {
            observationCount += 1
            if (observationCount == 1) throw IOException("Simulated local data read failure")
            emit(emptyList())
            awaitCancellation()
        }

        override fun observeById(id: Long): Flow<Product?> = flowOf(null)

        override suspend fun upsert(product: Product): Long = product.id

        override suspend fun deleteById(id: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())

        override suspend fun addCategory(name: String): Long = 1L

        override suspend fun seedDefaultCategoriesIfEmpty() = Unit
    }
}

package com.axenvoir.expira

import com.axenvoir.expira.domain.model.Category
import com.axenvoir.expira.domain.model.Product
import com.axenvoir.expira.domain.repository.ProductRepository
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ExpiraApplicationTest {
    @Test
    fun categorySeedFailureIsReturnedForLoggingInsteadOfEscaping() = runTest {
        val failure = IOException("Simulated category seeding failure")
        val repository = StubProductRepository {
            throw failure
        }

        val result = seedDefaultCategoriesSafely(repository)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isSameInstanceAs(failure)
    }

    private class StubProductRepository(
        private val seed: suspend () -> Unit,
    ) : ProductRepository {
        override fun observeAll(): Flow<List<Product>> = flowOf(emptyList())

        override fun observeById(id: Long): Flow<Product?> = flowOf(null)

        override suspend fun upsert(product: Product): Long = product.id

        override suspend fun deleteById(id: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())

        override suspend fun addCategory(name: String): Long = 1L

        override suspend fun seedDefaultCategoriesIfEmpty() = seed()
    }
}

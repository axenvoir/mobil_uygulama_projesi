package com.axenvoir.expira

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.axenvoir.expira.domain.repository.ProductRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ExpiraApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
        applicationScope.launch {
            seedDefaultCategoriesSafely(productRepository).onFailure { error ->
                Log.e(TAG, "Unable to seed default categories", error)
            }
        }
    }

    private companion object {
        const val TAG = "ExpiraApplication"
    }
}

internal suspend fun seedDefaultCategoriesSafely(
    productRepository: ProductRepository,
): Result<Unit> = try {
    productRepository.seedDefaultCategoriesIfEmpty()
    Result.success(Unit)
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (error: Exception) {
    Result.failure(error)
}

package com.axenvoir.expira.notification

import androidx.work.Operation
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val directExecutor = Executor { command -> command.run() }

internal suspend fun Operation.awaitCompletion() {
    val completed = withTimeoutOrNull(OPERATION_TIMEOUT_MILLIS) {
        awaitResult()
        true
    } ?: false
    check(completed) { "WorkManager operation did not complete before its timeout" }
}

private suspend fun Operation.awaitResult() {
    suspendCancellableCoroutine { continuation ->
        val future = result
        future.addListener(
            {
                try {
                    future.get()
                    continuation.resume(Unit)
                } catch (exception: ExecutionException) {
                    continuation.resumeWithException(exception.cause ?: exception)
                } catch (cancellation: CancellationException) {
                    continuation.resumeWithException(
                        IllegalStateException("WorkManager operation was cancelled", cancellation),
                    )
                } catch (interrupted: InterruptedException) {
                    Thread.currentThread().interrupt()
                    continuation.resumeWithException(interrupted)
                } catch (exception: Exception) {
                    continuation.resumeWithException(exception)
                }
            },
            directExecutor,
        )
    }
}

private const val OPERATION_TIMEOUT_MILLIS = 4_000L

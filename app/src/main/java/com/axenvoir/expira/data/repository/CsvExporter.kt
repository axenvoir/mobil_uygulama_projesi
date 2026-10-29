package com.axenvoir.expira.data.repository

import android.content.Context
import android.net.Uri
import com.axenvoir.expira.domain.model.Note
import com.axenvoir.expira.domain.model.Product
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.io.Writer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class CsvExporter @Inject constructor() {

    suspend fun export(
        context: Context,
        uri: Uri,
        products: List<Product>,
        notes: List<Note>,
    ): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.writer(Charsets.UTF_8).use { writer ->
                    writer.write('\uFEFF'.code)
                    val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
                    writer.appendCsvRow(
                        listOf(
                            "type",
                            "name",
                            "category",
                            "quantity",
                            "expiry_date",
                            "storage_location",
                            "title",
                            "content",
                            "pinned",
                            "updated_at",
                        )
                    )
                    products.forEach { product ->
                        writer.appendCsvRow(
                            listOf(
                                "product",
                                product.name,
                                product.categoryName,
                                product.quantity.toString(),
                                product.expiryDate.format(dateFormatter),
                                product.storageLocation,
                                "",
                                "",
                                "",
                                "",
                            )
                        )
                    }
                    notes.forEach { note ->
                        writer.appendCsvRow(
                            listOf(
                                "note",
                                "",
                                "",
                                "",
                                "",
                                "",
                                note.title,
                                note.content,
                                note.isPinned.toString(),
                                Instant.ofEpochMilli(note.updatedAt).toString(),
                            )
                        )
                    }
                }
            } ?: throw IllegalStateException("Cannot open output stream")
            Result.success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun Writer.appendCsvRow(values: List<String>) {
        append(values.joinToString(",", transform = ::escape))
        append("\r\n")
    }

    private fun escape(value: String): String {
        val spreadsheetSafe = if (value.firstOrNull { !it.isWhitespace() } in FORMULA_PREFIXES) {
            "'$value"
        } else {
            value
        }
        return if (spreadsheetSafe.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"${spreadsheetSafe.replace("\"", "\"\"")}\""
        } else {
            spreadsheetSafe
        }
    }

    private companion object {
        val FORMULA_PREFIXES = setOf('=', '+', '-', '@')
    }
}

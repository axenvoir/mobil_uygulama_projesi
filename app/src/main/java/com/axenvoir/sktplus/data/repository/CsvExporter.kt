package com.axenvoir.sktplus.data.repository

import android.content.Context
import android.net.Uri
import com.axenvoir.sktplus.domain.model.Note
import com.axenvoir.sktplus.domain.model.Product
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExporter @Inject constructor() {

    suspend fun export(
        context: Context,
        uri: Uri,
        products: List<Product>,
        notes: List<Note>,
    ): Result<Unit> = runCatching {
        val csv = buildString {
            appendLine("type,name,category,quantity,expiry_date,days_left,title,content,pinned,updated_at")
            val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
            products.forEach { p ->
                appendLine(
                    listOf(
                        "product",
                        escape(p.name),
                        escape(p.categoryName),
                        p.quantity.toString(),
                        p.expiryDate.format(dateFormatter),
                        "",
                        "",
                        "",
                        "",
                        "",
                    ).joinToString(",")
                )
            }
            notes.forEach { n ->
                appendLine(
                    listOf(
                        "note",
                        "",
                        "",
                        "",
                        "",
                        "",
                        escape(n.title),
                        escape(n.content),
                        n.isPinned.toString(),
                        java.time.Instant.ofEpochMilli(n.updatedAt).toString(),
                    ).joinToString(",")
                )
            }
        }
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(csv.toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("Cannot open output stream")
    }

    private fun escape(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}

package com.axenvoir.sktplus.domain.repository

import com.axenvoir.sktplus.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeAll(): Flow<List<Note>>
    fun observeById(id: Long): Flow<Note?>
    suspend fun upsert(note: Note): Long
    suspend fun deleteById(id: Long)
    suspend fun setPinned(id: Long, isPinned: Boolean)
}

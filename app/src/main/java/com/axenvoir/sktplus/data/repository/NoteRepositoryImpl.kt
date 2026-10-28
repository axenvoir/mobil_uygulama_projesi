package com.axenvoir.sktplus.data.repository

import com.axenvoir.sktplus.data.local.NoteDao
import com.axenvoir.sktplus.data.local.NoteEntity
import com.axenvoir.sktplus.domain.model.Note
import com.axenvoir.sktplus.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
) : NoteRepository {

    override fun observeAll(): Flow<List<Note>> =
        noteDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Note?> =
        noteDao.observeById(id).map { it?.toDomain() }

    override suspend fun upsert(note: Note): Long {
        val entity = NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            isPinned = note.isPinned,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
        )
        return if (note.id == 0L) noteDao.insert(entity) else {
            noteDao.upsert(entity)
            note.id
        }
    }

    override suspend fun deleteById(id: Long) = noteDao.deleteById(id)

    override suspend fun setPinned(id: Long, isPinned: Boolean) {
        noteDao.setPinned(id = id, isPinned = isPinned, updatedAt = System.currentTimeMillis())
    }

    private fun NoteEntity.toDomain() = Note(
        id = id,
        title = title,
        content = content,
        isPinned = isPinned,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

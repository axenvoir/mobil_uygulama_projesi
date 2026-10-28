package com.axenvoir.expira.di

import com.axenvoir.expira.data.repository.NoteRepositoryImpl
import com.axenvoir.expira.data.repository.ProductRepositoryImpl
import com.axenvoir.expira.domain.repository.NoteRepository
import com.axenvoir.expira.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
}

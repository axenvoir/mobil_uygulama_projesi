package com.axenvoir.sktplus.di

import com.axenvoir.sktplus.data.repository.NoteRepositoryImpl
import com.axenvoir.sktplus.data.repository.ProductRepositoryImpl
import com.axenvoir.sktplus.domain.repository.NoteRepository
import com.axenvoir.sktplus.domain.repository.ProductRepository
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

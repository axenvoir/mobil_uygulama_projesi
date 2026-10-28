package com.axenvoir.expira.di

import com.axenvoir.expira.billing.BillingManager
import com.axenvoir.expira.billing.BillingRepository
import com.axenvoir.expira.billing.BillingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {

    @Binds
    @Singleton
    abstract fun bindBillingRepository(impl: BillingRepositoryImpl): BillingRepository
}

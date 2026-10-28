package com.axenvoir.sktplus.di

import com.axenvoir.sktplus.billing.BillingManager
import com.axenvoir.sktplus.billing.BillingRepository
import com.axenvoir.sktplus.billing.BillingRepositoryImpl
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

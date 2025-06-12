package com.example.facefit.di

import com.example.facefit.data.repository.AuthRepositoryImpl
import com.example.facefit.data.repository.CartRepositoryImpl
import com.example.facefit.data.repository.GlassesRepositoryImpl
import com.example.facefit.data.repository.ReviewRepositoryImpl
import com.example.facefit.domain.repository.AuthRepository
import com.example.facefit.domain.repository.CartRepository
import com.example.facefit.domain.repository.GlassesRepository
import com.example.facefit.domain.repository.ReviewRepository
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
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGlassesRepository(impl: GlassesRepositoryImpl): GlassesRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository


    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

}

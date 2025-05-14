package com.example.facefit.di

import com.example.facefit.data.remote.ApiService
import com.example.facefit.data.repository.AuthRepositoryImpl
import com.example.facefit.data.repository.FavoritesRepositoryImpl
import com.example.facefit.data.repository.GlassesRepositoryImpl
import com.example.facefit.domain.repository.AuthRepository
import com.example.facefit.domain.repository.FavoritesRepository
import com.example.facefit.domain.repository.GlassesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
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


}

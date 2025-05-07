package com.example.facefit.di

import com.example.facefit.domain.repository.AuthRepository
import com.example.facefit.domain.repository.GlassesRepository
import com.example.facefit.domain.usecases.GetBestSellersUseCase
import com.example.facefit.domain.usecases.GetNewArrivalsUseCase
import com.example.facefit.domain.usecases.LoginUseCase
import com.example.facefit.domain.usecases.SignUpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideSignUpUseCase(authRepository: AuthRepository): SignUpUseCase {
        return SignUpUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase {
        return LoginUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideGetBestSellersUseCase(repository: GlassesRepository): GetBestSellersUseCase {
        return GetBestSellersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetNewArrivalsUseCase(repository: GlassesRepository): GetNewArrivalsUseCase {
        return GetNewArrivalsUseCase(repository)
    }
}
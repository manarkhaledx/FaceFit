package com.example.facefit.di

import android.content.Context
import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.remote.ApiService
import com.example.facefit.data.repository.UserRepositoryImpl
import com.example.facefit.domain.repository.UserRepository
import com.example.facefit.domain.usecases.auth.GetUserProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        tokenManager: TokenManager
    ): UserRepository {
        return UserRepositoryImpl(apiService, tokenManager)
    }


    @Provides
    @Singleton
    fun provideGetUserProfileUseCase(repository: UserRepository): GetUserProfileUseCase {
        return GetUserProfileUseCase(repository)
    }
}

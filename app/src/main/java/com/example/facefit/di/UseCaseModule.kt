package com.example.facefit.di

import com.example.facefit.data.remote.ApiService
import com.example.facefit.data.repository.FavoritesRepositoryImpl
import com.example.facefit.domain.repository.AuthRepository
import com.example.facefit.domain.repository.FavoritesRepository
import com.example.facefit.domain.repository.GlassesRepository
import com.example.facefit.domain.usecases.glasses.GetBestSellersUseCase
import com.example.facefit.domain.usecases.glasses.GetNewArrivalsUseCase
import com.example.facefit.domain.usecases.auth.LoginUseCase
import com.example.facefit.domain.usecases.auth.SignUpUseCase
import com.example.facefit.domain.usecases.favorites.GetFavoritesUseCase
import com.example.facefit.domain.usecases.favorites.ToggleFavoriteUseCase
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

    @Provides
    @Singleton
    fun provideGetFavoritesUseCase(repository: FavoritesRepository): GetFavoritesUseCase {
        return GetFavoritesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideToggleFavoriteUseCase(repository: FavoritesRepository): ToggleFavoriteUseCase {
        return ToggleFavoriteUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideFavoritesRepository(
        apiService: ApiService
    ): FavoritesRepository {
        return FavoritesRepositoryImpl(apiService)
    }
}
package com.example.facefit.di

import com.example.facefit.data.remote.ApiService
import com.example.facefit.data.repository.FavoritesRepositoryImpl
import com.example.facefit.data.repository.UserRepositoryImpl // Ensure UserRepositoryImpl is imported
import com.example.facefit.domain.repository.AuthRepository
import com.example.facefit.domain.repository.FavoritesRepository
import com.example.facefit.domain.repository.GlassesRepository
import com.example.facefit.domain.repository.OrderRepository
import com.example.facefit.domain.repository.ReviewRepository
import com.example.facefit.domain.repository.UserRepository // Ensure UserRepository is imported
import com.example.facefit.domain.usecases.glasses.GetBestSellersUseCase
import com.example.facefit.domain.usecases.glasses.GetNewArrivalsUseCase
import com.example.facefit.domain.usecases.auth.LoginUseCase
import com.example.facefit.domain.usecases.auth.SignUpUseCase
import com.example.facefit.domain.usecases.auth.UpdateUserProfileUseCase
import com.example.facefit.domain.usecases.auth.GetUserProfileUseCase
import com.example.facefit.domain.usecases.auth.UploadProfilePictureUseCase // Make sure this import is present!
import com.example.facefit.domain.usecases.favorites.GetFavoritesUseCase
import com.example.facefit.domain.usecases.favorites.ToggleFavoriteUseCase
import com.example.facefit.domain.usecases.order.CreateOrderUseCase
import com.example.facefit.domain.usecases.reviews.GetReviewsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideGetReviewsUseCase(repository: ReviewRepository): GetReviewsUseCase {
        return GetReviewsUseCase(repository)
    }

    // Ensure these UserProfile UseCases are provided
    @Provides
    @Singleton
    fun provideGetUserProfileUseCase(userRepository: UserRepository): GetUserProfileUseCase {
        return GetUserProfileUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateUserProfileUseCase(userRepository: UserRepository): UpdateUserProfileUseCase {
        return UpdateUserProfileUseCase(userRepository)
    }

    // Ensure this new UseCase is provided
    @Provides
    @Singleton
    fun provideUploadProfilePictureUseCase(userRepository: UserRepository): UploadProfilePictureUseCase {
        return UploadProfilePictureUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideCreateOrderUseCase(repository: OrderRepository): CreateOrderUseCase {
        return CreateOrderUseCase(repository)
    }

}
package com.example.facefit.di

import com.example.facefit.domain.repository.CartRepository
import com.example.facefit.domain.usecases.cart.AddToCartUseCase
import com.example.facefit.domain.usecases.cart.ClearCartUseCase
import com.example.facefit.domain.usecases.cart.GetCartItemCountUseCase
import com.example.facefit.domain.usecases.cart.GetCartUseCase
import com.example.facefit.domain.usecases.cart.RemoveCartItemUseCase
import com.example.facefit.domain.usecases.cart.UpdateCartItemUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CartModule {

    @Provides
    @Singleton
    fun provideGetCartUseCase(repository: CartRepository): GetCartUseCase {
        return GetCartUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddToCartUseCase(repository: CartRepository): AddToCartUseCase {
        return AddToCartUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateCartItemUseCase(repository: CartRepository): UpdateCartItemUseCase {
        return UpdateCartItemUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRemoveCartItemUseCase(repository: CartRepository): RemoveCartItemUseCase {
        return RemoveCartItemUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideClearCartUseCase(repository: CartRepository): ClearCartUseCase {
        return ClearCartUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCartItemCountUseCase(repository: CartRepository): GetCartItemCountUseCase {
        return GetCartItemCountUseCase(repository)
    }
}
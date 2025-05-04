//package com.example.facefit.di
//
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import kotlinx.coroutines.CoroutineDispatcher
//import kotlinx.coroutines.Dispatchers
//
//@Module
//@InstallIn(SingletonComponent::class)
//object CoroutineModule {
//    @Provides
//    @DefaultDispatcher
//    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
//
//    @Provides
//    @IoDispatcher
//    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
//
//    @Provides
//    @MainDispatcher
//    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
//}
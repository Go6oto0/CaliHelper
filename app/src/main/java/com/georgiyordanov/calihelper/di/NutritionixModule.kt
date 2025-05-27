// NutritionixModule.kt
package com.georgiyordanov.calihelper.di

import com.georgiyordanov.calihelper.network.NutritionixApiService
import com.georgiyordanov.calihelper.network.NutritionixRetrofitInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NutritionixModule {

    @Provides
    @Singleton
    fun provideNutritionixRetrofit(): Retrofit =
        NutritionixRetrofitInstance.retrofit

    @Provides
    @Singleton
    fun provideNutritionixApi(retrofit: Retrofit): NutritionixApiService =
        retrofit.create(NutritionixApiService::class.java)
}

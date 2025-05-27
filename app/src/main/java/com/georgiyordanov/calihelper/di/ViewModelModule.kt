// ViewModelModule.kt
package com.georgiyordanov.calihelper.di

import com.georgiyordanov.calihelper.network.NutritionixApiService
import com.georgiyordanov.calihelper.viewmodels.FoodSearchViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    fun provideFoodSearchViewModel(
        api: NutritionixApiService
    ): FoodSearchViewModel = FoodSearchViewModel(api)
}

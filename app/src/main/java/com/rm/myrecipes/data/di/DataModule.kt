package com.rm.myrecipes.data.di

import android.content.Context
import com.rm.myrecipes.data.RecipeRepositoryImpl
import com.rm.myrecipes.data.common.Constants
import com.rm.myrecipes.data.network.RecipesApi
import com.rm.myrecipes.data.network.RemoteDataSource
import com.rm.myrecipes.data.network.RetryInterceptor
import com.rm.myrecipes.data.network.dto.RecipeResponseMapper
import com.rm.myrecipes.data.room.AppDatabase
import com.rm.myrecipes.data.room.LocalDataSource
import com.rm.myrecipes.data.room.RecipesDao
import com.rm.myrecipes.domain.data.RecipeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Singleton
    @Provides
    fun provideOkhttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor())
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS )
            .build()

    @Singleton
    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()

    @Singleton
    @Provides
    fun provideRetrofitInstance(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()

    @Singleton
    @Provides
    fun provideRecipesApi(retrofit: Retrofit): RecipesApi = retrofit.create(RecipesApi::class.java)

    @Singleton
    @Provides
    fun providesAppDatabase(@ApplicationContext context: Context) : AppDatabase = AppDatabase.getInstance(context)

    @Singleton
    @Provides
    fun providesRecipesDao(database: AppDatabase): RecipesDao = database.recipesDao()

    @Singleton
    @Provides
    fun provideRecipeRepository(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource,
        recipeResponseMapper: RecipeResponseMapper
    ): RecipeRepository = RecipeRepositoryImpl(remoteDataSource, localDataSource, recipeResponseMapper)
}
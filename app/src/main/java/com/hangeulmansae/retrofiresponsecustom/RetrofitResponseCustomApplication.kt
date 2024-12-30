package com.hangeulmansae.retrofiresponsecustom

import android.app.Application
import com.google.gson.GsonBuilder
import com.hangeulmansae.retrofiresponsecustom.retrofit.TestApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitResponseCustomApplication: Application() {

    companion object{
        lateinit var retrofit: Retrofit
    }

    override fun onCreate() {
        super.onCreate()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val gsonConverterFactory = GsonConverterFactory.create(gson)

        val scalarsConverterFactory = ScalarsConverterFactory.create()

        val httpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .build()

        retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/todos/")
        .addConverterFactory(scalarsConverterFactory)
        .addConverterFactory(gsonConverterFactory)
        .client(client)
        .build()
    }

}
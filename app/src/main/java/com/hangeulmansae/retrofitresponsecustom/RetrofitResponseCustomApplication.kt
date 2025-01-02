package com.hangeulmansae.retrofitresponsecustom

import android.app.Application
import com.google.gson.GsonBuilder
import com.hangeulmansae.retrofitresponsecustom.retrofit.CustomCallAdapterFactory
import com.hangeulmansae.retrofitresponsecustom.retrofit.NullOnEmptyConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val SERVER_URL = BuildConfig.SERVER_IP

class RetrofitResponseCustomApplication : Application() {

    companion object {
        lateinit var retrofit: Retrofit
    }

    override fun onCreate() {
        super.onCreate()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val gsonConverterFactory = GsonConverterFactory.create(gson)

        val httpLoggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(NullOnEmptyConverterFactory())
            .addConverterFactory(gsonConverterFactory)
            .addCallAdapterFactory(CustomCallAdapterFactory())
            .client(client)
            .build()
    }

}
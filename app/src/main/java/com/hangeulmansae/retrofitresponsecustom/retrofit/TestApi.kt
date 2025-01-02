package com.hangeulmansae.retrofitresponsecustom.retrofit

import retrofit2.http.GET

interface TestApi {
    @GET("api/success/")
    suspend fun successApi(): Result<CommonResponse<String>>

    @GET("api/error/")
    suspend fun errorApi(): Result<CommonResponse<String>>
}
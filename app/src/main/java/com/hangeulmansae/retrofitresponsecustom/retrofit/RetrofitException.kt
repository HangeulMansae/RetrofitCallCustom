package com.hangeulmansae.retrofitresponsecustom.retrofit

/**
 * RetrofitException은
 * APi 호출 후 받은 Result를 getOrThrow로 호출했을 때,
 * 응답이 제대로 오지 않았을 경우
 * Error를 터뜨리기 위해 구현한 Exception이다.
 */
class RetrofitException(
    override val message: String,
    val code: Int,
    throwable: Throwable
) : Exception(message, throwable)
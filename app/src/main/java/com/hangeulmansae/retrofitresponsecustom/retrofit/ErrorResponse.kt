package com.hangeulmansae.retrofitresponsecustom.retrofit

import kotlinx.serialization.Serializable

/**
 * 제대로 API 요청을 하지 못하는 등
 * 요청에 성공하지 못했을 때,
 * 서버에서 보내는 에러
 */
@Serializable
data class ErrorResponse(
    val errorCode: Int,
    val message: String,
)
package com.hangeulmansae.retrofitresponsecustom.retrofit

import kotlinx.serialization.Serializable

@Serializable
/**
 * 서버에서 건네받는 데이터 타입
 * 어떤 API를 호출하던 위 형식에 Int 값이면 제네릭을 Int 꼴로 해서,
 * 들어오는 데이터를 받는다.
 */
data class CommonResponse<T>(
    val code: Int,
    val message: String,
    val data: T,
)
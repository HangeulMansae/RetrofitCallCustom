package com.hangeulmansae.retrofitresponsecustom.retrofit

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * CallAdapter는 CallAdapter<T, R> 꼴의 제네릭 2개를 받는데,
 * T는 서버로부터 응답받은 데이터의 타입이고,
 * R은 최종적으로 우리 입맛에 맞게 수정하여 반환할 Call 타입이다.
 * Call의 제네릭으로 최족적으로 반환받고 싶은 데이터 Type으로 지정하면 된다.
 * 본 예시의 경우
 * Result는 본 Custom 과정에서 넣어주는 것이지, 서버에서 오는 것이 아니므로,
 * Call<Result<T>> 꼴로 설정해야 한다.
 */
class CustomAdapter<T>(
    private val responseType: Type,
    private val retrofit: Retrofit
) : CallAdapter<T, Call<Result<T>>> {

    override fun responseType(): Type = responseType

    /***
     * Retrofit의 기본 HTTP 관리 Interface인 Call을
     * 내가 원하는 Type 등으로 세팅하기 위해서 구현한 CustomCall로 변환
     * 인자가 기본 Call이고, Return 값을 내가 Custom한 Call 타입으로 변경
     ***/
    override fun adapt(call: Call<T>): Call<Result<T>> = CustomCall(call, retrofit)

}
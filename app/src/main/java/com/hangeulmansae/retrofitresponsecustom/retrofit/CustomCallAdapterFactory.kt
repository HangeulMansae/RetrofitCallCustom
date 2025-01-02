package com.hangeulmansae.retrofitresponsecustom.retrofit

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

private const val TAG = "CustomCallAdapterFactor 싸피"

/**
 * Retrofit은 Nullable한 Return 값을 수용하지 않는다.
 *
 * => API에서 Int? 와 같은 Nullable한 Type으로 return 받으려고 하더라도
 * KType이 아니라, Java의 Type을 사용하기 때문에, Integer, Int와 같은 Non nullable한 Type으로 받아들인다.
 *
 * 그래서 Int?로 하더라도 Null이 오면 NullPointerException을 터트린다.
 *
 * 직접 Retrofit 개발에 참여한 Square사의 Jake Wharton님 답변 참고
 * Kotlin 메타 데이터로 인한 의존성 때문에 지원 X
 * https://github.com/square/retrofit/issues/3756 참고
 */
class CustomCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        /**
         * getRawType은 기본 타입을 반환한다.
         * 만약 A<B> 꼴의 Type이었다면 A를 반환한다.
         *
         * returnType !is ParameterizedType은 return Type이 제네릭 타입 정보를 포함하는지를 확인하는 것이다.
         * Call<전달받을 데이터 타입> 꼴로 있어야 정상이므로
         * RowType이 Call이면서 제네릭을 포함하는지를 확인하고, 아니라면 비정상적인 것이므로 null을 출력한다.
         *
         * API를 만들 때 Call Type을 명시한 적이 없겠지만 Retrofit 2.6.0 버전부터
         * suspend 함수가 있을 경우 자동으로 Call로 감싸서 비동기 작업인 enqueue를 진행하는 내부적인 작업이 이루어진다고 한다.
         * https://proandroiddev.com/suspend-what-youre-doing-retrofit-has-now-coroutines-support-c65bd09ba067
         *
         * // 이렇게 선언하면
         * @GET("/")
         * suspend fun getUser(): User
         *
         * // 내부적으로 이렇게 처리됨
         * fun getUser(): Call<User>
         */

        if (getRawType(returnType) != Call::class.java || returnType !is ParameterizedType) {
            return null
        }

        /**
         * getParameterUppderBound로 가장 바깥의 Type 하나를 벗긴다.
         * 즉 Call<전달받을 데이터 타입>에서 전달받을 데이터 타입을 추출한다.
         * 본 예시는 Call<Result<CommonResponse<T>>> 꼴일 것이다.
         */
        val upperBound = getParameterUpperBound(0, returnType)

        /**
         * 본 예시에서는 Result로 감싼 데이터를 받을 것이므로 Result가 맞는지를 확인하고,
         * 제네릭 타입을 받는지 확인한다.
         * 만약 맞다면 해당 데이터를 처리하기 위한 CustomAdapter를 return 한다.
         */
        return if (upperBound is ParameterizedType && upperBound.rawType == Result::class.java) {
            /**
             * Result 내의 제네릭을 추출한다. => 서버로부터 받는 데이터 Type을 추출한다.
             */
            val bodyType = getParameterUpperBound(0, upperBound)
            return CustomAdapter<Any>(bodyType, retrofit)
        } else {
            null
        }
    }
}
package com.hangeulmansae.retrofitresponsecustom.retrofit

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * 서버에서 Null을 던졌더라도, 제대로 받지 못하는 것을 해결하기 위한 Converter
 * length를 통해 비어있는지 확인하여 비어있다면 Null을 반환하도록 한다.
 */
class NullOnEmptyConverterFactory : Converter.Factory() {
    fun converterFactory() = this

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ) = object : Converter<ResponseBody, Any?> {
        val nextResponseBodyConverter = retrofit.nextResponseBodyConverter<Any?>(
            converterFactory(),
            type,
            annotations
        )

        override fun convert(value: ResponseBody) =
            /**
             * 만약 길이가 0이 아니면 그대로 value를 돌려주고,
             * 0이면 아무것도 없는 것이므로 null을 return 하도록 한다.
             */
            if (value.contentLength() != 0L) nextResponseBodyConverter.convert(value)
            else null
    }
}

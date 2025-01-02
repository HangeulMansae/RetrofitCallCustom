package com.hangeulmansae.retrofitresponsecustom.retrofit

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.io.FileNotFoundException
import java.io.IOException

private const val TAG = "CustomCall 싸피"

/**
 * CustomCall의 인자로 받는 delegate는 CallAdapter를 Custom 할 때
 * adapt 함수의 기본 Call과 제네릭을 받아서,
 * 원하는 Return Type을 제네릭으로 받는 수정한 Call 객체를 상속하는 것으로 구현한다.
 *
 * 지금은 서버로부터 제네릭 T에 해당하는 Type을 받아서 이를 Result로 감쌀 것이므로
 * Result<T>를 Call의 제네릭 인자로 넣어주었다.
 */
class CustomCall<T>(private val delegate: Call<T>, private val retrofit: Retrofit) :
    Call<Result<T>> {
    override fun clone(): Call<Result<T>> = CustomCall(delegate.clone(), retrofit)

    /**
     * 동기 실행을 위한 함수
     * api 함수를 만들 때, suspend를 붙여주지 않는다면 동기로 실행된다.
     *
     * 동기로 호출할 일이 있다면 구체적으로 구현해야겠지만
     * 본 예시에서는 생략한다.
     *
     * 쓰지 않을 것이므로 오류가 나지 않도록만 해서 아무렇게 구현하였다.
     */
    override fun execute(): Response<Result<T>> =
        Response.success(Result.success(delegate.execute().body()!!))


    /**
     * 비동기 실행을 위한 함수
     * api 함수를 만들 때, 흔히 suspend를 붙이면 작동하게 되는 방식이다.
     *
     * 비동기 통신의 결과가 들어있는 Callback을 우리 입맛에 맞게 수정하여
     * Return 값을 받고자 하던 형태로 수정할 것이다.
     *
     * 본 예제에서는 Result<T> 꼴로 수정할 것이다.
     */
    override fun enqueue(callback: Callback<Result<T>>) {
        /**
         * Callback은 비동기 결과를 처리하기 위해 제공되는 Interface이다.
         * 여기서 성공했을 때, 실패했을 때에 따른 분기를 처리한다.
         */
        delegate.enqueue(object : Callback<T> {
            /**
             * 서버로부터 응답이 왔을 때
             * 다만, 인자를 제대로 맞춰주지 않았거나 하는 이유로 404 등이 반환됐을 수도 있다.
             * 이러한 경우에도 onResponse로 오기 때문에 각 경우에 따른 분기 처리를 해주어야 한다.
             */
            override fun onResponse(call: Call<T>, response: Response<T>) {
                /**
                 * Response의 결과에 따라 Response 내의 데이터를 설정해서 넘기는 확장함수
                 */
                fun Response<T>.getResult(): Response<Result<T>> {
                    /**
                     * 만약 성공적인 경우
                     */
                    return if (isSuccessful) {
                        /**
                         * 기본적으로 response.body가 nullable하기 때문에 Null checking을 해야 한다
                         * 만약에 Api 호출시 기대 응답 데이터 타입이 Unit이었다면 Null이 들어와도 Unit으로 처리되어 상관 없지만
                         * Unit이 아니었음에도 Null이 들어왔을 경우에는 문제가 있는 것이므로 error를 반환해야 한다.
                         *
                         * 기본적으로 Retrofit은 Nullable한 Type을 지원하지 않는다.
                         * => api에서 Int? 꼴로 Return Type을 지원해도 Null 값이 들어오는 것을 허용하지 않는다.
                         */
                        response.body()?.let { body ->
                            /**
                             * Return Type을 Unit으로 해놓았다면,
                             * 데이터가 Null로 들어와도 Unit으로 들어오기 때문에 이쪽으로 들어오게 된다.
                             * Unit은 Null이 아니므로 이 때 또한 body를 넘겨주어도 문제 없다.
                             */
                            Response.success(
                                response.code(),
                                Result.success(body)
                            )
                        } ?:
                        /**
                         * Result Type이 Unit이 아닌데도 Null이 들어왔을 경우
                         * getOrThrow로 했을 때 에러가 터지도록 하고 에러를 넣어준다.
                         */
                        Response.success(
                            response.code(),
                            Result.failure(
                                RetrofitException(
                                    "Return Type이 Unit이 아님에도 불구하고 Null 값이 들어왔습니다!",
                                    -1,
                                    NullPointerException()
                                )
                            )
                        )
                    } else {
                        /**
                         * 성공일 때는 body로 응답이 들어오지만,
                         * 에러일 경우 들어오는 응답은 errorBody에 들어가있다.
                         *
                         * 만약 Success일 떄와 Error일 때 서버에서 보내주는 응답이 다를 떄
                         * 여기서 처리한다.
                         */

                        /**
                         * RetrofitException은
                         * Result를 getOrThrow로 호출했을 때,
                         * Error를 터뜨리기 위해 구현한 Exception이다.
                         */
                        errorBody()?.let { error ->
                            /**
                             * errorBody에 내용이 있다면
                             * 정상적으로 서버에서 ErrorResponse 형식으로 에러를 던진 것이기 때문에
                             * retrofit 내부에 있는 converter를 이용해서,
                             * errorBody의 내용을 ErrorResponse로 변환한다.
                             *
                             * 변환을 통해 나온 ErrorResponse 객체의 메세지와, 에러 코드를 가지고서,
                             * RetrofitException이라는 에러 객체를 만들어서,
                             * Result.failure 내에 Throwable 객체로 넣어준다.
                             *
                             * 이렇게 해서 api 호출 후 getOrThrow로 에러를 확인했을 때,
                             * API 호출이 제대로 되지 않은 경우 서버에서 넘겨준 에러 코드와 메세지를 담은
                             * 커스텀 에러가 터지도록 한다.
                             *
                             * 응답은 온 것이기 때문에 Response는 success이지만,
                             * 제대로 API 호출을 하지 못한 것이기 때문에 Error를 터뜨리도록
                             * Result는 failure로 줘야 한다.
                             */

                            val errorBody = retrofit.responseBodyConverter<ErrorResponse>(
                                ErrorResponse::class.java,
                                ErrorResponse::class.java.annotations
                            ).convert(error)

                            val message: String = errorBody?.message ?: "에러 메세지가 없습니다!"
                            val code: Int = errorBody?.errorCode ?: 404

                            Response.success(
                                Result.failure(
                                    RetrofitException(
                                        message,
                                        code,
                                        HttpException(response)
                                    )
                                )
                            )
                        } ?:
                        /**
                         * 그럴 일 없겠지만, 만약 API 호출이 실패했음에도 errorBody가 들어오지 않은 경우
                         * 기본 에러 하나 만들어서 돌려준다.
                         */
                        Response.success(
                            Result.failure(
                                RetrofitException(
                                    "에러 메세지가 없습니다",
                                    404,
                                    HttpException(response)
                                )
                            )
                        )
                    }
                }
                /**
                 * 기본적으로 Response Type이 가지고 있던 isSuccessful을 통해
                 * 응답의 성공, 실패 여부를 알 수 있고 이를 통해 분기처리를 진행한다.
                 */
                callback.onResponse(
                    this@CustomCall,
                    response.getResult()
                )
            }

            /**
             * 서버와 통신도 실패했을 때
             * 네트워크가 연결되지 않았던지 등의 이유로 아예 통신이 제대로 되지 않았을 때
             * error에 따라 when으로 분기처리를 하여 메세지를 추출하고, error를 넣어줄 수 있도록 한다.
             *
             * 통신도 제대로 안된 것이 맞지만, 에러 처리를 getOrThrow로 Result를 확인할 때, 진행하기 위해서
             * Response는 success, Result는 failure로 해서,
             * 당장 크래시가 나지 않게 하면서, getOrThrow로 까보았을 때 에러를 담는다.
             *
             * APi 호출 결과가 이상했던 위와 다르게  구분지어주기 위해서 RetrofitException이 아니라
             * RunTimeException으로 처리하였다.
             */
            override fun onFailure(p0: Call<T>, error: Throwable) {
                /**
                 * 실제로 모든 IO Exception이 인터넷 연결 문제라던지 하는 건 아니겠지만,
                 * 예외 처리를 진행한다는 것에 중점을 둔다.
                 */
                val errorMessage = when (error) {
                    is FileNotFoundException -> "파일을 찾을 수 없습니다"
                    is IOException -> "인터넷이 연결되어 있지 않습니다"
                    is HttpException -> "알 수 없는 에러가 발생했습니다!"
                    else -> error.localizedMessage
                }
                callback.onResponse(
                    this@CustomCall,
                    Response.success(Result.failure((RuntimeException(errorMessage, error))))
                )
            }
        }
        )
    }

    /**
     * 아래 부분은 Call에 이미 있는 것이므로 크게 건들지 않는다.
     */
    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() = delegate.cancel()

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()
}
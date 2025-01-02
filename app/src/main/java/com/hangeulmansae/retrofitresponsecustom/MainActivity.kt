package com.hangeulmansae.retrofitresponsecustom

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.hangeulmansae.retrofitresponsecustom.RetrofitResponseCustomApplication.Companion.retrofit
import com.hangeulmansae.retrofitresponsecustom.retrofit.RetrofitException
import com.hangeulmansae.retrofitresponsecustom.retrofit.TestApi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private fun showToastShort(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * API 호출 중 발생하는 에러를 처리하기 위한 Exception Handler 구현
     */
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        // error handling
        when (throwable) {
            /**
             * 만약 API 요청 문제 등으로 발생한 것이라면 RetrofitException에서 걸러질 것이고,
             * 넘겨받은 코드 번호와 message를 확인할 수 있다.
             */
            is RetrofitException -> throwable.apply {
                printStackTrace()
                showToastShort("$code : $message")
            }

            /**
             * 응답도 제대로 못받았거나 하는 경우 ,
             * 메세지를 통해 내용을 확인할 수 있다.
             */
            is RuntimeException -> throwable.apply {
                printStackTrace()
                showToastShort("통신 에러 : $message")
            }


            /**
             * 그 외 기타에서 발생하는 경우
             * 해당 에러에 대한 메세지를 발생하도록 한다.
             */
            else -> throwable.apply {
                printStackTrace()
                showToastShort("$message")
            }

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.tv_title)
        val testApi = retrofit.create(TestApi::class.java)

        /**
         * API 호출시 예외처리를 구현해놓은 Custom Exception Handler를 넣어주어
         * 예외 발생 시에도 Crash를 막고, 미리 작업해 놓은 Handler가 실행되도록 한다.
         */
        lifecycleScope.launch(coroutineExceptionHandler) {
            val successApiResult = testApi.successApi().getOrThrow().data
            textView.text = successApiResult
            val errorApiResult = testApi.errorApi().getOrThrow()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }
}
package com.hangeulmansae.retrofiresponsecustom

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.hangeulmansae.retrofiresponsecustom.RetrofitResponseCustomApplication.Companion.retrofit
import com.hangeulmansae.retrofiresponsecustom.retrofit.TestApi
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val testApi = retrofit.create(TestApi::class.java)

        lifecycleScope.launch{
            if(testApi.testApi().isSuccessful){
                // 통신이 성공적이었을 때
                val data = testApi.testApi().body()
                Log.d(TAG, "onCreate: ${testApi.testApi().body()}")
            }else{
                // 통신이 실패했을 때
                Log.e(TAG, "onCreate: 통신에 실패했습니다.", )
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
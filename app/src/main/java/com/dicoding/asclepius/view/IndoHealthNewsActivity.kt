package com.dicoding.asclepius.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.asclepius.R
import com.dicoding.asclepius.RetrofitClient
import com.dicoding.asclepius.response.ArticlesItem
import com.dicoding.asclepius.response.NewsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.widget.Toolbar

class IndoHealthNewsActivity : AppCompatActivity() {

    private val apiKey = "069888d5031c406c81f006ec9c4d793c"

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_indo_health_news)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        fetchCancerNews()
    }

    private fun fetchCancerNews() {
        RetrofitClient.instance.getCancerNews(apiKey = apiKey)
            .enqueue(object : Callback<NewsResponse> {
                override fun onResponse(
                    call: Call<NewsResponse>,
                    response: Response<NewsResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.articles?.let { articles ->
                            setupRecyclerView(articles)
                        }
                    } else {
                        Toast.makeText(this@IndoHealthNewsActivity, "Failed to load news", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                    Toast.makeText(this@IndoHealthNewsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("IndoHealthNewsActivity", "onFailure: ${t.message}")
                }
            })
    }

    private fun setupRecyclerView(articles: List<ArticlesItem?>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = NewsAdapter(articles)
    }
}
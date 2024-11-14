package com.dicoding.asclepius.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.asclepius.R
import com.dicoding.asclepius.response.ArticlesItem

class NewsAdapter(private val articles: List<ArticlesItem?>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]
        holder.bind(article)
    }

    override fun getItemCount(): Int = articles.size

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val newsImage: ImageView = itemView.findViewById(R.id.newsImage)
        private val newsTitle: TextView = itemView.findViewById(R.id.newsTitle)
        private val newsDescription: TextView = itemView.findViewById(R.id.newsDescription)

        fun bind(article: ArticlesItem?) {
            newsTitle.text = article?.title
            newsDescription.text = article?.description

            Glide.with(itemView.context)
                .load(article?.urlToImage)
                .placeholder(R.drawable.ic_place_holder)
                .into(newsImage)
        }
    }
}

package com.example.movie.view

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.movie.R
import com.example.movie.model.Movie
import com.example.movie.view_model.DetailViewModel
import com.example.movie.view_model.ViewModelProviderFactory
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.firebase.analytics.FirebaseAnalytics
import java.lang.Exception

class DetailActivity : AppCompatActivity() {
    private lateinit var nameofMovie: TextView
    private lateinit var plotSynopsis: TextView
    private lateinit var userRating: TextView
    private lateinit var releaseDate: TextView
    private lateinit var imageView: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var genre: TextView
    private lateinit var detailViewModel: DetailViewModel
    private lateinit var progressBar: ProgressBar
    private var movie: Movie? = null
    private var movieId: Int? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val viewModelProviderFactory = ViewModelProviderFactory(context = this)
        detailViewModel =
            ViewModelProvider(this, viewModelProviderFactory).get(DetailViewModel::class.java)
        bindView()
        initIntents()

        detailViewModel.liveData.observe(this, Observer { result ->
            when (result) {
                is DetailViewModel.State.ShowLoading -> {
                    progressBar.visibility = ProgressBar.VISIBLE
                }
                is DetailViewModel.State.HideLoading -> {
                    progressBar.visibility = ProgressBar.INVISIBLE
                }
                is DetailViewModel.State.Result -> {
                    if (result.likeInt == 1 || result.likeInt == 11) {
                        toolbar.menu.findItem(R.id.favourite).icon =
                            getDrawable(R.drawable.ic_favorite_liked)
                    } else {
                        toolbar.menu.findItem(R.id.favourite).icon =
                            getDrawable(R.drawable.ic_favorite_border)
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        hasLike()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        if (item.itemId == R.id.favourite) {
            val drawable: Drawable = item.icon.current
            if (drawable.constantState?.equals(getDrawable(R.drawable.ic_favorite_border)?.constantState) == true) {
                item.icon = getDrawable(R.drawable.ic_favorite_liked)
                likeMovie(true)
                val bundle = Bundle()
                bundle.putString("my_message", "movieLike")
                firebaseAnalytics.logEvent("countOfLike", bundle)
            } else {
                item.icon = getDrawable(R.drawable.ic_favorite_border)
                likeMovie(false)
            }
        }
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return true
    }

    private fun bindView() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initCollapsingToolbar()
        imageView = findViewById(R.id.thumbnail_image_header)
        nameofMovie = findViewById(R.id.title)
        plotSynopsis = findViewById(R.id.plotsynopsis)
        userRating = findViewById(R.id.userrating)
        releaseDate = findViewById(R.id.releasedate)
        genre = findViewById(R.id.genre)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initIntents() {
        //  val intent = intent
        try {
            movie = intent.extras?.getSerializable("movie") as Movie
            movieId = movie?.id
            val thumbnail = movie?.getPosterPath()
            val movieName = movie?.original_title
            val synopsis = movie?.overview
            val rating = movie?.vote_average.toString()
            val sateOfRelease = movie?.release_date

            try {
                Glide.with(this)
                    .load(thumbnail)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.loading)
                    .into(imageView)
            } catch (e: Exception) {
                Glide.with(this)
                    .load(R.drawable.loading)
                    .into(imageView)
            }

            nameofMovie.text = movieName
            plotSynopsis.text = synopsis
            userRating.text = rating
            releaseDate.text = sateOfRelease

        } catch (e: Exception) {
            Toast.makeText(this, "No API Data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasLike() {
        detailViewModel.haslike(movieId)
    }

    private fun likeMovie(favourite: Boolean) {
        detailViewModel.likeMovie(favourite, movie, movieId)
    }

    private fun initCollapsingToolbar() {
        val collapse: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)
        collapse.title = " "
        val appBarLayout: AppBarLayout = findViewById(R.id.appbar)
        appBarLayout.setExpanded(true)
        appBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var isShow = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    collapse.title = getString(R.string.movie_details)
                    isShow = true
                } else if (isShow) {
                    collapse.title = " "
                    isShow = false
                }
            }
        })
    }
}
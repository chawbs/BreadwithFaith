package com.kaldroid.breadwithfaith.ui.bread

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kaldroid.breadwithfaith.R

class BreadViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application = application
    private var feed: String = "Bread Data"

    private var _text = MutableLiveData<String>().apply {
        feed = getData()
        value = feed
    }
    var text: MutableLiveData<String> = this._text

    fun refreshData() {
        feed = getData()
        text = this._text
    }
    fun getData(): String {
        val cache: SharedPreferences = app.getSharedPreferences(app.getText(R.string.bread_cache).toString(),
            Context.MODE_PRIVATE
        )
        var feed: String = cache.getString("breadfeed", "<body>Daily Bread<body>").toString()
        val htmlbegin = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head>"
        val htmlend = "</html>"
        Log.i("BwF", "bread getData called, feed: $feed")
        feed = htmlbegin + feed + htmlend
        return feed
    }

}
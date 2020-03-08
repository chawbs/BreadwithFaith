package com.kaldroid.breadwithfaith.ui.justbread

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kaldroid.breadwithfaith.MainActivity
import com.kaldroid.breadwithfaith.R

class JustBreadViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application = application
    private var feed: String = "Verse Data"

    private var _text = MutableLiveData<String>().apply {
        feed = getData()
        value = feed
    }
    var text: MutableLiveData<String> = this._text

    fun refreshData() {
        feed = getData()
        text = this._text;
    }
    fun getData(): String {
        var cache: SharedPreferences = app.getSharedPreferences(app.getText(R.string.verse_cache).toString(),
            Context.MODE_PRIVATE
        )
        var feed: String = cache.getString("versefeed", "<body>Daily Verse</body>").toString()
        val htmlbegin: String = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head>"
        val htmlend: String = "</html>"
        Log.i("BwF", "verse getData called, feed: $feed")
        feed = htmlbegin + feed + htmlend
        return feed
    }
}
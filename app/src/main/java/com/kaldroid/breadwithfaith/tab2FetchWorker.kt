package com.kaldroid.breadwithfaith

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.AsyncTask
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.xml.sax.InputSource
import java.io.IOException
import java.net.URL
import java.util.*
import javax.xml.parsers.SAXParserFactory

class tab2FetchWorker(
        context: Context,
        workerParams: WorkerParameters) : Worker(context, workerParams) {
    companion object {
        lateinit var mContext: Context
    }

    init {
        mContext = context
    }

    @SuppressLint("WrongThread")
    override fun doWork(): Result {
        // get bread
        var url2 = mContext.getText(R.string.tab2_fetch) as String
        val curTime = System.currentTimeMillis()
        val unixTime = (curTime + TimeZone.getDefault().getOffset(curTime)) / 1000L
        val pref = mContext.getSharedPreferences(mContext.getText(R.string.pref) as String, MODE_PRIVATE)
        val trans = pref.getString("translation", "NIV")

        url2 = "$url2?trans=$trans"
        url2 = url2 + "&time=" + java.lang.Long.toString(unixTime)

        Log.v(mContext.getString(R.string.tag),"Async Task for: "+url2)
        GetFeedTask().execute(url2)

        return Result.success()
    }

    internal class GetFeedTask : AsyncTask<String, Void, RSSFeed>() {
        //private val mainActivity: MainActivity = MainActivity()
        private var mex: Exception? = null
        private var mioex: IOException? = null
        override fun doInBackground(vararg urls: String): RSSFeed? {
            try {
                // setup the url
                val url = URL(urls[0])
                val factory = SAXParserFactory.newInstance()
                val parser = factory.newSAXParser()
                val xmlreader = parser.xmlReader
                val theRssHandler = RSSHandler()
                xmlreader.contentHandler = theRssHandler
                val `is` = InputSource(url.openStream())
                xmlreader.parse(`is`)
                return theRssHandler.feed
            } catch (ioex: IOException) {
                mioex = ioex
                Log.e("BwF", "IOException:" + ioex.toString() + ioex.message)
                return null
            } catch (e1: Exception) {
                mex = e1
                Log.e("BwF", "Exception:" + e1.toString() + e1.message)
                return null
            }

        }

        override fun onPostExecute(feed: RSSFeed) {
            val curTime = System.currentTimeMillis()
            val unixTime = (curTime + TimeZone.getDefault().getOffset(curTime)) / 1000L
            val item = feed.getItem(0)
            //val content = item.content
            val description = item.description
            val cache = mContext.getSharedPreferences(mContext.getText(R.string.verse_cache).toString(), MODE_PRIVATE)
            val bread = cache.edit()
            bread.putString("versefeed", description)
            bread.apply()
            val pref = mContext.getSharedPreferences(mContext.getText(R.string.pref) as String, MODE_PRIVATE)
            val editor = pref.edit()
            editor.putLong("t2lastget", unixTime)
            editor.apply()
            Log.i("BwF", "Got verse at $unixTime calling updateVerse")
            MainActivity().runOnUiThread(MainActivity().verseText(description!!))
            MainActivity().runOnUiThread(MainActivity().updateVerse())
        }
    }
}

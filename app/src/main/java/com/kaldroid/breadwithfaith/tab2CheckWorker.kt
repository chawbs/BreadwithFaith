package com.kaldroid.breadwithfaith

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.AsyncTask
import android.util.Log
import androidx.concurrent.futures.ResolvableFuture
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*

class tab2CheckWorker(context: Context, workerParams: WorkerParameters) : ListenableWorker(context, workerParams) {
    @SuppressLint("RestrictedApi")
    private val future: ResolvableFuture<Result> = ResolvableFuture.create()
    private var tab2NeedsFetch = false
    companion object {
        lateinit var mContext: Context
    }
    init {
        mContext = context
    }

    override fun startWork(): ListenableFuture<Result> {
        // check bread
        tab2NeedsFetch = doWeNeedToFetch(R.string.tab2_time)
        // if bread updated start fetch worker
        //if (tab2NeedsFetch) {
        //    val mRequest = OneTimeWorkRequest.Builder(tab2FetchWorker::class.java).build()
        //    WorkManager.getInstance(applicationContext).enqueue(mRequest)
        //}
        return future
    }

    private fun doWeNeedToFetch(tab_time: Int): Boolean {
        var yesItDoes = false

        var urlt = mContext.getText(tab_time) as String
        val curTime = System.currentTimeMillis()
        val unixTime = (curTime + TimeZone.getDefault().getOffset(curTime)) / 1000L
        urlt = urlt + "?format=raw&time=" + java.lang.Long.toString(unixTime)

        try {
            CheckFeedTask().execute(urlt)
        } catch (ex: Exception) {
            yesItDoes = false
        }

        return yesItDoes
    }

    internal class CheckFeedTask : AsyncTask<String, Void, Int>() {
        private var mioex: IOException? = null
        private var lastUpdate: Long = 0
        private var lastGet: Long = 0
        var pref = mContext.getSharedPreferences(mContext.getText(R.string.pref) as String, MODE_PRIVATE)
        override fun doInBackground(vararg urls: String): Int? {
            try {
                val url = URL(urls[0])

                val input = BufferedReader(InputStreamReader(url.openStream()))
                val line = input.readLine()
                lastUpdate = line.toLong()
                input.close()
                val editor = pref.edit()
                editor.putLong("t2lastupdate", lastUpdate)
                editor.apply()
            } catch (ex: IOException) {
                mioex = ex
                //lastUpdate = lastCheck
            }

            return lastUpdate.toInt()
        }

        override fun onPostExecute(tim: Int?) {
            // check mioex & mex
            if (mioex is IOException) {
                // don't start fetch
                Log.e("BwF", "Tab2 IOException: "+mioex!!.message)
            } else {
                // check time & fetch bread...
                lastGet = pref.getLong("t2lastget", 0)
                Log.i("BwF","Verse: lastGet: $lastGet, lastUpdate: $lastUpdate")
                if (lastGet < lastUpdate) {
                    val mRequest = OneTimeWorkRequest.Builder(tab2FetchWorker::class.java).build()
                    WorkManager.getInstance(mContext).enqueue(mRequest)
                } else {
                    Log.i("BwF", "No update necessary for Verse")
                }
            }
        }
    }
}

package com.kaldroid.breadwithfaith

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.ConnectivityManager
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

class tab1CheckWorker(context: Context, workerParams: WorkerParameters) : ListenableWorker(context, workerParams) {
    @SuppressLint("RestrictedApi")
    private val future: ResolvableFuture<Result> = ResolvableFuture.create()
    private var mContext: Context? = null
    private var tab1NeedsFetch = false
    private val mRequest = OneTimeWorkRequest.Builder(tab1FetchWorker::class.java).build()

    init {
        mContext = context
    }

    override fun startWork(): ListenableFuture<Result> {
        // check bread
        tab1NeedsFetch = doWeNeedToFetch(R.string.tab1_time)
        // if bread updated start fetch worker
        if (tab1NeedsFetch) {
            WorkManager.getInstance(applicationContext).enqueue(mRequest)
        }
        return future
    }

    private fun doWeNeedToFetch(tab_time: Int): Boolean {
        var yesItDoes = false

        var urlt = mContext!!.getText(tab_time) as String
        val curTime = System.currentTimeMillis()
        val unixTime = (curTime + TimeZone.getDefault().getOffset(curTime)) / 1000L
        urlt = urlt + "?format=raw&time=" + java.lang.Long.toString(unixTime)
        val conMgr = mContext!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = conMgr.activeNetworkInfo

        try {
            if (info != null && info.isConnected) {
                //val pref = mContext!!.getSharedPreferences(mContext!!.getText(R.string.pref) as String, MODE_PRIVATE)
                CheckFeedTask().execute(urlt)
            } else {
                yesItDoes = false
            }
        } catch (ex: Exception) {
            yesItDoes = false
        }

        return yesItDoes
    }

    internal inner class CheckFeedTask : AsyncTask<String, Void, Int>() {
        private var mioex: IOException? = null
        private var lastUpdate: Long = 0
        private var lastGet: Long = 0
        var pref = mContext!!.getSharedPreferences(mContext!!.getText(R.string.pref) as String, MODE_PRIVATE)
        override fun doInBackground(vararg urls: String): Int? {
            try {
                val url = URL(urls[0])

                val input = BufferedReader(InputStreamReader(url.openStream()))
                val line = input.readLine()
                lastUpdate = line.toLong()
                input.close()
                val editor = pref.edit()
                editor.putLong("t1lastupdate", lastUpdate)
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
                Log.e("BwF", "Tab1 IOException: "+mioex!!.message)
            } else {
                // check time & fetch bread...
                lastGet = pref.getLong("t1lastget", 0)
                Log.i("BwF","Home: lastGet: $lastGet, lastUpdate: $lastUpdate")
                if (lastGet < lastUpdate) {
                    WorkManager.getInstance(applicationContext).enqueue(mRequest)
                } else {
                    Log.i("BwF", "No update necessary for Home")
                }
            }
        }
    }
}

package com.kaldroid.breadwithfaith.ui.bread

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.kaldroid.breadwithfaith.MainActivity
import com.kaldroid.breadwithfaith.R
import com.kaldroid.breadwithfaith.tab1FetchWorker
import kotlinx.android.synthetic.main.fragment_home.*

class BreadFragment : Fragment() {

    private lateinit var breadViewModel: BreadViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        breadViewModel = ViewModelProvider(this).get(BreadViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val webView: WebView = root.findViewById(R.id.text_home)
        breadViewModel.text.observe(viewLifecycleOwner, Observer {
            MainActivity().runOnUiThread(MainActivity().homeText(it))
            val base64: String = Base64.encodeToString(it.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
            webView.loadData(base64,"text/html; charset=utf-8", "base64")
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        breadswipetorefresh.setOnRefreshListener {
            val mRequestBread = OneTimeWorkRequest.Builder(tab1FetchWorker::class.java).build()
            WorkManager.getInstance(MainActivity().application).enqueue(mRequestBread)
            breadswipetorefresh.isRefreshing = false
        }
    }

}
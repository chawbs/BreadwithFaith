package com.kaldroid.breadwithfaith.ui.justbread

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
import com.kaldroid.breadwithfaith.tab2FetchWorker
import kotlinx.android.synthetic.main.fragment_justbread.*

class JustBreadFragment : Fragment() {

    private lateinit var justBreadViewModel: JustBreadViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        justBreadViewModel = ViewModelProvider(this).get(JustBreadViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_justbread, container, false)
        val webView: WebView = root.findViewById(R.id.text_justbread)
        justBreadViewModel.text.observe(viewLifecycleOwner, Observer {
            MainActivity().runOnUiThread(MainActivity().verseText(it))
            val base64: String = Base64.encodeToString(it.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
            webView.loadData(base64,"text/html; charset=utf-8", "base64")
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        justbreadswipetorefresh.setOnRefreshListener {
            val mRequestBread = OneTimeWorkRequest.Builder(tab2FetchWorker::class.java).build()
            WorkManager.getInstance(MainActivity().application).enqueue(mRequestBread)
            justbreadswipetorefresh.isRefreshing = false
        }
    }

}
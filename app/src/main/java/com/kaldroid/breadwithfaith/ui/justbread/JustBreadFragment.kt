package com.kaldroid.breadwithfaith.ui.justbread

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kaldroid.breadwithfaith.MainActivity
import com.kaldroid.breadwithfaith.R

class JustBreadFragment : Fragment() {

    private lateinit var justBreadViewModel: JustBreadViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        justBreadViewModel =
                ViewModelProviders.of(this).get(JustBreadViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_justbread, container, false)
        val webView: WebView = root.findViewById(R.id.text_justbread)
        justBreadViewModel.text.observe(this, Observer {
            MainActivity().runOnUiThread(MainActivity().verseText(it))
            var base64: String = Base64.encodeToString(it.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
            webView.loadData(base64,"text/html; charset=utf-8", "base64")
        })
        return root
    }
}
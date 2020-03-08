package com.kaldroid.breadwithfaith.ui.bread

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

class BreadFragment : Fragment() {

    private lateinit var breadViewModel: BreadViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        breadViewModel =
                ViewModelProviders.of(this).get(BreadViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val webView: WebView = root.findViewById(R.id.text_home)
        breadViewModel.text.observe(this, Observer {
            MainActivity().runOnUiThread(MainActivity().homeText(it))
            var base64: String = Base64.encodeToString(it.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
            webView.loadData(base64,"text/html; charset=utf-8", "base64")
        })
        return root
    }
}
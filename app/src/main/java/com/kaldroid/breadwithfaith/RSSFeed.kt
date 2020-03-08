package com.kaldroid.breadwithfaith

import android.text.Html
import android.text.TextUtils
import java.util.*

class RSSFeed internal constructor() {
    private var _rawText: String? = null
    internal var title: String? = null
    internal var pubDate: String? = null
    internal var itemCount = 0
        private set
    private val _itemlist: MutableList<RSSItem>
    internal val allItems: List<RSSItem>
        get() = _itemlist


    init {
        _itemlist = Vector(0)
    }

    internal fun addItem(item: RSSItem): Int {
        _itemlist.add(item)
        itemCount++
        return itemCount
    }

    internal fun getItem(location: Int): RSSItem {
        return _itemlist[location]
    }

    override fun toString(): String {
        _rawText = "<rss version=\"0.91\"><channel><title>" + title +
                "</title>"
        for (i in 0 until itemCount) {
            val itm = getItem(i)
            _rawText = _rawText + "<item><title>" + Html.escapeHtml(itm.title) + "</title>"
            _rawText = _rawText + "<link>" + Html.escapeHtml(itm.link) + "</link>"
            //var desc = itm.description
            if (!TextUtils.isEmpty(itm.description))
                _rawText = _rawText + "<description>" + Html.escapeHtml(itm.description) + "</description>"
            else {
                //desc = itm.content
                _rawText = _rawText + "<content>" + Html.escapeHtml(itm.content) + "</content>"
            }
            _rawText = _rawText!! + "</item>"
        }
        _rawText = _rawText!! + "</channel></rss>"
        return _rawText.toString()
    }
}

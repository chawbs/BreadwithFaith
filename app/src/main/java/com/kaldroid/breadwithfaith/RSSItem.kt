package com.kaldroid.breadwithfaith

class RSSItem internal constructor() {
    internal var title: String? = null
    internal var description: String? = null
    internal var link: String? = null
    internal var category: String? = null
    internal var pubDate: String? = null
    internal var content: String? = null
    override fun toString(): String {
        // limit how much text we display
        return if (title!!.length > 42) {
            title!!.substring(0, 42) + "..."
        } else title.toString()
    }
}

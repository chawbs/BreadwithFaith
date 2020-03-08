package com.kaldroid.breadwithfaith

import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

class RSSHandler/*
     * Constructor
     */
internal constructor() : DefaultHandler() {

    /*
     * getFeed - this returns our feed when all of the parsing is complete
     */
    internal var feed: RSSFeed = RSSFeed()
    internal var _item: RSSItem = RSSItem()
    internal val RSS_TITLE = 1
    internal val RSS_LINK = 2
    internal val RSS_DESCRIPTION = 3
    internal val RSS_CATEGORY = 4
    internal val RSS_PUBDATE = 5
    internal val RSS_CONTENT = 6

    internal var depth = 0
    internal var currentstate = 0


    @Throws(SAXException::class)
    override fun startDocument() {
        // initialize our RSSFeed object - this will hold our parsed contents
        feed = RSSFeed()
        // initialize the RSSItem object - we will use this as a crutch to grab the info from the channel
        // because the channel and items have very similar entries..
        _item = RSSItem()

    }

    @Throws(SAXException::class)
    override fun endDocument() {
    }

    @Throws(SAXException::class)
    override fun startElement(namespaceURI: String, localName: String, qName: String, atts: Attributes) {
        depth++
        if (localName == "channel" || localName == "feed") {
            currentstate = 0
            return
        }
        if (localName == "image") {
            // record our feed data - we temporarily stored it in the item :)
            feed.title = _item.title
            feed.pubDate = _item.pubDate
        }
        if (localName == "item" || localName == "entry") {
            // create a new item
            _item = RSSItem()
            return
        }
        if (localName == "title") {
            currentstate = RSS_TITLE
            return
        }
        if (localName == "description") {
            currentstate = RSS_DESCRIPTION
            _item.description = ""
            return
        }
        if (localName == "link") {
            currentstate = RSS_LINK
            return
        }
        if (localName == "category") {
            currentstate = RSS_CATEGORY
            return
        }
        if (localName == "pubDate") {
            currentstate = RSS_PUBDATE
            return
        }
        if (localName == "content" || localName == "encoded") {
            currentstate = RSS_CONTENT
            _item.content = ""
            return
        }
        // if we don't explicitly handle the element, make sure we don't wind up erroneously
        // storing a newline or other bogus data into one of our existing elements
        //currentstate = 0;
    }

    @Throws(SAXException::class)
    override fun endElement(namespaceURI: String, localName: String, qName: String) {
        depth--
        if (localName == "item" || localName == "entry") {
            // add our item to the list!
            feed.addItem(_item)
            return
        }
        if (localName == "channel" || localName == "feed") {
            currentstate = 0
            return
        }
        if (localName == "image") {
            currentstate = 0
            return
        }
        if (localName == "title") {
            currentstate = 0
            return
        }
        if (localName == "description") {
            currentstate = 0
            return
        }
        if (localName == "link") {
            currentstate = 0
            return
        }
        if (localName == "category") {
            currentstate = 0
            return
        }
        if (localName == "pubDate") {
            currentstate = 0
            return
        }
        if (localName == "content" || localName == "encoded") {
            currentstate = 0
            return
        }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        val theString = String(ch, start, length)
        //Log.i("RSSReader","characters[" + theString + "]");

        when (currentstate) {
            RSS_TITLE -> if (theString != "\n")
                _item.title = theString
            RSS_LINK -> if (theString != "\n")
                _item.link = theString
            RSS_DESCRIPTION ->
                // count be multi-line
                if (theString != "\n")
                    _item.description = _item.description + theString
            RSS_CATEGORY -> if (theString != "\n")
                _item.category = theString
            RSS_PUBDATE -> if (theString != "\n")
                _item.pubDate = theString
            RSS_CONTENT ->
                // could be multi-line
                if (theString != "\n")
                    _item.content = _item.content + theString
            else -> {
                // must reset if unknown
                currentstate = 0
                return
            }
        }//currentstate = 0;
        //currentstate = 0;
        //currentstate = 0;
        //currentstate = 0;
        //currentstate = 0;
        //currentstate = 0;
    }
}
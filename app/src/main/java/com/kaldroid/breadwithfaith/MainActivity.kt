package com.kaldroid.breadwithfaith

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.from
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.TimeUnit


val CHANNEL_ID = "com.kaldroid.breadwithfaith"
var breadHtml: String = ""
var verseHtml: String = ""
var builder: NotificationCompat.Builder? = null
var mustDing: Boolean = true
var mustVibrate: Boolean = true
var homeView: WebView? = null
var verseView: WebView? = null
var context: Context? = null
var inflater: LayoutInflater? = null
var pref: SharedPreferences? = null
var lastNotified: Long = 0
var lastFetched: Long = 0
var navController:NavController? = null

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val handler = Handler(Looper.getMainLooper())
    private var mBreadRequest: PeriodicWorkRequest? = null
    private var mJustBreadRequest: PeriodicWorkRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        lastNotified = System.currentTimeMillis()
        context = this
        inflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Sharing Devotion", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            val webView: WebView? = findViewById(R.id.text_home)
            var feed: String? = breadHtml
            if (webView == null) {
                //webView = findViewById(R.id.text_justbread)
                feed = verseHtml
            }
            //sharingIntent.type = "text/plain"
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, "Today's Devotion")
                putExtra(Intent.EXTRA_TEXT, feed)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        navController!!.addOnDestinationChangedListener { _: NavController, _: NavDestination, _: Bundle? ->
            homeView = findViewById(R.id.text_home)
            verseView = findViewById(R.id.text_justbread)
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home, R.id.nav_justbread), drawerLayout)
        setupActionBarWithNavController(navController!!, appBarConfiguration)
        navView.setupWithNavController(navController!!)

        pref = this.getSharedPreferences(this.getText(R.string.pref) as String, Context.MODE_PRIVATE )

        mustDing = pref!!.getBoolean("notifications_new_message", true)
        mustVibrate = pref!!.getBoolean("notifications_new_message_vibrate", true)

        createNotificationChannel()
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context!!, 0, intent, 0)
        try {
            builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_bread_with_faith)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        } catch (e:Exception) {
            Log.e("BwF", "NotificationBuilder Exception: ${e.message}")
        }

        // now get an update because we are new...
        val oRequestBread = OneTimeWorkRequest.Builder(tab1FetchWorker::class.java).build()
        val oRequestJustBread = OneTimeWorkRequest.Builder(tab2FetchWorker::class.java).build()
        WorkManager.getInstance(applicationContext).enqueue(oRequestBread)
        WorkManager.getInstance(applicationContext).enqueue(oRequestJustBread)

    }

    override fun onResume() {
        super.onResume()
        reQueue()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        homeView = findViewById(R.id.text_home)
        verseView = findViewById(R.id.text_justbread)
        mustDing = pref!!.getBoolean("notifications_new_message", true)
        mustVibrate = pref!!.getBoolean("notifications_new_message_vibrate", true)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        homeView = findViewById(R.id.text_home)
        verseView = findViewById(R.id.text_justbread)
        mustDing = pref!!.getBoolean("notifications_new_message", true)
        mustVibrate = pref!!.getBoolean("notifications_new_message_vibrate", true)
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        homeView = findViewById(R.id.text_home)
        verseView = findViewById(R.id.text_justbread)
        mustDing = pref!!.getBoolean("notifications_new_message", true)
        mustVibrate = pref!!.getBoolean("notifications_new_message_vibrate", true)
        if(id == R.id.action_settings) {
            val intent = Intent(this,MySettingsActivity::class.java)
            startActivity(intent)
            // re queue workers after returning
            reQueue()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // notification channel
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    fun dingerBread() {
        val notificationManager: NotificationManagerCompat = from(context!!)
        val homeDing = (lastFetched - lastNotified) / 1000
        Log.e("BwF","STUFF lastFetched: $lastFetched, lastNotified: $lastNotified")

        if(homeDing >  2) {
            try {
                lastNotified = System.currentTimeMillis()
                with(notificationManager) {
                    // notificationId is a unique int for each notification that you must define
                    val notificationId: Int = System.currentTimeMillis().toInt()
                    builder!!
                        .setContentTitle("New Daily Bread")
                        .setContentText("New Daily Bread devotional(s) available")
                        .setStyle(NotificationCompat.BigTextStyle().bigText("New Daily Bread devotional(s) available"))
                    notify(notificationId, builder!!.build()
                    )
                }
                Log.i("BwF", "lastNotified update time: "+lastNotified.toString(10))
            } catch (e:Exception) {
                Log.e("BwF", "dingerBread Exception: ${e.message}")
            }
        }
    }

    fun reQueue() {
        Log.i(getString(R.string.tag),"Re-Queueing workers")
        // attempt to stop any existing worker threads and submit a new one for each devotional
        val intv: Long = pref!!.getString("refresh", "30")!!.toLong()
        try {
            if(mBreadRequest!=null)
                WorkManager.getInstance(this).cancelWorkById(mBreadRequest!!.id)
            if(mJustBreadRequest!=null)
                WorkManager.getInstance(this).cancelWorkById(mJustBreadRequest!!.id)
        } catch (e:Exception) {
            Log.e("BwF", "Error Cancelling Requests: ${e.message}")
        }
        mBreadRequest = PeriodicWorkRequest.Builder(tab1CheckWorker::class.java, intv, TimeUnit.MINUTES)
            .setInitialDelay(2, TimeUnit.SECONDS)
            .build()
        mJustBreadRequest = PeriodicWorkRequest.Builder(tab2CheckWorker::class.java, intv, TimeUnit.MINUTES)
            .setInitialDelay(3, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(this).enqueue(mBreadRequest!!)
        WorkManager.getInstance(this).enqueue(mJustBreadRequest!!)
    }

    private fun checkNotificationPolicyAccess(notificationManager:NotificationManager):Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted){
                return true
            }else{
                // If notification policy access not granted for this package
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent)
            }
        }else{
            //toast("Device does not support this feature.")
            Log.i("BwF", "Not supported at this build level")
        }
        return false
    }

    fun updateHome(): Runnable {
        val runnable = object:Runnable {
            override fun run() {
                try {
                    Log.i("BwF", "updateHome called")
                    if(homeView != null) {
                        navController!!.popBackStack()
                        navController!!.navigate(R.id.nav_home)
                    }
                    dingerBread()
                } catch (e:Exception) {
                    Log.e("BwF", "updateHome Exception: ${e.message}")
                }
            }
        }
        return runnable
    }
    fun updateVerse(): Runnable {
        val runnable = object:Runnable {
            override fun run() {
                try {
                    Log.i("BwF", "updateVerse called")
                    if(verseView != null) {
                        navController!!.popBackStack()
                        navController!!.navigate(R.id.nav_justbread)
                    }
                    dingerBread()
                } catch (e:Exception) {
                    Log.e("BwF", "updateVerse Exception: ${e.message}")
                }
            }
        }
        return runnable
    }
    fun homeText(feed: String): Runnable {
        val runnable = object:Runnable {
            override fun run() {
                try {
                    breadHtml = feed
                    lastFetched = System.currentTimeMillis()
                    Log.i("BwF", "lastFetched1 update time: "+lastFetched.toString(10))
                } catch (e:Exception) {
                    Log.e("BwF", "homeText Exception: ${e.message}")
                }
            }
        }
        return runnable
    }
    fun verseText(feed: String): Runnable {
        val runnable = object:Runnable {
            override fun run() {
                try {
                    verseHtml = feed
                    lastFetched = System.currentTimeMillis()
                    Log.i("BwF", "lastFetched2 time updated: "+lastFetched.toString(10))
                } catch (e:Exception) {
                    Log.e("BwF", "verseText Exception: ${e.message}")
                }
            }
        }
        return runnable
    }
}

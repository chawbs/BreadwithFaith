package com.kaldroid.breadwithfaith

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager


class MySettingsActivity : AppCompatActivity() {
    var pref: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.preference_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.pref_screen, SettingsFragment())
            .commit()
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        pref =
            this.getSharedPreferences(this.getText(R.string.pref) as String, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preference_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.pref_screen, SettingsFragment())
            .commit()
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        pref =
            this.getSharedPreferences(this.getText(R.string.pref) as String, Context.MODE_PRIVATE)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val trans = pref!!.getString("translation", "XXX")
        val ref = pref!!.getString("refresh", "XXX")
        val not: Boolean? = pref!!.getBoolean("notifications_new_message", true)
        val nov: Boolean? = pref!!.getBoolean("notifications_new_message_vibrate", true)
        Log.i("BwF", "trans: $trans ref: $ref")
        Log.i("BwF", "notify: $not vibrate: $nov")
        val editor = pref!!.edit()
        editor.putInt("t1lastcheck", 0)
        editor.putInt("t1lastupdate", 0)
        editor.putInt("t2lastcheck", 0)
        editor.putInt("t2lastupdate", 0)
        editor.commit()
        val mRequest1 = OneTimeWorkRequest.Builder(tab1FetchWorker::class.java).build()
        val mRequest2 = OneTimeWorkRequest.Builder(tab2FetchWorker::class.java).build()
        WorkManager.getInstance(applicationContext).enqueue(mRequest1)
        WorkManager.getInstance(applicationContext).enqueue(mRequest2)
        setContentView(R.layout.activity_main)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private var mRefresh: ListPreference? = null
        private var mTranslation: ListPreference? = null
        private var mNotify: SwitchPreference? = null
        private var mVibrate: SwitchPreference? = null
        private var pref: SharedPreferences? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            pref = context!!.getSharedPreferences(
                getText(R.string.pref) as String,
                Context.MODE_PRIVATE
            )
            //    preferenceManager.sharedPreferences
            mRefresh = preferenceManager.findPreference("refresh")
            mTranslation = preferenceManager.findPreference("translation")
            mNotify = preferenceManager.findPreference("notifications_new_message")
            mVibrate = preferenceManager.findPreference("notifications_new_message_vibrate")
            mRefresh!!.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    val ed = pref!!.edit()
                    ed.putString("refresh", newValue as String)
                    ed.apply()
                    return true
                }
            }
            mTranslation!!.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
                    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                        val ed = pref!!.edit()
                        ed.putString("translation", newValue as String)
                        ed.apply()
                        return true
                    }
                }
            mNotify!!.onPreferenceChangeListener = object: Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    val ed = pref!!.edit()
                    ed.putBoolean("notifications_new_message", newValue as Boolean)
                    ed.apply()
                    return true
                }
            }
            mVibrate!!.onPreferenceChangeListener = object: Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    val ed = pref!!.edit()
                    ed.putBoolean("notifications_new_message_vibrate", newValue as Boolean)
                    ed.apply()
                    return true
                }
            }
            return super.onCreateView(inflater, container, savedInstanceState)
        }
    }
}
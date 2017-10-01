/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.ListPreference
import android.app.job.JobScheduler
import android.content.SharedPreferences
import android.content.res.Resources
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity

import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.utils.Utils
import android.support.v4.app.NavUtils
import android.view.MenuItem
import com.carbonylgroup.schoolpower.utils.ContextWrapper
import java.util.*


class SettingsActivity : AppCompatActivity() {

    private val localeSet = arrayListOf(Resources.getSystem().configuration.locale, Locale.ENGLISH, Locale.TRADITIONAL_CHINESE, Locale.SIMPLIFIED_CHINESE)

    override fun attachBaseContext(newBase: Context) {

        val utils = Utils(newBase)
        var newLocale = utils.readLangPref()
        if (newLocale == null) newLocale = 0
        val context = ContextWrapper.wrap(newBase, localeSet[newLocale])
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment()).commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = getString(R.string.settings)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {

        finish()
        return true
    }

    class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

        private var utils: Utils? = null

        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences_content)
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
            utils = Utils(activity)
            initPreferences()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (isAdded) refreshPreferences(sharedPreferences, key)
        }

        private fun initPreferences() {

            val dashboard_display = (findPreference("list_preference_dashboard_display") as ListPreference)
            dashboard_display.summary = activity.getString(R.string.dashboard_display_preference_summary_prefix) + dashboard_display.entry + activity.getString(R.string.dashboard_display_preference_summary_suffix)
        }

        private fun refreshPreferences(sharedPreferences: SharedPreferences?, key: String?) {

            if (key == "list_preference_dashboard_display") {

                val dashboard_display = (findPreference("list_preference_dashboard_display") as ListPreference)
                dashboard_display.summary = getString(R.string.dashboard_display_preference_summary_prefix) + dashboard_display.entry + activity.getString(R.string.dashboard_display_preference_summary_suffix)
                utils!!.setSettingsPreference(key, sharedPreferences!!.getString(key, "0"))

            }
            if (key == "list_preference_language") {

                utils!!.saveLangPref(sharedPreferences!!.getString(key, "0"))
                restart()
            }

            if (key == "preference_enable_notification") {

                val jobScheduler = MainActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                if (!PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("preference_enable_notification", true)) {
                    jobScheduler.cancelAll()
                }
            }
        }

        private fun restart() {

            startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
        }
    }
}

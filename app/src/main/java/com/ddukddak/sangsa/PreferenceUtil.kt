package com.ddukddak.sangsa

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil(context: Context) {


    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs_name", Context.MODE_PRIVATE)

    // 발신 시간
    var timeOption: String
        get() = prefs.getString("timeOption", "false").toString()
        set(value) = prefs.edit().putString("timeOption", value).apply()

    // 발신자
    var titleOption: String
        get() = prefs.getString("titleOption", "false").toString()
        set(value) = prefs.edit().putString("titleOption", value).apply()

}
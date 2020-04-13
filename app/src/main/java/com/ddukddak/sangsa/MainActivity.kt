package com.ddukddak.sangsa

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {
    var initalStatus: Boolean = false
    val NETWORK_STATE_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("hash key",getSigneture(this));

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), NETWORK_STATE_CODE)

        if (!permissionGranted()) {
            var intent: Intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        }

        showText.movementMethod = ScrollingMovementMethod() //스크롤바 달기

        val adapter = ArrayAdapter.createFromResource(
                this,
                R.array.speed,
                android.R.layout.simple_spinner_item
        ) // 안 변하니까 val 선언함

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        speed.adapter = adapter

        speed.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if (!initalStatus) {
                    initalStatus= !initalStatus
                    return
                }  // 어플 최초 실행시 자동으로 선택된 스피너에 대한 알림 주지 않기 위해서

                when (position) {
                    0 -> {
                        Toast.makeText(this@MainActivity, "You have chosen number 1", Toast.LENGTH_LONG).show()
                    }
                    1 -> {
                        Toast.makeText(this@MainActivity, "You have chosen number 2", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        registerReceiver(notificationReceiver, IntentFilter("Msg"))
    }

    /*
    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(notificationReceiver)
        }
        catch(i : IllegalArgumentException) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
    }*/
    val notificationReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            var appName = intent.getStringExtra("appName") // 앱 이름
            var title = intent.getStringExtra("title") // 발신자
            var text = intent.getStringExtra("text") // 카톡 내용
            var subText = intent.getStringExtra("subText") // 단톡방 이름

            var fullText: String = appName + title + text + subText
            showText.append(fullText + "\n")

            if(!TextUtils.isEmpty(text) && TextUtils.equals("com.kakao.talk", appName)) {
                val TTSIntent = Intent(context, TextToSpeech::class.java)
                TTSIntent.putExtra("TextForSpeech", text)
                startService(TTSIntent)
            }
        }
    }

    fun permissionGranted(): Boolean {
        var sets: Set<String>? = NotificationManagerCompat.getEnabledListenerPackages(this)
        return sets != null && sets.contains(packageName)
    }

    //to get hash key
    fun getSigneture(context: Context): String? {
        val pm: PackageManager = context.getPackageManager()
        try {
            val packageInfo: PackageInfo =
                pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES)
            for (i in packageInfo.signatures.indices) {
                val signature: android.content.pm.Signature? = packageInfo.signatures[i]
                try {
                    val md: MessageDigest = MessageDigest.getInstance("SHA")
                    if (signature != null) {
                        md.update(signature.toByteArray())
                    }
                    return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                } catch (e: NoSuchAlgorithmException) {
                    e.printStackTrace()
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }
}

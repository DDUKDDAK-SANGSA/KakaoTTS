package com.ddukddak.sangsa

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var initalStatus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    val notificationReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            var appName = intent.getStringExtra("appName") // 앱 이름
            var title = intent.getStringExtra("title") // 발신자
            var text = intent.getStringExtra("text") // 카톡 내용
            var subText = intent.getStringExtra("subText") // 단톡방 이름

            var fullText: String = appName + title + text + subText
            showText.append(fullText + "\n")
        }
    }

    fun permissionGranted(): Boolean {
        var sets: Set<String>? = NotificationManagerCompat.getEnabledListenerPackages(this)
        return sets != null && sets.contains(packageName)
    }
}

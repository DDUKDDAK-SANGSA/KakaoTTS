package com.ddukddak.sangsa

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main_2.*


class MainActivity : AppCompatActivity() {
    var initalStatus: Boolean = false
    val NETWORK_STATE_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_2)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), NETWORK_STATE_CODE)

        if (!permissionGranted()) {
            var intent: Intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        }

        expandable.parentLayout.setOnClickListener {
            if (expandable.isExpanded) {
                TransitionManager.beginDelayedTransition(expandable1, AutoTransition())
                TransitionManager.beginDelayedTransition(expandable2, AutoTransition())
                expandable1.visibility = View.VISIBLE
                expandable2.visibility = View.VISIBLE
                expandable.collapse()
            } else {
                expandable1.visibility = View.GONE
                expandable2.visibility = View.GONE
                expandable.expand()
            }
        }

        expandable1.parentLayout.setOnClickListener {
            if (expandable1.isExpanded) {
                TransitionManager.beginDelayedTransition(expandable, AutoTransition())
                TransitionManager.beginDelayedTransition(expandable2, AutoTransition())
                expandable.visibility = View.VISIBLE
                expandable2.visibility = View.VISIBLE
                expandable1.collapse()
            } else {
                expandable.visibility = View.GONE
                expandable2.visibility = View.GONE
                expandable1.expand()
            }
        }

        expandable2.parentLayout.setOnClickListener {
            if (expandable2.isExpanded) {
                TransitionManager.beginDelayedTransition(expandable, AutoTransition())
                TransitionManager.beginDelayedTransition(expandable1, AutoTransition())
                expandable.visibility = View.VISIBLE
                expandable1.visibility = View.VISIBLE
                expandable2.collapse()
            } else {
                expandable.visibility = View.GONE
                expandable1.visibility = View.GONE
                expandable2.expand()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
    }

    val notificationReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            var appName = intent.getStringExtra("appName") // 앱 이름
            var title = intent.getStringExtra("title") // 발신자
            var text = intent.getStringExtra("text") // 카톡 내용
            var subText = intent.getStringExtra("subText") // 단톡방 이름

            var fullText: String = appName + title + text + subText
            // showText.append(fullText + "\n")

            if (!TextUtils.isEmpty(text) && TextUtils.equals("com.kakao.talk", appName)) {
                val BWFIntent = Intent(context, BadwordFilter::class.java)
                BWFIntent.putExtra("TextToFilter", text)
                startService(BWFIntent)
            }
        }
    }

    fun permissionGranted(): Boolean {
        var sets: Set<String>? = NotificationManagerCompat.getEnabledListenerPackages(this)
        return sets != null && sets.contains(packageName)
    }

}


/*
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    //Log.e("hash key",getSignature(this));

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
 */
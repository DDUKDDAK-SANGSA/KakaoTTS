package com.ddukddak.sangsa

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ddukddak.sangsa.R.layout
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView
import kotlinx.android.synthetic.main.activity_main_2.*
import kotlinx.android.synthetic.main.inner_first_parent.*
import kotlinx.android.synthetic.main.inner_second_parent.*
import kotlinx.android.synthetic.main.inner_third_child.*
import kotlinx.android.synthetic.main.inner_third_parent.*


class MainActivity : AppCompatActivity(), OnToggledListener, View.OnClickListener{
    val NETWORK_STATE_CODE = 0

    var mainFunctionOption: Boolean = true // 메인 기능

    var timeOption: Boolean = false // 발신 시간
    var titleOption: Boolean = false // 발신자
    var currentStatusOption: Boolean = false // 현재 상태
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main_2)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), NETWORK_STATE_CODE)

        if (!permissionGranted()) {
            var intent: Intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        }

        registerReceiver(notificationReceiver, IntentFilter("Msg"))

        finishBtn.setOnClickListener(this)
        onoffswitch.setOnToggledListener(this)

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

        // 3.  텍스트 내용 설정 기능
        titleOptionSwitch.setOnCheckedChangeListener { _, isChecked -> titleOption = isChecked }
        timeOptionSwitch.setOnCheckedChangeListener { _, isChecked -> timeOption = isChecked }
        currentStatusSwitch.setOnCheckedChangeListener { _, isChecked -> currentStatusOption = isChecked } // 현재 상태가 뭐지..?
    }

    override fun onClick(v: View?) {
        finish()
    }

    @SuppressLint("ResourceAsColor")
    override fun onSwitched(toggleableView: ToggleableView, isOn:Boolean) = if(isOn){
        // 메인 기능 on
        mainFunctionOption = true

        // layout expansion enable
        expandable.parentLayout.isEnabled = true
        expandable1.parentLayout.isEnabled = true
        expandable2.parentLayout.isEnabled = true

        // 레이아웃 배경색 변경
        inner_first_layout.setBackgroundColor(Color.WHITE)
        inner_second_layout.setBackgroundColor(Color.WHITE)
        inner_third_layout.setBackgroundColor(Color.WHITE)

        // 폰트 색 변경
        inner_first_text.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorExtra1))
        inner_second_text.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorExtra1))
        inner_third_text.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorExtra1))
    } else{
        // 메인 기능 off
        mainFunctionOption = false

        Toast.makeText(this@MainActivity, "TTS 기능을 해제합니다.", Toast.LENGTH_LONG).show()

        // 열려있는 layout 모두 collapse
        expandable.visibility = View.VISIBLE
        expandable1.visibility = View.VISIBLE
        expandable2.visibility = View.VISIBLE

        expandable.collapse()
        expandable1.collapse()
        expandable2.collapse()

        // layout expansion disable
        expandable.parentLayout.isEnabled = false
        expandable1.parentLayout.isEnabled = false
        expandable2.parentLayout.isEnabled = false

        // 레이아웃 배경색 변경
        inner_first_layout.setBackgroundColor(Color.LTGRAY)
        inner_second_layout.setBackgroundColor(Color.LTGRAY)
        inner_third_layout.setBackgroundColor(Color.LTGRAY)

        // 폰트 색 변경
        inner_first_text.setTextColor(Color.GRAY)
        inner_second_text.setTextColor(Color.GRAY)
        inner_third_text.setTextColor(Color.GRAY)

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
    }

    val notificationReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            var appName: String? = intent.getStringExtra("appName") // 앱 이름
            var title: String? = "발신자 " + intent.getStringExtra("title") // 발신자
            var text: String? = intent.getStringExtra("text") // 카톡 내용
            var time = "발신 시간 " + intent.getStringExtra("time") // 발신 시간

            var fullText: String ="$appName ${if (titleOption) title else ""} ${if (timeOption) time else ""} $text"

            if(mainFunctionOption){
                if (!TextUtils.isEmpty(text) && TextUtils.equals("com.kakao.talk", appName)) {
                    val BWFIntent = Intent(context, BadwordFilter::class.java)
                    BWFIntent.putExtra("TextToFilter", fullText)
                    startService(BWFIntent)
                }
            } // 메인 기능이 켜져있을 경우에만
        }
    }

    fun permissionGranted(): Boolean {
        var sets: Set<String>? = NotificationManagerCompat.getEnabledListenerPackages(this)
        return sets != null && sets.contains(packageName)
    }
}

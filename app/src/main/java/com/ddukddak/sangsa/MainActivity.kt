package com.ddukddak.sangsa

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView
import kotlinx.android.synthetic.main.activity_main_2.*


class MainActivity : AppCompatActivity(), OnToggledListener{
    var initalStatus: Boolean = false
    val NETWORK_STATE_CODE = 0
    lateinit var spinner: Spinner
    val speedList = arrayOf<String>("0.5","1.0","2.0")
    var textSpeed : Double = 1.0
    private val TAG = "MAIN"
    private val REQUEST_CONNECT_DEVICE = 1
    private val REQUEST_ENABLE_BT = 2

    private lateinit var btn_Connect: Switch

    private var bluetoothService_obj: BluetoothService? = null

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_2)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), NETWORK_STATE_CODE)

        if (!permissionGranted()) {
            var intent: Intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        }

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

        //spinner
        spinner = findViewById(R.id.speed)
        spinner.onItemSelectedListener

        val adapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, speedList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(adapter)

        //volume
        var audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        //getSystemService(Context.AUDIO_SERVICE)

        var textVolume = findViewById<SeekBar>(R.id.volume)
        textVolume = findViewById<SeekBar>(R.id.volume)
        textVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))

        textVolume?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, fromUser: Boolean) {
                // Write code to perform some action when progress is changed.
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is started.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is stopped.
                Toast.makeText(this@MainActivity, "Progress is " + seekBar.progress + "%", Toast.LENGTH_SHORT).show()
            }
        })

        //bluetooth
        btn_Connect = findViewById(R.id.useFunction)
        btn_Connect.setOnClickListener(mClickListener)

        if (bluetoothService_obj == null) {
            bluetoothService_obj =  BluetoothService(this, mHandler)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(BluetoothService.TAG, "onActivityResult$resultCode")
        when (requestCode) {
            BluetoothService.REQUEST_ENABLE_BT -> //When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) //취소를 눌렀을 때
                {
                    bluetoothService_obj?.scanDevice();
                }
                else{
                    Log.d(TAG, "Bluetooth is not enable")
                }
            BluetoothService.REQUEST_CONNEXT_DEVICE ->
                if(resultCode ==Activity.RESULT_OK){
                    //bluetoothService_obj.getDeviceinfo(data)
                }
        }

    }

    private val mClickListener =
        View.OnClickListener { v ->
            //분기.
            when (v.id) {
                R.id.useFunction -> if (bluetoothService_obj!!.deviceState) // 블루투스 기기의 지원여부가 true 일때
                {
                    bluetoothService_obj!!.enableBluetooth() //블루투스 활성화 시작.
                } else {
                    finish()
                }
                else -> {
                }
            }
        }


    override fun onSwitched(toggleableView: ToggleableView, isOn:Boolean){
        if(isOn){
            Toast.makeText(this@MainActivity, "On", Toast.LENGTH_LONG).show()
        } else{
            Toast.makeText(this@MainActivity, "Off", Toast.LENGTH_LONG).show()
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
                BWFIntent.putExtra("Speed", textSpeed)
                startService(BWFIntent)
            }
        }
    }

    fun permissionGranted(): Boolean {
        var sets: Set<String>? = NotificationManagerCompat.getEnabledListenerPackages(this)
        return sets != null && sets.contains(packageName)
    }

    fun onItemSelected(
        arg0: AdapterView<*>?,
        arg1: View?,
        position: Int,
        id: Long
    ) {
        textSpeed = Integer.valueOf(speedList[position]).toDouble()
        //Toast.makeText(this, speedList[position], Toast.LENGTH_LONG).show()
    }

    fun onNothingSelected(arg0: AdapterView<*>?) {
        // TODO Auto-generated method stub
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
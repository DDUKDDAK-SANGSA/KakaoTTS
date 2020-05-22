package com.ddukddak.sangsa

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Vibrator
import android.text.TextUtils
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.inner_first_parent.*
import kotlinx.android.synthetic.main.inner_second_parent.*
import kotlinx.android.synthetic.main.inner_third_child.*
import kotlinx.android.synthetic.main.inner_third_parent.*


class MainActivity : AppCompatActivity(), OnToggledListener{
    private val TAG = "MAIN"
    var initalStatus: Boolean = false
    val NETWORK_STATE_CODE = 0

    val D = true

    val MESSAGE_STATE_CHANGE = 1
    lateinit var spinner: Spinner
    val speedList = arrayOf<String>("0.5","1.0","2.0")
    var textSpeed : Double = 1.0
    lateinit var alertSwitch : Switch
    var alertIsOn : Boolean = false
    lateinit var vibSwitch: Switch
    var vibIsOn : Boolean = false

    private val REQUEST_CONNECT_DEVICE = 1
    private val REQUEST_ENABLE_BT = 2
    var mainIsOn : Boolean = true

    private lateinit var btn_Connect: Switch
    private var bluetoothService_obj: BluetoothService? = null

    var mainFunctionOption: Boolean = true // 메인 기능

    var timeOption: Boolean = false // 발신 시간
    var titleOption: Boolean = false // 발신자
    var currentStatusOption: Boolean = false // 현재 상태

    private val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e(TAG, "onCreate")

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), NETWORK_STATE_CODE)

        if (!permissionGranted()) {
            var intent: Intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        }

        onoffswitch.setOnToggledListener(this)

        expandable.parentLayout.setOnClickListener {
            if (mainIsOn) {
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
        }

        expandable1.parentLayout.setOnClickListener {
            if (mainIsOn) {
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
        }

        expandable2.parentLayout.setOnClickListener {
            if (mainIsOn){
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

        // 3.  텍스트 내용 설정 기능
        titleOptionSwitch.setOnCheckedChangeListener { _, isChecked -> titleOption = isChecked }
        timeOptionSwitch.setOnCheckedChangeListener { _, isChecked -> timeOption = isChecked }

        //spinner
        spinner = findViewById(R.id.speed)
        spinner.onItemSelectedListener


        val adapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, speedList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(adapter)

        spinner.setSelection(1)

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                textSpeed = speedList[position].toDouble()
                //Toast.makeText(this, speedList[position], Toast.LENGTH_LONG).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>){
            }
        }

        registerReceiver(notificationReceiver, IntentFilter("Msg"))
        //volume
        var audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        //getSystemService(Context.AUDIO_SERVICE)

        var textVolume = findViewById<SeekBar>(R.id.volume)
        textVolume = findViewById<SeekBar>(R.id.volume)
        textVolume.setProgress(50);
        //textVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))

        textVolume?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, fromUser: Boolean) {
                Log.d("seekbar i : ", i.toString())
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(this@MainActivity, "Progress is " + seekBar.progress + "%", Toast.LENGTH_SHORT).show()
            }
        })

        //alertSwitch
        alertSwitch = findViewById(R.id.alertNoti)
        alertSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                alertIsOn = true
            }
            else {
                alertIsOn = false
            }
            Log.d(TAG, "switch : " + alertIsOn.toString())
        })

        //vibSwitch
        //alertSwitch
        vibSwitch = findViewById(R.id.vibNoti)
        vibSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                vibIsOn = true
            }
            else {
                vibIsOn = false
            }
        })

        //bluetooth
        btn_Connect = findViewById(R.id.useFunction)
        btn_Connect.setOnClickListener(mClickListener)

        if (bluetoothService_obj == null)
        {
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
                    bluetoothService_obj?.getDeviceinfo(data)
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


    @SuppressLint("ResourceAsColor")
    override fun onSwitched(toggleableView: ToggleableView, isOn:Boolean){
        if(isOn){
            Toast.makeText(this@MainActivity, "기능을 활성화합니다.", Toast.LENGTH_LONG).show()

            expandable.isEnabled=true
            expandable1.isEnabled=true
            expandable2.isEnabled=true

            inner_first_layout.setBackgroundColor(Color.WHITE)
            inner_second_layout.setBackgroundColor(Color.WHITE)
            inner_third_layout.setBackgroundColor(Color.WHITE)

            inner_first_text.setTextColor(Color.rgb(106, 150, 31))
            inner_second_text.setTextColor(Color.rgb(106, 150, 31))
            inner_third_text.setTextColor(Color.rgb(106, 150, 31))

            mainIsOn = true
            print(mainIsOn)
        } else{
            Toast.makeText(this@MainActivity, "기능을 비활성화합니다.", Toast.LENGTH_LONG).show()

            TransitionManager.beginDelayedTransition(expandable, AutoTransition())
            TransitionManager.beginDelayedTransition(expandable1, AutoTransition())
            TransitionManager.beginDelayedTransition(expandable2, AutoTransition())

            expandable.visibility = View.VISIBLE
            expandable1.visibility = View.VISIBLE
            expandable2.visibility = View.VISIBLE

            expandable.collapse()
            expandable1.collapse()
            expandable2.collapse()

            expandable.isEnabled=false
            expandable1.isEnabled=false
            expandable2.isEnabled=false

            inner_first_layout.setBackgroundColor(Color.rgb(170, 170, 170))
            inner_second_layout.setBackgroundColor(Color.rgb(170, 170, 170))
            inner_third_layout.setBackgroundColor(Color.rgb(170, 170, 170))

            inner_first_text.setTextColor(Color.DKGRAY)
            inner_second_text.setTextColor(Color.DKGRAY)
            inner_third_text.setTextColor(Color.DKGRAY)

            mainIsOn = false
            print(mainIsOn)
        }
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

            var fullText: String ="${if (titleOption) title else ""} ${if (timeOption) time else ""} $text"

            Log.d("Speed in Main : ", textSpeed.toString())
            if (!TextUtils.isEmpty(text) && TextUtils.equals("com.kakao.talk", appName)) {
                if (mainIsOn){
                    if(alertIsOn) {
                        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        val ringtone : Ringtone = RingtoneManager.getRingtone(applicationContext, notification)
                        ringtone.play()
                    }
                    if(vibIsOn) {
                        val vib : Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vib.vibrate(1000)
                    }
                    val BWFIntent = Intent(context, BadwordFilter::class.java)
                    BWFIntent.putExtra("TextToFilter", fullText)
                    BWFIntent.putExtra("Speed", textSpeed)
                    startService(BWFIntent)
                }
            }
        }
    }

    fun permissionGranted(): Boolean {
        var sets: Set<String>? = NotificationManagerCompat.getEnabledListenerPackages(this)
        return sets != null && sets.contains(packageName)
    }

    companion object {
        @kotlin.jvm.JvmField
        var MESSAGE_STATE_CHANGE: Int = 1
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
package com.ddukddak.sangsa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main_2.*


class MainActivity : AppCompatActivity() {

    var initalStatus: Boolean = false
    val NETWORK_STATE_CODE = 0

    val TAG = "MAIN"

    private val REQUEST_ENABLE_BT = 2
    private val REQUEST_CONNECT_DEVICE =1
    private val REQUEST_ENALBE_BT =2

    private var btn_Connect: Switch? = null
    private var bluetoothService_obj: BluetoothService? = null

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }
    }

    private var mClickListener: View.OnClickListener? = View.OnClickListener { v ->
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

    /*블루투스 접속에 따른 결과를 처리하는 메소드 이다.*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult$resultCode")
        // TODO Auto-generated method stub

        if(resultCode ==REQUEST_ENABLE_BT){
            if(resultCode ==Activity.RESULT_OK){
                bluetoothService_obj?.scanDevice();
            }
            else{
                Log.d(TAG, "Bluetooth is not enable")
            }
        }
        if(resultCode==REQUEST_CONNECT_DEVICE){
            if(resultCode==Activity.RESULT_OK){

                //bluetoothService_obj.getDeviceinfo(data);

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_2)

        btn_Connect = findViewById(R.id.useFunction) as Switch
        btn_Connect!!.setOnClickListener(mClickListener)

        if (bluetoothService_obj == null) {
            bluetoothService_obj = BluetoothService(this, mHandler)
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


   /*
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
                val BWFIntent = Intent(context, BadwordFilter::class.java)
                BWFIntent.putExtra("TextToFilter", text)
                startService(BWFIntent)
                /*
                val TTSIntent = Intent(context, TextToSpeech::class.java)
                TTSIntent.putExtra("TextForSpeech", text)
                startService(TTSIntent)

                 */
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
    */

}

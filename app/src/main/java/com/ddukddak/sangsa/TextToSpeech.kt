package com.ddukddak.sangsa

import android.R
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import android.widget.CompoundButton
import android.widget.Switch
import androidx.core.content.ContextCompat
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import com.kakao.sdk.newtoneapi.TextToSpeechClient
import com.kakao.sdk.newtoneapi.TextToSpeechListener
import com.kakao.sdk.newtoneapi.TextToSpeechManager

class TextToSpeech : Service() {
    val TAG = "Kakao"
    var ttsClient : TextToSpeechClient? = null

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        Log.d(TAG, "onStart")
        startSpeech(intent)
    }

    private fun startSpeech(intent: Intent?) {
        var permission_network = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_NETWORK_STATE
        )
        var permission_storage =
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET)

        if (permission_network != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission to recode denied")
            //ActivityCompat.requestPermissions(Te:class.java, arrayOf(android.Manifest.permission.RECORD_AUDIO), NETWORK_STATE_CODE)
        } else {
            //음성인식과 음성합성 두개의 초기화 코드를 다 넣어 줘야 에러가 없다.(뭐 이래)
            SpeechRecognizerManager.getInstance().initializeLibrary(this)
            TextToSpeechManager.getInstance().initializeLibrary(this)

            val speed = intent?.getDoubleExtra("Speed", 1.0)

            //TTS 클라이언트 생성
            if (speed != null) {
                ttsClient = TextToSpeechClient.Builder()
                    .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_1)     // 음성합성방식
                    .setSpeechSpeed(speed)            // 발음 속도(0.5~4.0)
                    .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM)  //TTS 음색 모드 설정(여성 차분한 낭독체)
                    .setListener(object : TextToSpeechListener {
                        //아래 두개의 메소드만 구현해 주면 된다. 음성합성이 종료될 때 호출된다.
                        override fun onFinished() {
                            val intSentSize = ttsClient?.getSentDataSize()      //세션 중에 전송한 데이터 사이즈
                            val intRecvSize = ttsClient?.getReceivedDataSize()  //세션 중에 전송받은 데이터 사이즈

                            val strInacctiveText =
                                "handleFinished() SentSize : $intSentSize  RecvSize : $intRecvSize"

                            Log.i(TAG, strInacctiveText)
                        }

                        override fun onError(code: Int, message: String?) {
                            Log.d(TAG, code.toString())
                        }
                    })
                    .build()
            }

            val text = intent?.getStringExtra("TextForSpeech")
            Log.d(TAG, text)
            ttsClient?.play(text)
        }
    }
    override fun onBind(intent: Intent?): IBinder? {

        return null;
    }
    override fun onDestroy() {
        super.onDestroy()
        TextToSpeechManager.getInstance().finalizeLibrary()
    }
}
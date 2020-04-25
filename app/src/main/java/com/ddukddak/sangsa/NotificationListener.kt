package com.ddukddak.sangsa

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("OverrideAbstract")
class NotificationListener : NotificationListenerService() {
    val TAG = "NotificationListener"

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, "onNotificationRemoved " + " packageName: " + sbn.packageName + " id: " + sbn.id)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("NotificationListener", "onStartCommand()");
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        var notification: Notification = sbn.notification
        var extras: Bundle = sbn.notification.extras
        var title: String? = extras.getString(Notification.EXTRA_TITLE) // 발신자
        var text: CharSequence? = extras.getCharSequence(Notification.EXTRA_TEXT) // 텍스트 내용

        var st:Timestamp = Timestamp(sbn.postTime);
        var sdf: SimpleDateFormat = SimpleDateFormat("MM월 dd일 E요일 a hh시 mm분", Locale.KOREA );
        var time = sdf.format(st) // 발신 시간


       var msgrcv: Intent = Intent("Msg")

        msgrcv.putExtra("appName", sbn.packageName)
        msgrcv.putExtra("title", title)
        msgrcv.putExtra("text", text)
        msgrcv.putExtra("time", time)

        Log.d(TAG, "onNotificationPosted " +
                " packageName: " + sbn.packageName +
                " real time: " + time +
                " title " + title +
                " text " + text)

        sendBroadcast(msgrcv)
    }
}
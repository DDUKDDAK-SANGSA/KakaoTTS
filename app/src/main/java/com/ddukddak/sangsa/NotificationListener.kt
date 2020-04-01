package com.ddukddak.sangsa

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

@SuppressLint("OverrideAbstract")
class NotificationListener : NotificationListenerService() {
    val TAG = "NotificationListener"

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

        Log.d(TAG, "onNotificationRemoved " + " packageName: " + sbn.packageName + " id: " + sbn.id)

    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        var notification: Notification = sbn.notification
        var extras: Bundle = sbn.notification.extras
        var title: String? = extras.getString(Notification.EXTRA_TITLE)
        var text: CharSequence? = extras.getCharSequence(Notification.EXTRA_TEXT)
        var subText: CharSequence? = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)

        var msgrcv: Intent = Intent("Msg")

        msgrcv.putExtra("appName", sbn.packageName)
        msgrcv.putExtra("title", title)
        msgrcv.putExtra("text", text)
        msgrcv.putExtra("subText", subText)

        Log.d(TAG, "onNotificationPosted " +
                " packageName: " + sbn.packageName +
                " id: " + sbn.id +
                " postTime: " + sbn.postTime +
                " title " + title +
                " text " + text +
                " subText " + subText)

        sendBroadcast(msgrcv)
    }
}
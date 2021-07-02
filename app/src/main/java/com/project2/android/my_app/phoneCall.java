package com.project2.android.my_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;


public class phoneCall extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE)))
            quiz.call = false;
        else if(TelephonyManager.EXTRA_STATE_IDLE.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE)))
            quiz.call = false;
        else if(TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE)))
            quiz.call = false;

    }
}

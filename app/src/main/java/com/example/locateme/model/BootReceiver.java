package com.example.locateme.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Actions to perform when the device boots up
            // For example, start a service or initialize components
            // Start the third receiver
           Intent thirdIntent = new Intent(context, SMSReceiver.class);
            context.sendBroadcast(thirdIntent);
        }
    }
}

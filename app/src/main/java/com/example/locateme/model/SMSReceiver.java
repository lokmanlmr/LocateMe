package com.example.locateme.model;


import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import com.example.locateme.model.localdatabase.PhoneNumbers;
import java.util.ArrayList;
import java.util.List;


public class SMSReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
     String startWord = "start tracking";
     String stopWord = "stop tracking";

    private Repository repository;
    ArrayList<PhoneNumbers> receiveFromNumbers=new ArrayList<>();


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null && intent.getAction().equals(SMS_RECEIVED)) {
            Bundle data = intent.getExtras();
            if (data != null) {
                Object[] pdus = (Object[]) data.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = getIncomingMessage(pdu, data);
                        String senderPhoneNumber = smsMessage.getDisplayOriginatingAddress();
                        String messageBody = smsMessage.getMessageBody();
                        Log.v("tag","jsbjsbjkbsbsbjks");
                        handleMessage(context, senderPhoneNumber, messageBody);
                    }
                }
            }
        }

    }

    private SmsMessage getIncomingMessage(Object pdu, Bundle bundle) {
        return SmsMessage.createFromPdu((byte[]) pdu, bundle.getString("format"));
    }

    private void handleMessage(Context context, String senderPhoneNumber, String messageBody) {
        SharedPreferences preferences =context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        startWord=preferences.getString("startWord","start tracking");
        stopWord=preferences.getString("stopWord","stop tracking");

        repository=new Repository((Application) context.getApplicationContext());
        LiveData<List< PhoneNumbers>> liveData=repository.getReceiveFromNumbers();
        liveData.observeForever(new Observer<List<PhoneNumbers>>() {
            @Override
            public void onChanged(List<PhoneNumbers> phoneNumbers) {
                receiveFromNumbers.clear();
                receiveFromNumbers.addAll(phoneNumbers);
                if (!receiveFromNumbers.isEmpty()) {
                    for (PhoneNumbers phoneNumber : receiveFromNumbers) {

                        String phoneNumberFromList=phoneNumber.getPhone_number();


                        if (phoneNumberFromList.equals(senderPhoneNumber) && messageBody.equalsIgnoreCase(startWord)) {

                            // Start the foreground service
                            if(preferences.getBoolean("isBoolean", false)){
                                startForegroundService(context);
                            }

                        } else if (phoneNumberFromList.equals(senderPhoneNumber) && messageBody.equalsIgnoreCase(stopWord)) {
                            // Stop the foreground service
                            Intent serviceIntent = new Intent(context, SendSmsAndLocation.class);
                            context.stopService(serviceIntent);
                        }
                    }
                }
            }
        });



    }
    private void startForegroundService(Context context) {
        Intent serviceIntent = new Intent(context, SendSmsAndLocation.class);
        ContextCompat.startForegroundService(context, serviceIntent);
    }


    
}

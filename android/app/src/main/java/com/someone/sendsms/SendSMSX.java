package com.someone.sendsms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import android.os.Handler;
// import android.util.Log;

/**
 * Created by yeyintkoko on 11/4/16.
 */

public class SendSMSX extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;
    private Callback callback = null;
    private Handler handler = null;
    private Runnable runnable = null;

    // public static final String LOG_TAG = "nadav";

    public SendSMSX(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "SendSMSX";
    }

    private void sendCallback(Integer messageId, String message){
        if (callback != null) {
            callback.invoke(messageId, message);
            callback = null;
        }
    }

    //---sends an SMS message to another device---
    @ReactMethod
    public void send(final Integer messageId, String phoneNumber, String message, final Callback cb ){

        try{

            this.callback = cb;
            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";
            this.runnable = new Runnable() {
                public void run() {
                    sendCallback(messageId, "Unknown error");
                }
            };
            this.handler = new android.os.Handler();
            handler.postDelayed(runnable, 8000);

            PendingIntent sentPI = PendingIntent.getBroadcast(reactContext, 0,
                    new Intent(SENT), 0);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(reactContext, 0,
                    new Intent(DELIVERED), 0);

            //---when the SMS has been sent---
            IntentFilter intentFilterSent = new IntentFilter(SENT);
            BroadcastReceiver broadcastReceiverSent = new BroadcastReceiver(){
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    handler.removeCallbacks(runnable);
                    switch (getResultCode())
                    {
                        case Activity.RESULT_OK:
                            sendCallback(messageId, "SMS sent");
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            sendCallback(messageId, "Generic failure");
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            sendCallback(messageId, "No service");
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            sendCallback(messageId, "Null PDU");
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            sendCallback(messageId, "Radio off");
                            break;
                    }
                }
            };
            intentFilterSent.setPriority(Integer.MAX_VALUE);
            reactContext.registerReceiver(broadcastReceiverSent, intentFilterSent);

            //---when the SMS has been delivered---
            IntentFilter intentFilterDelivered = new IntentFilter(SENT);
            BroadcastReceiver broadcastReceiverDelivered = new BroadcastReceiver(){
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    handler.removeCallbacks(runnable);
                    switch (getResultCode())
                    {
                        case Activity.RESULT_OK:
                            System.out.println("SMS delivered");
                            sendCallback(messageId, "SMS delivered");
                            break;
                        case Activity.RESULT_CANCELED:
                            System.out.println("SMS not delivered");
                            sendCallback(messageId, "SMS not delivered");
                            break;
                    }
                }
            };
            intentFilterDelivered.setPriority(Integer.MAX_VALUE);
            reactContext.registerReceiver(broadcastReceiverDelivered, intentFilterDelivered);

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

        }catch (Exception e) {

            sendCallback(messageId, "Unknown error");
            throw e;

        }

    }

}

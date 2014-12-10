package com.lewcpe.simnotify;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SmsConfirmationReceiver extends BroadcastReceiver {
    public SmsConfirmationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i("sim_state", "Got notify: " + action);
        if (action.equalsIgnoreCase(SimStateReceiver.SMS_SENT)) {
            Toast.makeText(context,"SMS Sent Confirmed", Toast.LENGTH_LONG);
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Log.i("sim_state","Success");
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Log.i("sim_state","Generic failed, retry");
                    Intent it = new Intent();
                    it.setAction("com.lewcpe.simnotify.SMS_RETRY");
                    context.sendBroadcast(it);
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Log.i("sim_state","No Service");
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Log.i("sim_state","NULL PDU");
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Log.i("sim_state","Radio Off");
                    break;
            }
        } else if (action.equalsIgnoreCase(SimStateReceiver.SMS_DELIVERED)) {
            Toast.makeText(context,"SMS Delivery Confirmed", Toast.LENGTH_LONG);
        }
    }
}

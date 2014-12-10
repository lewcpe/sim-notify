package com.lewcpe.simnotify;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class SimStateReceiver extends BroadcastReceiver {
    public static final String LAST_SIM_STATE = "LastSimState";
    public static final String SMS_SENT = "com.lewcpe.simnotify.SMS_SENT";
    public static final String SMS_DELIVERED = "com.lewcpe.simnotify.SMS_DELIVERED";

    public SimStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences simPreference = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = simPreference.edit();
        Resources res = context.getResources();
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int sim_state  = tm.getSimState();
        boolean retry = false;
        if(intent.getAction().equalsIgnoreCase("com.lewcpe.simnotify.SMS_RETRY")){
            Log.i("sim_state","This is a retry, force sending SMS.");
            retry = true;
        }
        if (sim_state == TelephonyManager.SIM_STATE_ABSENT) {
            Log.i("sim_state","SIM removed");
            Toast.makeText(context,"Comfirm SIM removed",Toast.LENGTH_LONG).show();
            editor.putBoolean(LAST_SIM_STATE, false);
            editor.commit();
        } else if (retry || sim_state == TelephonyManager.SIM_STATE_READY) {
            Log.i("sim_state","SIM ready");
            boolean last_sim_state = simPreference.getBoolean(LAST_SIM_STATE,true);
            boolean is_run = simPreference.getBoolean("run_sim_listener",false);
            if (retry || (last_sim_state == false && is_run == true)){
                Log.i("sim_state","Criteria met, sending SMS.");
                String destination_number = simPreference.getString("destination_number",res.getString(R.string.pref_default_destination_number));
                String prepared_message = simPreference.getString("prepared_message",res.getString(R.string.pref_default_prepared_message));
                try {

                    PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                            new Intent(SMS_SENT), 0);

                    PendingIntent deliveredPI = PendingIntent.getBroadcast(context,
                            0, new Intent(SMS_DELIVERED), 0);
                    SmsManager sm = SmsManager.getDefault();
                    Log.i("sim_state","Sending message to number "+destination_number);
                    Log.i("sim_state","Message: "+prepared_message);
                    sm.sendTextMessage(destination_number, null, prepared_message, sentPI, deliveredPI);
                    Toast.makeText(context,"SMS Sent", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(context,"SMS Failed", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
            editor.putBoolean(LAST_SIM_STATE, true);
            editor.commit();
        }
    }
}

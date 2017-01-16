package it.cnr.iit.broadcastsender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Created by Mattia Campana
 *
 * https://github.com/bytec0d3
 *
 * Broadcast receiver for the system's intents related to the battery.
 */
public class BatteryReceiver extends BroadcastReceiver {

    private static final String TAG = "BatteryReceiver";

    /**
     * Logs the battery level and if the smartphone is recharging or not.
     *
     * @param context           The context object
     * @param intent            Intent related the battery
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().compareToIgnoreCase(Intent.ACTION_BATTERY_CHANGED) == 0){

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if(level != -1 && scale != -1){

                float battPct = (float)level/(float)scale;
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

                LogManager.getInstance().logData(battPct+","+plugged,
                        LogManager.LOG_TYPE.TYPE_BATTERY);

            }else{
                Log.e(TAG, "Something goes wrong with the battery level.");
            }
        }
    }
}

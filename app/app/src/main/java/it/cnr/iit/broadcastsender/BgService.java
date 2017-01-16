package it.cnr.iit.broadcastsender;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

/**
 * Created by mattia on 14/01/17.
 */
public class BgService extends Service {

    public static final String INTENT_MODE_EXTRA = "mode";
    public static final String MODE_SENDER = "S";
    public static final String MODE_RECEIVER = "R";
    public static final String MODE_STOP = "STOP";

    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    private Thread receiver;
    private SenderThread sender;
    private BatteryReceiver batteryReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        acquireLocks();

        String mode = SharedPrefsController.getMode(getApplicationContext());

        if(intent != null){
            mode = intent.getStringExtra(INTENT_MODE_EXTRA);
            SharedPrefsController.setMode(getApplicationContext(), mode);
        }

        switch (mode){

            case MODE_RECEIVER:
                receiver = new Thread(){
                    @Override
                    public void run() {
                        new UDPReceiver().listen();
                    }

                };
                receiver.start();
                startBatteryReceiver();

                break;

            case MODE_SENDER:
                if(sender == null){
                    sender = new SenderThread(this);
                    sender.start();

                }else {
                    sender.setRunning(true);
                    sender.start();
                }
                startBatteryReceiver();

                break;

            case MODE_STOP:
                if(sender != null) sender.setRunning(false);
                stopBatteryReceiver();
        }


        return START_STICKY;
    }

    private void acquireLocks(){

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiLockTag");
        wifiLock.acquire();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Starts the Broadcast Receiver related to the battery status.
     */
    private void startBatteryReceiver(){
        if(batteryReceiver == null) {
            batteryReceiver = new BatteryReceiver();
            registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
    }

    private void stopBatteryReceiver(){
        if(batteryReceiver != null){
            unregisterReceiver(batteryReceiver);
            batteryReceiver = null;
        }
    }
}

package it.cnr.iit.broadcastsender;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by mattia on 14/01/17.
 */
public class BgService extends Service {

    private static final String TAG = "BgService";

    public static final String INTENT_MODE_EXTRA = "mode";
    public static final String MODE_SENDER = "S";
    public static final String MODE_RECEIVER = "R";
    public static final String MODE_STOP = "STOP";

    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    private Thread receiver;
    private SenderThread sender;
    private BatteryReceiver batteryReceiver;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BgService getService() {
            return BgService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        acquireLocks();

        /*String mode = SharedPrefsController.getMode(getApplicationContext());

        if(intent != null){
            mode = intent.getStringExtra(INTENT_MODE_EXTRA);
            SharedPrefsController.setMode(getApplicationContext(), mode);
        }

        if(mode != null) {

            switch (mode) {
                case MODE_RECEIVER:
                    modeReceiver();
                    break;

                case MODE_SENDER:
                    modeSender(null);
                    break;

                case MODE_STOP:
                    if (sender != null) sender.setRunning(false);
                    stopBatteryReceiver();
            }
        }
        */

        modeReceiver();

        Log.e(TAG, "Sending: "+SharedPrefsController.getSending(getApplicationContext()));

        if(SharedPrefsController.getSending(getApplicationContext())) {

            Set<String> addrSet = SharedPrefsController.getUnicastAddresses(getApplicationContext());
            List<String> addresses = null;

            if (addrSet != null) {
                addresses = new ArrayList<>();
                addresses.addAll(addrSet);
            }

            modeSender(addresses);
        }

        return START_STICKY;
    }

    public void modeSender(List<String> unicastAddresses){
        if(sender == null){
            sender = new SenderThread(this);
            sender.start();

        }else {
            sender.setRunning(true);
        }

        sender.setUnicastAddresses(unicastAddresses);

        startBatteryReceiver();
    }

    public void modeReceiver(){
        receiver = new Thread(){
            @Override
            public void run() {
                new UDPReceiver(getApplicationContext()).listen();
            }

        };
        receiver.start();
        startBatteryReceiver();
    }

    public void stopSending(){
        if(sender != null) sender.setRunning(false);
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
        return mBinder;
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

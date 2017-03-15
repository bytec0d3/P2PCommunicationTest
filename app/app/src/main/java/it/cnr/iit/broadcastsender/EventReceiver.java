package it.cnr.iit.broadcastsender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import it.cnr.iit.broadcastsender.controller.WifiController;
import it.cnr.iit.broadcastsender.view.MainActivity;

public class EventReceiver extends BroadcastReceiver {

    private static final boolean ENABLED = true;

    private static final String TAG = "EventReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ENABLED) {

            String action = intent.getAction();

            if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

                WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                Log.d(TAG, "My device name: "+device.deviceName);
                WifiController.getInstance(context).setDeviceName(device.deviceName);


            }else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

                WifiP2pGroup wifiP2pGroup = intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_GROUP);

                if(wifiP2pGroup != null &&
                        wifiP2pGroup.getNetworkName() != null &&
                        wifiP2pGroup.isGroupOwner()) {

                    String st = "SSID: "+wifiP2pGroup.getNetworkName()
                            +"\nPwd: "+wifiP2pGroup.getPassphrase();

                    WifiController.getInstance(context).setAmIAP(true);

                    Intent i = new Intent(MainActivity.INTENT_AP_CREATED);
                    i.putExtra("message", st);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(i);

                    Log.d(TAG, st);
                }

            }else if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){

                //NetworkInfo nwInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                //if(NetworkInfo.State.CONNECTED.equals(nwInfo.getState()) &&
                //        nwInfo.getType() == ConnectivityManager.TYPE_WIFI){

                WifiInfo wifiInfo = WifiController.getInstance(context).getConnectionInfo();

                if(wifiInfo.getSSID().contains("DIRECT") || wifiInfo.getSSID().contains("testAP")) {

                    final Context thisContext = context;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            WifiController.getInstance(thisContext).sendHelloMessage();
                        }
                    }, 1000);
                }

                //}
            }
        }
    }
}

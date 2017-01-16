package it.cnr.iit.broadcastsender;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by mattia on 11/01/17.
 */
class WifiController {

    private static final String TAG = "WifiController";

    private static WifiController instance;

    private WifiP2pManager p2p;
    private WifiP2pManager.Channel channel;

    private WifiManager wifiManager;

    private WifiController(Context context) {

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        p2p = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);

        if (p2p == null) {
            Log.e(TAG, "This device does not support Wi-Fi Direct");
        } else {
            channel = p2p.initialize(context, context.getMainLooper(), null);
        }
    }

    WifiInfo requestConnectionInfo(){
        return wifiManager.getConnectionInfo();
    }

    static WifiController getInstance(Context context){

        if(instance == null) instance = new WifiController(context);

        return instance;
    }

    void createGroup(){

        p2p.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Group removed.");
                cGroup();
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Failure removing Wifi P2P group. Reason: "+i);
                cGroup();
            }
        });
    }

    private void cGroup(){
        p2p.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Wifi P2P group created.");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Failure creating Wifi P2P group. Reason: "+reason);
            }
        });
    }

    void destroyGroup(){
        p2p.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Group removed.");
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Failure removing Wifi P2P group. Reason: "+i);
            }
        });
    }

    public String getMyIpAddress(){
        WifiInfo wifiinfo = wifiManager.getConnectionInfo();
        byte[] myIPAddress = BigInteger.valueOf(wifiinfo.getIpAddress()).toByteArray();
        // you must reverse the byte array before conversion. Use Apache's commons library
        ArrayUtils.reverse(myIPAddress);
        InetAddress myInetIP = null;
        try {
            myInetIP = InetAddress.getByAddress(myIPAddress);
        } catch (UnknownHostException e) {
            Log.e(TAG, e.getMessage());
        }

        if(myInetIP != null) return myInetIP.getHostAddress();

        return null;
    }
}

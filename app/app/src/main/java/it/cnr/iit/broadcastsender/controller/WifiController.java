package it.cnr.iit.broadcastsender.controller;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.cnr.iit.broadcastsender.UDPSender;
import it.cnr.iit.broadcastsender.model.GroupElement;
import it.cnr.iit.broadcastsender.view.Tabbed;

/**
 * Created by mattia on 11/01/17.
 */
public class WifiController {

    private static final String TAG = "WifiController";

    private static WifiController instance;

    private WifiP2pManager p2p;
    private WifiP2pManager.Channel channel;

    private WifiManager wifiManager;

    private String deviceName;
    private Context context;
    private boolean amIAP = false;
    private InetAddress networkBCastAddress;
    private InetAddress apAddress;
    private String myIp;

    private HashMap<String, String> group = new HashMap<>();

    private WifiController(Context context) {

        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        p2p = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);

        if (p2p == null) {
            Log.e(TAG, "This device does not support Wi-Fi Direct");
        } else {
            channel = p2p.initialize(context, context.getMainLooper(), null);
        }
    }

    public WifiInfo getConnectionInfo(){
        return wifiManager.getConnectionInfo();
    }

    public void setAmIAP(boolean amIAP){this.amIAP = amIAP;}
    public boolean getAmIAP(){return this.amIAP;}

    public InetAddress getNetworkBCastAddress(){return this.networkBCastAddress;}

    public String getDeviceName(){return this.deviceName;}

    public void setNetworkBCastAddress(String source){

        if(source != null) {

            String prefix;

            if (source.contains("/")) prefix = source.substring(1, source.lastIndexOf("."));
            else prefix = source.substring(0, source.lastIndexOf("."));

            try {
                this.networkBCastAddress = InetAddress.getByName(prefix + ".255");
                this.apAddress = InetAddress.getByName(prefix + ".1");
                Log.d(TAG, "Network BCast address: " + this.networkBCastAddress);

            } catch (UnknownHostException e) {
                Log.e(TAG, "setNetworkBCastAddress: " + e.getMessage());
            }
        }

    }

    public void addGroupElement(String name, String address){
        group.put(name, address);

        Intent i = new Intent(Tabbed.INTENT_NEW_GROUP_ELEMENT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    public void clearGroup(){this.group.clear();}

    public List<GroupElement> getGroup(){

        List<GroupElement> groupElements = new ArrayList<>();

        Iterator it = group.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            groupElements.add(new GroupElement((String)pair.getKey(), (String)pair.getValue()));
        }

        return groupElements;
    }

    public static WifiController getInstance(Context context){

        if(instance == null) instance = new WifiController(context);

        return instance;
    }

    public void sendHelloMessage(){
        Log.d(TAG, "Sending HELLO broadcast message.");

        String addr = getMyIpAddress();

        while(addr == null){
            addr = getMyIpAddress();
        }

        setNetworkBCastAddress(addr);

        final String msg = "HELLO "+this.deviceName+" "+addr;
        final UDPSender udpSender = new UDPSender(this.context);

        new Thread() {
            @Override
            public void run() {
                udpSender.sendMessage(apAddress, msg.getBytes(), false);
                udpSender.closeSocket();
            }
        }.start();
    }

    public void sendGroupStatus(){

        Log.d(TAG, "Sending GROUP_STATUS message.");

        StringBuilder stringBuilder = new StringBuilder();
        Iterator it = group.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            stringBuilder.append(" ");
            stringBuilder.append(pair.getKey());
            stringBuilder.append(":");
            stringBuilder.append(pair.getValue());
        }

        stringBuilder.append(" ");
        stringBuilder.append(this.deviceName);
        stringBuilder.append(":");
        stringBuilder.append(this.apAddress.toString().substring(1,this.apAddress.toString().length()));

        String msg = "GROUPMSG"+stringBuilder.toString();
        UDPSender udpSender = new UDPSender(this.context);

        udpSender.sendMessage(this.networkBCastAddress, msg.getBytes(), true);

        udpSender.closeSocket();

    }

    public void setDeviceName(String deviceName){this.deviceName = deviceName;}

    public void createGroup(){

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

    public void destroyGroup(){
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
            Log.e(TAG, "getMyIpAddress: "+e.getMessage());
        }

        if(myInetIP != null) return myInetIP.getHostAddress();

        return null;
    }

    public String getIp(){

        if(amIAP && apAddress != null) {
            return this.apAddress.toString().substring(1,this.apAddress.toString().length());

        } else if(amIAP){
            return null;

        } else {
            if (myIp == null) myIp = getMyIpAddress();
            return myIp;
        }
    }
}

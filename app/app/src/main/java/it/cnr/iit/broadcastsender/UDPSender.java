package it.cnr.iit.broadcastsender;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import it.cnr.iit.broadcastsender.controller.WifiController;

/**
 * Created by mattia on 11/01/17.
 */
public class UDPSender {

    private static final String TAG = "UDPSender";
    private static final int PORT = 6666;

    private Context context;
    private DatagramSocket socket;

    public UDPSender(Context context){
        this.context = context;

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void connect(InetAddress addres){
        socket.connect(addres, PORT);
    }

    public void send(byte[] data){
        try {
            socket.send(new DatagramPacket(data, data.length));
            LogManager.getInstance().logData((data.length / 1000.0) + " KB to " + socket.getInetAddress().getHostAddress(),
                    LogManager.LOG_TYPE.TYPE_NETWORK_SENT);
        } catch (IOException e) {
            Log.e(TAG, "Error sengind: "+e.getMessage());
        }
    }

    public boolean sendBroadcast(byte[] data) {
        InetAddress address = WifiController.getInstance(context).getNetworkBCastAddress();
        //Log.d(TAG,"Sending bradcast (" + address+ ") packet.");
        return sendMessage(address, data, true);
    }

    public void closeSocket(){this.socket.close();}

    public boolean sendMessage(InetAddress address, byte[] data, boolean broadcast){

        try {
            //Open a random port to send the package
            socket.setBroadcast(broadcast);
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, PORT);
            socket.send(sendPacket);
            //Log.d(TAG,"Broadcast packet sent to: " + address.getHostAddress());

            /*String msg = (sendPacket.getLength()/1000.0) + " byte TO " + address.getHostAddress();
            Intent i = new Intent(MainActivity.INTENT_MSG_SNT);
            i.putExtra("message", msg);
            LocalBroadcastManager.getInstance(context).sendBroadcast(i);*/

            return true;

        } catch (SocketException e) {
            Log.e(TAG, "SocketException: " + e.getMessage());
            return false;
            //LogManager.getInstance().logData(e.getMessage(), LogManager.LOG_TYPE.TYPE_ERROR);
        }catch (IOException e){
            Log.e(TAG, "IOException: " + e.getMessage());
            return false;
            //LogManager.getInstance().logData(e.getMessage(), LogManager.LOG_TYPE.TYPE_ERROR);
        }
    }

    private InetAddress getBroadcastAddress() {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

        InetAddress inetAddress = null;

        try {
            inetAddress = InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            Log.e(TAG, e.getMessage());
            //LogManager.getInstance().logData(e.getMessage(), LogManager.LOG_TYPE.TYPE_ERROR);
        }

        return inetAddress;
    }
}

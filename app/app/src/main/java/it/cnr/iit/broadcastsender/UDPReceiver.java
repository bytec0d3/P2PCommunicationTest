package it.cnr.iit.broadcastsender;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import it.cnr.iit.broadcastsender.controller.WifiController;
import it.cnr.iit.broadcastsender.model.GroupElement;
import it.cnr.iit.broadcastsender.view.Tabbed;

/**
 * Created by mattia on 11/01/17.
 */
class UDPReceiver{

    private static final String TAG = "UDPReceiver";
    private static final int PORT = 6666;

    private DatagramSocket socket;
    private Context context;

    public UDPReceiver(Context context){this.context = context;}

    void listen(){

        Log.d(TAG, "Listening...");

        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            socket = new DatagramSocket(PORT);
            socket.setBroadcast(true);

            while (true) {
                //Receive a packet
                byte[] recvBuf = new byte[1500];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                //Log.d(TAG, "Receiving message from "+packet.getAddress());

                String received = new String(packet.getData(), 0, packet.getLength());
                if (received.contains("HELLO")) {

                    Log.d(TAG, "Received HELLO message from " + packet.getAddress());

                    WifiController.getInstance(context).addGroupElement(received.split(" ")[1],
                            received.split(" ")[2]);

                    WifiController.getInstance(context).setNetworkBCastAddress(packet.getAddress().toString());

                    WifiController.getInstance(context).sendGroupStatus();

                } else if (received.contains("GROUPMSG") && !WifiController.getInstance(context).getAmIAP()) {
                    Log.d(TAG, "Group message received from " + packet.getAddress());
                    processGroupStatusMessage(received);

                } else {

                    String msg = (packet.getLength() / 1000.0) + " KB FROM "
                            + packet.getAddress().getHostAddress();
                    Log.d(TAG, msg);
                    LogManager.getInstance().logData(msg, LogManager.LOG_TYPE.TYPE_NETWORK);
                }
            }

        } catch (IOException ex) {
            //LogManager.getInstance().logData(ex.getMessage(), LogManager.LOG_TYPE.TYPE_ERROR);
            Log.e(TAG, "Oops" + ex.getMessage());
        }
    }

    private void processGroupStatusMessage(String message){

        String[] data = message.split(" ");

        for(int i=1; i<data.length; i++){

            WifiController.getInstance(context).addGroupElement(data[i].split(":")[0],
                    data[i].split(":")[1]);
        }

    }
}

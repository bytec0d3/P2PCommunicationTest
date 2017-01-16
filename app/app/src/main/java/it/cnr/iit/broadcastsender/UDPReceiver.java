package it.cnr.iit.broadcastsender;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by mattia on 11/01/17.
 */
class UDPReceiver{

    private static final String TAG = "UDPReceiver";
    private static final int PORT = 6666;

    private DatagramSocket socket;

    void listen(){
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            socket = new DatagramSocket(PORT);
            socket.setBroadcast(true);

            while (true) {
                //Receive a packet
                byte[] recvBuf = new byte[65000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                String msg = (packet.getLength()/1000.0) + " KB FROM " + packet.getAddress().getHostAddress();
                Log.d(TAG, msg);
                LogManager.getInstance().logData(msg, LogManager.LOG_TYPE.TYPE_NETWORK);
            }

        } catch (IOException ex) {
            LogManager.getInstance().logData(ex.getMessage(), LogManager.LOG_TYPE.TYPE_ERROR);
            Log.e(TAG, "Oops" + ex.getMessage());
        }
    }
}

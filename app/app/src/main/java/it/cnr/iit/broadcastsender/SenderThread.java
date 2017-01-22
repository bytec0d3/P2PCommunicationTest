package it.cnr.iit.broadcastsender;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by mattia on 12/01/17.
 */
class SenderThread extends Thread {

    private static final String TAG = "SenderThread";
    private static final int SLEEP_MS = 100;

    private boolean running = true;

    //private UDPSender sender;

    private List<String> unicastAddresses;

    private List<UDPSender> senderSockets = new ArrayList<>();
    private Context context;

    SenderThread(Context context){
        this.context = context;
        //this.sender = new UDPSender(context);
    }

    public void setUnicastAddresses(List<String> unicastAddresses){
        this.unicastAddresses = unicastAddresses;
    }

    @Override
    public void run() {

        for(String unicastAddress : unicastAddresses){
            UDPSender sender = new UDPSender(context);

            try {
                InetAddress address = InetAddress.getByName(unicastAddress);
                sender.connect(address);
                senderSockets.add(sender);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        while(true){

            if(!running) return;

            byte[] data = new byte[1472];
            new Random().nextBytes(data);

            if(this.unicastAddresses != null){

                for(UDPSender sender : senderSockets){
                    sender.send(data);
                }
            }//else {
              //  sender.sendBroadcast(data);
            //}
            SystemClock.sleep(SLEEP_MS);
        }
    }

    void setRunning(boolean running){this.running = running;}
}

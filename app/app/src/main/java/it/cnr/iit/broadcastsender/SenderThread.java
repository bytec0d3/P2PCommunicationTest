package it.cnr.iit.broadcastsender;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

/**
 * Created by mattia on 12/01/17.
 */
class SenderThread extends Thread {

    private static final String TAG = "SenderThread";
    private static final int SLEEP_MS = 100;

    private boolean running = true;

    private UDPSender sender;

    private List<String> unicastAddresses;

    SenderThread(Context context){
        this.sender = new UDPSender(context);
    }

    public void setUnicastAddresses(List<String> unicastAddresses){
        this.unicastAddresses = unicastAddresses;
    }

    @Override
    public void run() {

        while(true){

            if(!running) return;

            byte[] data = new byte[1472];
            new Random().nextBytes(data);

            if(this.unicastAddresses != null){

                for(String addressString : unicastAddresses){

                    try {
                        InetAddress address = InetAddress.getByName(addressString);
                        sender.sendMessage(address, data, false);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                sender.sendBroadcast(data);
            }
            SystemClock.sleep(SLEEP_MS);
        }
    }

    void setRunning(boolean running){this.running = running;}
}

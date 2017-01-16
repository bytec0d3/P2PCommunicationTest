package it.cnr.iit.broadcastsender;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Created by mattia on 12/01/17.
 */
class SenderThread extends Thread {

    private static final String TAG = "SenderThread";
    private static final int SLEEP_MS = 5;

    private boolean running = true;

    private UDPSender sender;

    SenderThread(Context context){
        this.sender = new UDPSender(context);
    }

    @Override
    public void run() {

        while(true){

            if(!running) return;

            byte[] data = new byte[1472];
            new Random().nextBytes(data);
            LogManager.getInstance().logData("Sending data", LogManager.LOG_TYPE.TYPE_NETWORK);

            sender.sendBroadcast(data);
            SystemClock.sleep(SLEEP_MS);
        }
    }

    void setRunning(boolean running){this.running = running;}
}

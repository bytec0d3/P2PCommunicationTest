package it.cnr.iit.broadcastsender.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import it.cnr.iit.broadcastsender.BgService;
import it.cnr.iit.broadcastsender.R;
import it.cnr.iit.broadcastsender.SharedPrefsController;
import it.cnr.iit.broadcastsender.controller.WifiController;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static String INTENT_AP_CREATED = "it.cnr.iit.broadcastsender.AP_CREATED";

    private WifiController wifiController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiController = WifiController.getInstance(this);

        checkMode();

        requestPermissions();

        if (Build.VERSION.SDK_INT >= 23 &&
                this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    private void checkMode(){
        switch (SharedPrefsController.getMode(this)){

            case BgService.MODE_RECEIVER:
                onReceiverModeClicked(null);
                break;

            case BgService.MODE_SENDER:
                onSenderModeClicked(null);
                break;

            case BgService.MODE_STOP:
                onStopClicked(null);
                break;
        }
    }

    public void onCreateGroupClicked(View view){

        findViewById(R.id.sender_button).setEnabled(false);

        wifiController.createGroup();
    }

    public void onSenderModeClicked(View view){

        findViewById(R.id.sender_button).setEnabled(false);
        findViewById(R.id.receiver_button).setEnabled(false);
        findViewById(R.id.group_button).setEnabled(false);
        ((TextView)findViewById(R.id.sender_tv)).setText("Sending data...");

        startBgService(BgService.MODE_SENDER);
    }

    public void onReceiverModeClicked(View view){

        findViewById(R.id.receiver_button).setEnabled(false);
        findViewById(R.id.group_button).setEnabled(false);
        ((TextView)findViewById(R.id.receiver_tv)).setText("Receiving data...");

        startBgService(BgService.MODE_RECEIVER);
    }

    public void onStopClicked(View view){

        findViewById(R.id.group_button).setEnabled(true);
        findViewById(R.id.receiver_button).setEnabled(true);
        findViewById(R.id.sender_button).setEnabled(true);

        ((TextView)findViewById(R.id.group_tv)).setText("");
        ((TextView)findViewById(R.id.sender_tv)).setText("");
        ((TextView)findViewById(R.id.receiver_tv)).setText("");

        wifiController.destroyGroup();

        startBgService(BgService.MODE_STOP);
    }

    private void startBgService(String mode){

        SharedPrefsController.setSending(this, true);

        Intent intent = new Intent(this, BgService.class);
        intent.putExtra(BgService.INTENT_MODE_EXTRA, mode);
        startService(intent);
    }

    private void setGroupMessage(String msg){
        ((TextView)findViewById(R.id.group_tv)).setText(msg);
        onReceiverModeClicked(null);
    }

    @Override
    protected void onStart() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_AP_CREATED);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);

        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if(action.equals(INTENT_AP_CREATED)){

                String data = intent.getStringExtra("message");
                setGroupMessage(data);

            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission granted");
        }
    }

    private void requestPermissions(){

        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(packageName))
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        else {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
        }

        startActivity(intent);
    }
}
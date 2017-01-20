package it.cnr.iit.broadcastsender.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import java.util.List;

import it.cnr.iit.broadcastsender.BgService;
import it.cnr.iit.broadcastsender.R;
import it.cnr.iit.broadcastsender.controller.WifiController;
import it.cnr.iit.broadcastsender.view.adapters.SectionAdapter;

public class Tabbed extends AppCompatActivity {

    private static final String TAG = "Tabbed";

    public static String INTENT_AP_CREATED = "it.cnr.iit.broadcastsender.AP_CREATED";
    public static String INTENT_NEW_GROUP_ELEMENT = "it.cnr.iit.broadcastsender.NEW_GROUP_ELEMENT";

    private SectionAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        PagerTabStrip strip = (PagerTabStrip) findViewById(R.id.pts_main);
        strip.setDrawFullUnderline(false);

        requestPermissions();

        if (Build.VERSION.SDK_INT >= 23 &&
                this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startBgService(String mode){
        Intent intent = new Intent(this, BgService.class);
        intent.putExtra(BgService.INTENT_MODE_EXTRA, mode);
        startService(intent);
    }

    public void clearGroup(){
        WifiController.getInstance(this).clearGroup();
        ((GroupFragment)mSectionsPagerAdapter.getItem(1)).clearGroup();
    }

    public List<String> getSelectedAddresses(){
        return ((GroupFragment)mSectionsPagerAdapter.getItem(1)).getCheckedAddresses();
    }


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

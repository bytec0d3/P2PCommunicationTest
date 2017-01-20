package it.cnr.iit.broadcastsender.view;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.cnr.iit.broadcastsender.BgService;
import it.cnr.iit.broadcastsender.R;
import it.cnr.iit.broadcastsender.SharedPrefsController;
import it.cnr.iit.broadcastsender.controller.WifiController;

/**
 * Created by mattia on 16/01/17.
 */
public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private TextView groupTV;
    private BgService mBoundService;
    private boolean mIsBound = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_fragment_layout, container, false);

        (rootView.findViewById(R.id.create_group_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WifiController.getInstance(getActivity()).createGroup();
            }
        });

        groupTV = (TextView) rootView.findViewById(R.id.group_tv);

        (rootView.findViewById(R.id.remove_group_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WifiController.getInstance(getActivity()).destroyGroup();
                ((Tabbed)getActivity()).clearGroup();
                groupTV.setText("");
            }
        });

        (rootView.findViewById(R.id.send_bcast_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPrefsController.setUnicastAddresses(getActivity(), null);
                SharedPrefsController.setSending(getActivity(), true);
                mBoundService.modeSender(null);
            }
        });

        (rootView.findViewById(R.id.send_unicast_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> selectedAddresses = ((Tabbed)getActivity()).getSelectedAddresses();
                if(selectedAddresses.size() != 0){

                    Set<String> addrSet = new HashSet<>();
                    addrSet.addAll(selectedAddresses);
                    SharedPrefsController.setUnicastAddresses(getActivity(), addrSet);
                    SharedPrefsController.setSending(getActivity(), true);

                    mBoundService.modeSender(selectedAddresses);

                }
            }
        });

        (rootView.findViewById(R.id.stop_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPrefsController.setUnicastAddresses(getActivity(), null);
                SharedPrefsController.setSending(getActivity(), false);
                mBoundService.stopSending();
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(Tabbed.INTENT_AP_CREATED));

        getActivity().startService(new Intent(getActivity(), BgService.class));

        doBindService();

        return rootView;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if(action.equals(Tabbed.INTENT_AP_CREATED)){

                String data = intent.getStringExtra("message");
                Log.d(TAG, "Goup created.\n"+data);
                groupTV.setText(data);
            }
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        getActivity().bindService(new Intent(getActivity(),
                BgService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((BgService.LocalBinder)service).getService();

            // Tell the user about this for our demo.
            Toast.makeText(getActivity(), "bgService connected", Toast.LENGTH_SHORT).show();
            mBoundService.modeReceiver();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Toast.makeText(getActivity(), "bgService disconnected", Toast.LENGTH_SHORT).show();
        }
    };
}

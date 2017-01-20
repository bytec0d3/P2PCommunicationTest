package it.cnr.iit.broadcastsender.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.cnr.iit.broadcastsender.R;
import it.cnr.iit.broadcastsender.controller.WifiController;
import it.cnr.iit.broadcastsender.model.GroupElement;
import it.cnr.iit.broadcastsender.view.adapters.GroupListAdapter;

/**
 * Created by mattia on 16/01/17.
 */
public class GroupFragment extends Fragment {

    private static final String TAG = "GroupFragment";

    private GroupListAdapter adapter;
    private List<GroupElement> group = new ArrayList<>();
    private Set<String> groupAddresses = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.group_fragment_layout, container, false);

        (rootView.findViewById(R.id.clear_group_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Tabbed)getActivity()).clearGroup();
            }
        });

        adapter = new GroupListAdapter(getActivity(), group);

        ListView listView = (ListView) rootView.findViewById(R.id.group_list_view);
        listView.setAdapter(adapter);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(Tabbed.INTENT_NEW_GROUP_ELEMENT));

        return rootView;
    }

    public void clearGroup(){
        this.group.clear();
        this.groupAddresses.clear();
        adapter.notifyDataSetChanged();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if(action.equals(Tabbed.INTENT_NEW_GROUP_ELEMENT)){

                /*GroupElement element = intent.getParcelableExtra("message");
                if(element != null && !groupAddresses.contains(element.address)){
                    group.add(element);
                    groupAddresses.add(element.address);
                    adapter.notifyDataSetChanged();
                }*/

                boolean changed = false;

                String myIp = WifiController.getInstance(context).getMyIpAddress();

                for(GroupElement element : WifiController.getInstance(context).getGroup()){
                    if(!groupAddresses.contains(element.address) &&
                            !element.address.equals(myIp)){
                        changed = true;
                        group.add(element);
                        groupAddresses.add(element.address);
                    }
                }

                if(changed)adapter.notifyDataSetChanged();
            }
        }
    };

    public List<String> getCheckedAddresses(){

        List<String> addresses = new ArrayList<>();

        for(int i=0; i<group.size(); i++){
            if(adapter.mCheckStates.get(i)){
                addresses.add(group.get(i).address);
            }
        }

        return addresses;
    }
}

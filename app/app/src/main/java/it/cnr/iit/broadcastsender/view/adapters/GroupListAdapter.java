package it.cnr.iit.broadcastsender.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import it.cnr.iit.broadcastsender.R;
import it.cnr.iit.broadcastsender.model.GroupElement;

/**
 * Created by mattia on 16/01/17.
 */
public class GroupListAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener{

    private Context context;
    private List<GroupElement> data;
    public SparseBooleanArray mCheckStates;

    public GroupListAdapter(Context context, List<GroupElement> data){
        this.context = context;
        this.data = data;
        this.mCheckStates = new SparseBooleanArray(data.size());
    }

    @Override
    public int getCount() {
        return (data == null) ? 0 : data.size();
    }

    @Override
    public Object getItem(int i) {
        return (data == null) ? null : data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        GroupElementViewHolder viewHolder;

        if(view == null){

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(R.layout.group_element, viewGroup, false);

            viewHolder = new GroupElementViewHolder();
            viewHolder.nameTV = (TextView) view.findViewById(R.id.name);
            viewHolder.addressTV = (TextView) view.findViewById(R.id.address);
            viewHolder.checkBox = (CheckBox) view.findViewById(R.id.chk);

            view.setTag(viewHolder);

        }else{
            viewHolder = (GroupElementViewHolder) view.getTag();
        }

        // object item based on the position
        GroupElement element = data.get(i);

        // assign values if the object is not null
        if(element != null) {

            if(element.address.endsWith(".1")) element.name += " (AP)";

            viewHolder.nameTV.setText(element.name);
            viewHolder.addressTV.setText(element.address);
            viewHolder.checkBox.setChecked(element.sendUnicast);
            viewHolder.checkBox.setTag(i);
            viewHolder.checkBox.setChecked(mCheckStates.get(i, false));
            viewHolder.checkBox.setOnCheckedChangeListener(this);
        }

        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        this.mCheckStates.put((Integer) compoundButton.getTag(), isChecked);
    }

    private static class GroupElementViewHolder {
        TextView addressTV, nameTV;
        CheckBox checkBox;
    }

}

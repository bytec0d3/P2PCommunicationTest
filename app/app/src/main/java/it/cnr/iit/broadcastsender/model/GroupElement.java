package it.cnr.iit.broadcastsender.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mattia on 16/01/17.
 */
public class GroupElement implements Parcelable{

    public String name, address;
    public boolean sendUnicast;

    public GroupElement(String name, String address){
        this.name = name;
        this.address = address;
    }

    public GroupElement(Parcel in){

        this.name = in.readString();
        this.address = in.readString();
        this.sendUnicast = in.readByte() != 0;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(this.name);
        parcel.writeString(this.address);
        parcel.writeByte((byte) (this.sendUnicast ? 1 : 0));

    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public GroupElement createFromParcel(Parcel in) {
            return new GroupElement(in);
        }

        public GroupElement[] newArray(int size) {
            return new GroupElement[size];
        }
    };
}

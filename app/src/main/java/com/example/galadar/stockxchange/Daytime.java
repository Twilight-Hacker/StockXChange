package com.example.galadar.stockxchange;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Galadar on 1/10/2015.
 */
public class Daytime implements Parcelable{
    int term;
    int day;
    int min;
    int hour;
    Context context;

    public Daytime(Context context){
        this.term =1;
        this.day = 1;
        this.hour = 9;
        this.min = 0;
        this.context = context;
    }

    public Daytime(Context context, int term, int day) {
        this.term =term;
        this.day = day;
        this.hour = 9;
        this.min = 0;
        this.context = context;
    }

    protected Daytime(Parcel in) {
        term = in.readInt();
        day = in.readInt();
        min = in.readInt();
        hour = in.readInt();
    }

    public static final Creator<Daytime> CREATOR = new Creator<Daytime>() {
        @Override
        public Daytime createFromParcel(Parcel in) {
            return new Daytime(in);
        }

        @Override
        public Daytime[] newArray(int size) {
            return new Daytime[size];
        }
    };

    public String DTtoString(){
        String zerodigit=" ";
        if(this.min==0){zerodigit="0 ";}
        String DT =  "Term "+this.term+", Day "+this.day+"  "+this.hour+":"+this.min+zerodigit;
        return DT;
    }

    public void increment(int UpdateInterval ){
        this.min += UpdateInterval;

        if(this.min==60){
            this.hour++;
            this.min =0;
        }

        if(this.hour==15&&this.min>=30){
            this.day++;
            this.hour = 9;
            this.min = 0;
            Intent i = new Intent("DayEnded");
            this.context.sendBroadcast(i);
            //LocalBroadcastManager.getInstance(this.context).sendBroadcast(i);
        }

        if(this.day==60){
            this.term++;
            this.day =1;
            Intent i = new Intent("TermEnded");
            this.context.sendBroadcast(i);
            //LocalBroadcastManager.getInstance(this.context).sendBroadcast(i);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(term);
        dest.writeInt(day);
        dest.writeInt(min);
        dest.writeInt(hour);
    }
}

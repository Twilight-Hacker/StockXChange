package com.galadar.example.stockxchange;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Galadar on 1/10/2015.
 * Daytime Object
 */
public class Daytime{
    int term;
    int day;
    int min;
    int hour;
    LocalBroadcastManager context;

    public Daytime(LocalBroadcastManager context){
        this.term =1;
        this.day = 1;
        this.hour = 8;
        this.min = 30;
        this.context = context;
    }

    public Daytime(LocalBroadcastManager context, int term, int day) {
        this.term =term;
        this.day = day;
        this.hour = 8;
        this.min = 40;
        this.context = context;
    }

    public String DTtoString(){
        String zerodigit=" ";
        if(this.min==0){zerodigit="0 ";}
        return "Term "+ this.term+", Day "+ this.day+"  "+ this.hour+":"+ this.min+zerodigit;
    }

    public void increment(int UpdateInterval ){
        this.min += UpdateInterval;


        if(this.min==60){
            this.hour++;
            this.min =0;
        }

        if(this.hour==15&&this.min==30) {
            Intent i = new Intent("DayEnded");
            this.context.sendBroadcast(i);
            //LocalBroadcastManager.getInstance(this.context).sendBroadcast(i);
        } else if(this.hour==15&&this.min>30) {
            this.day++;
            this.hour = 8;
            this.min = 40;
            Intent i = new Intent("DayReset");
            this.context.sendBroadcast(i);
        } else if(this.hour==9&&this.min==0){
            Intent intent2 = new Intent("DayStarted");
            this.context.sendBroadcast(intent2);
        }

    }

    public void nextTerm() {
        this.term++;
        this.day = 1;
        this.hour = 8;
        this.min = 40;
    }

    public int totalDays(){
        return (this.term-1)*60 + this.day;
    }

    //returns what the tatal days will be after X days
    public int totalDays(int duration){
        return (this.term-1)*60 + this.day + duration;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

/*
    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }
*/
}

package com.example.galadar.stockxchange;

/**
 * Created by Galadar on 1/10/2015.
 */
public class Daytime {
    int min;
    int hour;

    public Daytime(){
        this.hour = 9;
        this.min = 0;
    }

    public void increment(int UpdateInterval ){
        this.min += UpdateInterval;

        if(this.min==60){
            this.hour++;
            this.min =0;
        }
    }

}

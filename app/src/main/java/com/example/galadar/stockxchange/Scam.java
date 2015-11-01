package com.example.galadar.stockxchange;

/**
 * Created by Galadar on 1/11/2015.
 */
public class Scam {

    int sid;
    int type;
    int resolutionDay;

    public Scam(int sid, int type, int resolutionDay) {
        this.sid = sid;
        this.type = type;
        this.resolutionDay = resolutionDay;
    }

    public int getSid() {
        return sid;
    }

    public int getType() {
        return type;
    }

    public int getResolutionDay() {
        return resolutionDay;
    }
}

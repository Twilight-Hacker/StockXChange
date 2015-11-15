package com.example.galadar.stockxchange;

/**
 * Created by Galadar on 1/10/2015.
 */
public class Share {

    int currentSharePrice;
    int prevDayClose;
    int sid;
    String name;

    public int getTotalShares() {
        return total;
    }

    int total;

    public Share(String name, int sid, int currentSharePrice, int total) {
        this.name = name;
        this.sid = sid;
        this.currentSharePrice = currentSharePrice;
        this.prevDayClose = currentSharePrice;
        this.total = total;
    }

    public int getId() {
        return sid;
    }

    public String getName() {
        return name;
    }

    public int getPrevDayClose() {
        return prevDayClose;
    }

    public int getCurrentSharePrice() {

        return currentSharePrice;
    }
}

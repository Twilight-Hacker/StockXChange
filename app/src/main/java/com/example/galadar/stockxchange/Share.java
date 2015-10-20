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

    public void setTotal(int total) {
        this.total = total;
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

    public void setPrevDayClose(int prevDayClose) {
        this.prevDayClose = prevDayClose;
    }

    public int getCurrentSharePrice() {

        return currentSharePrice;
    }

    public void setCurrentSharePrice(int currentSharePrice) {
        this.currentSharePrice = currentSharePrice;
    }
}

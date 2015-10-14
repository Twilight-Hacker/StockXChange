package com.example.galadar.stockxchange;

/**
 * Created by Galadar on 1/10/2015.
 */
public class Share {

    int currentSharePrice;
    int prevDayClose;
    int sid;
    String name;

    public Share(String name, int sid, int currentSharePrice) {
        this.name = name;
        this.sid = sid;
        this.currentSharePrice = currentSharePrice;
        this.prevDayClose = currentSharePrice;
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

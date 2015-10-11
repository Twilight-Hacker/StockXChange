package com.example.galadar.stockxchange;

/**
 * Created by Galadar on 1/10/2015.
 */
public class Share {

    int currentSharePrice;
    int prevDayClose;
    int id;
    String name;

    public Share(String name, int id, int currentSharePrice) {
        this.name = name;
        this.id = id;
        this.currentSharePrice = currentSharePrice;
        this.prevDayClose = currentSharePrice;
    }

}

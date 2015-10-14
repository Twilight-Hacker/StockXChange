package com.example.galadar.stockxchange;

/**
 * Created by Galadar on 29/9/2015.
 */
public class Company {
    String name;
    int totalValue;
    int currentValue;
    int percentageValue;
    int investment;
    int totalShares;
    double outlook;
    enum Sectors{Construction, Transportation, Oil, Technology, Food, Telecommunications, Defence, Entertainment, Education, Tourism};
    Sectors Sector;
    double marketShare;
    int revenue;

    public Company(String name) {
        this.name = name;
        totalValue = (int) Math.round( Math.random()*100000000 );
        currentValue = totalValue;
        percentageValue = 0;
        totalShares = (int)Math.round(Math.random()*10000);
        investment = 0;
        outlook = 1;
        Sector = RandomSector();
        marketShare = Math.min(Math.random(), 0.3);
        revenue = 0;
    }

    private Sectors RandomSector(){
        int i = (int)Math.round(Math.random()*100)%(Sectors.values().length);

        return Sectors.values()[i];

        //return Sectors.Construction;
    }

    public int shareStart(){
        return (int)Math.round(this.totalValue/this.totalShares);
    }
}

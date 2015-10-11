package com.example.galadar.stockxchange;

/**
 * Created by Galadar on 29/9/2015.
 */
public class Company {
    String name;
    double totalValue;
    double currentValue;
    double percentageValue;
    double investment;
    int totalShares;
    double outlook;
    enum Sectors{Construction, Transportation, Oil, Technology, Food, Telecommunications, Defence, Entertainment, Education, Tourism};
    Sectors Sector;
    double marketShare;
    double revenue;

    public Company(String name) {
        this.name = name;
        totalValue = Math.random()*1000000;
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
        //int i = (int)Math.round(Math.random()*1000%Sectors.values().length)+1;

        return Sectors.Construction;
    }

    public int shareStart(){
        return (int)Math.round(this.totalValue/this.totalShares);
    }
}

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
    int fame;

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
        fame = 300;
    }

    private Sectors RandomSector(){
        int i = (int)Math.round(Math.random()*100)%(Sectors.values().length);

        return Sectors.values()[i];

        //return Sectors.Construction;
    }

    public String getName() {
        return name;
    }

    public int getFame() {
        return fame;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }


    public int getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(int totalValue) {
        this.totalValue = totalValue;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public int getPercentageValue() {
        return percentageValue;
    }

    public void setPercentageValue(int percentageValue) {
        this.percentageValue = percentageValue;
    }

    public int getInvestment() {
        return investment;
    }

    public void setInvestment(int investment) {
        this.investment = investment;
    }

    public int getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(int totalShares) {
        this.totalShares = totalShares;
    }

    public double getOutlook() {
        return outlook;
    }

    public void setOutlook(double outlook) {
        this.outlook = outlook;
    }

    public String getSector() {
        return Sector.name();
    }

    public void setSector(Sectors sector) {
        Sector = sector;
    }

    public double getMarketShare() {
        return marketShare;
    }

    public void setMarketShare(double marketShare) {
        this.marketShare = marketShare;
    }

    public int getRevenue() {
        return revenue;
    }

    public void setRevenue(int revenue) {
        this.revenue = revenue;
    }
    public int shareStart(){
        return (int)Math.round(this.totalValue/this.totalShares);
    }
}

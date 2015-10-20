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
    public enum  Sectors{
        Constr(0), Transp(1), Oil(2), Tech(3), Food(4), Telecomm(5), Defence(6), Entertainment(7), Education(8), Tourism(9);

        private int value;

        Sectors(int value){
            this.value =value;
        }

        public int getValue(){return value;}
    };
    Sectors Sector;
    double marketShare;
    int revenue;
    int fame;
    int lastRevenue;

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
        lastRevenue = (int)Math.round( Math.random()*100000 );

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

    public int get10000Outlook(){
        return (int)Math.round(this.outlook*10000);
    }

    public int getFame() {
        return fame;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public static int getSectorInt(String sec){
        int i=0;
        while (Sectors.values()[i].toString()!=sec){
            i++;
            if(i==Sectors.values().length) return 0;
        }
        return i;
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

    public int getSectorInt(){
        return Sector.ordinal();
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


    public int getLastRevenue() {
        return lastRevenue;
    }

    //TODO real last revenue update
    public void setLastRevenue(int lastRevenue) {
        this.lastRevenue = lastRevenue;
    }

}

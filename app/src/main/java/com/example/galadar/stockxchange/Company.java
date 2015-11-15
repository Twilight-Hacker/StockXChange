package com.example.galadar.stockxchange;

import java.util.Random;

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
        Constr(0), Transp(1), Oil(2), Tech(3), Food(4), Telecom(5), Defence(6), Entert(7), Educ(8), Tourism(9);

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
        Random r = new Random();
        this.name = name;
        this.totalValue = r.nextInt(1000000)+r.nextInt(100000)+r.nextInt(10000)+250000;
        this.currentValue = 0;
        this.percentageValue = 0;
        this.totalShares = r.nextInt(10000)+r.nextInt(1000)+r.nextInt(100)+2500;
        this.investment = 0;
        this.outlook = r.nextDouble();
        this.Sector = RandomSector();
        this.marketShare = Math.min(Math.random(), 0.3);
        this.revenue = 0;
        this.lastRevenue = r.nextInt(10000000);

        fame = 300;
    }

    public Company(String name, Sectors sec) {
        Random r = new Random();
        this.name = name;
        this.totalValue = r.nextInt(1000000)+r.nextInt(100000)+r.nextInt(10000)+250000;
        this.currentValue = 0;
        this.percentageValue = 0;
        this.totalShares = r.nextInt(10000)+r.nextInt(1000)+r.nextInt(100)+2500;
        this.investment = 0;
        this.outlook = r.nextDouble();
        this.Sector = sec;
        this.marketShare = Math.min(Math.random(), 0.3);
        this.revenue = 0;
        this.lastRevenue = r.nextInt(10000000);

        fame = 300;
    }


    public Sectors getSector(int i){
        return Sectors.values()[i];
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
        return (int)Math.round((double)this.totalValue*100/(double)this.totalShares);
    }


    public int getLastRevenue() {
        return lastRevenue;
    }

    public void setLastRevenue(int lastRevenue) {
        this.lastRevenue = lastRevenue;
    }

}

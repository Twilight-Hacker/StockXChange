package com.galadar.example.stockxchange;

import java.util.Random;

/*
 * Created by Galadar on 29/9/2015.
 * Company Object
 */

public class Company {
    String name;
    long totalValue;
    long currentValue;
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
    }
    Sectors Sector;
    double marketShare;
    int revenue;
    int fame;
    int lastRevenue;

    public Company(String name) {
        Random r = new Random();
        this.name = name;
        this.totalValue = r.nextInt(400000)+r.nextInt(10000)+r.nextInt(1000)+452500;
        this.currentValue = totalValue;
        this.percentageValue = 0;
        this.totalShares = Math.round(totalValue/(30 + r.nextInt(599)/100));
        this.investment = 0;
        this.outlook = r.nextDouble()*0.5-0.2;
        this.Sector = RandomSector();
        this.marketShare = Math.random()*0.2 + 0.1;
        this.revenue = 0;
        this.lastRevenue = (int)Math.round( r.nextDouble()*0.3*totalValue );

        fame = 300;
    }

    public Company(String name, Sectors sec) {
        Random r = new Random();
        this.name = name;
        this.totalValue = r.nextInt(400000)+r.nextInt(10000)+r.nextInt(1000)+452500;
        this.currentValue = totalValue;
        this.percentageValue = 0;
        this.totalShares = Math.round(totalValue/(30 + r.nextInt(599)/100));
        this.investment = 0;
        this.outlook = r.nextDouble();
        this.Sector = sec;
        this.marketShare = Math.random()*0.5 - 0.15;
        this.revenue = 0;
        this.lastRevenue = r.nextInt(100000);

        fame = 300;
    }


/*
    public Sectors getSector(int i){
        return Sectors.values()[i];
    }
*/

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

    public static int getSectorInt(String sec){
        int i=0;
        while (!Sectors.values()[i].toString().equals(sec)){
            i++;
            if(i==Sectors.values().length) return 0;
        }
        return i;
    }

    public long getTotalValue() {
        return totalValue;
    }

    public long getCurrentValue() {
        return currentValue;
    }

    public int getPercentageValue() {
        return percentageValue;
    }

    public int getInvestment() {
        return investment;
    }

    public int getTotalShares() {
        return totalShares;
    }

    public double getOutlook() {
        return outlook;
    }

    public String getSector() {
        return Sector.name();
    }

    public int getSectorInt(){
        return Sector.getValue();
    }

    public double getMarketShare() {
        return marketShare;
    }

    public int getRevenue() {
        return revenue;
    }

    public int shareStart(){
        return (int)Math.round(100*(double)this.totalValue/(double)this.totalShares);
    }


    public int getLastRevenue() {
        return lastRevenue;
    }
}

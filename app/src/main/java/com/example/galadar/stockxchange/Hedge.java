package com.example.galadar.stockxchange;

/**
 * Created by Galadar on 29/9/2015.
 */
public class Hedge {

    private double outlook;
    double value;
    private double risk;
    String name;
    int id; //always negative to differentiate from shares
    int remainingTerms;

    public Hedge(String name, int id) {
        this.id =id;
        this.name = name;
        this.outlook = Math.random()*2-1;
        this.risk = Math.random();
        this.remainingTerms = 1+ (int)Math.round(Math.random()*20);
    }

    public double getRevenue(double perc_owned){
        double revenue=0;
        double newValue;

        newValue = getValue(this.value, this.risk);

        revenue = (newValue-this.value)*perc_owned;
        this.value = newValue;

        return revenue;
    }

    private double getValue(double value, double risk) {
        double random = Math.random();

        if(random>risk){
            return value+(value*risk*Math.random());
        } else {
            return value-(value*risk*Math.random());
        }
    }

}

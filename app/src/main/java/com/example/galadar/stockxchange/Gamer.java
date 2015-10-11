package com.example.galadar.stockxchange;

/**
 * Created by Galadar on 29/9/2015.
 */
public class Gamer {

    String name;
    int money;
    int assets;
    private int fame;
    int[][] owned;

    public Gamer(String name, int OwnedSize) {
        this.name = name;
        this.money = 10000;
        this.assets = 0;
        this.fame = 0;

        owned = new int[OwnedSize][2];

        for(int i=0;i<owned.length;i++){
            owned[i][0]=0;
            owned[i][1]=0;
        }
    }

    public String getName() {
        return name;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money += money;
    }

    public int getAssets() {
        return assets;
    }

    public void incAssets() {
        this.assets++;
    }

    public void decAssets() {
        this.assets--;
    }

    public int getFame() {
        return fame;
    }

    public void setFame(int fame) {
        this.fame += fame;
    }
}

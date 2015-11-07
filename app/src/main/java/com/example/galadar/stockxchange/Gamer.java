package com.example.galadar.stockxchange;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Galadar on 29/9/2015.
 */
public class Gamer {

    long money;
    int assets;
    int level;
    private int fame;



    public Gamer(MemoryDB DBHandler) {
        this.money = DBHandler.getPlayerMoney();
        this.assets = DBHandler.getAssets();
        this.level = DBHandler.getLevel();
        this.fame = DBHandler.getFame();
    }

    public Gamer(long money, int level, int assets, int fame) {
        this.money = money;
        this.level = level;
        this.assets = assets;
        this.fame = fame;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public void alterMoney(long amount){
        this.money -= amount;
    }

    public int getAssets() {
        return assets;
    }

    public void setAssets(int assets) {
        this.assets = assets;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFame() {
        return fame;
    }

    public void alterFame(int alteration) {
        this.fame += fame;
    }

}

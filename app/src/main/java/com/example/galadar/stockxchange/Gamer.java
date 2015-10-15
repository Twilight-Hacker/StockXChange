package com.example.galadar.stockxchange;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Galadar on 29/9/2015.
 */
public class Gamer {

    int money;
    int assets;
    int level;
    private int fame;



    public Gamer(MemoryDB DBHandler) {
    }

    public Gamer(int money, int level, int assets, int fame) {
        this.money = money;
        this.level = level;
        this.assets = assets;
        this.fame = fame;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
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

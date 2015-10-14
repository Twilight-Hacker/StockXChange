package com.example.galadar.stockxchange;

import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by Galadar on 29/9/2015.
 */
public class Finance {

    int EconomySize;
    double[] outlooks;
    HashSet CompaniesNames;
    Company[] CompaniesList;
    Share[] SharesList;
    int term;
    int day;


    public Finance(MemoryDB DBHandler, int size) {
        int numComp = size*10;
        CompaniesNames = new HashSet();
        CompaniesList = new Company[numComp];
        int totShares;
        //SharedPreferences.Editor editor2 = Ownerships.edit();
        for(int i=0;i<numComp;i++){
            String name = randomName();
            boolean go = CompaniesNames.add(name);
            if(go) {
                CompaniesList[i]=new Company(name);
                totShares = CompaniesList[i].shareStart();
                DBHandler.addShare(new Share(name, i, totShares));

                //editor2.putInt(Integer.toString(i), 0);

            } else {
                i--;
            }
        }
        //editor2.commit();

        outlooks = new double[11];

        for(int i=0;i<outlooks.length;i++){
            outlooks[i] = Math.random()*2-1;
        }

        term =1;
        day=1;
    }

    public Share getShare(int i){
        return this.SharesList[i];
    }

    private String randomName() {

        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final int N = alphabet.length();
        Random r = new Random();

        String value="";
        for (int i = 0; i < 4; i++) {
            value = value + (alphabet.charAt(r.nextInt(N)));
        }
        return value;
    }
}

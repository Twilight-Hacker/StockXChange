package com.example.galadar.stockxchange;

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


    public Finance(int size) {
        int numComp = size*10;
        CompaniesNames = new HashSet();
        CompaniesList = new Company[numComp];
        SharesList = new Share[CompaniesList.length];
        int totShares;
        for(int i=1;i<numComp;i++){
            String name = randomName();
            boolean go = CompaniesNames.add(new Company(name) );
            if(go) {
                CompaniesList[i]=new Company(name);
                totShares = CompaniesList[i].shareStart();
                SharesList[i]=new Share(name, i, totShares);
            } else {
                i--;
            }
        }

        outlooks = new double[11];

        for(int i=0;i<outlooks.length;i++){
            outlooks[i] = Math.random()*2-1;
        }

        term =1;
        day=1;
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

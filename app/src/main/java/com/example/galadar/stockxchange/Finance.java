package com.example.galadar.stockxchange;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by Galadar on 29/9/2015.
 */
public class Finance implements Parcelable {

    int EconomySize;
    double[] outlooks;
    HashSet CompaniesNames;
    String[] Names;
    int[][] Shares;
    int[][] Companies;
    Company company;
    Share share;
    int numComp;


    public Finance(MemoryDB DBHandler){
        String name;
        numComp = DBHandler.getMaxSID();
        Companies = new int[numComp][6];
        Shares = new int[numComp][4];
        Names = new String[numComp];
        CompaniesNames = new HashSet();
        for(int i=0; i<DBHandler.getMaxSID();i++){
            name = DBHandler.getDBShareName(i);
            boolean ok = CompaniesNames.add(name);
            while(!ok){
                String newName = randomName();
                ok = CompaniesNames.add(newName);
                if(ok) {
                    DBHandler.setDBShareName(i, newName);
                    DBHandler.setDBCompName(name, newName);
                    name = newName;
                }
            }
            Companies[i][0] = DBHandler.getCompTotalValue(name);
            Companies[i][1] = Company.getSectorInt(DBHandler.getCompanySector(name));
            Companies[i][2] = DBHandler.getCompRevenue(name);
            Companies[i][3] = DBHandler.get10000CompOutlook(name);
            Companies[i][4] = DBHandler.getLastRevenue(name);
            Companies[i][5] = DBHandler.getInvestment(name);
            Shares[i][0] = DBHandler.getDBCurrPrice(i);
            Shares[i][1] = DBHandler.getOwnedShare(i);
            Shares[i][2] = DBHandler.getTotalShares(i);
            Names[i]=name;
        }

        outlooks = new double[Company.Sectors.values().length];

        for (int i = 0; i < outlooks.length ; i++) {
            outlooks[i] = DBHandler.getOutlook(Company.Sectors.values()[i].toString());
        }
    }

    public Finance(MemoryDB DBHandler, int size) {
        numComp = size*10;
        Companies = new int[numComp][6];
        Shares = new int[numComp][4];
        Names = new String[numComp];
        CompaniesNames = new HashSet();
        for(int i=0;i<numComp;i++){
            String name = randomName();
            boolean go = CompaniesNames.add(name);
            if(go) {
                company = new Company(name);
                DBHandler.addCompany(company, i);
                share = new Share(name, i, company.shareStart(), company.getTotalShares());
                DBHandler.addShare(share);
                Companies[i][0] = company.getTotalValue();
                Companies[i][1] = company.getSectorInt();
                Companies[i][2] = company.getRevenue();
                Companies[i][3] = company.get10000Outlook();
                Companies[i][4] = company.getLastRevenue();
                Companies[i][5] = company.getInvestment();
                Shares[i][0] = share.getCurrentSharePrice();
                Shares[i][1] = 0; //Amount Owned
                Shares[i][2] = company.getTotalShares();
                Shares[i][3] = share.getPrevDayClose();
                Names[i]=name;
            } else {
                i--;
            }
        }

        outlooks = new double[Company.Sectors.values().length];

        for(int i=0;i<outlooks.length;i++){
            outlooks[i] = Math.random()*2-1;
            DBHandler.setOutlook(Company.Sectors.values()[i].toString(), outlooks[i]);
        }

        this.EconomySize = calcEconomySize();

    }

    protected Finance(Parcel in) {
        EconomySize = in.readInt();
        outlooks = in.createDoubleArray();
        Names = in.createStringArray();
    }

    public static final Creator<Finance> CREATOR = new Creator<Finance>() {
        @Override
        public Finance createFromParcel(Parcel in) {
            return new Finance(in);
        }

        @Override
        public Finance[] newArray(int size) {
            return new Finance[size];
        }
    };

    public int getEconomySize() {
        return this.EconomySize;
    }

    public int getLastRevenue(int id) {
        return Companies[id][4];
    }

    public void updateLastRevenue(int id, int last) {
        Companies[id][4] = last;
    }

    public void resetEconomySize() {
        this.EconomySize = calcEconomySize();
    }

    public int calcEconomySize(){
        int size = 0;

        for (int i = 0; i < Companies.length; i++) {
            size += Companies[i][0];
        }

        return size;
    }


    public int getShareCurrPrince(int id){
        return Shares[id][0];
    }

    public String getName(int id){
        return Names[id];
    }

    public int getLastClose(int id){
        return Shares[id][3];
    }

    public void DayCloseShares(){
        for(int i=0;i<Shares.length;i++){
            Shares[i][3]=Shares[i][0];
        }
    }

    public void alterShareCurrPrice(int id, int alteration){
        Shares[id][0] += alteration;
    }

    public int getSharesOwned(int id){
        return Shares[id][1];
    }

    public void TransactShares(int id, int amount){
        Shares[id][1] += amount;
    }

    public int getTotalShares(int id){
        return Shares[id][2];
    }


    public int getCompTotalValue(int id){
        return Companies[id][0];
    }

    public void setCompTotalValue(int id, int newV){
        Companies[id][0] = newV;
    }

    public String getCompSector(int id){
        return Company.Sectors.values()[Companies[id][1]].toString();
    }

    public int getCompRevenue(int id){
        return Companies[id][2];
    }

    public void UpdateCompRevenue(int id, int amount){
        Companies[id][2] += amount;
    }

    public void ResetCompRevenue(int id){
        Companies[id][2] = 0;
    }

    public double getCompOutlook(int id){
        return (double)Companies[id][3]/10000;
    }

    public void setCompOutlook(int id, double newO){
        Companies[id][3] = (int)Math.round(newO/10000);
    }

    public double getSectorOutlook(String sec){
        int i=0;
        while (Company.Sectors.values()[i].toString().equalsIgnoreCase(sec)){
            i++;
            if(i== Company.Sectors.values().length) return 0;
        }
        return outlooks[i];
    }

    public void setSectorOutlook(String sec, double newO){
        int i=0;
        while (Company.Sectors.values()[i].toString()!=sec){
            i++;
        }
        outlooks[i]=newO;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(EconomySize);
        dest.writeDoubleArray(outlooks);
        dest.writeStringArray(Names);
    }

    public int getInvestment(int id) {

        return Companies[id][5];

    }

    public void setInvestment(int id, int inv){
        Companies[id][5]=inv;
    }

    public int getNumComp() {
        return numComp;
    }

    public int getSumShares(){
        int sum=0;
        for (int i = 0; i <getNumComp() ; i++) {
            sum += Shares[i][2];
        }
        return sum;
    }

}

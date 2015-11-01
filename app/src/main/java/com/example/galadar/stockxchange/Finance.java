package com.example.galadar.stockxchange;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by Galadar on 29/9/2015.
 */
public class Finance {

    long EconomySize;
    double[][] outlooks;
    HashSet CompaniesNames;
    HashSet Scams;
    int[][] ScamResolution;
    String[] Names;
    int[][] Shares;
    int[][] Companies;
    int[][] Short;
    Company company;
    Share share;
    int numComp;


    public Finance(MemoryDB DBHandler){
        String name;
        int CurrentDay = MainActivity.getClock().totalDays();
        numComp = DBHandler.getMaxSID();
        Companies = new int[numComp][6];
        Shares = new int[numComp][5];
        Names = new String[numComp];
        Short = new int[numComp][2];
        CompaniesNames = new HashSet();
        Scams = new HashSet();
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
            Shares[i][3] = DBHandler.getDBLastClose(i);
            Shares[i][4] = DBHandler.getRemShares(i);
            Shares[i][0] = DBHandler.getShortAmount(i); //Amount of Share i to settle
            int days = DBHandler.getShortDays(i)-CurrentDay;
            if(days>0) {
                Shares[i][1] = days; //Remaining days until settle
            } else {
                Shares[i][1] = -1;
            }
            Names[i]=name;
        }

        outlooks = new double[Company.Sectors.values().length][2];

        for (int i = 0; i < outlooks.length ; i++) {
            outlooks[i][0] = DBHandler.getOutlook(Company.Sectors.values()[i].toString());
            outlooks[i][1] = 0;
        }

        ScamResolution = new int[numComp][2];
        for (int i = 0; i < ScamResolution.length; i++) {
            if(DBHandler.isScam(i)){
                Scams.add(Names[i]);                //Add name to Hashset
                ScamResolution[i][0] =  DBHandler.getScamType(i);
                ScamResolution[i][1] = DBHandler.getScamResolutionDay(i)-CurrentDay;
            } else {
                ScamResolution[i][0] = 0;   //Scam type/category 1-5, 0 for no scam
                ScamResolution[i][1] = -1;  //remaining days, -1 for no scams
            }
            if(this.ScamResolution[i][0]==2) {       //Alter Scam Share Outlook for execution of Pump&Dump Scam
                if (ScamResolution[i][1] < 6) {
                    Companies[ScamResolution[i][0]][3] += 5 * (5 - ScamResolution[i][1]);
                }
            }
            if(ScamResolution[i][0]==3) {       //Alter Scam Share Outlook for execution of Short&Distort Scam
                if (ScamResolution[i][1] < 4) {
                    Companies[ScamResolution[i][0]][3] -= 5 * (3 - ScamResolution[i][1]);
                }
            }
        }

    }

    public void resetAllScams(){ //For QuickGame Usage

    }

    public int getRemShares(int id){
        return Shares[id][4];
    }

    public void alterRemShares(int id, int amount){
        Shares[id][4] -= amount;
    }

    public Finance(MemoryDB DBHandler, int size) {
        numComp = size*10;
        Companies = new int[numComp][6];
        Shares = new int[numComp][5];
        Names = new String[numComp];
        Short = new int[numComp][2];
        CompaniesNames = new HashSet();
        ScamResolution = new int[numComp][2];
        for(int i=0;i<numComp;i++){
            String name = randomName();
            boolean go = CompaniesNames.add(name);
            if(go) {
                company = new Company(name);
                DBHandler.addCompany(company, i);
                share = new Share(name, i, company.shareStart(), company.getTotalShares());
                DBHandler.addShare(share);
                Names[i]=name;
                Companies[i][0] = company.getTotalValue()*100;
                Companies[i][1] = company.getSectorInt();
                Companies[i][2] = company.getRevenue();
                Companies[i][3] = company.get10000Outlook();
                Companies[i][4] = company.getLastRevenue();
                Companies[i][5] = company.getInvestment();
                Shares[i][0] = share.getCurrentSharePrice();
                Shares[i][1] = 0; //Amount Owned
                Shares[i][2] = company.getTotalShares();
                Shares[i][3] = share.getPrevDayClose();
                Shares[i][4] = Math.round(share.getTotalShares() / 2);
                Short[i][0] = 0; //Amount to Settle
                Short[i][1] = -1; //Remaining days
                ScamResolution[i][0]=0;
                ScamResolution[i][1]=-1;
            } else {
                i--;
            }
        }

        outlooks = new double[Company.Sectors.values().length][2];

        for(int i=0;i<outlooks.length;i++){
            outlooks[i][0] = Math.random()*2-1;
            DBHandler.setOutlook(Company.Sectors.values()[i].toString(), outlooks[i][0]);
            outlooks[i][1]=0;
        }

        this.EconomySize = calcEconomySize();

    }

    public void ShortShare(int sid, int amount, int Days){
        Short[sid][0]=amount;
        Short[sid][1]=Days;
    }

    public int getTodaysShort(int SID){
        if(Short[SID][1]==0){
            int amount = Short[SID][0];
            Short[SID][0]=0;
            Short[SID][1]=-1;
            return amount;
        } else {
            return 0;
        }
    }

    public int getShortRemainingDays(int SID){
        return Short[SID][1];
    }

    public long getEconomySize() {
        return this.EconomySize;
    }

    public int getLastRevenue(int id) {
        return Companies[id][4];
    }

    public void resetEconomySize() {
        this.EconomySize = calcEconomySize();
    }

    public long calcEconomySize(){
        long size = 0;

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
            if(Short[i][1]>0) {
                Short[i][1]--;
            }
        }
        for (int i = 0; i < ScamResolution.length; i++) {
            if(ScamResolution[i][0]!=0 & ScamResolution[i][1]>0){
                ScamResolution[i][1]--;
            }
        }
    }

    public boolean isScam(int i){
        if (Scams.isEmpty())return false;
        return Scams.contains(getName(i));
    }

    public int getScamType(int ScamTableIndex){
        return ScamResolution[ScamTableIndex][0];
    }

    public int getScamRemDays(int ScamTableIndex){
        return ScamResolution[ScamTableIndex][1];
    }

    public int getScamsNo(){
        return ScamResolution.length;
    }

    public void clearScam(int sid){
        Scams.remove(getName(sid));
        ScamResolution[sid][1]=0;
        ScamResolution[sid][2]=-1;
    }

    public void setShareCurrPrice(int id, int price){
        Shares[id][0] = price;
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
        return (double)Companies[id][3]/1000;
    }

    public void setCompOutlook(int id, double newO){
        Companies[id][3] = (int)Math.round(newO*1000);
    }

    public double getSectorOutlook(int i){
        return outlooks[i][0]+outlooks[i][1];
    }

    public double getBaseSectorOutlook(int i){
        return outlooks[i][0];
    }

    public void setSectorOutlook(int index, double newO){
        outlooks[index][0]=newO;
    }

    public void setSectorEventOutlook(int index, double newO){
        outlooks[index][1]+=newO;
    }

    public boolean addScam(int sid){
        return Scams.add(getName(sid));
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

    public int getInvestment(int id) {

        return Companies[id][5];

    }

    public boolean addCompanyName(String name){
        return !CompaniesNames.contains(name);
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

    public long getSecEconSize(int sectorI) {
        long size =0;
        for (int i = 0; i < Companies.length; i++) {
            if(Companies[i][1]==sectorI){
                size += getCompTotalValue(i);
            }
        }
        return size;
    }

    public int getCompSectorInt(int i) {
        return Companies[i][1];
    }

    public long NetWorth(){
        long value = 0;
        for (int i = 0; i < Shares.length; i++) {
            value += getShareCurrPrince(i)*getSharesOwned(i);
        }
        return value;
    }

    public void addScamData(int sid, int type, int totalDays) {
        ScamResolution[sid][0]=type;
        ScamResolution[sid][1]=totalDays;
    }

    public void removeScam(int i){
        ScamResolution[i][0]=0;
        ScamResolution[i][1]=-1;
    }

    public void Backrupt(int i) {
        setCompTotalValue(i, 0);
        setShareCurrPrice(i, 0);
    }
}

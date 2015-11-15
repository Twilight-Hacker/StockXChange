package com.example.galadar.stockxchange;

import com.example.galadar.stockxchange.Company.Sectors;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by Galadar on 29/9/2015.
 */
public class Finance {

    long EconomySize;
    private double[][] outlooks;
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

    public String getSetCompName(int i){
        Object[] com = CompaniesNames.toArray();
        return com[i].toString();
    }

    public int getSetCompNum(){
        return CompaniesNames.size();
    }


    public Finance(int size) { //Quick Game Constructor
        numComp = size*10;
        Companies = new int[numComp][6];
        Shares = new int[numComp][5];
        Names = new String[numComp];
        Short = new int[numComp][2];
        CompaniesNames = new HashSet();
        Scams = new HashSet();
        ScamResolution = new int[numComp][2];
        for(int i=0;i<numComp;i++){
            String name = randomName();
            boolean go = CompaniesNames.add(name);
            if(go) {
                company = new Company(name);
                share = new Share(name, i, company.shareStart(), company.getTotalShares());
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

        outlooks = new double[Sectors.values().length+1][2];

        outlooks[0][0]=0;
        outlooks[0][1]=0;

        for(int i=1;i<outlooks.length;i++){
            outlooks[i][0] = Math.random()*2-1;
            outlooks[i][1]=0;
        }

        Random random = new Random();

        int newScams = random.nextInt(10)+5;
        int sid;
        int type;
        int totalDays;
        for (int i = 0; i < newScams; i++) {
            do{
                sid=random.nextInt(getNumComp()-1);
            } while(!addScam(sid));

            double e = random.nextDouble(); //5 is current total Number of Categories, from 1 to 5, see MainActivity Scam Resolution Function for details.

            if(e<0.1) type = 1;                     //Ponzi Scheme
            else if (e<=0.3) type = 2;              //Pump&Dump
            else if (e<=0.5) type = 3;              //Short&Distort
            else if (e<0.6) type = 4;               //Empty Room
            else type =5;                           //Lawbreaker Scandal

            totalDays=random.nextInt(30)+25;
            addScamData(sid, type, totalDays);
        }

        this.EconomySize = calcEconomySize();

    }

    public Finance(MemoryDB DBHandler){ //Loaded Game Constructor
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
            Short[i][0] = DBHandler.getShortAmount(i); //Amount of Share i to settle
            int days = DBHandler.getShortDays(i)-CurrentDay;
            if(days>0) {
                Short[i][1] = days; //Remaining days until settle
            } else {
                Short[i][1] = -1;
            }
            Names[i]=name;
        }

        outlooks = new double[Sectors.values().length+1][2];

        outlooks[0][0]=DBHandler.getEconomyOutlook();
        outlooks[0][1]=0;

        for (int i = 1; i < outlooks.length ; i++) {
            outlooks[i][0] = DBHandler.getOutlook(Sectors.values()[i-1].toString());
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

    public Finance(MemoryDB DBHandler, int size) { //New Game Contsructor
        numComp = size*10;
        Companies = new int[numComp][6];
        Shares = new int[numComp][5];
        Names = new String[numComp];
        Short = new int[numComp][2];
        Scams = new HashSet();
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
                Shares[i][4] = Math.round(share.getTotalShares() / 2);
                Short[i][0] = 0; //Amount to Settle
                Short[i][1] = -1; //Remaining days
                ScamResolution[i][0]=0;
                ScamResolution[i][1]=-1;
            } else {
                i--;
            }
        }

        outlooks = new double[Sectors.values().length+1][2];

        outlooks[0][0]=DBHandler.getEconomyOutlook();
        outlooks[0][1]=0;

        for(int i=1;i<outlooks.length;i++){
            outlooks[i][0] = Math.random()*2-1;
            DBHandler.setOutlook(Sectors.values()[i-1].toString(), outlooks[i][0]);
            outlooks[i][1]=0;
        }

        this.EconomySize = calcEconomySize();

    }

    public void clearShort(int SID){
        Short[SID][0]=0;
        Short[SID][1]=-1;
    }


    public void ShortShare(int sid, int amount, int days){
        Short[sid][0]=amount;
        Short[sid][1]=days;
    }

    public boolean isShorted(int SID){
        return Short[SID][0]!=0;
    }

    public int getNumOfOutlooks(){
        return outlooks.length;
    }

    public int getRemShares(int id){
        return Shares[id][4];
    }

    public void alterRemShares(int id, int amount){
        Shares[id][4] -= amount;
    }

    public int getShortRemainingDays(int SID){ return Short[SID][1]; }

    public int getPosShortAmount(int SID){ return Math.abs(Short[SID][0]); }

    public long getEconomySize() {
        return calcEconomySize();
    }

    public int getLastRevenue(int id) {
        return Companies[id][4];
    }

    public void resetEconomySize() {
        this.EconomySize = calcEconomySize();
    }

    public long calcEconomySize(){
        double size = 0;

        for (int i = 0; i < Companies.length; i++) {
            size += (double)getCompTotalValue(i)/100;
        }

        return Math.round(size);
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
            Companies[i][2]+=Shares[i][0]-Shares[i][3]; //Add to company revenue, reflecting the share popularity to company market profits
            Shares[i][3]=Shares[i][0];
            if(Short[i][1]>=0) {
                Short[i][1]--;
            }
            if(ScamResolution[i][1]>=0){
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
        return Scams.size();
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
        return Sectors.values()[Companies[id][1]].toString();
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
        if(Math.abs(outlooks[i][0]+outlooks[i][1])<=2)return outlooks[i][0]+outlooks[i][1];
        else return Math.signum(outlooks[i][0]+outlooks[i][1])*2;
    }

    public double getBaseSectorOutlook(int i){
        return outlooks[i][0];
    }

    public void setSectorOutlook(int index, double newO){
        outlooks[index][0]=newO;
    }

    public void alterSectorOutlook(int index, double newO){
        outlooks[index][0]+=newO;
    }

    public void setSectorEventOutlook(int index, double newO){
        outlooks[index][1]+=newO;
    }

    public boolean addScam(int sid){
        return Scams.add(getName(sid));
    }


    public static String randomName() {

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

    public void setInvestment(int id, int newInv){
        Companies[id][5]=newInv;
    }

    public boolean addCompanyName(String name){
        return CompaniesNames.add(name);
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
        Scams.remove(getName(i));
        ScamResolution[i][0]=0;
        ScamResolution[i][1]=-1;
    }

    public void Backrupt(int i) {
        setCompTotalValue(i, 0);
        setCompOutlook(i, -10);
        setShareCurrPrice(i, 20);
        resetRemShares(i);
    }

    public int getSectorOutlookIndex(String Sector){
        int index = -1;
        switch (Sector){
            case "Constr":
                index=1;
                break;
            case "Transp":
                index=2;
                break;
            case "Oil":
                index=3;
                break;
            case "Tech":
                index=4;
                break;
            case "Food":
                index=5;
                break;
            case "Telecom":
                index=6;
                break;
            case "Defence":
                index=7;
                break;
            case "Entert":
                index=8;
                break;
            case "Educ":
                index=9;
                break;
            case "Tourism":
                index=10;
                break;
        }
        return index;
    }

    public void resetRemShares(int SID) {
        Shares[SID][4]=(int)Shares[SID][2]/2;
    }

    public int getRandomActiveSID() {
        int sid;
        Random random = new Random();
        do{
            sid=random.nextInt(getNumComp());
        } while(isScam(sid) | (getCompTotalValue(sid)==0));
        return sid;
    }

    public void removeCompanyName(String name) {
        CompaniesNames.remove(name);
    }

    public int getoldSID(String name) {
        for (int i = 0; i <Names.length; i++) {
            if(Names[i]==name) return i;
        }
        return -1;
    }

    public int Getdivident(int i, int revenue) {
        int divident = Math.max((int)getShareCurrPrince(i)/100, 100); //min divident of $1, max at 1%of share price
        Random random = new Random();
        while (divident*getTotalShares(i)*2>revenue){ //no more than half of revenue to divident
            divident-=random.nextInt(100);
        }
        return divident;
    }

    public long getShareEconSize() {
        long size=0;
        for (int i = 0; i < getNumComp(); i++) {
            size+=getTotalShares(i)*getShareCurrPrince(i);
        }
        return size;
    }

    public void resetAllNames(){
        Object[] NamesObj = CompaniesNames.toArray();
        Names = new String[CompaniesNames.size()];
        for (int i = 0; i < CompaniesNames.size(); i++) {
            Names[i]=NamesObj[i].toString();
        }
        numComp=Names.length;
    }
}

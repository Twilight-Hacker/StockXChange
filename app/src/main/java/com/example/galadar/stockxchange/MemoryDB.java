package com.example.galadar.stockxchange;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

/**
 * This class is only for setting and retrieving values from/to the Database. It will only do a minimal amount of Calculations where absolutely required.
 * Created by Galadar on 11/10/2015.
 */


public class MemoryDB extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Galadar.DBStockXChange.db";
    public static final int DATABASE_VER = 9;
    public static final String ALL_TABLES_COLUMN_ID = "_id";

    public static final String COMPANIES_TABLE_NAME = "Companies";
    public static final String COMPANIES_COLUMN_NAME = "Name";
    public static final String COMPANIES_COLUMN_TOTAL_VALUE = "TotalValue";
    public static final String COMPANIES_COLUMN_CURRENT_VALUE = "CurrValue";
    public static final String COMPANIES_COLUMN_PERCENTAGE_VALUE = "PercValue";
    public static final String COMPANIES_COLUMN_INVESTMENT = "Investment";
    public static final String COMPANIES_COLUMN_OUTLOOK = "Outlook";
    public static final String COMPANIES_COLUMN_SECTOR = "Sector";
    public static final String COMPANIES_COLUMN_MARKET_SHARE = "MarketShare";
    public static final String COMPANIES_COLUMN_REVENUE = "Revenue";
    public static final String COMPANIES_COLUMN_LAST_REVENUE = "LRevenue";
    public static final String COMPANIES_COLUMN_FAME = "Fame";
    public static final String COMPANIES_COLUMN_CID = "cid";

    public static final String SHARES_TABLE_NAME = "shares";
    public static final String SHARES_COLUMN_NAME = "name";
    public static final String SHARES_COLUMN_SID = "sid";
    public static final String SHARES_COLUMN_CURRENT_PRICE = "currvalue";
    public static final String SHARES_COLUMN_LAST_CLOSE = "Lastllose";
    public static final String SHARES_COLUMN_TOTAL_SHARES = "TotalShares";
    public static final String SHARES_COLUMN_REMAINING_SHARES = "remainingShares";

    public static final String SCAMS_TABLE_NAME = "scams";
    public static final String SCAMS_COLUMN_SID = "sid";
    public static final String SCAMS_COLUMN_TYPE = "category";
    public static final String SCAMS_COLUMN_RESOLUTION_DAY = "endDay";

    public static final String OUTLOOK_TABLE_NAME = "outlooks";
    public static final String OUTLOOK_COLUMN_NAME = "Sector";
    public static final String OUTLOOK_COLUMN_OUTLOOK = "Value";

    public static final String DATA_TABLE_NAME = "GameData";
    public static final String DATA_COLUMN_ENTRY_NAME = "Name";
    public static final String DATA_COLUMN_ENTRY_VALUE = "Value";

    public static final String PROPERTY_TABLE_NAME = "Owned";
    public static final String PROPERTY_COLUMN_SHARE = "Share";
    public static final String PROPERTY_COLUMN_AMOUNT = "Amount";

    public static final String SHORT_TABLE_NAME = "ShortSales";
    public static final String SHORT_COLUMN_SID = "sid";
    public static final String SHORT_COLUMN_AMOUNT = "amount";
    public static final String SHORT_COLUMN_TOTAL_SETTLE_DAYS = "totalDays";

    public static final String EVENTS_TABLE_NAME = "Events";
    public static final String EVENTS_COLUMN_TYPE = "title";
    public static final String EVENTS_COLUMN_MAGNITUDE = "body";
    public static final String EVENTS_COLUMN_END_DAY = "endday";

    public MemoryDB(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL(
                "CREATE TABLE " + SCAMS_TABLE_NAME + "(" +
                        ALL_TABLES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SCAMS_COLUMN_SID + " INTEGER NOT NULL, " +
                        SCAMS_COLUMN_TYPE + " INTEGER, " +
                        SCAMS_COLUMN_RESOLUTION_DAY + " INTEGER, " +
                        " FOREIGN KEY (" + SCAMS_COLUMN_SID + ") REFERENCES " + SHARES_TABLE_NAME + "(" + SHARES_COLUMN_SID + ") ON DELETE CASCADE ON UPDATE CASCADE" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE " + COMPANIES_TABLE_NAME + "(" +
                        ALL_TABLES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COMPANIES_COLUMN_NAME + " CHAR(4) NOT NULL UNIQUE, " +
                        COMPANIES_COLUMN_CID + " INTEGER NOT NULL UNIQUE, " +
                        COMPANIES_COLUMN_TOTAL_VALUE + " INTEGER NOT NULL, " +
                        COMPANIES_COLUMN_CURRENT_VALUE + " INTEGER NOT NULL, " +
                        COMPANIES_COLUMN_PERCENTAGE_VALUE + " INTEGER NOT NULL, " +
                        COMPANIES_COLUMN_INVESTMENT + " INTEGER NOT NULL, " +
                        COMPANIES_COLUMN_OUTLOOK + " REAL NOT NULL, " +
                        COMPANIES_COLUMN_SECTOR + " TEXT NOT NULL, " +
                        COMPANIES_COLUMN_MARKET_SHARE + " REAL NOT NULL, " +
                        COMPANIES_COLUMN_REVENUE + " INTEGER NOT NULL, " +
                        COMPANIES_COLUMN_LAST_REVENUE + " INTEGER NOT NULL, " +
                        COMPANIES_COLUMN_FAME + " INTEGER  NOT NULL" +
                        ");"
        );

        //The game data table will include Player money, assets, fame, as well as economy size and various other numeric values not belonging to companies, shares or outlooks.
        db.execSQL(
                "CREATE TABLE " + DATA_TABLE_NAME + "(" +
                        ALL_TABLES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DATA_COLUMN_ENTRY_NAME + " TEXT NOT NULL UNIQUE, " +
                        DATA_COLUMN_ENTRY_VALUE + " INTEGER  NOT NULL" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE " + OUTLOOK_TABLE_NAME + "(" +
                        ALL_TABLES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        OUTLOOK_COLUMN_NAME + " TEXT NOT NULL UNIQUE, " +
                        OUTLOOK_COLUMN_OUTLOOK + " REAL NOT NULL" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE " + SHARES_TABLE_NAME + "(" +
                        ALL_TABLES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SHARES_COLUMN_NAME + " CHAR(4) NOT NULL UNIQUE, " +
                        SHARES_COLUMN_SID + " INTEGER NOT NULL UNIQUE, " +
                        SHARES_COLUMN_CURRENT_PRICE + " INTEGER NOT NULL, " +
                        SHARES_COLUMN_LAST_CLOSE + " INTEGER NOT NULL, " +
                        SHARES_COLUMN_TOTAL_SHARES + " INTEGER NOT NULL, " +
                        SHARES_COLUMN_REMAINING_SHARES + " INTEGER NOT NULL" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE " + PROPERTY_TABLE_NAME + "(" +
                        ALL_TABLES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PROPERTY_COLUMN_SHARE + " INTEGER NOT NULL UNIQUE, " +
                        PROPERTY_COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                        " FOREIGN KEY (" + PROPERTY_COLUMN_SHARE + ") REFERENCES " + SHARES_TABLE_NAME + "(" + SHARES_COLUMN_SID + ") ON DELETE CASCADE ON UPDATE CASCADE" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE " + SHORT_TABLE_NAME + "(" +
                        ALL_TABLES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SHORT_COLUMN_SID + " INTEGER NOT NULL, " +
                        SHORT_COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                        SHORT_COLUMN_TOTAL_SETTLE_DAYS + " INTEGER NOT NULL, " +
                        " FOREIGN KEY (" + SHORT_COLUMN_SID + ") REFERENCES " + SHARES_TABLE_NAME + "(" + SHARES_COLUMN_SID + ") ON DELETE CASCADE ON UPDATE CASCADE" +
                        ");"
        );

                db.execSQL(
                        "CREATE TABLE " + EVENTS_TABLE_NAME + "(" +
                                ALL_TABLES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                EVENTS_COLUMN_MAGNITUDE + " INTEGER NOT NULL, " +
                                EVENTS_COLUMN_TYPE + " INTEGER NOT NULL, " +
                                EVENTS_COLUMN_END_DAY + " INTEGER NOT NULL" +
                                ");"
                );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void addScam(int sid, int type, int totalDays){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SCAMS_COLUMN_SID, sid);
        values.put(SCAMS_COLUMN_TYPE, type);
        values.put(SCAMS_COLUMN_RESOLUTION_DAY, totalDays);
        db.insert(SCAMS_TABLE_NAME, null, values);
        db.close();
    }

    public int getScamsNo(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SCAMS_TABLE_NAME + " where 1;", null);
        c.moveToFirst();
        int a = c.getCount();
        c.close();
        return a;
    }

    public int getScamType(int sid){
        int type=0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SCAMS_TABLE_NAME + " where " + SCAMS_COLUMN_SID + "=" + sid + ";", null);
        if(c.moveToFirst()) type=c.getInt(c.getColumnIndex(SCAMS_COLUMN_TYPE));
        c.close();
        return type;
    }

    public int getScamResolutionDay(int sid){
        int days = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SCAMS_TABLE_NAME + " where " + SCAMS_COLUMN_SID + "=" + sid + ";", null);
        if(c.moveToFirst()) days=c.getInt(c.getColumnIndex(SCAMS_COLUMN_RESOLUTION_DAY));
        c.close();
        return days;
    }

    public void ShortShare(int sid, int NewAmount, int days, long Pmoney){ //SEND TOTAL DAYS
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c =  db.rawQuery("select " + SHORT_COLUMN_AMOUNT + " from " + SHORT_TABLE_NAME + " where " + SHORT_COLUMN_SID + "=" + sid + " AND " + SHORT_COLUMN_TOTAL_SETTLE_DAYS + "=" + days + ";", null);
        if(c.getCount()==0) {
            String where = SHORT_COLUMN_SID+"=?";
            String[] args = {Integer.toString(sid)};
            ContentValues values = new ContentValues();
            values.put(SHORT_COLUMN_AMOUNT, NewAmount);
            values.put(SHORT_COLUMN_TOTAL_SETTLE_DAYS, days);
            db.update(SHORT_TABLE_NAME, values, where, args);
        } else {
            int amount=0;
            if(c.moveToFirst())amount=c.getInt(c.getColumnIndex(SHORT_COLUMN_AMOUNT));
            NewAmount+=amount;
            String where = SHORT_COLUMN_SID+"=?";
            String[] args = {Integer.toString(sid)};
            ContentValues values = new ContentValues();
            values.put(SHORT_COLUMN_AMOUNT, NewAmount);
            db.update(SHORT_TABLE_NAME, values, where, args);
        }
        c.close();
        db.close();
        setPlayerMoney(Pmoney);
    }

    public void ShortSettle(int Totalday){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SHORT_TABLE_NAME, SHORT_COLUMN_TOTAL_SETTLE_DAYS+"=?", new String[] {Integer.toString(Totalday)});
        db.close();
    }

    public int getShortAmount(int sid){
        int amount=0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SHORT_TABLE_NAME + " where " + SHORT_COLUMN_SID + "=" + sid + " ORDER BY " + SHORT_COLUMN_TOTAL_SETTLE_DAYS + " ASC;", null);

        if(c.getCount()==0){
            c.close();
            db.close();
            return 0;
        }

        if(c.moveToFirst())amount=c.getInt(c.getColumnIndex(SHORT_COLUMN_AMOUNT));
        c.close();
        db.close();

        return amount;
    }

    public int getShortDays(int sid){
        int amount=-1;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SHORT_TABLE_NAME + " where " + SHORT_COLUMN_SID + "=" + sid + " ORDER BY " + SHORT_COLUMN_TOTAL_SETTLE_DAYS + " ASC;", null);

        if(c.getCount()==0){
            c.close();
            db.close();
            return -1;
        }

        if(c.moveToFirst())amount=c.getInt(c.getColumnIndex(SHORT_COLUMN_TOTAL_SETTLE_DAYS));
        /*while (!c.isAfterLast()){

            c.moveToNext();
        }*/
        c.close();
        db.close();

        return amount;
    }

    public void TransactShare(int SID, int NewAmount, long newCash){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = PROPERTY_COLUMN_SHARE+"=?";
        String[] args = {Integer.toString(SID)};
        ContentValues values = new ContentValues();
        values.put(PROPERTY_COLUMN_AMOUNT,NewAmount);
        db.update(PROPERTY_TABLE_NAME, values, where, args);
        setPlayerMoney(newCash);
        db.close();
    }

    public int getOwnedShare(int sid){
        int amount=0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + PROPERTY_TABLE_NAME + " where " + PROPERTY_COLUMN_SHARE + "=" + sid + ";", null);

        if(c.getCount()==0){
            c.close();
            db.close();
            return 0;
        }

        c.moveToFirst();
        while (!c.isAfterLast()){
            amount=c.getInt(c.getColumnIndex(PROPERTY_COLUMN_AMOUNT));
            c.moveToNext();
        }
        c.close();
        db.close();

        return amount;
    }

    public void addCompany(Company company, int CID){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COMPANIES_COLUMN_NAME, company.getName());
        values.put(COMPANIES_COLUMN_TOTAL_VALUE, company.getTotalValue()*100);
        values.put(COMPANIES_COLUMN_CURRENT_VALUE, company.getCurrentValue());
        values.put(COMPANIES_COLUMN_PERCENTAGE_VALUE, company.getPercentageValue());
        values.put(COMPANIES_COLUMN_INVESTMENT, company.getInvestment());
        values.put(COMPANIES_COLUMN_OUTLOOK, company.getOutlook());
        values.put(COMPANIES_COLUMN_SECTOR, company.getSector());
        values.put(COMPANIES_COLUMN_MARKET_SHARE, company.getMarketShare());
        values.put(COMPANIES_COLUMN_REVENUE, company.getRevenue());
        values.put(COMPANIES_COLUMN_LAST_REVENUE, company.getLastRevenue());
        values.put(COMPANIES_COLUMN_FAME, company.getFame());
        values.put(COMPANIES_COLUMN_CID, CID);
        db.insert(COMPANIES_TABLE_NAME, null, values);
    }

    public void setOutlook(String name, double outlook){
        SQLiteDatabase db = this.getWritableDatabase();
        int IntOutlook = (int)Math.round(outlook*100);
        String where = OUTLOOK_COLUMN_NAME+"=?";
        String[] args = {name};
        ContentValues values = new ContentValues();
        values.put(OUTLOOK_COLUMN_OUTLOOK, IntOutlook);
        db.update(OUTLOOK_TABLE_NAME, values, where, args);
        db.close();
    }

    public double getOutlook(String name){
        double last = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select " + OUTLOOK_COLUMN_OUTLOOK + " from " + OUTLOOK_TABLE_NAME + " where " + OUTLOOK_COLUMN_NAME + "=\"" + name + "\";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last = c.getInt(c.getColumnIndex(OUTLOOK_COLUMN_OUTLOOK));
            c.moveToNext();
        }
        c.close();
        db.close();
        return last/100;
    }

    public int getCompPercValue(String name){
        int last = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select " + COMPANIES_COLUMN_PERCENTAGE_VALUE + " from " + COMPANIES_TABLE_NAME + " where " + COMPANIES_COLUMN_NAME + "=\"" + name + "\";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last = c.getInt(c.getColumnIndex(COMPANIES_COLUMN_PERCENTAGE_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();
        return last;
    }

    public int get10000CompOutlook(String name){
        int last;
        double temp = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select " + COMPANIES_COLUMN_OUTLOOK + " from " + COMPANIES_TABLE_NAME + " where " + COMPANIES_COLUMN_NAME + "=\"" + name + "\";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            temp = c.getDouble(c.getColumnIndex(COMPANIES_COLUMN_OUTLOOK));
            c.moveToNext();
        }
        c.close();
        db.close();
        last = (int)Math.round(temp*10000);
        return last;
    }

    public int getCompRevenue(String name){
        int last = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select " + COMPANIES_COLUMN_REVENUE + " from " + COMPANIES_TABLE_NAME + " where " + COMPANIES_COLUMN_NAME + "=\"" + name + "\";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last = c.getInt(c.getColumnIndex(COMPANIES_COLUMN_REVENUE));
            c.moveToNext();
        }
        c.close();
        db.close();
        return last;
    }

    public double getCompMarketShare(String name){
        double last = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select " + COMPANIES_COLUMN_MARKET_SHARE + " from " + COMPANIES_TABLE_NAME + " where " + COMPANIES_COLUMN_NAME + "=\"" + name + "\";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last = c.getInt(c.getColumnIndex(COMPANIES_COLUMN_MARKET_SHARE));
            c.moveToNext();
        }
        c.close();
        db.close();
        return last;
    }

    public void addShare  (Share share) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SHARES_COLUMN_NAME, share.getName());
        values.put(SHARES_COLUMN_SID, share.getId());
        values.put(SHARES_COLUMN_CURRENT_PRICE, share.getCurrentSharePrice());
        values.put(SHARES_COLUMN_TOTAL_SHARES, share.getTotalShares());
        values.put(SHARES_COLUMN_LAST_CLOSE, share.getPrevDayClose());
        values.put(SHARES_COLUMN_REMAINING_SHARES, Math.round(share.getTotalShares() / 2));
        db.insert(SHARES_TABLE_NAME, null, values);

        values = new ContentValues();
        values.put(PROPERTY_COLUMN_SHARE, share.getId());
        values.put(PROPERTY_COLUMN_AMOUNT, 0);
        db.insert(PROPERTY_TABLE_NAME, null, values);

        values = new ContentValues();
        values.put(SHORT_COLUMN_SID, share.getId());
        values.put(SHORT_COLUMN_AMOUNT, 0);
        values.put(SHORT_COLUMN_TOTAL_SETTLE_DAYS, -1);
        db.insert(SHORT_TABLE_NAME, null, values);
        db.close();
    }

    public void DayCloseShare(int sid, int price){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = SHARES_COLUMN_SID+"=?";
        String[] args = {Integer.toString(sid)};
        ContentValues values = new ContentValues();
        values.put(SHARES_COLUMN_LAST_CLOSE, price);
        db.update(SHARES_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setRemShares(int sid, int amount){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = SHARES_COLUMN_SID+"=?";
        String[] args = {Integer.toString(sid)};
        ContentValues values = new ContentValues();
        values.put(SHARES_COLUMN_REMAINING_SHARES, amount);
        db.update(SHARES_TABLE_NAME,values, where, args);
        db.close();
    }

    public void setCompRevenue(int sid, int amount){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COMPANIES_COLUMN_CID+"=?";
        String[] args = {Integer.toString(sid)};
        ContentValues values = new ContentValues();
        values.put(COMPANIES_COLUMN_REVENUE, amount);
        db.update(COMPANIES_TABLE_NAME, values, where, args);
        db.close();
    }

    public int getRemShares(int sid){
        int curr = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SHARES_TABLE_NAME + " where " + SHARES_COLUMN_SID + "=" + sid + ";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            curr = c.getInt(c.getColumnIndex(SHARES_COLUMN_REMAINING_SHARES));
            c.moveToNext();
        }
        c.close();
        db.close();
        return curr;
    }

/*
    public int numberOfShares(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, SHARES_TABLE_NAME);
        return numRows;
    }
*/

    public String getDBShareName(int sid){
        String name = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SHARES_TABLE_NAME + " where " + SHARES_COLUMN_SID + "=" + sid + ";", null);
        c.moveToFirst();
        if(c.getCount()==0)return name;
        while (!c.isAfterLast()){
            name=c.getString(c.getColumnIndex(SHARES_COLUMN_NAME));
            c.moveToNext();
        }
        c.close();
        db.close();
        return name;
    }

    public int getDBCurrPrice(int sid){
        int curr = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SHARES_TABLE_NAME + " where " + SHARES_COLUMN_SID + "=" + sid + ";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            curr = c.getInt(c.getColumnIndex(SHARES_COLUMN_CURRENT_PRICE));
            c.moveToNext();
        }
        c.close();
        db.close();
        return curr;
    }

    public int getDBLastClose(int sid){
        int last = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SHARES_TABLE_NAME + " where " + SHARES_COLUMN_SID + "=" + sid + ";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last = c.getInt(c.getColumnIndex(SHARES_COLUMN_LAST_CLOSE));
            c.moveToNext();
        }
        c.close();
        db.close();
        return last;
    }

    public int getCompTotalValue(String name){
        int last = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select "+COMPANIES_COLUMN_TOTAL_VALUE+" from " + COMPANIES_TABLE_NAME + " where " + COMPANIES_COLUMN_NAME + "=\"" + name + "\";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last = c.getInt(c.getColumnIndex(COMPANIES_COLUMN_TOTAL_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();
        last = Math.round(last/100);
        return last;
    }

    public int getCompFame(String name){
        int last = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select "+COMPANIES_COLUMN_FAME+" from " + COMPANIES_TABLE_NAME + " where " + COMPANIES_COLUMN_NAME + "=\"" + name + "\";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last = c.getInt(c.getColumnIndex(COMPANIES_COLUMN_FAME));
            c.moveToNext();
        }
        c.close();
        db.close();
        return last;
    }

    public void setDBCurrPrice(int sid, int newP) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = SHARES_COLUMN_SID+"=?";
        String[] args = {Integer.toString(sid)};
        ContentValues values = new ContentValues();
        values.put(SHARES_COLUMN_CURRENT_PRICE, newP);
        db.update(SHARES_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setPlayerMoney(long newCash){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"money"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE, newCash);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setCompPercValue(String name, int newV){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COMPANIES_COLUMN_NAME+"=?";
        String[] args = {name};
        ContentValues values = new ContentValues();
        values.put(COMPANIES_COLUMN_PERCENTAGE_VALUE, newV);
        db.update(COMPANIES_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setCompTotValue(String name, int newV){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COMPANIES_COLUMN_NAME+"=?";
        String[] args = {name};
        ContentValues values = new ContentValues();
        values.put(COMPANIES_COLUMN_TOTAL_VALUE, newV);
        db.update(COMPANIES_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setCompInvest(String name, int newV){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COMPANIES_COLUMN_NAME+"=?";
        String[] args = {name};
        ContentValues values = new ContentValues();
        values.put(COMPANIES_COLUMN_INVESTMENT,newV);
        db.update(COMPANIES_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setCompFame(String name, int newV){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COMPANIES_COLUMN_NAME+"=?";
        String[] args = {name};
        ContentValues values = new ContentValues();
        values.put(COMPANIES_COLUMN_FAME, newV);
        db.update(COMPANIES_TABLE_NAME, values, where, args);
        db.close();
    }

    public long getPlayerMoney(){
        long cash = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"money\" ;", null);

        c.moveToFirst();
        while (!c.isAfterLast()){
            cash = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();
        return cash;
    }

    public int getAssets(){
        int assets=0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"assets\" ;", null);
        c.moveToFirst();

        if(c.getCount()==0){
            return 0;
        }

        c.moveToFirst();
        while (!c.isAfterLast()){
            assets=c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();
        //the main app and the user never see the assets stored as thousands, they only get the full assets as int
        return (int)Math.floor( assets/1000 );
    }

    public double getPartAssets(){
        double assets=0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"assets\" ;", null);
        c.moveToFirst();

        if(c.getCount()==0){
            return 0;
        }

        c.moveToFirst();
        while (!c.isAfterLast()){
            assets=c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();
        //the main app and the user never see the assets stored as thousands, they only get the full assets as int
        return assets/1000;
    }

    public void incAssets(double amount){
        double newAssets = getPartAssets();
        newAssets+=amount;
        int newAmount = (int)Math.round(newAssets*1000);
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"assets"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE, newAmount);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setAssets(int newAssets){
        //Assets are stored as thousands, so 2 assets would be stored as 2000. If the app sends eg 4 assets to set (whitch would be 0.004), it is considered it wanted 4000 assets insted, or 4 full assets
        if(newAssets<10){
            newAssets=newAssets*1000;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"assets"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE, newAssets);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setFame(int newFame){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"fame"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE, newFame);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    public int getFame(){
        int fame = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"fame\" ;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            fame = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();
        return fame;

    }

    public void PrepGame(long time, int assets, Company.Sectors[] sectors, int sound){
        assets *= 1000;     //Because full assets are stored as thousands
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + DATA_TABLE_NAME + " WHERE 1;");
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_NAME, "money");
        values.put(DATA_COLUMN_ENTRY_VALUE, 1000000);
        db.insert(DATA_TABLE_NAME, null, values);
        values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_NAME, "level");
        values.put(DATA_COLUMN_ENTRY_VALUE, 1);
        db.insert(DATA_TABLE_NAME, null, values);
        values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_NAME, "PrevNetWorth");
        values.put(DATA_COLUMN_ENTRY_VALUE, 1000000);
        db.insert(DATA_TABLE_NAME, null, values);
        values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_NAME, "assets");
        values.put(DATA_COLUMN_ENTRY_VALUE, assets);
        db.insert(DATA_TABLE_NAME, null, values);
        values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_NAME, "fame");
        values.put(DATA_COLUMN_ENTRY_VALUE, 0);
        db.insert(DATA_TABLE_NAME, null, values);
        values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_NAME, "economysize1");
        values.put(DATA_COLUMN_ENTRY_VALUE, 0);
        db.insert(DATA_TABLE_NAME, null, values);
        values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_NAME, "economysize2");
        values.put(DATA_COLUMN_ENTRY_VALUE, 0);
        db.insert(DATA_TABLE_NAME, null, values);
        values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_NAME, "term");
        values.put(DATA_COLUMN_ENTRY_VALUE, 1);
        db.insert(DATA_TABLE_NAME, null, values);
        values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_NAME, "day");
        values.put(DATA_COLUMN_ENTRY_VALUE, 1);
        db.insert(DATA_TABLE_NAME, null, values);
        values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_NAME, "sound");
        values.put(DATA_COLUMN_ENTRY_VALUE, sound);
        db.insert(DATA_TABLE_NAME, null, values);
        values.clear();
        values.put(DATA_COLUMN_ENTRY_NAME, "nextInvite");
        values.put(DATA_COLUMN_ENTRY_VALUE, time);
        db.insert(DATA_TABLE_NAME, null, values);
        values.clear();
        values.put(DATA_COLUMN_ENTRY_NAME, "eventGen");
        values.put(DATA_COLUMN_ENTRY_VALUE, 0);
        db.insert(DATA_TABLE_NAME, null, values);

        values.clear();
        values.put(OUTLOOK_COLUMN_NAME, "economy");
        values.put(OUTLOOK_COLUMN_OUTLOOK, 0);
        db.insert(OUTLOOK_TABLE_NAME, null, values);

        for (Company.Sectors sector : sectors) {
            values = new ContentValues();
            values.put(OUTLOOK_COLUMN_NAME, sector.toString());
            values.put(OUTLOOK_COLUMN_OUTLOOK, 0);
            db.insert(OUTLOOK_TABLE_NAME, null, values);
        }

        db.close();

    }

    public int getPrevNetWorth(){
        int gen = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"PrevNetWorth\" ;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            gen = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();

        return gen;
    }

    public int getEventGen(){
        int gen = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"eventGen\" ;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            gen = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();

        return gen;
    }

    public void setPrevNetWorth(long prev){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"PrevNetWorth"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE, prev);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setEventGen(int gen){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"eventGen"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE, gen);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    public boolean PlaySound(){
        int play = 1;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"sound\" ;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            play = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();

        return play == 1;
    }

    public void setSound(boolean play){
        int enter;
        if(play){
            enter = 1;
        } else {
            enter = 0;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"sound"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE, enter);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    //DELETE SHARES, COMPANIES, SECTORS OUTLOOKS AND GAME DATA, EVERYTHING EXCEPT ASSETS - TO START A NEW GAME
    public void clearData(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + PROPERTY_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + COMPANIES_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + OUTLOOK_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + SHORT_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + SHARES_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + EVENTS_TABLE_NAME + " WHERE 1;");
    }

    public void bankrupt(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + COMPANIES_TABLE_NAME + " WHERE "+COMPANIES_COLUMN_NAME+" = \""+name+"\";");
        db.execSQL("DELETE FROM " + SHARES_TABLE_NAME + " WHERE "+SHARES_COLUMN_NAME+" = \""+name+"\";");
        db.close();
    }

    public int getShareCount(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SHARES_TABLE_NAME + " where 1;", null);
        c.moveToFirst();
        int a = c.getCount();
        c.close();
        return a;
    }

    public void UpdateSID(int primaryID, int oldSID, int NewSID) {
        if(oldSID==NewSID)return;

        SQLiteDatabase db = this.getWritableDatabase();

        String where = ALL_TABLES_COLUMN_ID + "=?";
        String[] args = new String[]{Integer.toString(primaryID)};
        ContentValues values = new ContentValues();
        values.put(SHARES_COLUMN_SID, NewSID);
        db.update(SHARES_TABLE_NAME, values, where, args);

        values.clear();
        values.put(COMPANIES_COLUMN_CID, NewSID);
        db.update(COMPANIES_TABLE_NAME, values, where, args);

        db.close();
    }


    public int getLevel() {
        int level = 1;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"level\" ;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            level = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();

        return level;
    }

    public long getNextInviteTime() {
        long time = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"nextInvite\" ;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            time = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();

        return time;
    }

    public double getEconomyOutlook() {
        long level = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"economy\" ;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            level = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();

        return (double)level/100;
    }


    public int getMaxSID(){
        int max = 0;
        int temp;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select "+ SHARES_COLUMN_SID + " from " + SHARES_TABLE_NAME + " where 1;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            temp = c.getInt(c.getColumnIndex(SHARES_COLUMN_SID));
            if(temp>max) max = temp;
            c.moveToNext();
        }
        c.close();
        db.close();

        return max;
    }


    public void setLevel(int newLevel){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"level"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE, newLevel);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setNextInviteTime(long MStime){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"nextInvite"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE, MStime);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    public int getTerm() {
        int term = 1;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"term\" ;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            term = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();

        return term;
    }

    public void setTerm(int newTerm){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"term"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE, newTerm);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    public int getDay() {
        int day = 1;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"day\" ;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            day = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();

        return day;
    }

    public void setDay(int newDay){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"day"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE, newDay);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setDBShareName(int sid, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = SHARES_COLUMN_SID+"=?";
        String[] args = {Integer.toString(sid)};
        ContentValues values = new ContentValues();
        values.put(SHARES_COLUMN_NAME,newName);
        db.update(SHARES_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setDBCompName(String name, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COMPANIES_COLUMN_NAME+"=?";
        String[] args = {name};
        ContentValues values = new ContentValues();
        values.put(COMPANIES_COLUMN_NAME, newName);
        db.update(COMPANIES_TABLE_NAME, values, where, args);
        db.close();
    }

    public int getTotalShares(int sid) {
        int last = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select "+SHARES_COLUMN_TOTAL_SHARES+" from " + SHARES_TABLE_NAME + " where " + SHARES_COLUMN_SID + "=" + sid + ";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last = c.getInt(c.getColumnIndex(SHARES_COLUMN_TOTAL_SHARES));
            c.moveToNext();
        }
        c.close();
        db.close();
        return last;
    }

    public int getInvestment(String name) {
        int last = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select "+COMPANIES_COLUMN_INVESTMENT+" from " + COMPANIES_TABLE_NAME + " where " + COMPANIES_COLUMN_NAME + "=\"" + name + "\";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last = c.getInt(c.getColumnIndex(COMPANIES_COLUMN_INVESTMENT));
            c.moveToNext();
        }
        c.close();
        db.close();
        return last;
    }

    public int getLastRevenue(String name){
        int last = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select "+COMPANIES_COLUMN_LAST_REVENUE+" from " + COMPANIES_TABLE_NAME + " where " + COMPANIES_COLUMN_NAME + "=\"" + name + "\";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last = c.getInt(c.getColumnIndex(COMPANIES_COLUMN_LAST_REVENUE));
            c.moveToNext();
        }
        c.close();
        db.close();
        return last;
    }

    public String getCompanySector(String name) {
        String sect="";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select "+COMPANIES_COLUMN_SECTOR+" from " + COMPANIES_TABLE_NAME + " where " + COMPANIES_COLUMN_NAME + "=\"" + name + "\";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            sect = c.getString(c.getColumnIndex(COMPANIES_COLUMN_SECTOR));
            c.moveToNext();
        }
        c.close();
        db.close();
        return sect;
    }

    public long getEconomySize1() {
        long size = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"economysize1\" ;", null);

        c.moveToFirst();
        while (!c.isAfterLast()){
            size = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();
        return size;
    }

    public long getEconomySize2() {
        long size = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"economysize2\" ;", null);

        c.moveToFirst();
        while (!c.isAfterLast()){
            size = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();
        return size;
    }

    public void setEconomySize1(long size) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"economysize1"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE, size);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setEconomySize2(long size) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = DATA_COLUMN_ENTRY_NAME+"=?";
        String[] args = {"economysize2"};
        ContentValues values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_VALUE,size);
        db.update(DATA_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setCompMarketShare(String name, double newMS) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COMPANIES_COLUMN_NAME+"=?";
        String[] args = {name};
        ContentValues values = new ContentValues();
        values.put(COMPANIES_COLUMN_MARKET_SHARE, newMS);
        db.update(COMPANIES_TABLE_NAME, values, where, args);
        db.close();
    }

    public void setCompLastRevenue(String name, int revenue) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COMPANIES_COLUMN_NAME+"=?";
        String[] args = {name};
        ContentValues values = new ContentValues();
        values.put(COMPANIES_COLUMN_LAST_REVENUE, revenue);
        db.update(COMPANIES_TABLE_NAME,values, where, args);
        db.close();
    }

    public void setCompOutlook(String name, double newO) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COMPANIES_COLUMN_NAME+"=?";
        String[] args = {name};
        ContentValues values = new ContentValues();
        values.put(COMPANIES_COLUMN_OUTLOOK, newO);
        db.update(COMPANIES_TABLE_NAME,values, where, args);
        db.close();
    }

    public boolean isScam(int sid){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SCAMS_TABLE_NAME + " where " + SCAMS_COLUMN_SID + "=" + sid + ";", null);
        boolean a = c.getCount()>0;
        c.close();
        return a;
    }

    public void addEvent(Event event, int totalDays) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EVENTS_COLUMN_TYPE, event.getType());
        values.put(EVENTS_COLUMN_MAGNITUDE, event.getMagnitude());
        values.put(EVENTS_COLUMN_END_DAY, totalDays);
        db.insert(EVENTS_TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<Event> retrieveEvents(int currentDay) {
        ArrayList<Event> Events = new ArrayList<>();

        int type, magnitude, remDays;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + EVENTS_TABLE_NAME + " where 1;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            type = c.getInt(c.getColumnIndex(EVENTS_COLUMN_TYPE));
            magnitude = c.getInt(c.getColumnIndex(EVENTS_COLUMN_MAGNITUDE));
            remDays = c.getInt(c.getColumnIndex(EVENTS_COLUMN_END_DAY))-currentDay;
            Events.add(new Event(type, magnitude, remDays));
            c.moveToNext();
        }
        c.close();
        db.close();
        return Events;
    }

    public void ClearCompleteEvents(int Totalday) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(EVENTS_TABLE_NAME, EVENTS_COLUMN_END_DAY + "=?", new String[]{Integer.toString(Totalday)});
        db.close();
    }

    public void removeScam(int cid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SCAMS_TABLE_NAME, SCAMS_COLUMN_SID + "=?", new String[]{Integer.toString(cid)});
        db.close();
    }

    public int getCompanyCID(String name) {
        int size = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + COMPANIES_TABLE_NAME + " where " + COMPANIES_COLUMN_NAME + " = \""+name+"\" ;", null);

        c.moveToFirst();
        while (!c.isAfterLast()){
            size = c.getInt(c.getColumnIndex(COMPANIES_COLUMN_CID));
            c.moveToNext();
        }
        c.close();
        db.close();
        return size;
    }

    public int getDBSharePrimaryID(String name) {
        int size = -1;
        if(name.equals(""))return size;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SHARES_TABLE_NAME + " where " + COMPANIES_COLUMN_NAME + " = \""+name+"\" ;", null);
        if(c.getCount()==0)return size;
        if(c.moveToFirst())size = c.getInt(c.getColumnIndex(ALL_TABLES_COLUMN_ID));
        c.close();
        db.close();
        return size;
    }
}

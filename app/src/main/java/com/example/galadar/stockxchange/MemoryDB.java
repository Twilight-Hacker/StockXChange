package com.example.galadar.stockxchange;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.PublicKey;

/**
 * This class is only for aetting and retrieving values from/to the Database. It will only do a minimal amount of Calculations where absolutely required.
 * Created by Galadar on 11/10/2015.
 */


public class MemoryDB extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "StockXChangeGame.db";

    public static final String COMPANIES_TABLE_NAME = "Companies";
    public static final String COMPANIES_COLUMN_NAME = "Name";
    public static final String COMPANIES_COLUMN_ID = "id";
    public static final String COMPANIES_COLUMN_TOTAL_VALUE = "TotalValue";
    public static final String COMPANIES_COLUMN_CURRENT_VALUE = "CurrValue";
    public static final String COMPANIES_COLUMN_PERCENTAGE_VALUE = "PercValue";
    public static final String COMPANIES_COLUMN_TOTAL_SHARES = "TotalShares";
    public static final String COMPANIES_COLUMN_INVESTMENT = "Investment";
    public static final String COMPANIES_COLUMN_OUTLOOK = "Outlook";
    public static final String COMPANIES_COLUMN_SECTOR = "Sector";
    public static final String COMPANIES_COLUMN_MARKET_SHARE = "MarketShare";
    public static final String COMPANIES_COLUMN_REVENUE = "Revenue";
    public static final String COMPANIES_COLUMN_FAME = "Fame";

    public static final String SHARES_TABLE_NAME = "shares";
    public static final String SHARES_COLUMN_ID = "_id";
    public static final String SHARES_COLUMN_NAME = "name";
    public static final String SHARES_COLUMN_SID = "sid";
    public static final String SHARES_COLUMN_CURRENT_PRICE = "currvalue";
    public static final String SHARES_COLUMN_LAST_CLOSE = "Lastllose";

    public static final String OUTLOOK_TABLE_NAME = "outlooks";
    public static final String OUTLOOK_COLUMN_ID = "_id";
    public static final String OUTLOOK_COLUMN_NAME = "Sector";
    public static final String OUTLOOK_COLUMN_OUTLOOK = "Value";

    public static final String DATA_TABLE_NAME = "GameData";
    public static final String DATA_COLUMN_ID = "_id";
    public static final String DATA_COLUMN_ENTRY_NAME = "Name";
    public static final String DATA_COLUMN_ENTRY_VALUE = "Value";

    public static final String PROPERTY_TABLE_NAME = "Owned";
    public static final String PROPERTY_TABLE_ID = "_id";
    public static final String PROPERTY_TABLE_SHARE = "Share";
    public static final String PROPERTY_TABLE_AMOUNT = "Amount";

    public MemoryDB(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + COMPANIES_TABLE_NAME + "(" +
                        COMPANIES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COMPANIES_COLUMN_NAME + " CHAR(4), " +
                        COMPANIES_COLUMN_TOTAL_VALUE + " INTEGER, " +
                        COMPANIES_COLUMN_CURRENT_VALUE + " INTEGER, " +
                        COMPANIES_COLUMN_PERCENTAGE_VALUE + " INTEGER, " +
                        COMPANIES_COLUMN_TOTAL_SHARES + " INTEGER, " +
                        COMPANIES_COLUMN_INVESTMENT + " INTEGER, " +
                        COMPANIES_COLUMN_OUTLOOK + " REAL, " +
                        COMPANIES_COLUMN_SECTOR + " TEXT, " +
                        COMPANIES_COLUMN_MARKET_SHARE + " REAL, " +
                        COMPANIES_COLUMN_REVENUE + " INTEGER, " +
                        COMPANIES_COLUMN_FAME + " INTEGER " +
                ");"
        );

        //The game data table will include Player money, assets, fame, as well as economy size and various other numeric values not belonging to companies, shares or outlooks.
        db.execSQL(
                "CREATE TABLE " + DATA_TABLE_NAME + "(" +
                        DATA_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DATA_COLUMN_ENTRY_NAME + " TEXT, " +
                        DATA_COLUMN_ENTRY_VALUE + " REAL " +
                        ");"
        );


        db.execSQL(
                "CREATE TABLE " + OUTLOOK_TABLE_NAME + "(" +
                        OUTLOOK_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        OUTLOOK_COLUMN_NAME + " TEXT, " +
                        OUTLOOK_COLUMN_OUTLOOK + " REAL " +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE " + SHARES_TABLE_NAME + "(" +
                        SHARES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SHARES_COLUMN_NAME + " CHAR(4), " +
                        SHARES_COLUMN_SID + " INTEGER, " +
                        SHARES_COLUMN_CURRENT_PRICE + " INTEGER, " +
                        SHARES_COLUMN_LAST_CLOSE + " INTEGER" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE " + PROPERTY_TABLE_NAME + "(" +
                        PROPERTY_TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PROPERTY_TABLE_SHARE + " CHAR(4), " +
                        PROPERTY_TABLE_AMOUNT + " INTEGER, " +
                        " FOREIGN KEY (" + PROPERTY_TABLE_SHARE + ") REFERENCES " + SHARES_TABLE_NAME + "(" + SHARES_COLUMN_SID + ")" +
                        ");"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void sellShare(int sid, int NewAmount, int newCash){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + PROPERTY_TABLE_NAME + " SET " + PROPERTY_TABLE_AMOUNT + "=" + NewAmount + " WHERE " + PROPERTY_TABLE_SHARE +" = " +sid+ ";" ;
        db.execSQL(query);
        setPlayerMoney(newCash);
        db.close();
    }

    public void BuyShare(int SID, int NewAmount, int newCash){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + PROPERTY_TABLE_NAME + " SET " + PROPERTY_TABLE_AMOUNT + "=" + NewAmount + " WHERE " + PROPERTY_TABLE_SHARE +" = " +SID+ ";" ;
        db.execSQL(query);
        setPlayerMoney(newCash);
        db.close();
    }

    public int getOwnedShare(int sid){
        int amount=0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + PROPERTY_TABLE_NAME + " where " + PROPERTY_TABLE_SHARE + "=" + sid + ";", null);

        if(c.getCount()==0){
            c.close();
            db.close();
            return 0;
        }

        c.moveToFirst();
        while (!c.isAfterLast()){
            amount=c.getInt(c.getColumnIndex(PROPERTY_TABLE_AMOUNT));
            c.moveToNext();
        }
        c.close();
        db.close();

        return amount;
    }

    public void addCompany(Company company){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COMPANIES_COLUMN_NAME, company.getName());
        values.put(COMPANIES_COLUMN_TOTAL_VALUE, company.getTotalValue());
        values.put(COMPANIES_COLUMN_CURRENT_VALUE, company.getCurrentValue());
        values.put(COMPANIES_COLUMN_PERCENTAGE_VALUE, company.getPercentageValue());
        values.put(COMPANIES_COLUMN_TOTAL_SHARES, company.getTotalShares());
        values.put(COMPANIES_COLUMN_INVESTMENT, company.getInvestment());
        values.put(COMPANIES_COLUMN_OUTLOOK, company.getOutlook());
        values.put(COMPANIES_COLUMN_SECTOR, company.getSector());
        values.put(COMPANIES_COLUMN_MARKET_SHARE, company.getMarketShare());
        values.put(COMPANIES_COLUMN_REVENUE, company.getRevenue());
        values.put(COMPANIES_COLUMN_FAME, company.getFame());
        db.insert(COMPANIES_TABLE_NAME, null, values);
    }

    public void addSector(String sector){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(OUTLOOK_COLUMN_NAME, sector);
        values.put(OUTLOOK_COLUMN_OUTLOOK, 0);
        db.insert(OUTLOOK_TABLE_NAME, null, values);
    }

    public void setOutlook(String name, double outlook){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + OUTLOOK_TABLE_NAME + " SET " + OUTLOOK_COLUMN_OUTLOOK + "=" + outlook + " WHERE " + OUTLOOK_COLUMN_NAME +" = \"" +name+ "\";" ;
        db.execSQL(query);
        db.close();
    }

    public void addShare  (Share share) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SHARES_COLUMN_NAME, share.getName());
        values.put(SHARES_COLUMN_SID, share.getId());
        values.put(SHARES_COLUMN_CURRENT_PRICE, share.getCurrentSharePrice());
        values.put(SHARES_COLUMN_LAST_CLOSE, share.getPrevDayClose());
        db.insert(SHARES_TABLE_NAME, null, values);

        values = new ContentValues();
        values.put(PROPERTY_TABLE_SHARE, share.getId());
        values.put(PROPERTY_TABLE_AMOUNT, 0);
        db.insert(PROPERTY_TABLE_NAME, null, values);
        db.close();
    }

    public void DayCloseShare(int sid, int price){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + SHARES_TABLE_NAME + " SET " + SHARES_COLUMN_LAST_CLOSE + "=" + price + " WHERE " + SHARES_COLUMN_SID +" = " +sid+";";
        db.execSQL(query);
        db.close();
    }

    public int numberOfShares(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, SHARES_TABLE_NAME);
        return numRows;
    }

    public String getDBShareName(int sid){
        String name = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SHARES_TABLE_NAME + " where " + SHARES_COLUMN_SID + "=" + sid + ";", null);
        c.moveToFirst();
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

    public void setDBCurrPrice(int sid, int newP) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + SHARES_TABLE_NAME + " SET " + SHARES_COLUMN_CURRENT_PRICE + "=" + newP + " WHERE " + SHARES_COLUMN_SID +" = " +sid+";";
        db.execSQL(query);
        db.close();
    }

    public void setPlayerMoney(int newCash){
        int curr=getPlayerMoney();
        curr+=newCash;
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + curr + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"money\" ;";
        db.execSQL(query);
        db.close();
    }

    public int getPlayerMoney(){
        int cash = 0;
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

        return assets;
    }

    public void setAssets(int newAssets){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + newAssets + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"assets\" ;";
        db.execSQL(query);
        db.close();
    }

    public void setFame(int newFame){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + newFame + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"fame\" ;";
        db.execSQL(query);
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

    public void PrepGamer(int assets){
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
        values.put(DATA_COLUMN_ENTRY_NAME, "assets");
        values.put(DATA_COLUMN_ENTRY_VALUE, assets);
        db.insert(DATA_TABLE_NAME, null, values);
        values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_NAME, "fame");
        values.put(DATA_COLUMN_ENTRY_VALUE, 0);
        db.insert(DATA_TABLE_NAME, null, values);
        values = new ContentValues();
        values.put(DATA_COLUMN_ENTRY_NAME, "economysize");
        values.put(DATA_COLUMN_ENTRY_VALUE, 0);
        db.insert(DATA_TABLE_NAME, null, values);
        db.close();
    }


    //DELETE SHARES, COMPANIES, SECTORS OUTLOOKS AND GAME DATA, EVERYTHING EXCEPT ASSETS - TO START A NEW GAME
    public void clearData(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + PROPERTY_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + COMPANIES_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + OUTLOOK_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + SHARES_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + SHARES_TABLE_NAME + " WHERE 1;");
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

    public void setLevel(int newLevel){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + newLevel + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"level\" ;";
        db.execSQL(query);
        db.close();
    }

    //heavy workload, call in own syncronised thread

    public int GetSharesValue(String name){
        int res = 0;
        int price=0;
        int shares=0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor ts = db.rawQuery("select * from " + COMPANIES_TABLE_NAME + " WHERE " + COMPANIES_COLUMN_NAME + " = \"" + name + "\";", null);
        Cursor pr = db.rawQuery("select * from " + SHARES_TABLE_NAME + " WHERE " + SHARES_COLUMN_NAME + " = \"" + name + "\";", null);
        ts.moveToFirst();
        pr.moveToFirst();
        while (!ts.isAfterLast()&&!pr.isAfterLast()){
            price = pr.getInt(pr.getColumnIndex(SHARES_COLUMN_CURRENT_PRICE));
            shares = ts.getInt(ts.getColumnIndex(COMPANIES_COLUMN_TOTAL_SHARES));
            res += price*shares;
            ts.moveToNext();
            pr.moveToNext();
        }
        return res;
    }

    public void setDBShareName(int sid, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + SHARES_TABLE_NAME + " SET " + SHARES_COLUMN_NAME + "=" + newName + " WHERE " + SHARES_COLUMN_SID +" = "+ sid +";";
        db.execSQL(query);
        db.close();
    }

    public void setDBCompName(String name, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + COMPANIES_TABLE_NAME + " SET " + COMPANIES_COLUMN_NAME + "=" + newName + " WHERE " + COMPANIES_COLUMN_NAME +" = \""+ name +"\";";
        db.execSQL(query);
        db.close();
    }
}

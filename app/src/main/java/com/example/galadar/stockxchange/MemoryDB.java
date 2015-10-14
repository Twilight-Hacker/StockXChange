package com.example.galadar.stockxchange;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Galadar on 11/10/2015.
 */
public class MemoryDB extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Stocks.db";

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

    public static final String SHARES_TABLE_NAME = "sharestock";
    public static final String SHARES_COLUMN_ID = "_id";
    public static final String SHARES_COLUMN_NAME = "name";
    public static final String SHARES_COLUMN_SID = "sid";
    public static final String SHARES_COLUMN_CURRENT_PRICE = "currvalue";
    public static final String SHARES_COLUMN_LAST_CLOSE = "Lastllose";

    public static final String DATA_TABLE_NAME = "Player";
    public static final String DATA_COLUMN_PLAYER_NAME = "Name";
    public static final String DATA_COLUMN_PLAYER_MONEY = "Money";
    public static final String DATA_COLUMN_PLAYER_ASSETS = "Assets";
    public static final String DATA_COLUMN_PLAYER_LEVEL = "Level";
    public static final String DATA_COLUMN_PLAYER_FAME = "Fame";
    public static final String DATA_COLUMN_GAME_ECONOMY = "EconomySize";

    public MemoryDB(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
        db.execSQL(
                "CREATE TABLE " + COMPANIES_TABLE_NAME + "(" +
                        COMPANIES_COLUMN_NAME + " CHAR(4) PRIMARY KEY, " +
                        COMPANIES_COLUMN_ID + " INTEGER, " +
                        COMPANIES_COLUMN_TOTAL_VALUE + " INTEGER, " +
                        COMPANIES_COLUMN_CURRENT_VALUE + " INTEGER, " +
                        COMPANIES_COLUMN_PERCENTAGE_VALUE + " INTEGER, " +
                        COMPANIES_COLUMN_TOTAL_SHARES + " INTEGER, " +
                        COMPANIES_COLUMN_INVESTMENT + " INTEGER, " +
                        COMPANIES_COLUMN_OUTLOOK + " REAL, " +
                        COMPANIES_COLUMN_SECTOR + " TEXT, " +
                        COMPANIES_COLUMN_MARKET_SHARE + " REAL, " +
                        COMPANIES_COLUMN_REVENUE + " INTEGER" +
                "):"
        );

        db.execSQL(
                "CREATE TABLE " + DATA_TABLE_NAME + "(" +
                        DATA_COLUMN_PLAYER_NAME + " TEXT, " +
                        DATA_COLUMN_PLAYER_MONEY + " INTEGER, " +
                        DATA_COLUMN_PLAYER_ASSETS + " INTEGER, " +
                        DATA_COLUMN_PLAYER_LEVEL + " INTEGER, " +
                        DATA_COLUMN_PLAYER_FAME + " INTEGER, " +
                        DATA_COLUMN_GAME_ECONOMY + " INTEGER" +
                        ");"
        );

        */

        db.execSQL(
                "CREATE TABLE " + SHARES_TABLE_NAME + "(" +
                        SHARES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SHARES_COLUMN_NAME + " CHAR(4), " +
                        SHARES_COLUMN_SID + " INTEGER, " +
                        SHARES_COLUMN_CURRENT_PRICE + " INTEGER, " +
                        SHARES_COLUMN_LAST_CLOSE + " INTEGER" +
                        ");"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void addShare  (Share share) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SHARES_COLUMN_NAME, share.getName());
        contentValues.put(SHARES_COLUMN_SID, share.getId());
        contentValues.put(SHARES_COLUMN_CURRENT_PRICE, share.getCurrentSharePrice());
        contentValues.put(SHARES_COLUMN_LAST_CLOSE, share.getPrevDayClose());
        db.insert(SHARES_TABLE_NAME, null, contentValues);
    }

    public void DayCloseShare(int id, int price){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + SHARES_TABLE_NAME + " SET " + SHARES_COLUMN_LAST_CLOSE + "=" + price + " WHERE " + SHARES_COLUMN_SID +" = " +id+";";
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
        Cursor c =  db.rawQuery("select * from " + SHARES_TABLE_NAME + " where " + SHARES_COLUMN_SID + "=" + sid + "", null);
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
        Cursor c =  db.rawQuery("select * from " + SHARES_TABLE_NAME + " where " + SHARES_COLUMN_SID + "=" + sid + "", null);
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
        Cursor c =  db.rawQuery("select * from " + SHARES_TABLE_NAME + " where " + SHARES_COLUMN_SID + "=" + sid + "", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last = c.getInt(c.getColumnIndex(SHARES_COLUMN_LAST_CLOSE));
            c.moveToNext();
        }
        c.close();
        db.close();
        return last;
    }

    public void setDBCurrPrice(int id, int newP) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + SHARES_TABLE_NAME + " SET " + SHARES_COLUMN_CURRENT_PRICE + "=" + newP + " WHERE " + SHARES_COLUMN_SID +" = " +id+";";
        db.execSQL(query);
        db.close();
    }
}

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

    private static MemoryDB DBHandler;

    public static final String DATABASE_NAME = "galad-StockXChange.db";
    public static final int DATABASE_VER = 8;

    public static final String COMPANIES_TABLE_NAME = "Companies";
    public static final String COMPANIES_COLUMN_NAME = "Name";
    public static final String COMPANIES_COLUMN_ID = "id";
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
    public static final String SHARES_COLUMN_ID = "_id";
    public static final String SHARES_COLUMN_NAME = "name";
    public static final String SHARES_COLUMN_SID = "sid";
    public static final String SHARES_COLUMN_CURRENT_PRICE = "currvalue";
    public static final String SHARES_COLUMN_LAST_CLOSE = "Lastllose";
    public static final String SHARES_COLUMN_TOTAL_SHARES = "TotalShares";
    public static final String SHARES_COLUMN_REMAINING_SHARES = "remainingShares";

    public static final String OUTLOOK_TABLE_NAME = "outlooks";
    public static final String OUTLOOK_COLUMN_ID = "_id";
    public static final String OUTLOOK_COLUMN_NAME = "Sector";
    public static final String OUTLOOK_COLUMN_OUTLOOK = "Value";

    public static final String DATA_TABLE_NAME = "GameData";
    public static final String DATA_COLUMN_ID = "_id";
    public static final String DATA_COLUMN_ENTRY_NAME = "Name";
    public static final String DATA_COLUMN_ENTRY_VALUE = "Value";

    public static final String PROPERTY_TABLE_NAME = "Owned";
    public static final String PROPERTY_COLUMN_ID = "_id";
    public static final String PROPERTY_COLUMN_SHARE = "Share";
    public static final String PROPERTY_COLUMN_AMOUNT = "Amount";

    public static final String MESSAGE_TABLE_NAME = "Messages";
    public static final String MESSAGE_COLUMN_ID = "_id";
    public static final String MESSAGE_COLUMN_TITLE = "title";
    public static final String MESSAGE_COLUMN_BODY = "body";
    public static final String MESSAGE_COLUMN_END_DAY = "endday";


    public static synchronized MemoryDB getInstance(Context context){
        if(DBHandler==null){
            DBHandler = new MemoryDB(context.getApplicationContext());
        }
        return DBHandler;
    }

    public MemoryDB(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + MESSAGE_TABLE_NAME + "(" +
                        MESSAGE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MESSAGE_COLUMN_TITLE + " TEXT, " +
                        MESSAGE_COLUMN_BODY + " TEXT, " +
                        MESSAGE_COLUMN_END_DAY + " INTEGER " +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE " + COMPANIES_TABLE_NAME + "(" +
                        COMPANIES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COMPANIES_COLUMN_NAME + " CHAR(4), " +
                        COMPANIES_COLUMN_TOTAL_VALUE + " INTEGER, " +
                        COMPANIES_COLUMN_CURRENT_VALUE + " INTEGER, " +
                        COMPANIES_COLUMN_PERCENTAGE_VALUE + " INTEGER, " +
                        COMPANIES_COLUMN_INVESTMENT + " INTEGER, " +
                        COMPANIES_COLUMN_OUTLOOK + " REAL, " +
                        COMPANIES_COLUMN_SECTOR + " TEXT, " +
                        COMPANIES_COLUMN_MARKET_SHARE + " REAL, " +
                        COMPANIES_COLUMN_REVENUE + " INTEGER, " +
                        COMPANIES_COLUMN_LAST_REVENUE + " INTEGER, " +
                        COMPANIES_COLUMN_FAME + " INTEGER, " +
                        COMPANIES_COLUMN_CID + "INTEGER " +
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
                        SHARES_COLUMN_LAST_CLOSE + " INTEGER, " +
                        SHARES_COLUMN_TOTAL_SHARES + " INTEGER, " +
                        SHARES_COLUMN_REMAINING_SHARES + " INTEGER" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE " + PROPERTY_TABLE_NAME + "(" +
                        PROPERTY_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PROPERTY_COLUMN_SHARE + " CHAR(4), " +
                        PROPERTY_COLUMN_AMOUNT + " INTEGER, " +
                        " FOREIGN KEY (" + PROPERTY_COLUMN_SHARE + ") REFERENCES " + SHARES_TABLE_NAME + "(" + SHARES_COLUMN_SID + ")" +
                        ");"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE " + SHARES_TABLE_NAME + " ADD COLUMN " + SHARES_COLUMN_REMAINING_SHARES + " INTEGER default 30000;");
    }

    public int getMessagesNumber(){
        int max = 0;
        int temp = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select "+ MESSAGE_COLUMN_ID + " from " + MESSAGE_TABLE_NAME + " where 1;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            temp = c.getInt(c.getColumnIndex(MESSAGE_COLUMN_ID));
            if(temp>max) max = temp;
            c.moveToNext();
        }
        c.close();
        db.close();

        return max;
    }

    public void storeDay(int day, int term){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + day + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"day\" ;";
        db.execSQL(query);
        query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + term + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"term\" ;";
        db.execSQL(query);
        db.close();
    }

    public int[][] getMessages(int currentDay){
        int No = getMessagesNumber();
        if(No==0) return null;


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + MESSAGE_TABLE_NAME + " where 1;", null);
        c.moveToFirst();
        int[][] Messages = new int[2][c.getCount()];
        int i = 0;
        while (!c.isAfterLast()){
            Messages[0][i] = c.getInt(c.getColumnIndex(MESSAGE_COLUMN_ID));
            Messages[1][i] = c.getInt(c.getColumnIndex(MESSAGE_COLUMN_END_DAY)) - currentDay;
            c.moveToNext();
            i++;
        }
        c.close();
        db.close();

        return Messages;
    }

    public String[][] getMessageDetails(){
        int No = getMessagesNumber();
        if(No==0) return null;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + MESSAGE_TABLE_NAME + " where 1;", null);
        c.moveToFirst();
        String[][] Messages = new String[2][c.getCount()];
        int i = 0;
        while (!c.isAfterLast()){
            Messages[0][i] = c.getString(c.getColumnIndex(MESSAGE_COLUMN_TITLE));
            Messages[1][i] = c.getString(c.getColumnIndex(MESSAGE_COLUMN_BODY));
            c.moveToNext();
            i++;
        }
        c.close();
        db.close();

        return Messages;
    }

    public int publishMessage(Message message, int CurrentDay){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MESSAGE_COLUMN_TITLE, message.getTitle());
        values.put(MESSAGE_COLUMN_BODY, message.getBody());
        values.put(MESSAGE_COLUMN_END_DAY, message.getDuration() + CurrentDay);
        db.insert(MESSAGE_TABLE_NAME, null, values);
        return getMessagesNumber();
    }

    public void clearDoneMessages(int CurrentDay){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + MESSAGE_TABLE_NAME + "WHERE " + MESSAGE_COLUMN_END_DAY +" = " +CurrentDay+ ";" ;
        db.execSQL(query);
        db.close();
    }

    public void removeCompany(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + COMPANIES_TABLE_NAME + "WHERE " + COMPANIES_COLUMN_NAME +" = \"" +name+ "\";" ;
        db.execSQL(query);
        db.close();
    }

    public void clearALL(SQLiteDatabase db){
        db.execSQL("DROP TABLE " + MESSAGE_TABLE_NAME + ";");
        db.execSQL("DROP TABLE " + COMPANIES_TABLE_NAME + ";");
        db.execSQL("DROP TABLE " + DATA_TABLE_NAME + ";");
        db.execSQL("DROP TABLE " + OUTLOOK_TABLE_NAME + ";");
        db.execSQL("DROP TABLE " + PROPERTY_TABLE_NAME + ";");
        db.execSQL("DROP TABLE " + SHARES_TABLE_NAME + ";");
    }

    public void SellShare(int sid, int NewAmount, long newCash){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + PROPERTY_TABLE_NAME + " SET " + PROPERTY_COLUMN_AMOUNT + "=" + NewAmount + " WHERE " + PROPERTY_COLUMN_SHARE +" = " +sid+ ";" ;
        db.execSQL(query);
        setPlayerMoney(newCash);
        db.close();
    }

    public void BuyShare(int SID, int NewAmount, long newCash){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + PROPERTY_TABLE_NAME + " SET " + PROPERTY_COLUMN_AMOUNT + "=" + NewAmount + " WHERE " + PROPERTY_COLUMN_SHARE +" = " +SID+ ";" ;
        db.execSQL(query);
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
        String query = "UPDATE " + OUTLOOK_TABLE_NAME + " SET " + OUTLOOK_COLUMN_OUTLOOK + "=" + outlook + " WHERE " + OUTLOOK_COLUMN_NAME +" = \"" +name+ "\";" ;
        db.execSQL(query);
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
        return last;
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
        int last = 0;
        double temp = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select "+COMPANIES_COLUMN_OUTLOOK+" from " + COMPANIES_TABLE_NAME + " where " + COMPANIES_COLUMN_NAME + "=\"" + name + "\";", null);
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
        values.put(SHARES_COLUMN_REMAINING_SHARES, Math.round(share.getTotalShares()/2));
        db.insert(SHARES_TABLE_NAME, null, values);

        values = new ContentValues();
        values.put(PROPERTY_COLUMN_SHARE, share.getId());
        values.put(PROPERTY_COLUMN_AMOUNT, 0);
        db.insert(PROPERTY_TABLE_NAME, null, values);
        db.close();
    }

    public void DayCloseShare(int sid, int price){
        SQLiteDatabase db2 = this.getWritableDatabase();
        String query = "UPDATE " + SHARES_TABLE_NAME + " SET " + SHARES_COLUMN_LAST_CLOSE + "=" + price + " WHERE " + SHARES_COLUMN_SID +" = " +sid+";";
        db2.execSQL(query);
        db2.close();
    }

    public void setRemShares(int sid, int amount){
        SQLiteDatabase db2 = this.getWritableDatabase();
        String query = "UPDATE " + SHARES_TABLE_NAME + " SET " + SHARES_COLUMN_REMAINING_SHARES + "=" + amount + " WHERE " + SHARES_COLUMN_SID +" = " +sid+";";
        db2.execSQL(query);
        db2.close();
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
        SQLiteDatabase db1 = this.getWritableDatabase();
        String query = "UPDATE " + SHARES_TABLE_NAME + " SET " + SHARES_COLUMN_CURRENT_PRICE + "=" + newP + " WHERE " + SHARES_COLUMN_SID +" = " +sid+";";
        db1.execSQL(query);
    }

    public void setPlayerMoney(long newCash){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + newCash + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"money\" ;";
        db.execSQL(query);
        db.close();
    }

    public void setCompPercValue(String name, int newV){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + COMPANIES_TABLE_NAME + " SET " + COMPANIES_COLUMN_PERCENTAGE_VALUE + "=" + newV + " WHERE " + COMPANIES_COLUMN_NAME +" = \""+ name +"\" ;";
        db.execSQL(query);
        db.close();
    }

    public void setCompTotValue(String name, int newV){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + COMPANIES_TABLE_NAME + " SET " + COMPANIES_COLUMN_TOTAL_VALUE + "=" + newV + " WHERE " + COMPANIES_COLUMN_NAME +" = \""+ name +"\" ;";
        db.execSQL(query);
        db.close();
    }

    public void setCompInvest(String name, int newV){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + COMPANIES_TABLE_NAME + " SET " + COMPANIES_COLUMN_INVESTMENT + "=" + newV + " WHERE " + COMPANIES_COLUMN_NAME +" = \""+ name +"\" ;";
        db.execSQL(query);
        db.close();
    }

    public void setCompFame(String name, int newV){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + COMPANIES_TABLE_NAME + " SET " + COMPANIES_COLUMN_FAME + "=" + newV + " WHERE " + COMPANIES_COLUMN_NAME +" = \""+ name +"\" ;";
        db.execSQL(query);
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

    public void addAssets(){
        int newAssets = getAssets();
        newAssets++;
        newAssets*=1000;
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + newAssets + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"assets\" ;";
        db.execSQL(query);
        db.close();
    }

    public void addAssets(int amount){
        int newAssets = getAssets();
        newAssets*=1000;
        if(amount<10) {amount*=1000;}
        newAssets+=amount;
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + newAssets + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"assets\" ;";
        db.execSQL(query);
        db.close();
    }

    //return to check success on calling method
    public boolean spendAsset(){
        int newAssets = getAssets();
        if(newAssets>0) {
            newAssets--;
            newAssets *= 1000;
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + newAssets + " WHERE " + DATA_COLUMN_ENTRY_NAME + " = \"assets\" ;";
            db.execSQL(query);
            db.close();
            return true;
        } else {
            return false;
        }
    }
    public void setAssets(int newAssets){
        //Assets are stored as thousands, so 2 assets would be stored as 2000. If the app sends eg 4 assets to set (whitch would be 0.004), it is considered it wanted 4000 assets insted, or 4 full assets
        if(newAssets<10){
            newAssets*=1000;
        }
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

    public void PrepGame(int assets, Company.Sectors[] sectors){
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
        values.put(DATA_COLUMN_ENTRY_VALUE, 1);
        db.insert(DATA_TABLE_NAME, null, values);

        values = new ContentValues();
        values.put(OUTLOOK_COLUMN_NAME, "economy");
        values.put(OUTLOOK_COLUMN_OUTLOOK, 0);
        db.insert(OUTLOOK_TABLE_NAME, null, values);

        for (int i = 0; i < sectors.length; i++) {
            values = new ContentValues();
            values.put(OUTLOOK_COLUMN_NAME, sectors[i].toString());
            values.put(OUTLOOK_COLUMN_OUTLOOK, 0);
            db.insert(OUTLOOK_TABLE_NAME, null, values);
        }

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

        if(play==1){
            return true;
        } else {
            return false;
        }
    }

    public void setSound(boolean play){
        int enter;
        if(play){
            enter = 1;
        } else {
            enter = 0;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + enter + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"sound\" ;";
        db.execSQL(query);
        db.close();

    }

    //DELETE SHARES, COMPANIES, SECTORS OUTLOOKS AND GAME DATA, EVERYTHING EXCEPT ASSETS - TO START A NEW GAME
    public void clearData(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + PROPERTY_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + COMPANIES_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + OUTLOOK_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + SHARES_TABLE_NAME + " WHERE 1;");
        db.execSQL("DELETE FROM " + MESSAGE_TABLE_NAME + " WHERE 1;");
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

    public int getMaxSID(){
        int max = 0;
        int temp = 0;

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
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + newLevel + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"level\" ;";
        db.execSQL(query);
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
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + newTerm + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"term\" ;";
        db.execSQL(query);
        db.close();
    }

    public int getDay() {
        int day = 1;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"level\" ;", null);
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
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + newDay + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"level\" ;";
        db.execSQL(query);
        db.close();
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

    public int getTotalShares(String name) {
        int last = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select "+SHARES_COLUMN_TOTAL_SHARES+" from " + SHARES_TABLE_NAME + " where " + SHARES_COLUMN_NAME + "=\"" + name + "\";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last = c.getInt(c.getColumnIndex(SHARES_COLUMN_TOTAL_SHARES));
            c.moveToNext();
        }
        c.close();
        db.close();
        return last;
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

    public int getSumShares() {

        int last = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select "+SHARES_COLUMN_TOTAL_SHARES+" from " + SHARES_TABLE_NAME + " where 1;", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            last += c.getInt(c.getColumnIndex(SHARES_COLUMN_TOTAL_SHARES));
            c.moveToNext();
        }
        c.close();
        db.close();
        return last;

    }

    public int getSharesTotalValue(int sid){
        int prod = 0;
        int amount = 0;
        int price = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + SHARES_TABLE_NAME + " where " + SHARES_COLUMN_SID + "=" + sid + ";", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            amount = c.getInt(c.getColumnIndex(SHARES_COLUMN_TOTAL_SHARES));
            price = c.getInt(c.getColumnIndex(SHARES_COLUMN_CURRENT_PRICE));
            prod = amount*price;
            c.moveToNext();
        }
        c.close();
        db.close();
        return prod;

    }

    //TODO Why is this negative
    public long getEconomySize() {

        int size = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("select * from " + DATA_TABLE_NAME + " where " + DATA_COLUMN_ENTRY_NAME + " = \"economysize\" ;", null);

        c.moveToFirst();
        while (!c.isAfterLast()){
            size = c.getInt(c.getColumnIndex(DATA_COLUMN_ENTRY_VALUE));
            c.moveToNext();
        }
        c.close();
        db.close();
        return size;

    }

    public void setEconomySize(long size) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_ENTRY_VALUE + "=" + size + " WHERE " + DATA_COLUMN_ENTRY_NAME +" = \"economysize\" ;";
        db.execSQL(query);
        db.close();
    }

    public void setCompMarketShare(String name, double newMS) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + COMPANIES_TABLE_NAME + " SET " + COMPANIES_COLUMN_MARKET_SHARE + "=" + newMS + " WHERE " + COMPANIES_COLUMN_NAME +" = \"" +name+ "\" ;";
        db.execSQL(query);
        db.close();
    }

    public void setCompLastRevenue(String name, int revenue) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + COMPANIES_TABLE_NAME + " SET " + COMPANIES_COLUMN_LAST_REVENUE + "=" + revenue + " WHERE " + COMPANIES_COLUMN_NAME +" = \"" +name+ "\" ;";
        db.execSQL(query);
        db.close();
    }

    public void setCompOutlook(String name, double newO) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + COMPANIES_TABLE_NAME + " SET " + COMPANIES_COLUMN_OUTLOOK + "=" + newO + " WHERE " + COMPANIES_COLUMN_NAME +" = \"" +name+ "\" ;";
        db.execSQL(query);
        db.close();
    }
}

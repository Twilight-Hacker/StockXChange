package com.example.galadar.stockxchange;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

/*
Specific View IDs
SID: Share id: share identifier im Memory and DB, 0 to NumberOfCompanies-1
Share Name View: 200000+SID
Share Price View: 100000+SID
Share Buy Button: 300000+SID
Share Sell Button: 400000+SID
 */

public class MainActivity extends AppCompatActivity {

    public static Finance f;
    public static final int[] nextLevel = {0, 125000, 620000, 1250000, 5500000, 25000000, 0}; //To be printed as is, not /100.
    static Gamer p;
    static MemoryDB DBHandler;
    static Daytime time;
    static TextView topBarPlayer;
    static TextView topBarDaytime;
    static Thread upd;
    static int[][] Messages;
    static boolean playSound;
    static boolean dayOpen = false;
    static boolean gaming = false;


    public static Finance getFinance(){
        return f;
    }

    public static Daytime getClock(){
        return time;
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateTopBar(topBarPlayer, topBarDaytime);
    }


    public int getNextLevelPreq(){
        return nextLevel[p.getLevel()];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBHandler = MemoryDB.getInstance(getApplicationContext());

        if(DBHandler.getMaxSID()<1) {
            f = new Finance(DBHandler, 5);
            p = new Gamer(DBHandler);
            time = new Daytime(LocalBroadcastManager.getInstance(this.getApplicationContext()));
            DBHandler.PrepGame(0, Company.Sectors.values());
        } else {
            f = new Finance(DBHandler);
            p = new Gamer(DBHandler.getPlayerMoney(), DBHandler.getLevel(), DBHandler.getAssets(), DBHandler.getFame());
            time = new Daytime(LocalBroadcastManager.getInstance(this.getApplicationContext()), DBHandler.getTerm(), DBHandler.getDay());
            Toast.makeText(this,"Game Loaded", Toast.LENGTH_SHORT).show();
        }

        playSound = DBHandler.PlaySound();

        //Retrieve / Generate invitation, event and other messages
        Messages = new int[100][2];
        for(int i=0; i<Messages.length;i++){
            Messages[i][0]=-1;
            Messages[i][1]=-1;
        }

        topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);

        UpdateTopBar(topBarPlayer, topBarDaytime);

        UpdateCentralUI();

        DBHandler.setEconomySize(f.calcEconomySize());

        //New thread update time
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                boolean running = true;
                try {
                    while (running) {
                        synchronized (this) {
                            //TODO change wait from 5000ms to 10000ms (10 seconds)
                            Thread.sleep(5000);
                            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent("TimeForwarded"));
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        upd = new Thread(r);


        LocalBroadcastManager.getInstance(this).registerReceiver(DayStartedMessageRec, new IntentFilter("DayStarted"));
        LocalBroadcastManager.getInstance(this).registerReceiver(SharesTransactionedRec, new IntentFilter("SharesTransaction"));
        LocalBroadcastManager.getInstance(this).registerReceiver(SpecificElementUpdate, new IntentFilter("SpecificPriceChange"));
        LocalBroadcastManager.getInstance(this).registerReceiver(DayEndedMessageRec, new IntentFilter("DayEnded"));
        LocalBroadcastManager.getInstance(this).registerReceiver(TermEndedMessageRec, new IntentFilter("TermEnded"));
        LocalBroadcastManager.getInstance(this).registerReceiver(SoundAlteredRec, new IntentFilter("SoundAltered"));
        LocalBroadcastManager.getInstance(this).registerReceiver(LeveledUp, new IntentFilter("LevelUp"));
        LocalBroadcastManager.getInstance(this).registerReceiver(AdvanceTime, new IntentFilter("TimeForwarded"));

        gaming = true;
        upd.start();
    }

    private void UpdateCentralUI() {
        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.layout);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view;
        int intprice;
        double dprice;
        parentLayout.removeAllViews();
        for(int i=0;i<f.getNumComp();i++){
            view = layoutInflater.inflate(R.layout.main_share, parentLayout, false);
            LinearLayout shareData = (LinearLayout)view.findViewById(R.id.shareData);

            TextView shareInfo = (TextView)shareData.findViewById(R.id.shareInfo);
            shareInfo.setId(200000 + i);
            shareInfo.setText(f.getName(i));
            intprice = f.getShareCurrPrince(i);
            dprice = (double)intprice/100;
            String zerodigit = "";
            if(intprice%10==0){zerodigit="0";}
            TextView sharePrices = (TextView)shareData.findViewById(R.id.sharePrice);
            sharePrices.setId(100000 + i);
            sharePrices.setText(Double.toString(dprice) + zerodigit);

            Button Buy = (Button)shareData.findViewById(R.id.BuyButton);
            Buy.setId(300000 + i);
            Buy.setText("Buy");
            Buy.setEnabled(dayOpen);
            Button Sell = (Button)shareData.findViewById(R.id.SellButton);
            Sell.setId(400000 + i);
            Sell.setText("Sell");
            if((f.getSharesOwned(i)>0) & dayOpen) {
                Sell.setEnabled(true);
                Sell.setTextColor(0xffffffff);
            } else {
                Sell.setEnabled(false);
                Sell.setTextColor(0xff000000);
            }

            parentLayout.addView(shareData);
        }
    }

    public String ClocktoString(){
        return time.DTtoString();
    }

    private BroadcastReceiver AdvanceTime = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if(gaming) {
                time.increment(10);
                UpdateTimeView(time);
                callforTransactions();
            }
        }
    };

    private BroadcastReceiver DayStartedMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dayOpen = true;
            UpdateCommandsUI();
            Toast.makeText(MainActivity.this, "Day Started", Toast.LENGTH_SHORT).show();
        }
    };

    private BroadcastReceiver LeveledUp = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            p.setLevel(p.getLevel()+1);
            DBHandler.setLevel(p.getLevel());
            long priceMoney=0;
            int prizeAssets=p.getAssets();
            //TODO add tutorial calls here
            switch (p.getLevel()){
                case 2: {
                    priceMoney=-25000;
                    prizeAssets+=2;
                    break;
                }
                case 3: {
                    priceMoney=-120000;
                    prizeAssets+=2;
                    break;
                }
                case 4: {
                    priceMoney=-250000;
                    prizeAssets+=3;
                    break;
                }
                case 5: {
                    priceMoney=-500000;
                    prizeAssets+=4;
                    break;
                }
                case 6: {
                    priceMoney=-1000000;
                    prizeAssets+=6;
                    break;
                }
            }

            p.alterMoney(priceMoney*100);
            p.setAssets(prizeAssets);
        }
    };

    private void callforTransactions() {
        if(!dayOpen) return;
        int temp;
        for(int i=0;i<f.getNumComp();i++) {
            temp = getSharesAmount(f.getCompOutlook(i), f.getSectorOutlook(i), f.getRemShares(i), f.getTotalShares(i));
            if (temp != 0) {
                Intent intent = new Intent("SharesTransaction");
                Bundle data = new Bundle();
                data.putInt("SID", i);
                data.putInt("amount", temp);
                data.putInt("atPrice", f.getShareCurrPrince(i));
                data.putBoolean("ByPlayer", false);
                intent.putExtras(data);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
            }
        }
    }

    public int getSharesAmount(double C, double S, int remaining, int total){
        Random random = new Random();
        double perOwned = (double)remaining/total;

        double determinant = random.nextDouble()*2-1.5 + perOwned + (2*C+S)/10;
        int temp = random.nextInt(3);

        if(temp!=2) {
            //transact shares only 1/3 of the time, so as not to overwhelm user with changes
            return 0;
        } else {
            double am = Math.abs(random.nextGaussian())*(determinant*remaining);
            if(Math.abs(am)<=20) am = random.nextInt(50)+20;
            if(Math.abs(am)>0.35*remaining) am=Math.signum(am)*0.35*remaining;
            int amount = (int)Math.round(am);
            //if the system tries to buy more than those available for sale, just buy thise available
            if(amount+remaining>total) return remaining;
            //total-remaining is the amount of shares owned
            //remaining-total (as negative) sets a down limit of the amount of shares that are owned and can be sold (sells are denoted as negative amounts)
            if(amount<remaining-total) return remaining-total;
            return amount;
        }
    }

    private BroadcastReceiver SharesTransactionedRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            int SID = data.getInt("SID");
            int amount = data.getInt("amount");
            int oldPrice = data.getInt("atPrice");
            boolean byPlayer = data.getBoolean("ByPlayer");

            int remaining = f.getRemShares(SID);
            int total = f.getTotalShares(SID);

            if(byPlayer){
                f.TransactShares(SID, amount);
                p.alterMoney(amount*oldPrice);
                if(amount>0){
                    DBHandler.BuyShare(SID, amount, p.getMoney());
                } else {
                    DBHandler.SellShare(SID, amount, p.getMoney());
                }
            }

            f.alterRemShares(SID, amount);
            DBHandler.setRemShares(SID, f.getRemShares(SID));

            double value = (double)f.getCompTotalValue(SID)/100;
            int max = (int)Math.round( value/total );
            int newPrice = oldPrice + (int) Math.round( (1 - (double)remaining/total )*max + Math.random()*50+10 );

            if(newPrice<0) {
                newPrice=Math.abs(newPrice);
                Toast.makeText(MainActivity.this, "Price Error " + (double)remaining/total +" "+max, Toast.LENGTH_SHORT).show();
            }

            f.setShareCurrPrice(SID, newPrice);
            DBHandler.setDBCurrPrice(SID, newPrice);

            Intent intent1 = new Intent("SpecificPriceChange");
            Bundle data1 = new Bundle();
            data1.putInt("SID", SID);
            data1.putInt("newPrice", newPrice);
            data1.putInt("oldPrice", oldPrice);
            data1.putBoolean("PlayerOwner", f.getSharesOwned(SID)>0);
            intent1.putExtras(data1);
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent1);
        }
    };

    private BroadcastReceiver SpecificElementUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            int SID = data.getInt("SID");
            int price = data.getInt("newPrice");
            int oldPrice = data.getInt("oldPrice");
            boolean sellactive = data.getBoolean("PlayerOwner");

            String zerodigit = "";
            if(price%10==0){zerodigit="0";}
            TextView sharePrices = (TextView)findViewById(100000 + SID);
            if(price>oldPrice) {
                sharePrices.setTextColor(0xff00ff00); //Color green for price going up
            } else if (price<oldPrice) {
                sharePrices.setTextColor(0xffff0000); //Color red for price going down
            } else {
                sharePrices.setTextColor(0xffffffff); //Color white for price unchanged
            }
            sharePrices.setText(Double.toString((double)price/100) + zerodigit);

            Button Sell = (Button)findViewById(400000 + SID);
            Sell.setEnabled(sellactive & dayOpen);
            if(sellactive){
                Sell.setTextColor(0xffffffff);
            } else {
                Sell.setTextColor(0xff000000);
            }
        }
    };

    private BroadcastReceiver SoundAlteredRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playSound = !playSound;
            DBHandler.setSound(playSound);
        }
    };

    public void UpdateTimeView(Daytime time){
        TextView DT = (TextView)findViewById(R.id.DaytimeInfo);
        DT.setText(time.DTtoString());
    }

    private BroadcastReceiver DayEndedMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dayOpen = false;
            Toast.makeText(MainActivity.this, "Day Ended", Toast.LENGTH_SHORT).show();
            f.DayCloseShares();

            if((p.getMoney()+f.NetWorth())<-10000000) {
                gaming = false;
                if (p.getAssets() > 0) {
                    p.setAssets(p.getAssets() - 1);
                    DBHandler.setAssets(p.getAssets());
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle("GAME OVER");
                    String q = "You have been arrested after amassing a tremendous amount of debt. \n\n You have lost the game.";
                    builder.setMessage(q);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            DBHandler.clearData();
                            DBHandler.PrepGame(0, Company.Sectors.values());
                            f = new Finance(DBHandler, 4);
                            UpdateCentralUI();
                            p = new Gamer(DBHandler);
                            time = new Daytime(LocalBroadcastManager.getInstance(MainActivity.this.getApplicationContext()));
                            DBHandler.setEconomySize(f.calcEconomySize());
                            dialog.dismiss();
                        }

                    });

                    AlertDialog d = builder.create();
                    d.show();
                }
            } else {
                for (int i = 0; i < f.getNumComp(); i++) {
                    DBHandler.DayCloseShare(i, f.getLastClose(i));
                }
                //Updating DB to show the NEXT day (and possibly term) than the one Just ENDED
                if (time.getDay() == 60) {
                    time.nextTerm();
                    DBHandler.storeDay(1, time.getTerm());
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent("TermEnded"));
                } else {
                    DBHandler.setDay(time.getDay() + 1);
                }
            }
            UpdateCommandsUI();
            gaming = true;
        }
    };

    //RUN ONLY ON UI THREAD
    public void UpdateCommandsUI(){
        Button B;
        try{
            for (int i = 0; i < f.getNumComp(); i++) {
                B = (Button) findViewById(300000 + i);
                B.setEnabled(dayOpen);
                B = (Button) findViewById(400000 + i);
                B.setEnabled(dayOpen & (f.getSharesOwned(i) > 0));
                if (B.isEnabled()) {
                    B.setTextColor(0xffffffff);
                } else {
                    B.setTextColor(0xff000000);
                }
            }
        } catch (Exception e){

        }
    }

    public void MessagesLoad(View v){
        Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
        Bundle data = new Bundle();
        data.putBoolean("playsound", playSound);
        String[][] Messages = DBHandler.getMessageDetails();
        if(Messages!=null) {
            data.putInt("Number", Messages[0].length);
            data.putStringArray("Titles", Messages[0]);
            data.putStringArray("Bodies", Messages[1]);
        } else {
            data.putInt("Number", 0);
            data.putStringArray("Titles", null);
            data.putStringArray("Bodies", null);
        }
        intent.putExtras(data);
        startActivity(intent);
    }

    public void EconomyInfoLaunch(View v){
        Intent intent = new Intent(MainActivity.this, EconomyInfoActivity.class);
        Bundle data = new Bundle();
        data.putBoolean("Sound", playSound);
        data.putLong("Economy_size", f.getEconomySize());
        data.putInt("TotalCompanies", f.getNumComp());
        data.putInt("SumOfShares", f.getSumShares());
        intent.putExtras(data);
        startActivity(intent);
    }

    private BroadcastReceiver TermEndedMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            gaming = false;
            Toast.makeText(context.getApplicationContext(), "Term Ended", Toast.LENGTH_LONG).show();

            //new companies added directly to DB (temp table for QuickGame)

            int revenue, TotVal, newInv, oldPerc, newPerc, newFame;
            double divident;
            Random r = new Random();
            int newCompCounter=0;
            for (int i = 0; i < f.getNumComp(); i++) {
                revenue = f.getCompRevenue(i);                                              //TODO Different getRevenue for scams, this is used in else of if( isScam() )
                TotVal = f.getCompTotalValue(i);

                revenue += (int)Math.round( (f.getEconomySize()*DBHandler.getCompMarketShare(f.getName(i))) / (Math.random()*135+45) );
                double investmentRes = (Math.random()*1.5+0.5)*f.getInvestment(i);          //calculating investment gains
                revenue+=(int)Math.round(investmentRes);                                    //adding investment gains to revenue
                revenue -= (int)Math.round( (Math.random()*0.05+0.05)*revenue );            //Remove upkeep costs from revenue
                revenue -= Math.max( revenue*0.2 , 0);                                      //Remove taxes from remaining revenue
                newInv = (int)Math.round(revenue*r.nextDouble()*0.2);                       //Calculating new investment based on remaining revenue
                DBHandler.setCompInvest(f.getName(i), newInv);                              //store to DB
                oldPerc = DBHandler.getCompPercValue(f.getName(i));                         //Getting Percentage Change in Value
                newPerc = oldPerc + (int)Math.round((double)revenue/TotVal)*100;            //Calculating new Percentage Change in Value
                DBHandler.setCompPercValue(f.getName(i), newPerc);                          //Store to DB
                if(newPerc>100 & revenue>f.getShareCurrPrince(i)*f.getTotalShares(i)){      //Decide and Calculate dividents
                    divident = newPerc*(double)f.getShareCurrPrince(i)/100;
                    p.alterMoney(Math.round(divident*f.getSharesOwned(i)));
                } else {
                    divident=0;
                }
                newFame=0;
                if(divident!=0)newFame+=100;
                newFame += DBHandler.getCompFame(f.getName(i))*r.nextInt(newPerc);   //Calculate new Fame
                DBHandler.setCompFame(f.getName(i), newFame);
                DBHandler.setCompTotValue(f.getName(i), f.getCompTotalValue(i)+revenue);
                DBHandler.setCompLastRevenue(f.getName(i), revenue);
            }

            p.setMoney(p.getMoney()-taxes());

            if(p.getLevel()>5&((p.getMoney()+f.NetWorth())/100)>1000000000) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Congratulations");
                String q = "You are now a billionaire. \n\nYou can continue playing or start a new game. \n\nYou have completed this game in " + time.totalDays() + " days.";
                builder.setMessage(q);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog d = builder.create();
                d.show();
            }

            DBHandler.setPlayerMoney(p.getMoney()); //Update player money (dividents added)

            for (int i = 1; i <= Company.Sectors.values().length; i++) {
                while(f.getSectorOutlook(i)>0.6 & (f.getSectorOutlook(i)+f.getSectorOutlook(0))>0){
                    String name;
                    do {
                        name = randomName();
                    } while (f.addCompanyName(name));
                    Company c = new Company(name, Company.Sectors.values()[i]);
                    newCompCounter++;
                    DBHandler.addCompany(c, f.getNumComp() + newCompCounter);
                    Share s = new Share(name, f.getNumComp() + newCompCounter, c.shareStart(), c.getTotalShares());
                    DBHandler.addShare(s);
                    f.outlooks[0][0] += (double)c.getTotalValue()/f.getEconomySize();
                    f.outlooks[i][0] -= (double)c.getTotalValue()/f.getEconomySize();
                }
                DBHandler.setOutlook(Company.Sectors.values()[i-1].toString(), f.getBaseSectorOutlook(i));
                DBHandler.setOutlook("economy", f.getBaseSectorOutlook(0));
            }

            for (int i = 0; i < f.getNumComp(); i++) {
                if(DBHandler.getCompPercValue(f.getName(i))<-80){
                    DBHandler.removeCompany(f.getName(i));
                    f.outlooks[0][0] -= (double)f.getCompTotalValue(i)/f.getEconomySize();
                    f.outlooks[i][0] += (double)f.getCompTotalValue(i)/f.getEconomySize();
                }
                DBHandler.setOutlook(Company.Sectors.values()[i-1].toString(), f.outlooks[i][0]);
                DBHandler.setOutlook("economy", f.outlooks[0][0]);
            }

            long oldEcon = f.getEconomySize();
            f = new Finance(DBHandler);
            //AT this point, the RAM finance tables are updated. All major alterations have been complete (adding and removing companies.

            f.resetEconomySize();
            DBHandler.setEconomySize(f.getEconomySize());
            long newEcon = f.getEconomySize();

            long[] SecEconSizes = new long[Company.Sectors.values().length];
            for (int i = 0; i < SecEconSizes.length; i++) {
                SecEconSizes[i] = f.getSecEconSize(i);
            }

            double curr, newMS;
            int P, F;
            String name;
            for (int i = 0; i < DBHandler.getMaxSID(); i++) {
                name = DBHandler.getDBShareName(i);
                curr = DBHandler.getCompMarketShare(name);
                P = DBHandler.getCompPercValue(name);
                F = DBHandler.getCompFame(name);

                newMS = curr*((double)P/100)*((double)F/500) + ((double)f.getCompTotalValue(i)/f.Companies[i][1]);
                DBHandler.setCompMarketShare(name, newMS);
            }
            //The market share is always positive but does not sum up to 100%, because people may use more than one company in each sector
            //plus verifications and alterations are too difficult at this point, and require direct manipulation on the DB that cannot be implemented safely on a Thread
            //the Market share is only used for Company outlook and Term revenue;

            double newO = r.nextDouble()*0.2*(double)(newEcon-oldEcon)/oldEcon;
            if(Math.abs(newO)>1) newO = Math.signum(newO);
            f.setSectorOutlook(0, newO);
            DBHandler.setOutlook("economy", newO);

            for (int i = 0; i < Company.Sectors.values().length; i++) {
                newO = f.getBaseSectorOutlook(i)*(double)SecEconSizes[i]/newEcon;
                if(Math.abs(newO)>2) newO = Math.signum(newO)*2;
                DBHandler.setOutlook(Company.Sectors.values()[i].toString(), newO);
            }

            double MS, SO, newOut;
            int nP, nrevenue;
            for (int i = 0; i < f.getNumComp(); i++) {
                //Market share*Sector outlook + percentage change/100 + Revenue/Economy Size
                MS = DBHandler.getCompMarketShare(f.getName(i));
                SO = f.getSectorOutlook(f.getCompSectorInt(i)+1);
                nP = DBHandler.getCompPercValue(f.getName(i));
                nrevenue = f.getLastRevenue(i);

                newOut = MS*SO+ ((double)nP/100) + (double)nrevenue/newEcon;
                f.setCompOutlook(i, newOut);
                DBHandler.setCompOutlook(f.getName(i), newOut);
            }

            //TODO Add Scams from existing companies (no New Companies added at this stage)

            //term update up to here
            //call dialog to report economy size change, number of companies opened and No of companies closed

            gaming = true;
        }
    };

    private long taxes() {
        long tax1 = Math.round((p.getLevel()*0.05)*p.getMoney());
        long tax2 = Math.max(0, (p.getLevel()-2)*1250000);
        int[] Upkeep = {50000, 125000, 750000, 1250000, 2500000, 5000000};

        return tax1+tax2+Upkeep[p.getLevel()];
    }


    //Button And Menu Selections


    @Override
    protected void onDestroy() {
        upd.interrupt();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(DayStartedMessageRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(DayEndedMessageRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(TermEndedMessageRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(SoundAlteredRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(SharesTransactionedRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(SpecificElementUpdate);
        super.onDestroy();
    }

    public void PlayerButtonClick(View v) {
        Intent intent = new Intent(MainActivity.this, PlayerInfoActivity.class);
        Bundle data = new Bundle();
        data.putParcelable("DT", time);
        data.putBoolean("playSound", playSound);
        data.putLong("Pmoney", p.getMoney());
        data.putInt("level", p.getLevel());
        data.putInt("assets", p.getAssets());
        data.putInt("next", getNextLevelPreq());
        data.putLong("NetWorth", f.NetWorth());
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickInfo(View v){

        Intent intent = new Intent(this, InfoActivity.class );
        Bundle data = new Bundle();
        data.putParcelable("DT", time);
        data.putLong("Pmoney", p.getMoney());
        data.putInt("Plevel", p.getLevel());
        data.putInt("Passets", p.getAssets());
        intent.putExtras(data);
        startActivity(intent);
    }

    public void UpdateTopBar(TextView player, TextView daytime){
        long money = p.getMoney();
        int level = p.getLevel();
        int assets = p.getAssets();
        String TBPlayer = "Lvl "+level+": $"+Double.toString((double)money/100)+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());
    }

    public void BuyClick(View v){
        Intent intent = new Intent(this, BuyActivity.class);
        Bundle data = new Bundle();
        int SID = v.getId() - 300000;
        data.putInt("SID", SID );
        data.putParcelable("DT", time);
        data.putString("name", f.getName(SID));
        data.putInt("Sprice", f.getShareCurrPrince(SID));
        data.putInt("owned", f.getSharesOwned(SID));
        data.putLong("Pmoney", p.getMoney());
        data.putInt("level", p.getLevel());
        data.putInt("assets", p.getAssets());
        data.putInt("total", f.getTotalShares(SID));
        data.putInt("lastClose", f.getLastClose(SID));
        intent.putExtras(data);
        startActivity(intent);
    }

    public void SellClick(View v){
        Intent intent = new Intent(this, SellActivity.class);
        Bundle data = new Bundle();
        int SID = v.getId() - 400000;
        data.putInt("SID", SID );
        data.putParcelable("DT", time);
        data.putString("name", f.getName(SID));
        data.putInt("Sprice", f.getShareCurrPrince(SID));
        data.putInt("owned", f.getSharesOwned(SID));
        data.putLong("Pmoney", p.getMoney());
        data.putInt("level", p.getLevel());
        data.putInt("assets", p.getAssets());
        data.putInt("total", f.getTotalShares(SID));
        data.putInt("lastClose", f.getLastClose(SID));
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickPrice(View v){
        Intent intent = new Intent(this, ShareActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", (v.getId() - 100000) );
        data.putParcelable("DT", time);
        data.putLong("Pmoney", p.getMoney());
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickName(View v){
        Intent intent = new Intent(this, ShareActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", (v.getId() - 200000) );
        data.putParcelable("DT", time);
        data.putLong("Pmoney", p.getMoney());
        intent.putExtras(data);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.menu_sound).setChecked(playSound);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.menu_Exit:
                ExitClicked();
                break;
            case R.id.menu_sound:
                playSound = !playSound;
                DBHandler.setSound(playSound);
                item.setChecked(playSound);
                break;
            case R.id.menu_NewGame:
                SelectNewGame();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void ExitClicked() {
        DBHandler.close();
        upd.interrupt();
        MainActivity.this.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private void SelectNewGame(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("New Game");
        String q = "Proceeding will erase all of your data and progress except your full assets. \n\nYou will start again at level 1 with $10000 and nothing owned. \n\nYou will also lose any part-gained assets. \n\nAre you sure you want to proceed?";
        builder.setMessage(q);

        builder.setPositiveButton("New Game", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                gaming = false;
                int assets = DBHandler.getAssets();
                DBHandler.clearData();
                DBHandler.PrepGame(assets, Company.Sectors.values());
                f = new Finance(DBHandler, 5);
                UpdateCentralUI();
                p = new Gamer(DBHandler);
                time = new Daytime(LocalBroadcastManager.getInstance(MainActivity.this.getApplicationContext()));
                DBHandler.setEconomySize(f.calcEconomySize());
                dialog.dismiss();
                gaming = true;
            }

        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
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

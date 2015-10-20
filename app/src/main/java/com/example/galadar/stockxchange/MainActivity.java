package com.example.galadar.stockxchange;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import java.util.logging.Handler;

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
    static Gamer p;
    static MemoryDB DBHandler;
    static Daytime time;
    static TextView topBarPlayer;
    static TextView topBarDaytime;
    TextView sharePrices;
    static Thread upd;
    static int[][] Messages;
    static boolean playSound;
    static boolean dayOpen = false;


    public static Finance getFinance(){
        return f;
    }

    public static Daytime getClock(){
        return time;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        LocalBroadcastManager.getInstance(this).registerReceiver(DayStartedMessageRec, new IntentFilter("DayStarted"));
//        LocalBroadcastManager.getInstance(this).registerReceiver(DayEndedMessageRec, new IntentFilter("DayEnded"));
//        LocalBroadcastManager.getInstance(this).registerReceiver(TermEndedMessageRec, new IntentFilter("TermEnded"));
//        LocalBroadcastManager.getInstance(this).registerReceiver(SoundAlteredRec, new IntentFilter("SoundAltered"));
//        LocalBroadcastManager.getInstance(this).registerReceiver(SharesTransactionedRec, new IntentFilter("SharesTransaction"));
//        LocalBroadcastManager.getInstance(this).registerReceiver(SpecificElementUpdate, new IntentFilter("SpecificPriceChange"));
        UpdateTopBar(topBarPlayer, topBarDaytime);
    }

/*
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(DayStartedMessageRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(DayEndedMessageRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(TermEndedMessageRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(SoundAlteredRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(SharesTransactionedRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(SpecificElementUpdate);
        super.onPause();
    }
*/

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

        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.layout);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view;
        int intprice;
        double dprice;

        for(int i=0;i<f.getNumComp();i++){
            view = layoutInflater.inflate(R.layout.main_share, parentLayout, false);
            LinearLayout shareData = (LinearLayout)view.findViewById(R.id.shareData);

            TextView shareInfo = (TextView)shareData.findViewById(R.id.shareInfo);
            shareInfo.setText(f.getName(i));
            shareInfo.setId(200000 + i);
            intprice = f.getShareCurrPrince(i);
            dprice = (double)intprice/100;
            String zerodigit = "";
            if(intprice%10==0){zerodigit="0";}
            TextView sharePrices = (TextView)shareData.findViewById(R.id.sharePrice);
            sharePrices.setText(Double.toString(dprice)+zerodigit);
            sharePrices.setId(100000 + i);

            Button Buy = (Button)shareData.findViewById(R.id.BuyButton);
            Buy.setText("Buy");
            Buy.setId(300000 + i);
            Buy.setEnabled(dayOpen);
            Button Sell = (Button)shareData.findViewById(R.id.SellButton);
            Sell.setText("Sell");
            Sell.setId(400000 + i);
            if((f.getSharesOwned(i)>0)&&dayOpen) {
                Sell.setEnabled(true);
                Sell.setTextColor(0xffffffff);
            } else {
                Sell.setEnabled(false);
                Sell.setTextColor(0xff000000);
            }

            parentLayout.addView(shareData);
        }

        DBHandler.UpdateEconomySize();

/*
        Messages[DBHandler.getMessagesNumber()][0] = DBHandler.publishMessage(new Message(3, "I am a message.", "I am a Message Body"), time.day);
        Messages[DBHandler.getMessagesNumber()][1]=3;
        Messages[DBHandler.getMessagesNumber()][0] = DBHandler.publishMessage(new Message(1, "I am a message too.", "I am Message Body too"), time.day);
        Messages[DBHandler.getMessagesNumber()][1]=1;
        Messages[DBHandler.getMessagesNumber()][0] = DBHandler.publishMessage(new Message(4, "I am message 3.", "I am its Message Body"), time.day);
        Messages[DBHandler.getMessagesNumber()][1]=4;
*/

        //New thread update time
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                boolean running = true;
                while (running) {
                    try {
                        synchronized (this) {
                            //TODO change wait from 5000ms to 10000ms (10 seconds)
                            wait(5000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    time.increment(10);
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

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                UpdateTimeView(time);
                callforTransactions();
            }
        }, new IntentFilter("TimeForwarded"));


        upd.start();
    }

    private BroadcastReceiver DayStartedMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dayOpen = true;
            UpdateUI();
            Toast.makeText(MainActivity.this, "Day Started", Toast.LENGTH_SHORT).show();
        }
    };


    private void callforTransactions() {
        if(!dayOpen) return;
        int temp;
        for(int i=0;i<f.getNumComp();i++) {
            temp = getSharesAmount(f.getCompOutlook(i), f.getSectorOutlook(f.getCompSector(i)), f.getTotalShares(i));
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

    public int getSharesAmount(double C, double S, int total){
        Random random = new Random();
        int temp = random.nextInt(4);
        if(temp!=2) {
            return 0;
        } else {
            double am = (random.nextGaussian()+5*C+S)*(0.3*total);
            if(am<=10) am = random.nextInt(50)+1;
            if(Math.abs(am)>0.12*total) am=Math.signum(am)*0.12*total;
            return (int)Math.round(am);
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

            double transPerc = 10*amount/f.getTotalShares(SID);
            int newPrice = (int)Math.round(oldPrice*(1 + transPerc));

            if(byPlayer){
                f.TransactShares(SID, amount);
                p.alterMoney(amount*oldPrice);
                if(amount>0){
                    DBHandler.BuyShare(SID, amount, p.getMoney());
                } else {
                    DBHandler.SellShare(SID, amount, p.getMoney());
                }
            }

            Intent intent1 = new Intent("SpecificPriceChange");
            Bundle data1 = new Bundle();
            data1.putInt("SID", SID);
            data1.putInt("newPrice", newPrice);
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
            boolean sellactive = data.getBoolean("PlayerOwner");

            double dprice = (double)price/100;
            String zerodigit = "";
            if(price%10==0){zerodigit="0";}
            TextView sharePrices = (TextView)findViewById(100000 + SID);
            double oldPrice = Double.parseDouble( sharePrices.getText().toString() );
            if(dprice>oldPrice) {
                sharePrices.setTextColor(0xff00ff00); //Color green for price going up
            } else {
                sharePrices.setTextColor(0xffff0000); //Color red for price going down
            }
            sharePrices.setText(Double.toString(dprice) + zerodigit);

            Button Sell = (Button)findViewById(400000 + SID);
            Sell.setEnabled(sellactive&&dayOpen);
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
            UpdateUI();
        }
    };

    //RUN ONLY ON UI THREAD
    public void UpdateUI(){
        Button B;
        for(int i=0;i<f.getNumComp();i++){
            B = (Button)findViewById(300000+i);
            B.setEnabled(dayOpen);
            B = (Button)findViewById(400000+i);
            B.setEnabled(dayOpen&&(f.getSharesOwned(i)>0));
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
        data.putInt("Economy_size", f.getEconomySize());
        data.putInt("TotalCompanies", f.getNumComp());
        data.putInt("SumOfShares", f.getSumShares());
        intent.putExtras(data);
        startActivity(intent);
    }

    private BroadcastReceiver TermEndedMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                upd.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Toast.makeText(context.getApplicationContext(), "Term Ended", Toast.LENGTH_LONG).show();
            //TODO Term update
            upd.notify();
        }
    };

    public void updateAll(){
        int intprice;
        double dprice;
        int old;

        for(int i=0;i<f.getNumComp();i++) {
            intprice = f.getShareCurrPrince(i);
            sharePrices = (TextView) findViewById(100000 + i);
            intprice = f.getShareCurrPrince(i);
            try {
                old = (int) Math.round(Double.parseDouble(sharePrices.getText().toString()) * 100);
            } catch (NullPointerException e) {
                old = f.getShareCurrPrince(i);
            }

            if (intprice > old) {
                sharePrices.setTextColor(0xff00ff00); //Color green for price going up
            } else if (intprice < old) {
                sharePrices.setTextColor(0xffff0000); //Color red for price going down
            } else {
                sharePrices.setTextColor(0xffffffff); //Color white for price unchanged
            }

            dprice = (double)intprice/100;
            String zerodigit = "";
            if(intprice%10==0){zerodigit="0";}
            sharePrices.setText(Double.toString(dprice)+zerodigit);
            Button Sell = (Button)findViewById(400000 + i);
            if((f.getSharesOwned(i)>0) && dayOpen){
                Sell.setEnabled(true);
                Sell.setTextColor(0xffffffff);
            } else {
                Sell.setEnabled(false);
                Sell.setTextColor(0xff000000);
            }
        }

        UpdateTopBar(topBarPlayer, topBarDaytime);
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
        data.putParcelable("Finance", f);
        data.putInt("Pmoney", p.getMoney());
        data.putInt("level", p.getLevel());
        data.putInt("assets", p.getAssets());
        data.putInt("NewWorth", 100); //TODO net worth function
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickInfo(View v){

        Intent intent = new Intent(this, InfoActivity.class );
        Bundle data = new Bundle();
        data.putParcelable("DT", time);
        data.putInt("Pmoney", p.getMoney());
        data.putInt("Plevel", p.getLevel());
        data.putInt("Passets", p.getAssets());
        intent.putExtras(data);
        startActivity(intent);
    }

    public void UpdateTopBar(TextView player, TextView daytime){
        int money = p.getMoney();
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
        data.putInt("Pmoney", p.getMoney());
        data.putInt("Plevel", p.getLevel());
        data.putInt("Passets", p.getAssets());
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
        data.putInt("price", f.getShareCurrPrince(SID));
        data.putInt("owned", f.getSharesOwned(SID));
        data.putInt("Pmoney", p.getMoney());
        data.putInt("Plevel", p.getLevel());
        data.putInt("Passets", p.getAssets());
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
        data.putParcelable("Finance", f);
        data.putInt("Pmoney", p.getMoney());
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickName(View v){
        Intent intent = new Intent(this, ShareActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", (v.getId() - 200000) );
        data.putParcelable("DT", time);
        data.putParcelable("Finance", f);
        data.putInt("Pmoney", p.getMoney());
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
                int assets = DBHandler.getAssets();
                DBHandler.clearData();
                DBHandler.PrepGame(assets, Company.Sectors.values());
                f = new Finance(DBHandler, 5);
                p = new Gamer(DBHandler);
                time = new Daytime(LocalBroadcastManager.getInstance(MainActivity.this.getApplicationContext()));
                DBHandler.setEconomySize(f.calcEconomySize());
                dialog.dismiss();
                MainActivity.this.updateAll();
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

}

package com.example.galadar.stockxchange;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
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


public class MainActivity extends Activity {

    Finance f;
    Gamer p;
    MemoryDB DBHandler;
    Daytime time;
    TextView topBarPlayer;
    TextView topBarDaytime;
    TextView sharePrices;
    Thread upd;
    public static final int CurrAPI = android.os.Build.VERSION.SDK_INT;


    @Override
    protected void onResume() {
        super.onResume();
        UpdateTopBar(topBarPlayer, topBarDaytime);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBHandler = MemoryDB.getInstance(getApplicationContext());

        if(DBHandler.getMaxSID()<1) {
            f = new Finance(DBHandler, 5);
            p = new Gamer(DBHandler);
            time = new Daytime(this);
            DBHandler.PrepGamer(0);
        } else {
            f = new Finance(DBHandler);
            p = new Gamer(DBHandler.getPlayerMoney(), DBHandler.getLevel(), DBHandler.getAssets(), DBHandler.getFame());
            time = new Daytime(this, DBHandler.getTerm(), DBHandler.getDay());
        }

        topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);

        UpdateTopBar(topBarPlayer, topBarDaytime);

        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.layout);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view;
        int intprice;
        double dprice;

        for(int i=0;i<=DBHandler.getMaxSID();i++){
            view = layoutInflater.inflate(R.layout.main_share, parentLayout, false);
            LinearLayout shareData = (LinearLayout)view.findViewById(R.id.shareData);

            TextView shareInfo = (TextView)shareData.findViewById(R.id.shareInfo);
            shareInfo.setText(DBHandler.getDBShareName(i));
            shareInfo.setId(200000 + i);
            intprice = DBHandler.getDBCurrPrice(i);
            dprice = (double)intprice/100;
            String zerodigit = "";
            if(intprice%10==0){zerodigit="0";}
            TextView sharePrices = (TextView)shareData.findViewById(R.id.sharePrice);
            sharePrices.setText(Double.toString(dprice)+zerodigit);
            sharePrices.setId(100000 + i);

            Button Buy = (Button)shareData.findViewById(R.id.BuyButton);
            Buy.setText("Buy");
            Buy.setId(300000 + i);
            Button Sell = (Button)shareData.findViewById(R.id.SellButton);
            Sell.setText("Sell");
            Sell.setId(400000 + i);
            Sell.setEnabled(false);
            if(CurrAPI>=23) {
                Sell.setTextColor(getColor(R.color.black));
            } else {
                Sell.setTextColor(getResources().getColor(R.color.black));
            }
            if(DBHandler.getOwnedShare(i)>0){
                Sell.setEnabled(true);
                if(CurrAPI>=23) {
                    Sell.setTextColor(getColor(R.color.white));
                } else {
                    Sell.setTextColor(getResources().getColor(R.color.white));
                }
            }

            parentLayout.addView(shareData);
        }

        Button PlayerButton = (Button)findViewById(R.id.PlayerInfo);
        PlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayerInfoActivity.class);
                Bundle data = new Bundle();
                data.putParcelable("DT", time);
                intent.putExtras(data);
                startActivity(intent);
            }
        });


        //New thread update time
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                boolean running = true;
                while (running) {
                    try {
                        synchronized (this) {
                            //TODO change wait from 3000ms to 15000ms (15 seconds)
                            wait(10000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updatePrices();
                }
            }
        };
        upd = new Thread(r);
        upd.start();


        LocalBroadcastManager.getInstance(this).registerReceiver(PricesUpdateMessageRec, new IntentFilter("DBPricesUpdated"));
        LocalBroadcastManager.getInstance(this).registerReceiver(DayEndedMessageRec, new IntentFilter("DayEnded"));
        LocalBroadcastManager.getInstance(this).registerReceiver(TermEndedMessageRec, new IntentFilter("TermEnded"));
    }

    private BroadcastReceiver PricesUpdateMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    private BroadcastReceiver DayEndedMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO Play sound for day end
            synchronized (upd) {
                try {
                    upd.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i <= DBHandler.getMaxSID(); i++) {
                    DBHandler.DayCloseShare(i, DBHandler.getDBCurrPrice(i));
                }
                upd.notify();
            }
        }
    };

    private BroadcastReceiver TermEndedMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                upd.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Toast.makeText(context.getApplicationContext(), "Term Ended", Toast.LENGTH_LONG);
            //TODO Term update
            upd.notify();
        }
    };


    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(PricesUpdateMessageRec);
        upd.interrupt();
        super.onDestroy();
    }

    public void updateUI(View v) {
        updateUI();
    }

    public void updateUI(){
        int intprice;
        double dprice;
        int old;

        for(int i=0;i<DBHandler.numberOfShares();i++) {
            intprice = DBHandler.getDBCurrPrice(i);
            sharePrices = (TextView) findViewById(100000 + i);
            old = (int)Math.round(Double.parseDouble(sharePrices.getText().toString()) * 100);

            if (intprice > old) {
                sharePrices.setTextColor(0xff00ff00); //Color green for price going up
            } else if (intprice < old) {
                sharePrices.setTextColor(0xffff0000); //Color red for price going down
            } else {
                sharePrices.setTextColor(0xffffffff); //Color white for price unchanged
            }

            intprice = DBHandler.getDBCurrPrice(i);
            dprice = (double)intprice/100;
            String zerodigit = "";
            if(intprice%10==0){zerodigit="0";}
            sharePrices.setText(Double.toString(dprice)+zerodigit);
            Button Sell = (Button)findViewById(400000 + i);
            if(DBHandler.getOwnedShare(i)>0){
                Sell.setEnabled(true);
                if(CurrAPI>=23) {
                    Sell.setTextColor(getColor(R.color.white));
                } else {
                    Sell.setTextColor(getResources().getColor(R.color.white));
                }
            } else {
                Sell.setEnabled(false);
                if(CurrAPI>=23) {
                    Sell.setTextColor(getColor(R.color.black));
                } else {
                    Sell.setTextColor(getResources().getColor(R.color.black));
                }
            }
        }

        UpdateTopBar(topBarPlayer, topBarDaytime);
    }

    public void updatePrices(){
        int temp;
        for(int i=0;i<DBHandler.numberOfShares();i++){
            temp = getNewSharePrice(DBHandler.getDBCurrPrice(i));
            DBHandler.setDBCurrPrice(i, temp);
        }
        //Toast.makeText(getApplicationContext(), Double.toString((double)DBHandler.getDBCurrPrice(0)/100), Toast.LENGTH_SHORT).show();
        time.increment(10);
        sendPriceUpdMessage();
    }

    private void sendPriceUpdMessage() {
        Intent i = new Intent("DBPricesUpdated");
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    public int getNewSharePrice(int old){
        Random random = new Random();
        if(random.nextInt(4)!=2) {
            return old;
        } else {
            double alteration=0;

            alteration+=random.nextGaussian()*(old*0.05);
            //TODO Add sector outlook effect
            //TODO Add company outlook effect

            while(Math.abs(100*alteration)>0.13*old){
                alteration *= 0.95;
            }
            old += (int)Math.round(alteration*100);

            return old;
        }
    }

    public void clickInfo(View v){

        Intent intent = new Intent(this, InfoActivity.class );
        Bundle data = new Bundle();
        data.putInt("SID", (v.getId() - 300000) );
        data.putParcelable("DT", time);
        intent.putExtras(data);
        startActivity(intent);
    }

    public void UpdateTopBar(TextView player, TextView daytime){
        int money = DBHandler.getPlayerMoney();
        int level = DBHandler.getLevel();
        int assets = DBHandler.getAssets();
        String TBPlayer = "Lvl "+level+": $"+Double.toString(money/100)+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());

    }

    public void BuyClick(View v){
        Intent intent = new Intent(this, BuyActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", (v.getId() - 300000) );
        data.putParcelable("DT", time);
        intent.putExtras(data);
        startActivity(intent);
    }

    public void SellClick(View v){
        Intent intent = new Intent(this, SellActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", (v.getId() - 400000) );
        data.putParcelable("DT", time);
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickPrice(View v){
        Intent intent = new Intent(this, ShareActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", (v.getId() - 100000) );
        data.putParcelable("DT", time);
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickName(View v){
        Intent intent = new Intent(this, ShareActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", (v.getId() - 200000) );
        data.putParcelable("DT", time);
        intent.putExtras(data);
        startActivity(intent);
    }

    /*
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
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void ExitClicked(View view) {
        DBHandler.close();
        MainActivity.this.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

}

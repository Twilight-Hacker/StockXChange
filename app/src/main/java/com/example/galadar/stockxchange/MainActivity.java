package com.example.galadar.stockxchange;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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


    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBHandler = new MemoryDB(this);

        if(DBHandler.numberOfShares()<1) {
            f = new Finance(DBHandler, 5);
            p = new Gamer(DBHandler);
            time = new Daytime();
            DBHandler.PrepGamer(0);
        } else {
            f = new Finance(DBHandler);
            p = new Gamer( DBHandler.getPlayerMoney(), DBHandler.getLevel(), DBHandler.getAssets(), DBHandler.getFame());
            time = new Daytime(DBHandler.getTerm(), DBHandler.getDay());
        }

        topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);

        UpdateTopBar(topBarPlayer, topBarDaytime);

        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.layout);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view;
        int intprice;
        double dprice;

        for(int i=0;i<DBHandler.numberOfShares();i++){
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

            parentLayout.addView(shareData);
        }

        Button PlayerButton = (Button)findViewById(R.id.PlayerInfo);
        PlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayerInfoActivity.class);
                Bundle data = new Bundle();
                data.putInt("SID", (v.getId() - 300000) );
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
                            //TODO change wait from 5000ms to 15000ms (15 seconds)
                            wait(5000);
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
    }

    private BroadcastReceiver PricesUpdateMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
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
        //TODO the real share price update function
        return (int)Math.round( Math.random() * 100000 );
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
        String TBPlayer = "Lvl "+level+": $"+(money/100)+" ("+assets+") ";
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
    }

}

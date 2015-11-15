package com.example.galadar.stockxchange;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ShareActivity extends AppCompatActivity {

    static Daytime time;
    static boolean playSound;
    static Finance f;
    static long money;
    static int price;
    static int owned;
    static String name;
    static int level;
    static int assets;
    static boolean dayOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Bundle data = getIntent().getExtras();
        final int SID = data.getInt("SID");
        time = MainActivity.getClock();
        money = data.getLong("Pmoney");
        level = data.getInt("level");
        assets = data.getInt("assets");
        f = MainActivity.getFinance();
        dayOpen = data.getBoolean("dayOpen");
        playSound = data.getBoolean("playSound");

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView) findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);

        TextView ShareName = (TextView)findViewById(R.id.ShareNameData);
        ShareName.setText(f.getName(SID));

        final TextView SharePrice = (TextView)findViewById(R.id.ShareCurrPriData);
        price = f.getShareCurrPrince(SID);
        String zerodigit;
        if(price%10==0)zerodigit="0";
        else zerodigit = "";
        SharePrice.setText("$"+Double.toString(((double)price)/100)+zerodigit);

        TextView ShareOwned = (TextView)findViewById((R.id.ShareOwnedData));
        owned = f.getSharesOwned(SID);
        ShareOwned.setText(Integer.toString(owned));

        TextView ShareTotal = (TextView)findViewById(R.id.ShareTotalData);
        ShareTotal.setText(Integer.toString(f.getTotalShares(SID)));

        TextView SharesValue = (TextView)findViewById(R.id.ShareOwnedVData);
        int val = owned*price;
        if(val%10==0)zerodigit="0";
        else zerodigit = "";
        SharesValue.setText("$" + Double.toString((double) val / 100) + zerodigit);

        final TextView ShareLast = (TextView)findViewById(R.id.SharePrevData);
        ShareLast.setText("$"+Double.toString((double) f.getLastClose(SID) / 100));

        final Button BuyButton = (Button)findViewById(R.id.BuyButton);
        if(dayOpen & (money>0)) {
            BuyButton.setEnabled(true);
            BuyButton.setTextColor(0xffffffff);
        } else if(dayOpen & (level>=4)){
            BuyButton.setEnabled(true);
            BuyButton.setTextColor(0xffff0000);
        } else {
            BuyButton.setEnabled(false);
            BuyButton.setTextColor(0xff000000);
        }
        BuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BuyShare(SID);
            }
        });

        final Button SellButton = (Button)findViewById(R.id.SellButton);
        if((f.getSharesOwned(SID)>0) & dayOpen) {
            SellButton.setEnabled(true);
            SellButton.setTextColor(0xffffffff);
        } else if(level>=4 & !f.isShorted(SID) & dayOpen){
            SellButton.setEnabled(true);
            SellButton.setTextColor(0xffff0000); //Red Color for short positions
        } else {
            SellButton.setEnabled(false);
            SellButton.setTextColor(0xff000000);
        }
        SellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SellShare(SID);
            }
        });

        final Button InfoButton = (Button)findViewById(R.id.CompanyInfo);
        InfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShareActivity.this, CompanyActivity.class);
                Bundle data = new Bundle();
                data.putInt("CID", SID);
                data.putLong("Pmoney", money);
                data.putInt("level", level);
                data.putInt("assets", assets);
                intent.putExtras(data);
                startActivity(intent);
                ShareActivity.this.finish();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                UpdateTimeView(time);
            }
        }, new IntentFilter("TimeForwarded"));

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getExtras().getInt("SID")==SID) {
                    price = intent.getExtras().getInt("newPrice");
                    SharePrice.setText("$"+Double.toString((double)price/100));
                }
            }
        }, new IntentFilter("SpecificPriceChange"));

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                BuyButton.setEnabled(false);
                SellButton.setEnabled(false);
                dayOpen=false;
                ShareLast.setText("$"+Double.toString((double)f.getLastClose(SID)/100));
            }
        }, new IntentFilter("DayEnded"));

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                dayOpen=true;
                if(dayOpen & (money>0)) {
                    BuyButton.setEnabled(true);
                    BuyButton.setTextColor(0xffffffff);
                } else if(dayOpen & (level>=4)){
                    BuyButton.setEnabled(true);
                    BuyButton.setTextColor(0xffff0000);
                } else {
                    BuyButton.setEnabled(false);
                    BuyButton.setTextColor(0xff000000);
                }
                if((f.getSharesOwned(SID)>0) & dayOpen) {
                    SellButton.setEnabled(true);
                    SellButton.setTextColor(0xffffffff);
                } else if(level>=4 & !f.isShorted(SID) & dayOpen){
                    SellButton.setEnabled(true);
                    SellButton.setTextColor(0xffff0000); //Red Color for short positions
                } else {
                    SellButton.setEnabled(false);
                    SellButton.setTextColor(0xff000000);
                }
            }
        }, new IntentFilter("DayStarted"));

    }

    public void BuyShare(int SID){
        Intent intent = new Intent(this, BuyActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", SID );
        data.putLong("Pmoney", money);
        data.putInt("level", level);
        data.putInt("assets", assets);
        data.putBoolean("playSound", playSound);
        data.putString("Sname", f.getName(SID));
        data.putInt("Sprice", f.getShareCurrPrince(SID));
        data.putInt("Owned", f.getSharesOwned(SID));
        data.putInt("totalShares", f.getTotalShares(SID));
        intent.putExtras(data);
        startActivity(intent);
        ShareActivity.this.finish();
    }

    public void SellShare(int SID){
        Intent intent = new Intent(this, SellActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", SID );
        data.putLong("Pmoney", money);
        data.putBoolean("playSound", playSound);
        data.putString("Sname", name);
        data.putInt("Sprice", price);
        data.putInt("Owned", owned);
        data.putInt("level", level);
        data.putInt("assets", assets);
        intent.putExtras(data);
        startActivity(intent);
        ShareActivity.this.finish();
    }

    public void UpdateTopBar(TextView player, TextView daytime) {
        String zerodigit;
        if(money%10==0)zerodigit="0";
        else zerodigit = "";
        String TBPlayer = "Lvl " + level + ": $" + Double.toString((double)money / 100) + zerodigit+" (" + assets + ") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nonmain, menu);
        menu.findItem(R.id.menu_sound).setChecked(playSound);
        return true;
    }

    public void UpdateTimeView(Daytime time){
        TextView DT = (TextView)findViewById(R.id.DaytimeInfo);
        DT.setText(time.DTtoString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.menu_sound:
                playSound = !playSound;
                LocalBroadcastManager.getInstance(ShareActivity.this).sendBroadcast(new Intent("SoundAltered").putExtra("sound", playSound));
                item.setChecked(playSound);
                break;
            case R.id.menu_backMain:
                ShareActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}

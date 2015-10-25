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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Bundle data = getIntent().getExtras();
        final int SID = data.getInt("SID");
        time = MainActivity.getClock();
        money = data.getLong("Pmoney");
        f = MainActivity.getFinance();

        playSound = true;

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView) findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);

        TextView ShareName = (TextView)findViewById(R.id.ShareNameData);
        ShareName.setText(f.getName(SID));

        TextView SharePrice = (TextView)findViewById(R.id.ShareCurrPriData);
        price = f.getShareCurrPrince(SID);
        SharePrice.setText(Double.toString(((double)price)/100));

        TextView ShareOwned = (TextView)findViewById((R.id.ShareOwnedData));
        owned = f.getSharesOwned(SID);
        ShareOwned.setText(Integer.toString(owned));

        TextView ShareTotal = (TextView)findViewById(R.id.ShareTotalData);
        ShareTotal.setText(Integer.toString(f.getTotalShares(SID)));

        TextView SharesValue = (TextView)findViewById(R.id.ShareOwnedVData);
        int val = owned*price;
        SharesValue.setText(Double.toString((double)val/100));

        TextView ShareLast = (TextView)findViewById(R.id.SharePrevData);
        ShareLast.setText(Double.toString((double)f.getLastClose(SID)/100));

        final Button BuyButton = (Button)findViewById(R.id.BuyButton);
        BuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BuyShare(SID);
            }
        });

        final Button SellButton = (Button)findViewById(R.id.SellButton);
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

    }

    public void BuyShare(int SID){
        Intent intent = new Intent(this, BuyActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", SID );
        data.putParcelable("DT", time);
        data.putLong("Pmoney", money);
        data.putInt("level", level);
        data.putInt("assets", assets);
        data.putBoolean("playSound", playSound);
        data.putString("Sname", f.getName(SID));
        data.putInt("Sprice", f.getShareCurrPrince(SID));
        data.putInt("Owned", f.getSharesOwned(SID));
        intent.putExtras(data);
        startActivity(intent);
        ShareActivity.this.finish();
    }

    public void SellShare(int SID){
        Intent intent = new Intent(this, SellActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", SID );
        data.putParcelable("DT", time);
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
        String TBPlayer = "Lvl " + level + ": $" + Double.toString(money / 100) + " (" + assets + ") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nonmain, menu);
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

package com.example.galadar.stockxchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EconomyInfoActivity extends AppCompatActivity {

    static boolean playSound;
    static int assets;
    static int level;
    static long money;
    static Daytime time;
    String zerodigit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_economy_info);
        Bundle data = getIntent().getExtras();
        time = MainActivity.getClock();
        long EconSize = data.getLong("Economy_size");
        int totComp = data.getInt("TotalCompanies");
        int SumShares = data.getInt("SumOfShares");

        money = data.getLong("Pmoney");
        level = data.getInt("level");
        assets = data.getInt("assets");

        this.setTitle(getString(R.string.title_activity_economy_info));

        TextView EconomySize = (TextView)findViewById(R.id.EconomySizeDt);
        EconomySize.setText("M$"+Long.toString(Math.round(EconSize/1000000)));
        TextView CompanySize = (TextView)findViewById(R.id.TotCompDt);
        CompanySize.setText(Integer.toString(totComp));
        TextView TotalShares = (TextView)findViewById(R.id.TotalSharesDt);
        TotalShares.setText(Integer.toString(SumShares));

        playSound = data.getBoolean("Sound");

        Button Back = (Button)findViewById(R.id.OK);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EconomyInfoActivity.this.finish();
            }
        });

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                UpdateTimeView(time);
            }
        }, new IntentFilter("TimeForwarded"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nonmain, menu);
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
            case R.id.menu_sound:
                playSound = !playSound;
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("SoundAltered").putExtra("playSound", playSound));
                item.setChecked(playSound);
                break;
            case R.id.menu_backMain:
                EconomyInfoActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void UpdateTopBar(TextView player, TextView daytime){
        if(money%10==0)zerodigit="0";
        else zerodigit="";
        String TBPlayer = "Lvl "+level+": $"+Double.toString((double)money/100)+zerodigit+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());
    }

    public void UpdateTimeView(Daytime time){
        TextView DT = (TextView)findViewById(R.id.DaytimeInfo);
        DT.setText(time.DTtoString());
    }

}

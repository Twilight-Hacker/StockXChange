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

public class PlayerInfoActivity extends AppCompatActivity {

    boolean playSound;
    static Daytime time;
    static TextView daytimeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_info);

        time = MainActivity.getClock();
        Bundle data = getIntent().getExtras();

        int next = data.getInt("next");
        long money = data.getLong("Pmoney");
        int assets =data.getInt("assets");
        int level = data.getInt("level");
        long NetWorth = data.getLong("NetWorth");
        playSound = data.getBoolean("playSound");

        long value = money+NetWorth;

        //boolean activate LevelUp button
        boolean ena = (((double)value/100)>=next) & (level<6);

        TextView playerInfoBar = (TextView)findViewById(R.id.PlayerDataInfo);
        String q = "Lvl "+level+": $"+Double.toString((double)money/100)+" ("+assets+") ";
        playerInfoBar.setText(q);

        daytimeView = (TextView)findViewById(R.id.DaytimeInfo);
        updateTime();
        LocalBroadcastManager.getInstance(this).registerReceiver(timeUpdated, new IntentFilter("TimeForwarded"));

        TextView MoneyView = (TextView)findViewById(R.id.PlayerMoneyDt);
        MoneyView.setText(Double.toString((double) money / 100));

        TextView AssetsView = (TextView)findViewById(R.id.AssetsDt);
        AssetsView.setText(Integer.toString(assets));

        TextView LevelView = (TextView)findViewById(R.id.PlLevelDt);
        LevelView.setText(Integer.toString(level));

        TextView NextView = (TextView)findViewById(R.id.NextLevelDt);
        NextView.setText(Integer.toString(next));

        TextView NetWorthView = (TextView)findViewById(R.id.PlNetWorthDt);
        NetWorthView.setText(Double.toString((double)value / 100));

        Button Back = (Button)findViewById(R.id.OK);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerInfoActivity.this.finish();
            }
        });

        Button LevelUp = (Button)findViewById(R.id.LevelUp);
        if(ena){
            LevelUp.setTextColor(0xffffffff);
        } else {
            LevelUp.setTextColor(0xff000000);
        }
        LevelUp.setEnabled(ena);

        LevelUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager.getInstance(PlayerInfoActivity.this).unregisterReceiver(timeUpdated);
                LocalBroadcastManager.getInstance(PlayerInfoActivity.this).sendBroadcast(new Intent("LevelUp"));
                PlayerInfoActivity.this.finish();
            }
        });
    }

    private void updateTime() {
        daytimeView.setText(time.DTtoString());
    }

    private BroadcastReceiver timeUpdated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            updateTime();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nonmain, menu);
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
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("SoundAltered"));
                item.setChecked(playSound);
                break;
            case R.id.menu_backMain:
                PlayerInfoActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}

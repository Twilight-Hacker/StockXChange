package com.example.galadar.stockxchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class InfoActivity extends AppCompatActivity {


    static Daytime time;
    static boolean playSound;
    static int assets;
    static long money;
    static int level;
    String zeridigit;
    static TextView topBarPlayer;
    static TextView topBarDaytime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        time = MainActivity.getClock();
        Bundle data = getIntent().getExtras();
        money = data.getLong("Pmoney");
        assets = data.getInt("Passets");
        level = data.getInt("Plevel");
        final ArrayList<String> info = data.getStringArrayList("Info");
        playSound =data.getBoolean("playSound");

        if(info.isEmpty())info.add("There are no info tips at this point");

        ListView infoView = (ListView)findViewById(R.id.InfoList);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.text_simple_whiteonblack, R.id.WhiteOnBlack, info);
        infoView.setAdapter(adapter);

        topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);

        topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);

        UpdateTopBar(topBarPlayer, topBarDaytime);

        final Button upD = (Button) findViewById(R.id.GetInfo);
        if(assets>0){
            upD.setTextColor(0xffffffff);
            upD.setEnabled(true);
        } else {
            upD.setEnabled(false);
            upD.setTextColor(0xff000000);
        }

        upD.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(info.contains("There are no info tips at this point"))info.remove("There are no info tips at this point");
                assets--;
                if(assets==0){
                    upD.setEnabled(false);
                    upD.setTextColor(0xff000000);
                }
                info.add(MainActivity.addAssetInfo());
                adapter.notifyDataSetChanged();
                UpdateTopBar(topBarPlayer, topBarDaytime);
            }

        });

        final Button BackB = (Button)findViewById(R.id.BackButton);

        BackB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoActivity.this.finish();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                UpdateTopBar(topBarPlayer, topBarDaytime);
            }
        }, new IntentFilter("TimeForwarded"));

    }

    public void UpdateTopBar(TextView player, TextView daytime){
        if(money%10==0)zeridigit="0";
        else zeridigit="";
        String TBPlayer = "Lvl "+level+": $"+Double.toString((double)money/100)+zeridigit+" ("+assets+") ";
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
                InfoActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
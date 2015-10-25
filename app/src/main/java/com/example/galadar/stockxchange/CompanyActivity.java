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

public class CompanyActivity extends AppCompatActivity {


    //static MemoryDB DBHandler;
    static Finance f;
    static Daytime time;
    static boolean playSound;
    static int money;
    static int level;
    static int assets;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company);
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        int CID = data.getInt("CID");
        f = MainActivity.getFinance();
        time = MainActivity.getClock();
        playSound = data.getBoolean("playSound");
        money = data.getInt("Pmoney");
        level = data.getInt("level");
        assets = data.getInt("assets");

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);

        TextView NameView = (TextView)findViewById(R.id.CompNameDt);
        NameView.setText(f.getName(CID));

        TextView SectorView = (TextView)findViewById(R.id.CompSectorDt);
        SectorView.setText(f.getCompSector(CID));

        TextView TotalValueView = (TextView)findViewById(R.id.TotalValDt);
        TotalValueView.setText(Double.toString((double)f.getCompTotalValue(CID)/100));

        TextView TotalSharesView = (TextView)findViewById((R.id.TotalSharesDt));
        TotalSharesView.setText(Integer.toString(f.getTotalShares(CID)));

        TextView LastRevView = (TextView)findViewById(R.id.LastTermRevenueDt);
        LastRevView.setText(Double.toString((double)f.getLastRevenue(CID)/100));

        TextView InvestView = (TextView)findViewById(R.id.LastTermInvDt);
        InvestView.setText(Double.toString((double)f.getInvestment(CID)/100));

        Button Report = (Button)findViewById(R.id.ScamCheck);
        Report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CompanyActivity.this.finish();
            }
        });

        Button OK = (Button)findViewById(R.id.OK);
        OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CompanyActivity.this.finish();
            }
        });

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
                CompanyActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void UpdateTopBar(TextView player, TextView daytime){
        String TBPlayer = "Lvl "+level+": $"+Double.toString(money/100)+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());
    }

    public void UpdateTimeView(Daytime time){
        TextView DT = (TextView)findViewById(R.id.DaytimeInfo);
        DT.setText(time.DTtoString());
    }

}

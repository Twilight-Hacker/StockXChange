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


    static MemoryDB DBHandler;
    static Daytime time;
    static boolean playSound;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company);
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        String name = data.getString("name");
        DBHandler = MemoryDB.getInstance(getApplicationContext());
        time = MainActivity.getClock();
        playSound = true;

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);

        TextView NameView = (TextView)findViewById(R.id.CompNameDt);
        NameView.setText(name);

        TextView SectorView = (TextView)findViewById(R.id.CompSectorDt);
        SectorView.setText(DBHandler.getCompanySector(name));

        TextView TotalValueView = (TextView)findViewById(R.id.TotalValDt);
        TotalValueView.setText(Double.toString((double)DBHandler.getCompTotalValue(name)/100));

        TextView TotalSharesView = (TextView)findViewById((R.id.TotalSharesDt));
        TotalSharesView.setText(Integer.toString(DBHandler.getTotalShares(name)));

        TextView LastRevView = (TextView)findViewById(R.id.LastTermRevenueDt);
        LastRevView.setText(Double.toString((double)DBHandler.getLastRevenue(name)/100));

        TextView InvestView = (TextView)findViewById(R.id.LastTermInvDt);
        InvestView.setText(Double.toString((double)DBHandler.getInvestment(name)/100));

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
                DBHandler.setSound(playSound);
                item.setChecked(playSound);
                break;
            case R.id.menu_backMain:
                CompanyActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void UpdateTopBar(TextView player, TextView daytime){
        int money = DBHandler.getPlayerMoney();
        int level = DBHandler.getLevel();
        int assets = DBHandler.getAssets();
        String TBPlayer = "Lvl "+level+": $"+Double.toString(money/100)+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());
    }

    public void UpdateTimeView(Daytime time){
        TextView DT = (TextView)findViewById(R.id.DaytimeInfo);
        DT.setText(time.DTtoString());
    }

}

package com.galadar.example.stockxchange;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
    static long money;
    static int level;
    static int assets;
    String zerodigit;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company);

        Bundle data = getIntent().getExtras();
        final int CID = data.getInt("CID");
        f = MainActivity.getFinance();
        time = MainActivity.getClock();
        playSound = data.getBoolean("playSound");
        money = data.getLong("Pmoney");
        level = data.getInt("level");
        assets = data.getInt("assets");

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);

        TextView NameView = (TextView)findViewById(R.id.CompNameDt);
        NameView.setText(f.getName(CID));
        String title = f.getName(CID) + " "+ getString(R.string.Company_Activity_Title);
        this.setTitle(title);

        TextView SectorView = (TextView)findViewById(R.id.CompSectorDt);
        SectorView.setText(f.getCompSector(CID));

        TextView TotalValueView = (TextView)findViewById(R.id.TotalValDt);
        long value = f.getCompTotalValue(CID);
        TotalValueView.setText("$"+Long.toString(value));

        TextView TotalSharesView = (TextView)findViewById((R.id.TotalSharesDt));
        TotalSharesView.setText(Integer.toString(f.getTotalShares(CID)));

        long rev = f.getLastRevenue(CID);
        TextView LastRevView = (TextView)findViewById(R.id.LastTermRevenueDt);
        LastRevView.setText("$"+Long.toString(rev));

        int inv = f.getInvestment(CID);
        TextView InvestView = (TextView)findViewById(R.id.LastTermInvDt);
        InvestView.setText("$"+Long.toString(inv));

        Button Report = (Button)findViewById(R.id.ScamCheck);
        Report.setEnabled(assets>0);
        Report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(CompanyActivity.this);

                builder.setTitle(getString(R.string.ReportTitle));
                builder.setMessage(getString(R.string.ReportBody));
                builder.setPositiveButton(getString(R.string.ReportYes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        CompanyActivity.this.finish();
                        MainActivity.callScam(CID);
                    }

                });

                builder.setNegativeButton(getString(R.string.CancelButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog d = builder.create();
                d.show();
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
                CompanyActivity.this.finish();
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

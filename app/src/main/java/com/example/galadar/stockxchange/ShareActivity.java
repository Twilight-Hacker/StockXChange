package com.example.galadar.stockxchange;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ShareActivity extends Activity {

    MemoryDB DBHandler;
    Daytime time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        setContentView(R.layout.activity_share);

        Bundle data = i.getExtras();
        final int SID = data.getInt("SID");
        DBHandler = MemoryDB.getInstance(getApplicationContext());
        time = data.getParcelable("DT");

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);

        TextView ShareName = (TextView)findViewById(R.id.ShareNameData);
        final String name = DBHandler.getDBShareName(SID); //data.getString("name");
        ShareName.setText(name);

        TextView SharePrice = (TextView)findViewById(R.id.ShareCurrPriData);
        int price = DBHandler.getDBCurrPrice(SID); //data.getInt("price");
        SharePrice.setText(Double.toString(((double)price)/100));

        TextView ShareOwned = (TextView)findViewById((R.id.ShareOwnedData));
        int owned = DBHandler.getOwnedShare(SID); //data.getInt("owned",0);
        ShareOwned.setText(Integer.toString(owned));

        TextView SharesValue = (TextView)findViewById(R.id.ShareOwnedVData);
        int val = owned*price;
        SharesValue.setText(Double.toString((double)val/100));

        TextView ShareLast = (TextView)findViewById(R.id.SharePrevData);
        int prev = DBHandler.getDBLastClose(SID);
        ShareLast.setText(Double.toString((double)prev/100));

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
                data.putString("name", name);
                data.putParcelable("DT", time);
                intent.putExtras(data);
                startActivity(intent);
                ShareActivity.this.finish();
            }
        });

    }

    public void BuyShare(int SID){
        Intent intent = new Intent(this, BuyActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", SID);
        data.putParcelable("DT", time);
        intent.putExtras(data);
        startActivity(intent);
        ShareActivity.this.finish();
    }

    public void SellShare(int SID){
        Intent intent = new Intent(this, SellActivity.class);
        Bundle data = new Bundle();
        //data.putString("name", name);
        //data.putInt("price", price);
        //data.putInt("owned", owned);
        data.putInt("SID", SID);
        data.putParcelable("DT", time);
        intent.putExtras(data);
        startActivity(intent);
        ShareActivity.this.finish();
    }

    public void UpdateTopBar(TextView player, TextView daytime){
        int money = DBHandler.getPlayerMoney();
        int level = DBHandler.getLevel();
        int assets = DBHandler.getAssets();
        String TBPlayer = "Lvl "+level+": $"+Double.toString(money/100)+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());

    }
}

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

    //SharedPreferences Ownerships;
    MemoryDB DBHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        setContentView(R.layout.activity_share);

        Bundle data = i.getExtras();
        final int SID = data.getInt("SID");
        DBHandler = new MemoryDB(this);

        TextView ShareName = (TextView)findViewById(R.id.ShareNameData);
        final String name = DBHandler.getDBShareName(SID); //data.getString("name");
        ShareName.setText(name);

        TextView SharePrice = (TextView)findViewById(R.id.ShareCurrPriData);
        int price = DBHandler.getDBCurrPrice(SID); //data.getInt("price");
        SharePrice.setText(Double.toString(((double)price)/100));

        TextView ShareOwned = (TextView)findViewById((R.id.ShareOwnedData));
        int owned = 250; //data.getInt("owned",0);
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
                intent.putExtra("name", name);
                startActivity(intent);
                DBHandler.close();
                ShareActivity.this.finish();
            }
        });

    }

    public void BuyShare(int SID){
        Intent intent = new Intent(this, BuyActivity.class);
        Bundle data = new Bundle();
        //data.putString("name", name);
        //data.putInt("price", price);
        //data.putInt("money", money);
        data.putInt("SID", SID );
        intent.putExtras(data);
        startActivity(intent);
        DBHandler.close();
        ShareActivity.this.finish();
    }

    public void SellShare(int SID){
        Intent intent = new Intent(this, SellActivity.class);
        Bundle data = new Bundle();
        //data.putString("name", name);
        //data.putInt("price", price);
        //data.putInt("owned", owned);
        data.putInt("SID", SID );
        intent.putExtras(data);
        startActivity(intent);
        DBHandler.close();
        ShareActivity.this.finish();
    }
}

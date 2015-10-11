package com.example.galadar.stockxchange;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ShareActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        Bundle data = i.getExtras();
        setContentView(R.layout.activity_share);

        TextView ShareName = (TextView)findViewById(R.id.ShareNameData);
        final String name = data.getString("name");
        ShareName.setText(name);
        TextView SharePrice = (TextView)findViewById(R.id.ShareCurrPriData);
        final int price = data.getInt("price");
        SharePrice.setText(Double.toString(((double)price)/100));

        final Button BuyButton = (Button)findViewById(R.id.BuyButton);
        BuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BuyShare(name, price, 2500000);
            }
        });

        final Button Info = (Button)findViewById(R.id.CompanyInfo);
        Info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ShareActivity.this, CompanyActivity.class);
                i.putExtra("name", name);
                startActivity(i);
            }
        });

        final Button SellButton = (Button)findViewById(R.id.SellButton);
        SellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SellShare(name, price, 2500);
            }
        });

        final Button InfoButton = (Button)findViewById(R.id.CompanyInfo);
        InfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShareActivity.this, CompanyActivity.class);
                intent.putExtra("name", name);
                startActivity(intent);
            }
        });

    }

    public void BuyShare(String name, int price, int max){
        Intent intent = new Intent(this, BuyActivity.class);
        Bundle data = new Bundle();
        data.putString("name", name);
        data.putInt("price", price);
        data.putInt("money", 25000);
        intent.putExtras(data);
        startActivity(intent);
        ShareActivity.this.finish();
    }

    public void SellShare(String name, int price, int max){
        Intent intent = new Intent(this, SellActivity.class);
        Bundle data = new Bundle();
        data.putString("name", name);
        data.putInt("price", price);
        data.putInt("max", max);
        intent.putExtras(data);
        startActivity(intent);
        ShareActivity.this.finish();
    }
}

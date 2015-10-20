package com.example.galadar.stockxchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SellActivity extends AppCompatActivity {

    static int price;
    static int amount;
    static int total;
    static int SID;
    static int money;
    static int level;
    static int assets;
    static Daytime time;
    static boolean playSound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);


        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        SID = data.getInt("SID");
        time = MainActivity.getClock();
        playSound = true;

        String name = data.getString("name");
        price = data.getInt("Sprice");
        final int max = data.getInt("owned");
        money = data.getInt("money");
        level = data.getInt("level");
        assets = data.getInt("assets");
        amount = 0;

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);


        TextView ShareName = (TextView)findViewById(R.id.ShareNameDt);
        ShareName.setText(name);
        TextView SharePrice = (TextView)findViewById(R.id.ShareCurrPriDt);
        SharePrice.setText(Double.toString(((double)price)/100));
        TextView SharesOwned = (TextView)findViewById(R.id.SharesOwnedDt);
        SharesOwned.setText(Integer.toString(max));

        final TextView ShareAmount = (TextView)findViewById(R.id.ShareAmountDt);
        final TextView Cost = (TextView)findViewById(R.id.TotalValueDt);
        ShareAmount.setText(Integer.toString(amount));
        total = amount*price;
        Cost.setText(Double.toString(((double)total)/100));

        final Button maxButton = (Button)findViewById(R.id.MaxSharesButton);
        final Button plusOne = (Button)findViewById(R.id.AddSharesButton);
        final Button minusOne = (Button)findViewById(R.id.RemSharesButton);
        final Button resetAll = (Button)findViewById(R.id.ZeroSharesButton);

        maxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount = max;
                ShareAmount.setText(Integer.toString(amount));
                total = amount*price;
                Cost.setText(Double.toString(((double)total)/100));
            }
        });

        plusOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(amount<max) {
                    amount++;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot sell more shares than those you have", Toast.LENGTH_SHORT).show();
                }
            }
        });

        minusOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(amount>0) {
                    amount--;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else {
                    Toast.makeText(getApplicationContext(),"You cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                }
            }
        });

        resetAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount = 0;
                ShareAmount.setText(Integer.toString(amount));
                total = amount*price;
                Cost.setText(Double.toString(((double)total)/100));
            }
        });


        Button Execute = (Button)findViewById(R.id.SellButton);
        Button Cancel = (Button)findViewById(R.id.CancelButton);

        Execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent SharesSold = new Intent("SharesTransaction"); //To update prices
                Bundle Sdata = new Bundle();
                Sdata.putInt("SID", SID);
                Sdata.putInt("amount", (0-amount) );
                Sdata.putInt("atPrice", price);
                Sdata.putBoolean("ByPlayer", true);
                SharesSold.putExtras(Sdata);
                LocalBroadcastManager.getInstance(SellActivity.this).sendBroadcast(SharesSold);
                SellActivity.this.finish();
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SellActivity.this.finish();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(PricesUpdateMessageRec, new IntentFilter("SpecificPriceUpdated"));

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
                LocalBroadcastManager.getInstance(SellActivity.this).sendBroadcast(new Intent("SoundAltered").putExtra("sound", playSound));
                item.setChecked(playSound);
                break;
            case R.id.menu_backMain:
                SellActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void UpdateTopBar(TextView player, TextView daytime){
        String TBPlayer = "Lvl "+level+": $"+Double.toString(money/100)+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());

    }

    private BroadcastReceiver PricesUpdateMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getExtras().getInt("SID")==SID) {
                Button Execute = (Button) findViewById(R.id.SellButton);
                Execute.setEnabled(false);
                final TextView Cost = (TextView) findViewById(R.id.TotalValueDt);
                TextView SharePrice = (TextView) findViewById(R.id.ShareCurrPriDt);
                price = intent.getExtras().getInt("newPrice");
                SharePrice.setText(Double.toString(((double) price) / 100));
                total = amount * price;
                Cost.setText(Double.toString(((double) total) / 100));
                Execute.setEnabled(true);
            }
        }
    };

    public void UpdateTimeView(Daytime time){
        TextView DT = (TextView)findViewById(R.id.DaytimeInfo);
        DT.setText(time.DTtoString());
    }

}

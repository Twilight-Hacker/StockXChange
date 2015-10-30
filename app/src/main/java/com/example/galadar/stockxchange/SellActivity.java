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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SellActivity extends AppCompatActivity {

    static int price;
    static int amount;
    static int total;
    static int SID;
    static long money;
    static int owned;
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
        owned = data.getInt("owned");
        money = data.getLong("money");
        level = data.getInt("level");
        assets = data.getInt("assets");
        amount = 0;

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);

        final EditText Days = (EditText)findViewById(R.id.SettleDaysEditText);

        if(owned==0){
            TextView view1 = (TextView)findViewById(R.id.ShortSettleLbl);
            view1.setVisibility(TextView.VISIBLE);
            TextView view2 = (TextView)findViewById(R.id.ShortSellWarning);
            view2.setVisibility(TextView.VISIBLE);
            Days.setVisibility(EditText.VISIBLE);
            Days.setText(Integer.toString(10));
        }

        TextView ShareName = (TextView)findViewById(R.id.ShareNameDt);
        ShareName.setText(name);
        TextView SharePrice = (TextView)findViewById(R.id.ShareCurrPriDt);
        SharePrice.setText(Double.toString(((double)price)/100));
        TextView SharesOwned = (TextView)findViewById(R.id.SharesOwnedDt);
        SharesOwned.setText(Integer.toString(owned));

        final TextView ShareAmount = (TextView)findViewById(R.id.ShareAmountDt);
        final TextView Cost = (TextView)findViewById(R.id.TotalValueDt);
        ShareAmount.setText(Integer.toString(amount));
        total = amount*price;
        Cost.setText(Double.toString(((double)total)/100));

        final Button maxButton = (Button)findViewById(R.id.MaxSharesButton);
        final Button plusOne = (Button)findViewById(R.id.AddSharesButton);
        final Button plus10 = (Button)findViewById(R.id.Add10SharesButton);
        final Button plus100 = (Button)findViewById(R.id.Add100SharesButton);
        final Button minusOne = (Button)findViewById(R.id.RemSharesButton);
        final Button minus10 = (Button)findViewById(R.id.Rem10SharesButton);
        final Button minus100 = (Button)findViewById(R.id.Rem100SharesButton);
        final Button resetAll = (Button)findViewById(R.id.ZeroSharesButton);

        if(owned==0){
            maxButton.setEnabled(false);
        } else {
            maxButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    amount = owned;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double) total) / 100));
                    }
                }
            );
        }

        plusOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    amount++;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else if(amount<owned) {
                    amount++;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot sell more shares than those you have", Toast.LENGTH_SHORT).show();
                }
            }
        });

        plus10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    amount=+10;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else if(amount+10<=owned) {
                    amount=+10;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot sell more shares than those you have", Toast.LENGTH_SHORT).show();
                    amount = owned;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double) total) / 100));
                }
            }
        });

        plus100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    amount=+100;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else if(amount+100<=owned) {
                    amount=+100;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot sell more shares than those you have", Toast.LENGTH_SHORT).show();
                    amount = owned;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                }
            }
        });

        minusOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    amount--;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else if(amount>0) {
                    amount--;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else {
                    Toast.makeText(getApplicationContext(),"You cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                }
            }
        });

        minus10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    amount-=10;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else if(amount-10>=0) {
                    amount-=10;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else {
                    Toast.makeText(getApplicationContext(),"You cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                    amount=0;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double) total) / 100));
                }
            }
        });

        minus100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    amount-=100;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else if(amount-100>=0) {
                    amount-=100;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
                } else {
                    Toast.makeText(getApplicationContext(),"You cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                    amount=0;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    Cost.setText(Double.toString(((double)total)/100));
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
                if(owned==0){
                    Intent SharesSold = new Intent("SharesShortTransaction"); //To add to short sales, and them broadcast to update prices
                    Bundle Sdata = new Bundle();
                    Sdata.putInt("SID", SID);
                    Sdata.putInt("amount", (0 - amount));
                    Sdata.putInt("atPrice", price);
                    Sdata.putBoolean("ByPlayer", true);
                    Sdata.putInt("Days", Integer.parseInt( Days.getText().toString()));
                    SharesSold.putExtras(Sdata);
                    LocalBroadcastManager.getInstance(SellActivity.this).sendBroadcast(SharesSold);
                    SellActivity.this.finish();
                } else {
                    Intent SharesSold = new Intent("SharesTransaction"); //To update prices
                    Bundle Sdata = new Bundle();
                    Sdata.putInt("SID", SID);
                    Sdata.putInt("amount", (0 - amount));
                    Sdata.putInt("atPrice", price);
                    Sdata.putBoolean("ByPlayer", true);
                    SharesSold.putExtras(Sdata);
                    LocalBroadcastManager.getInstance(SellActivity.this).sendBroadcast(SharesSold);
                    SellActivity.this.finish();
                }
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

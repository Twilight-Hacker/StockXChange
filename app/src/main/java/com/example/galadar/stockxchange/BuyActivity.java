package com.example.galadar.stockxchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BuyActivity extends AppCompatActivity {

    static int amount;
    static int price;
    static int total;
    static int level;
    static int assets;
    static int max;
    static long money;
    static int SID;
    static String Sname;
    static int owned;
    static Daytime time;
    static boolean playSound;
    String zerodigit;
    static int totalShares;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        Intent intent = getIntent();
        final Bundle data = intent.getExtras();
        SID = data.getInt("SID");
        time = MainActivity.getClock();
        money = data.getLong("Pmoney");
        level = data.getInt("level");
        assets = data.getInt("assets");
        playSound = data.getBoolean("playSound");
        Sname = data.getString("Sname");
        price = data.getInt("Sprice");
        owned = data.getInt("Owned");
        totalShares = data.getInt("totalShares");

        String title = getString(R.string.BuyActivityTitle) +" "+ Sname + " "+getString(R.string.shares);
        this.setTitle(title);

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);

        TextView ShareName = (TextView)findViewById(R.id.ShareNameDt);
        ShareName.setText(Sname);

        TextView SharePrice = (TextView) findViewById(R.id.ShareCurrPriDt);
        if(price%10==0)zerodigit="0";
        else zerodigit="";
        SharePrice.setText("$"+Double.toString(((double)price)/100)+zerodigit);

        amount = 0; //Amount of shares to Buy
        total = 0; //Amount of money to give - total price of the transaction

        max = (int) Math.floor( money/price );
        max = Math.min(max, totalShares);

        final TextView ShareAmount = (TextView)findViewById(R.id.ShareAmountDt);
        final TextView Cost = (TextView)findViewById(R.id.TotalValueDt);

        final Button maxButton = (Button)findViewById(R.id.MaxSharesButton);
        final Button plusOne = (Button)findViewById(R.id.AddSharesButton);
        final Button plus10 = (Button)findViewById(R.id.Add10SharesButton);
        final Button plus100 = (Button)findViewById(R.id.Add100SharesButton);
        final Button minusOne = (Button)findViewById(R.id.RemSharesButton);
        final Button minus10 = (Button)findViewById(R.id.Rem10SharesButton);
        final Button minus100 = (Button)findViewById(R.id.Rem100SharesButton);
        final Button resetAll = (Button)findViewById(R.id.ZeroSharesButton);

        maxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount = max;
                ShareAmount.setText(Integer.toString(amount));
                total = amount*price;
                if(total%10==0)zerodigit="0";
                else zerodigit="";
                Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
            }
        });

        plusOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(amount<max) {
                    amount++;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit="";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else {
                    Toast.makeText(getApplicationContext(),"Not enough Money",Toast.LENGTH_SHORT).show();
                }
            }
        });

        plus10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(amount+10<=max) {
                    amount+=10;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit="";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else {
                    Toast.makeText(getApplicationContext(),"Not enough Money",Toast.LENGTH_SHORT).show();
                    amount = max;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount*price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit="";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                }
            }
        });

        plus100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(amount+100<=max) {
                    amount+=100;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit="";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else {
                    Toast.makeText(getApplicationContext(),"Not enough Money",Toast.LENGTH_SHORT).show();
                    amount = max;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount*price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit="";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
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
                    if(total%10==0)zerodigit="0";
                    else zerodigit="";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else {
                    Toast.makeText(getApplicationContext(),"You cannot buy less than 0 shares", Toast.LENGTH_SHORT).show();
                }
            }
        });

        minus10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(amount-10>0) {
                    amount-=10;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit="";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else {
                    Toast.makeText(getApplicationContext(),"You cannot buy less than 0 shares", Toast.LENGTH_SHORT).show();
                    amount = 0;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount*price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit="";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                }
            }
        });

        minus100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(amount-100>0) {
                    amount-=100;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit="";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else {
                    Toast.makeText(getApplicationContext(),"You cannot buy less than 0 shares", Toast.LENGTH_SHORT).show();
                    amount = 0;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount*price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit="";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                }
            }
        });

        resetAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount = 0;
                ShareAmount.setText(Integer.toString(amount));
                total = amount*price;
                if(total%10==0)zerodigit="0";
                else zerodigit="";
                Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
            }
        });

        final Button Execute = (Button)findViewById(R.id.BuyButton);
        Button Cancel = (Button)findViewById(R.id.CancelButton);

        Execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent SharesBought = new Intent("SharesTransaction"); //To update prices
                Bundle Bdata = new Bundle();
                Bdata.putInt("SID", SID);
                Bdata.putInt("amount", amount);
                Bdata.putInt("atPrice", price);
                Bdata.putBoolean("ByPlayer", true);
                SharesBought.putExtras(Bdata);
                LocalBroadcastManager.getInstance(BuyActivity.this).sendBroadcast(SharesBought);
                BuyActivity.this.finish();
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BuyActivity.this.finish();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(PricesUpdateMessageRec, new IntentFilter("SpecificPriceChange"));
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Execute.setEnabled(false);
                Execute.setTextColor(0xff000000);
            }
        }, new IntentFilter("DayEnded"));
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Execute.setEnabled(true);
                Execute.setTextColor(0xffffffff);
            }
        }, new IntentFilter("DayStarted"));

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
                LocalBroadcastManager.getInstance(BuyActivity.this).sendBroadcast(new Intent("SoundAltered").putExtra("sound", playSound));
                item.setChecked(playSound);
                break;
            case R.id.menu_backMain:
                BuyActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void UpdateTimeView(Daytime time){
        TextView DT = (TextView)findViewById(R.id.DaytimeInfo);
        DT.setText(time.DTtoString());
    }

    private BroadcastReceiver PricesUpdateMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Button Execute = (Button)findViewById(R.id.BuyButton);
            Execute.setEnabled(false);
            final TextView ShareAmount = (TextView)findViewById(R.id.ShareAmountDt);
            final TextView Cost = (TextView)findViewById(R.id.TotalValueDt);
            if(intent.getExtras().getInt("SID")==SID) {
                TextView SharePrice = (TextView) findViewById(R.id.ShareCurrPriDt);
                int price = intent.getExtras().getInt("newPrice");
                if(price%10==0)zerodigit="0";
                else zerodigit="";
                SharePrice.setText("$"+Double.toString(((double) price) / 100)+zerodigit);
                max = (int) Math.floor( money/price );
                max = Math.min(max, totalShares);
                if (amount > max) {
                    amount = max;
                }
                ShareAmount.setText(Integer.toString(amount));
                total = amount * price;
                if(total%10==0)zerodigit="0";
                else zerodigit="";
                Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
            }
            Execute.setEnabled(true);
        }
    };

    public void UpdateTopBar(TextView player, TextView daytime){
        if(money%10==0)zerodigit="0";
        else zerodigit="";
        String TBPlayer = "Lvl "+level+": $"+Double.toString((double)money/100)+zerodigit+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());
    }
}

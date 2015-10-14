package com.example.galadar.stockxchange;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SellActivity extends AppCompatActivity {

    int price;
    int max;
    int amount;
    int total;
    int SID;
    int money;

    //SharedPreferences Ownerships;
    //SharedPreferences sharedPref;
    MemoryDB DBHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        Context context = getApplicationContext();
        //sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        //Ownerships = context.getSharedPreferences(("com.example.galadar.stockxchange.Owner"), Context.MODE_PRIVATE);
        DBHandler = new MemoryDB(this);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        SID = data.getInt("SID");

        String name = DBHandler.getDBShareName(SID); //data.getString("name");
        price = DBHandler.getDBCurrPrice(SID); //data.getInt("price");
        max = 250; //data.getInt("owned");

        money = 10000; //sharedPref.getInt(getString(R.string.Player_Money), 1000000);

        TextView ShareName = (TextView)findViewById(R.id.ShareNameDt);
        ShareName.setText(name);
        TextView SharePrice = (TextView)findViewById(R.id.ShareCurrPriDt);
        SharePrice.setText(Double.toString(((double)price)/100));
        TextView SharesOwned = (TextView)findViewById(R.id.SharesOwnedDt);
        SharesOwned.setText(Integer.toString(max));

        final TextView ShareAmount = (TextView)findViewById(R.id.ShareAmountDt);
        final TextView Cost = (TextView)findViewById(R.id.TotalValueDt);

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
                //TODO Sell Shares
                //int temp = Ownerships.getInt(Integer.toString(SID), 0);
                //temp = temp-amount;

                //SharedPreferences.Editor editor = Ownerships.edit();
                //editor.putInt(Integer.toString(SID), temp);
                //editor.commit();

                //temp = money + total;
                //SharedPreferences.Editor editor1 = sharedPref.edit();
                //editor.putInt(getString(R.string.Player_Money), temp);
                //editor1.commit();

                DBHandler.close();
                SellActivity.this.finish();
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHandler.close();
                SellActivity.this.finish();
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sell, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.example.galadar.stockxchange;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private boolean playing = false;
    private boolean on = false;
    int UpdateInterval = 15;

    Finance f;
    MemoryDB DBHandler;

    String name;
    int level;
    int money;
    int assets;
    int fame;

    //SharedPreferences Ownerships;
    //SharedPreferences sharedPref;

//    int[] Prices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBHandler = new MemoryDB(this);

        Context context = getApplicationContext();
        //sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        //Ownerships = context.getSharedPreferences(("com.example.galadar.stockxchange.Owner"), Context.MODE_PRIVATE);

        //f = new Finance(5, Ownerships);
        //player = new Gamer("Bill", f.SharesList.length);

        if(DBHandler.numberOfShares()<1) {
            f = new Finance(DBHandler, 5);
        }

        String name = "Bill";

        money = (int)Math.round(Math.random()*1000000);

        /*
        if (!sharedPref.contains(getString(R.string.Player_Name))) {

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.Player_Name), name);
            editor.putInt(getString(R.string.Player_Level), 1);
            editor.putInt(getString(R.string.Player_Money), money);
            editor.putInt(getString(R.string.Player_Assets), 0);
            editor.putInt(getString(R.string.Player_Fame), 0);
            editor.commit();
            level = 1;
            assets = 0;
            fame = 0;
        } else {
            name = sharedPref.getString(getString(R.string.Player_Name), "Jack");
            level = sharedPref.getInt(getString(R.string.Player_Level), 1);
            money = sharedPref.getInt(getString(R.string.Player_Money), money);
            assets = sharedPref.getInt(getString(R.string.Player_Assets), 0);
            fame = sharedPref.getInt(getString(R.string.Player_Fame), 0);
        }
        */


        //Daytime time = new Daytime();


        //LinearLayout barLayout = (LinearLayout)findViewById(R.id.layout);

        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.layout);

        LayoutInflater layoutInflater = getLayoutInflater();
        View view;

        /*
        Names = new String[15];
        for(int i=0;i<Names.length;i++){
            Names[i]=randomName() ;
        }

        Prices = new int[15];
        for(int i=0;i<Prices.length;i++){
            Prices[i] = (int)Math.round((Math.random()*100000)+(Math.random()*1000));
        }

        */

        for(int i=0;i<DBHandler.numberOfShares();i++){
            view = layoutInflater.inflate(R.layout.main_share, parentLayout, false);
            LinearLayout shareData = (LinearLayout)view.findViewById(R.id.shareData);

            TextView shareInfo = (TextView)shareData.findViewById(R.id.shareInfo);
            shareInfo.setText(DBHandler.getDBShareName(i));
            shareInfo.setId(200000 + i);

            TextView sharePrices = (TextView)shareData.findViewById(R.id.sharePrice);
            sharePrices.setText(Double.toString(((double)DBHandler.getDBCurrPrice(i))/100));
            sharePrices.setId(100000 + i);

            Button Buy = (Button)shareData.findViewById(R.id.BuyButton);
            Buy.setText("Buy");
            Buy.setId(300000 + i);
            Button Sell = (Button)shareData.findViewById(R.id.SellButton);
            Sell.setText("Sell");
            Sell.setId(400000 + i);

            parentLayout.addView(shareData);

        }

        final Button upD = (Button)findViewById(R.id.pricesUpdate);

        upD.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Clicked Update Button", Toast.LENGTH_SHORT).show();

                int temp;
                int old;
                TextView sharePrices;

                for(int i=0;i<DBHandler.numberOfShares();i++){
                    sharePrices = (TextView)findViewById(100000 + i);

                    old = DBHandler.getDBCurrPrice(i);
                    DBHandler.DayCloseShare(i, old);
                    temp = (int)Math.round( Math.random() * 100000 );
                    DBHandler.setDBCurrPrice(i, temp);

                    if (temp>old){
                        sharePrices.setTextColor( 0xff00ff00 ); //Color green for price going up
                    } else if  (temp<old){
                        sharePrices.setTextColor( 0xffff0000 ); //Color red for price going down
                    } else {
                        sharePrices.setTextColor( 0xffffffff ); //Color white for price unchanged
                    }

                    sharePrices.setText(Double.toString( (double)temp/100 ));
                }
                Toast.makeText(getApplicationContext(), Double.toString((double)DBHandler.getDBCurrPrice(0)/100), Toast.LENGTH_SHORT).show();
            }

        });

        Button PlayerButton = (Button)findViewById(R.id.PlayerInfo);
        final String finalName = name;
        PlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PlayerInfoActivity.class);
                Bundle data = new Bundle();
                data.putString("name", finalName);
                data.putInt("money", money);
                data.putInt("assets", assets);
                data.putInt("level", level);
                i.putExtras(data);
                startActivity(i);
            }
        });

    }

    public void clickInfo(View v){

        Intent InfoIntent = new Intent(this, InfoActivity.class );
        startActivity(InfoIntent);

    }

    public void BuyClick(View v){
        Intent intent = new Intent(this, BuyActivity.class);
        Bundle data = new Bundle();
        //data.putString("name", DBHandler.getDBShareName(v.getId()-300000)); //f.SharesList[v.getId() - 300000].name);
        //data.putInt("price", DBHandler.getDBCurrPrice(v.getId() - 300000)); //f.SharesList[v.getId() - 300000].currentSharePrice);
        data.putInt("money", money);
        data.putInt("SID", (v.getId() - 300000) );
        intent.putExtras(data);
        startActivity(intent);
    }

    public void SellClick(View v){
        Intent intent = new Intent(this, SellActivity.class);
        Bundle data = new Bundle();
        //data.putString("name", f.SharesList[v.getId()-400000].name);
        //data.putInt("price", f.SharesList[v.getId() - 400000].currentSharePrice);
        //data.putInt("max", 250); //Ownerships.getInt(Integer.toString(v.getId() - 400000),0));
        data.putInt("SID", (v.getId() - 400000) );
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickPrice(View v){
        Intent intent = new Intent(this, ShareActivity.class);
        Bundle data = new Bundle();
        //data.putString("name", f.SharesList[v.getId()-100000].name);
        //data.putInt("price", f.SharesList[v.getId() - 100000].currentSharePrice);
        //data.putInt("owned", 250); //Ownerships.getInt(Integer.toString(v.getId() - 100000), 0));
        data.putInt("SID", (v.getId() - 100000) );
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickName(View v){
        Intent intent = new Intent(this, ShareActivity.class);
        Bundle data = new Bundle();
        //data.putString("name", f.SharesList[v.getId()-200000].name);
        //data.putInt("price", f.SharesList[v.getId() - 200000].currentSharePrice);
        //data.putInt("owned", 250); //Ownerships.getInt(Integer.toString(v.getId() - 200000), 0));
        data.putInt("SID", (v.getId() - 200000) );
        intent.putExtras(data);
        startActivity(intent);
    }
    private String randomName() {

        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final int N = alphabet.length();
        Random r = new Random();

        String value="";
        for (int i = 0; i < 4; i++) {
            value = value + (alphabet.charAt(r.nextInt(N)));
        }
        return value;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void ExitClicked(View view) {
        DBHandler.close();
        MainActivity.this.finish();
    }
}

package com.example.galadar.stockxchange;

import android.content.Intent;
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

    int[] Prices;
    String[] Names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Finance f = new Finance(5);
        //Gamer player = new Gamer("Bill", f.CompaniesList.length+f.HList.length+1);

        //Daytime time = new Daytime();


        //LinearLayout barLayout = (LinearLayout)findViewById(R.id.layout);

        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.layout);

        LayoutInflater layoutInflater = getLayoutInflater();
        View view;

        Names = new String[15];
        for(int i=0;i<Names.length;i++){
            Names[i]=randomName() ;
        }

        Prices = new int[15];
        for(int i=0;i<Prices.length;i++){
            Prices[i] = (int)Math.round((Math.random()*100000)+(Math.random()*1000));
        }

        for(int i=0;i<Names.length;i++){
            view = layoutInflater.inflate(R.layout.main_share, parentLayout, false);
            String Info;
            //Info =  f.SharesList[i].name + "   " + f.SharesList[i].currentSharePrice;

            LinearLayout shareData = (LinearLayout)view.findViewById(R.id.shareData);

            TextView shareInfo = (TextView)shareData.findViewById(R.id.shareInfo);
            shareInfo.setText(Names[i]);
            shareInfo.setId(200000 + i);

            TextView sharePrices = (TextView)shareData.findViewById(R.id.sharePrice);
            sharePrices.setText(Double.toString(((double)Prices[i])/100));
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
                Toast.makeText(getApplicationContext(),
                        "Clicked Update Button",
                        Toast.LENGTH_SHORT).show();

                double temp;
                TextView sharePrices;

                for(int i=0;i<Prices.length;i++){
                    sharePrices = (TextView)findViewById(100000 + i);
                    temp = Math.random()*100000;

                    if (temp>Prices[i]){
                        sharePrices.setTextColor( 0xff00ff00 ); //Color green for price going up
                    } else if  (temp<Prices[i]){
                        sharePrices.setTextColor( 0xffff0000 ); //Color red for price going down
                    } else {
                        sharePrices.setTextColor( 0xffffffff ); //Color white for price unchanged
                    }

                    Prices[i] = (int)Math.round(temp);
                    temp = ((double)Prices[i])/100;

                    sharePrices.setText(Double.toString( temp ));
                }
                //Toast.makeText(getApplicationContext(), Double.toString(temp), Toast.LENGTH_SHORT).show();
            }

        });

        Button PlayerButton = (Button)findViewById(R.id.PlayerInfo);
        PlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PlayerInfoActivity.class);
                i.putExtra("name", "Bill");
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
        data.putString("name", Names[v.getId()-300000]);
        data.putInt("price", Prices[v.getId() - 300000]);
        data.putInt("money", 2500000);
        intent.putExtras(data);
        startActivity(intent);
    }

    public void SellClick(View v){
        Intent intent = new Intent(this, SellActivity.class);
        Bundle data = new Bundle();
        data.putString("name", Names[v.getId()-400000]);
        data.putInt("price", Prices[v.getId() - 400000]);
        data.putInt("max", 2500);
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickPrice(View v){
        Intent intent = new Intent(this, ShareActivity.class);
        Bundle data = new Bundle();
        data.putString("name", Names[v.getId()-100000]);
        data.putInt("price", Prices[v.getId() - 100000]);
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickName(View v){
        Intent intent = new Intent(this, ShareActivity.class);
        Bundle data = new Bundle();
        data.putString("name", Names[v.getId()-200000]);
        data.putInt("price", Prices[v.getId() - 200000]);
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
}

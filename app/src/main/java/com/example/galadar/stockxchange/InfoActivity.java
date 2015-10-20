package com.example.galadar.stockxchange;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InfoActivity extends AppCompatActivity {

    MemoryDB DBHandler;
    Daytime time;
    boolean playSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        DBHandler = MemoryDB.getInstance(getApplicationContext());

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        time = data.getParcelable("DT");
        playSound =true;

        final int NumInfo = (int) Math.round(Math.random() * 8 + 7);

        final LinearLayout parentLayout = (LinearLayout) findViewById(R.id.layoutInfo);

        LayoutInflater layoutInflater = getLayoutInflater();
        View view;

        for (int i = 0; i < NumInfo; i++) {
            view = layoutInflater.inflate(R.layout.main_info, parentLayout, false);

            RelativeLayout info = (RelativeLayout) view.findViewById(R.id.infoData);
            info.setId(400000 + i);
            TextView date = (TextView) info.findViewById(R.id.info_date);
            TextView user = (TextView) info.findViewById(R.id.info_user);
            user.setText("U " + (i + 1));
            TextView body = (TextView) info.findViewById(R.id.info_body);


            parentLayout.addView(info);
        }

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);

        final Button upD = (Button) findViewById(R.id.UpdateInfo);

        upD.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        "Clicked Update Button",
                        Toast.LENGTH_SHORT).show();

                double rand;
                for (int i = 0; i < NumInfo; i += 2) {
                    parentLayout.removeView(findViewById(400000 + i));
                }
            }

        });

        final Button BackB = (Button)findViewById(R.id.BackButton);

        BackB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoActivity.this.finish();
            }
        });

    }

    public void UpdateTopBar(TextView player, TextView daytime){
        int money = DBHandler.getPlayerMoney();
        int level = DBHandler.getLevel();
        int assets = DBHandler.getAssets();
        String TBPlayer = "Lvl "+level+": $"+Double.toString(money/100)+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());

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
                DBHandler.setSound(playSound);
                item.setChecked(playSound);
                break;
            case R.id.menu_backMain:
                InfoActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
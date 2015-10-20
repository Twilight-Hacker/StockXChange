package com.example.galadar.stockxchange;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.BlockingDeque;

public class PlayerInfoActivity extends AppCompatActivity {

    MemoryDB DBHandler;
    boolean playSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_info);

        DBHandler = MemoryDB.getInstance(getApplicationContext());

        String name = "William";
        int money = DBHandler.getPlayerMoney(); //data.getInt("money");
        int assets = DBHandler.getAssets(); //data.getInt("assets");
        int level = DBHandler.getLevel(); //data.getInt("level");
        playSound =true;

        TextView NameView = (TextView)findViewById(R.id.PlayerNameDt);
        NameView.setText(name);

        TextView MoneyView = (TextView)findViewById(R.id.PlayerMoneyDt);
        MoneyView.setText(Double.toString((double)money / 100));

        TextView AssetsView = (TextView)findViewById(R.id.AssetsDt);
        AssetsView.setText(Integer.toString(assets));

        TextView LevelView = (TextView)findViewById(R.id.PlLevelDt);
        LevelView.setText(Integer.toString(level));


        Button Back = (Button)findViewById(R.id.OK);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerInfoActivity.this.finish();
            }
        });
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
                PlayerInfoActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}

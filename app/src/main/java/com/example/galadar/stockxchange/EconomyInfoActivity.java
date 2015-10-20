package com.example.galadar.stockxchange;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class EconomyInfoActivity extends AppCompatActivity {

    boolean playSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_economy_info);
        Bundle data = getIntent().getExtras();
        int EconSize = data.getInt("Economy_size");
        int totComp = data.getInt("TotalCompanies");
        int SumShares = data.getInt("SumOfShares");

        TextView EconomySize = (TextView)findViewById(R.id.EconomySizeDt);
        EconomySize.setText(Integer.toString(EconSize));
        TextView CompanySize = (TextView)findViewById(R.id.TotCompDt);
        CompanySize.setText(Integer.toString(totComp));
        TextView TotalShares = (TextView)findViewById(R.id.TotalSharesDt);
        TotalShares.setText(Integer.toString(SumShares));

        playSound = data.getBoolean("Sound");

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
                item.setChecked(playSound);
                break;
            case R.id.menu_backMain:
                EconomyInfoActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}

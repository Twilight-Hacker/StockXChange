package com.example.galadar.stockxchange;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MessagesActivity extends AppCompatActivity {

    static boolean playSound;
    static ArrayList MessagesText = new ArrayList();
    static int assets;
    static int level;
    static long money;
    static Daytime time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Bundle data = getIntent().getExtras();
        playSound = data.getBoolean("playsound");
        time = MainActivity.getClock();
        money = data.getLong("Pmoney");
        level = data.getInt("level");
        assets = data.getInt("assets");

        MessagesText.clear();

        MainActivity.EconomyState state = MainActivity.getEconomyState();
        if(state!= MainActivity.EconomyState.Normal){
            String str = "The economy is ";
            switch (state){
                case Boom:
                    str+="Booming. The goverment officials report that the economy is Booming, and great profit is to be made in the foreseable future, in all Industries.";
                    break;
                case Accel:
                    str+="Accelarating. The goverment officials report that the future of our economy is bright, and companies can expect above average profits.";
                    break;
                case Recess:
                    str+="destabilizing. The goverment officials report that the economy not doing very well, and companies should steer carefully to avoid losses.";
                    break;
                case Depres:
                    str+="in depression and goverment officials are discussing possible actions to help the economy recover.";
                    break;
            }
            MessagesText.add(str);
        }

        if(MainActivity.Events.size()!=0) {
            for (Event event : MainActivity.Events) {
                MessagesText.add(getMessageString(event.getType(), event.getMagnitude()));
            }
        } else {
            MessagesText.add("There are no Event Messages at this point.");
        }


        Button BackButton = (Button)findViewById(R.id.BackButton);
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessagesActivity.this.finish();
            }
        });

        ListView listview = (ListView)findViewById(R.id.MessageView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.text_simple_whiteonblack, R.id.WhiteOnBlack, MessagesText);
        listview.setAdapter(adapter);

/*
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MessagesActivity.this);
                builder.setTitle(Titles[position]);
                builder.setMessage(Messages[position]);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
*/

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);

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
        menu.findItem(R.id.menu_sound).setChecked(playSound);
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
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("SoundAltered").putExtra("playSound", playSound));
                item.setChecked(playSound);
                break;
            case R.id.menu_backMain:
                MessagesActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getMessageString(int type, int magnitude) {
        String str = "";

        str += "There are reports of ";
        if(magnitude>75)str+="a great ";
        else if(magnitude>=45)str+="a medium-sized " ;
        else str+="a small ";
        switch(type){
            case 1:
                str+="Earthquake in the area.";
                break;
            case 2:
                str+="Typhoon in the area.";
                break;
            case 3:
                str+="Explosion in the area.";
                break;
            case 4:
                str+="Riot in the area.";
                break;
            case 5:
                str+="enemy invasion in our territory.";
                break;
            default:
                str+="Earthquake in the area.";
                break;
        }
        str+=" The events will affect companies of multiple Industries.";

        return str;
    }

    public void UpdateTopBar(TextView player, TextView daytime){
        String zerodigit;
        if(money%10==0)zerodigit="0";
        else zerodigit = "";
        String TBPlayer = "Lvl "+level+": $"+Double.toString((double)money/100)+zerodigit+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());
    }

    public void UpdateTimeView(Daytime time){
        TextView DT = (TextView)findViewById(R.id.DaytimeInfo);
        DT.setText(time.DTtoString());
    }

}

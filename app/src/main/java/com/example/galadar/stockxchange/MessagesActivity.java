package com.example.galadar.stockxchange;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class MessagesActivity extends AppCompatActivity {

    boolean playSound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Bundle data = getIntent().getExtras();
        playSound = data.getBoolean("playsound");
        //TODO Retrieve Messages table from data Bundle and replace Dummy table
        final String[] Messages;
        final String[] Titles;
        int No = data.getInt("Number");

        if(No==0) {
            No = 12;
            Messages =  new String[No];
            Titles = new String[No];
            for (int i = 0; i < Messages.length; i++) {
                Titles[i] = "AVKVNDSKDJVN " + (i+1);
                Messages[i] = "akjcnskj jeffckhcbak ijaicban iancnddl " + (i+1);
            }
        } else {
            No--;
            Titles = new String[No];
            Messages = new String[No];
            String[] Titles2 = data.getStringArray("Titles");
            String[] Messages2 = data.getStringArray("Bodies");

            for(int i=0;i<Messages.length;i++){
                Titles[i] = Titles2[i];
                Messages[i] = Messages2[i];
            }
        }

        Button BackButton = (Button)findViewById(R.id.BackButton);
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessagesActivity.this.finish();
            }
        });

        ListView listview = (ListView)findViewById(R.id.MessageView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.text_simple_whiteonblack, R.id.WhiteOnBlack, Titles);
        listview.setAdapter(adapter);

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
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("SoundAltered").putExtra("playSound", playSound));
                item.setChecked(playSound);
                break;
            case R.id.menu_backMain:
                MessagesActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}

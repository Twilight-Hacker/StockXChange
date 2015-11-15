package com.example.galadar.stockxchange;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MeetingActivity extends Activity {

    static int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        Bundle data = getIntent().getExtras();
        String title = data.getString("title");
        final ArrayList speech = data.getStringArrayList("speech");

        MeetingActivity.this.setTitle(title);

        final TextView PartView = (TextView)findViewById(R.id.MessageArea);
        i=0;
        PartView.setText(speech.get(i).toString());

        final Button NextButton = (Button)findViewById(R.id.NextButton);
        final Button PrevButton = (Button)findViewById(R.id.PrevButton);
        PrevButton.setEnabled(i > 0);
        PrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i--;
                PartView.setText(speech.get(i).toString());
                PrevButton.setEnabled(i > 0);
                NextButton.setEnabled(true);
            }
        });

        NextButton.setEnabled(i < speech.size()-1);
        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i++;
                if(i==speech.size()){
                    MeetingActivity.this.finish();
                } else {
                    PartView.setText(speech.get(i).toString());
                    PrevButton.setEnabled(i > 0);
                    NextButton.setEnabled(i < speech.size());
                }
            }
        });

        Button CancelButton = (Button)findViewById(R.id.CancelButton);
        CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MeetingActivity.this.finish();
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

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }


}

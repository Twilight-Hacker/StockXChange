package com.example.galadar.stockxchange;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        final int NumInfo = (int) Math.round(Math.random() * 8 + 7);

        final LinearLayout parentLayout = (LinearLayout) findViewById(R.id.layoutInfo);

        LayoutInflater layoutInflater = getLayoutInflater();
        View view;

        for (int i = 0; i < NumInfo; i++) {
            view = layoutInflater.inflate(R.layout.main_info, parentLayout, false);

            RelativeLayout info = (RelativeLayout) view.findViewById(R.id.infoData);
            info.setId(400000 + i);
            TextView date = (TextView) info.findViewById(R.id.info_date); //(TextView)view.findViewById(R.id.shareInfo);
            TextView user = (TextView) info.findViewById(R.id.info_user);
            user.setText("U " + (i + 1));
            TextView body = (TextView) info.findViewById(R.id.info_body);


            parentLayout.addView(info);
        }

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
    }
}
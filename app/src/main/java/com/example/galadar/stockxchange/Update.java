package com.example.galadar.stockxchange;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

/**
 * Created by Galadar on 1/10/2015.
 */

public class Update extends IntentService {

    MemoryDB DBHandler;
    Daytime time;
    public static final String UPDATE_SERVICE_NAME = "com.galadar.StockXChange.UpdatePricesDB";

    public Update() {
        super("UpdatePricesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DBHandler = new MemoryDB(this);
        time = intent.getParcelableExtra("DT");
        boolean playing = true;
        while(playing){
            try {
                wait(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DBPriceUpdate();
        }

        time.increment(10);
        sendPriceUpdMessage();
    }

    private void DBPriceUpdate(){
        int temp;

        for(int i=0;i<DBHandler.getMaxSID();i++){
            temp = getNewSharePrice(DBHandler.getDBCurrPrice(i));
            DBHandler.setDBCurrPrice(i, temp);
        }
    }

    private void sendPriceUpdMessage() {
        Intent i = new Intent("DBPricesUpdated");
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    public int getNewSharePrice(int old){
        //TODO the real share price update function
        return (int)Math.round( Math.random() * 100000 );
    }

    public void interupt(){
        Update.this.stopService(new Intent(UPDATE_SERVICE_NAME));
    }

    public void resume(){
        Update.this.startService(new Intent(UPDATE_SERVICE_NAME));
    }

}
package com.example.galadar.stockxchange;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
Specific View IDs for editing textViews
SID: Share id: share identifier im Memory and DB, 0 to NumberOfCompanies-1
Share Name View: 200000+SID
Share Price View: 100000+SID
Share Buy Button: 300000+SID
Share Sell Button: 400000+SID
 */

public class MainActivity extends AppCompatActivity {

    public static Finance f;
    public static final int[] nextLevel = {0, 125000, 620000, 1250000, 5500000, 25000000, 0}; //To be printed as is, not /100.
    static Gamer p;
    static MemoryDB DBHandler;
    static Daytime time;
    static TextView topBarPlayer;
    static TextView topBarDaytime;
    static Thread upd;
    static int eventGen;
    static ArrayList<Event> Events = new ArrayList<>();
    static boolean playSound;
    static boolean dayOpen = false;
    static boolean gaming = false;
    static int infoGen;
    static long nextInvite;
    static ArrayList Infos = new ArrayList();
    static MediaPlayer soundplayer;

    public enum EconomyState{Normal, Accel, Boom, Recess, Depres}
    static EconomyState state;
    static String[] News = new String[2]; //News[0] is for title, News[1] is for body (~250 characters)
    static int NewsPriority = 0; //Something with lower news priority cannot change the news. There is only one news at a time. Priority: min=0, max=100.
    static boolean fullGame;

    public static EconomyState getEconomyState(){
        return state;
    }

    public static Finance getFinance(){
        return f;
    }

    public static Daytime getClock(){
        return time;
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateTopBar(topBarPlayer, topBarDaytime);
    }


    public int getNextLevelPreq(){
        return nextLevel[p.getLevel()];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBHandler = new MemoryDB(this);
        fullGame=true;

        if(DBHandler.getMaxSID()<1) {
            time = new Daytime(LocalBroadcastManager.getInstance(this.getApplicationContext()));
            DBHandler.PrepGame(0, Company.Sectors.values());
            f = new Finance(DBHandler, 5);
            p = new Gamer(DBHandler);
        } else {
            time = new Daytime(LocalBroadcastManager.getInstance(this.getApplicationContext()), DBHandler.getTerm(), DBHandler.getDay());
            f = new Finance(DBHandler);
            p = new Gamer(DBHandler.getPlayerMoney(), DBHandler.getLevel(), DBHandler.getAssets(), DBHandler.getFame());
            Toast.makeText(this,"Game Loaded", Toast.LENGTH_SHORT).show();
        }

        playSound = DBHandler.PlaySound();
        infoGen=0;
        eventGen=DBHandler.getEventGen();
        String q = "There are no info tips at this point";
        Infos.add(q);

        state = getEconomyState(f.getSectorOutlook(0));

        switch (state){ //SetUp Current GGE
            case Boom:
                for(int i=1;i<f.getNumOfOutlooks();i++){
                    f.setSectorEventOutlook(i, 1);
                }
                break;
            case Accel:
                for(int i=1;i<f.getNumOfOutlooks();i++){
                    f.setSectorEventOutlook(i, 0.5);
                }
                break;
            case Recess:
                for(int i=1;i<f.getNumOfOutlooks();i++){
                    f.setSectorEventOutlook(i, -0.5);
                }
                break;
            case Depres:
                for(int i=1;i<f.getNumOfOutlooks();i++){
                    f.setSectorEventOutlook(i, -1);
                }
                break;
            default:
                break;
        }

        if(fullGame)Events=DBHandler.retrieveEvents(time.totalDays());
        else {Events = new ArrayList<>();}

        if(Events.size()!=0) {
            for (Event event : Events) {
                alterOutlooks(event.getType(), event.getMagnitude());
            }
        }
        //Retrieve / Generate invitation, event and other messages

        topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);

        UpdateTopBar(topBarPlayer, topBarDaytime);

        UpdateCentralUI();

        if(fullGame)DBHandler.setEconomySize(f.calcEconomySize());

        //New thread update time
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                boolean running = true;
                try {
                    while (running) {
                        synchronized (this) {
                            //TODO change wait from 5000ms to 10000ms (10 seconds)
                            Thread.sleep(5000);
                            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent("TimeForwarded"));
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        upd = new Thread(r);

        if(fullGame)nextInvite = DBHandler.getNextInviteTime();
        else nextInvite = 0;

        if(nextInvite==0){
            if(fullGame)DBHandler.setNextInviteTime(System.currentTimeMillis()+82800000);
        } else if(System.currentTimeMillis()>=nextInvite){
            callInvite(p.getLevel());
            DBHandler.setNextInviteTime(System.currentTimeMillis() + 82800000);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(DayStartedMessageRec, new IntentFilter("DayStarted"));
        LocalBroadcastManager.getInstance(this).registerReceiver(SharesTransactionedRec, new IntentFilter("SharesTransaction"));
        LocalBroadcastManager.getInstance(this).registerReceiver(SharesShortTransactionedRec, new IntentFilter("SharesShortTransaction"));
        LocalBroadcastManager.getInstance(this).registerReceiver(SpecificElementUpdate, new IntentFilter("SpecificPriceChange"));
        LocalBroadcastManager.getInstance(this).registerReceiver(DayEndedMessageRec, new IntentFilter("DayEnded"));
        LocalBroadcastManager.getInstance(this).registerReceiver(TermEndedMessageRec, new IntentFilter("TermEnded"));
        LocalBroadcastManager.getInstance(this).registerReceiver(SoundAlteredRec, new IntentFilter("SoundAltered"));
        LocalBroadcastManager.getInstance(this).registerReceiver(LeveledUp, new IntentFilter("LevelUp"));
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                callforMeetings();
                resetNews();
            }
        }, new IntentFilter("DayReset"));

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(playSound){
                    soundplayer = MediaPlayer.create(MainActivity.this, R.raw.bell);
                    soundplayer.start();
                }
            }
        }, new IntentFilter("RingBell"));


        LocalBroadcastManager.getInstance(this).registerReceiver(AdvanceTime, new IntentFilter("TimeForwarded"));

        gaming = true;
        upd.start();
    }


    private EconomyState getEconomyState(double EconomyOutlook) {
        if(EconomyOutlook>0.75)return EconomyState.Boom;
        if(EconomyOutlook>0.5)return EconomyState.Accel;
        if(EconomyOutlook<-0.5)return EconomyState.Recess;
        if(EconomyOutlook<-0.75)return EconomyState.Depres;
        return EconomyState.Normal;
    }

    private void resetNews(){
        News = new String[2];
        News[0] = "All is well";
        News[1] = "Nothing to report";
        NewsPriority = 1;
    }

    public void editNews(int priority, String title, String body){
        if(priority>NewsPriority){
            NewsPriority=priority;
            News[0]=title;
            News[1]=body;
        }
    }

    private void callInvite(int level) {
        final int cost = level*(int)Math.round(Math.random() * 300 + 700);
        final int reward = level*(int)Math.round(Math.random() *3 + 7);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Invitation to a party");
        String q = "You received an Invitation to a party.\nAttending will cost $"+cost+".00 and you will earn "+(double)reward/100+" assets.\n\nWhat do you want to do?";
        builder.setMessage(q);

        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if(fullGame) {
                    DBHandler.incAssets((double)reward/100);
                    p.setAssets(DBHandler.getAssets());
                    p.alterMoney(cost * 100);
                    DBHandler.setPlayerMoney(p.getMoney());
                    UpdateTopBar(topBarPlayer, topBarDaytime);
                }
                dialog.dismiss();
            }

        });

        builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void NewsLoad(View v){
        Intent intent = new Intent(MainActivity.this, NewsActivity.class);
        Bundle data = new Bundle();
        data.putStringArray("news", News);
        intent.putExtras(data);
        startActivity(intent);
    }

    private void callforMeetings(){
        gaming=false;

        List<Meeting> MeetingsList;
        MeetingXMLParser MeetingsParser = new MeetingXMLParser();
        try {
            MeetingsList = MeetingsParser.parse(this.getResources().openRawResource(R.raw.meetings));
        } catch (Exception e) {
            e.printStackTrace();
            android.os.Process.killProcess(android.os.Process.myPid());
            return;
        }

        for (int i = 0; i < MeetingsList.size(); i++) {
            if( MeetingsList.get(i).getMeetingDay()==time.totalDays() ){
                LaunchMeeting(MeetingsList.get(i).getMeetingTitle(), MeetingsList.get(i).getMeetingSpeech());
            }
        }

        gaming=true;
    }

    private void LaunchMeeting(String title, ArrayList speech) {
        Intent intent = new Intent(MainActivity.this, MeetingActivity.class);
        Bundle data = new Bundle();
        data.putStringArrayList("speech", speech);
        data.putString("title", title);
        intent.putExtras(data);
        startActivity(intent);
    }


    private void UpdateCentralUI() {
        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.layout);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view;
        int intprice;
        double dprice;
        parentLayout.removeAllViews();
        for(int i=0;i<f.getNumComp();i++){
            view = layoutInflater.inflate(R.layout.main_share, parentLayout, false);
            LinearLayout shareData = (LinearLayout)view.findViewById(R.id.shareData);

            TextView shareInfo = (TextView)shareData.findViewById(R.id.shareInfo);
            shareInfo.setId(200000 + i);
            shareInfo.setText(f.getName(i));
            intprice = f.getShareCurrPrince(i);
            dprice = (double)intprice/100;
            String zerodigit = "";
            if(intprice%10==0){zerodigit="0";}
            TextView sharePrices = (TextView)shareData.findViewById(R.id.sharePrice);
            sharePrices.setId(100000 + i);
            sharePrices.setText(Double.toString(dprice) + zerodigit);

            Button Buy = (Button)shareData.findViewById(R.id.BuyButton);
            Buy.setId(300000 + i);
            Buy.setText("Buy");
            Buy.setEnabled(dayOpen&(p.getMoney()>0));
            Button Sell = (Button)shareData.findViewById(R.id.SellButton);
            Sell.setId(400000 + i);
            Sell.setText("Sell");
            if((f.getSharesOwned(i)>0) & dayOpen) {
                Sell.setEnabled(true);
                Sell.setTextColor(0xffffffff);
            } else if(p.getLevel()>=4 & f.getTodaysShort(i)==0 & dayOpen){
                Sell.setEnabled(true);
                Sell.setTextColor(0xffff0000); //Red Color for short positions
            } else {
                Sell.setEnabled(false);
                Sell.setTextColor(0xff000000);
            }

            parentLayout.addView(shareData);
        }
    }

    public String ClocktoString(){
        return time.DTtoString();
    }

    private BroadcastReceiver AdvanceTime = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if(gaming) {
                time.increment(10);
                UpdateTimeView(ClocktoString());
                callforTransactions();
                callInfoGen();
            }
        }
    };

    private void callInfoGen() {
        infoGen += Math.random()*0.1*(1+p.getAssets());
        if(infoGen>=1){
            infoGen=0;
            String str=getNewInfo();
            if(MainActivity.Infos.contains("There are no info tips at this point"))MainActivity.Infos.remove("There are no info tips at this point");
            if(!MainActivity.Infos.contains(str))MainActivity.Infos.add(str);
        }
    }

    private String getNewInfo() {
        String info="";

        Random r = new Random();
        int user = 1000+r.nextInt(5000);
        int type;
        if(r.nextDouble()<0.25) type=1;
        else type=0; //0: share, 1: Scams.
        boolean truth = r.nextDouble() >= 0.25;

        int reference = r.nextInt(f.getNumComp());
        info+="USER: "+user+": ";
        info+="The "+f.getName(reference)+" company ";

        switch (type){

            case 1:
                if(truth){
                    if(f.isScam(reference)) {
                        info+="is involved in illegal activities";
                    } else {
                        info+="is not involved in illegal activities";
                    }
                } else { //Just the inverse of the above, because the info is a lie
                    if(f.isScam(reference)) {
                        info+="is not involved in illegal activities";
                    } else {
                        info+="is involved in illegal activities";
                    }
                }
                break;

            default:
                double a = f.getCompOutlook(reference)+f.getSectorOutlook(reference)+((double)f.getRemShares(reference)/f.getTotalShares(reference))-0.5;
                if(truth){
                    if(a>0) {
                        info+="share price is expected to increase";
                    } else {
                        info+="share price is expected to decrease";
                    }
                } else { //Just the inverse of the above, because the info is a lie
                    if(a<=0) {
                        info+="share price is expected to increase";
                    } else {
                        info+="share price is expected to decrease";
                    }
                }
        }
        return info;
    }

    public static String addCertainInfo(int reference, int user){
        //These infos are always true. They are to be used for Scams.
        //For Asset info use function below
        //This automatically adds the info to Infos List;

        String info ="";
        if(reference>f.getNumComp()) return info;
        info+="USER: "+user+": ";

        info+="The "+f.getName(reference)+" company ";

        double a = f.getCompOutlook(reference)+f.getSectorOutlook(reference)+((double)f.getRemShares(reference)/f.getTotalShares(reference))-0.5;
        if(a>0) {
            info+="share price is expected to increase";
        } else {
            info+="share price is expected to decrease";
        }
        if(MainActivity.Infos.contains("There are no info tips at this point"))MainActivity.Infos.remove("There are no info tips at this point");
        if(!MainActivity.Infos.contains(info)){
            MainActivity.Infos.add(info);
            return info;
        } else {
            return addCertainInfo(reference, user);
        }
    }

    public static String addAssetInfo(){
        //These info are always true.
        //This removes an asset from DB and player
        //This automatically adds the info to Infos List, and then returns it;

        p.setAssets(p.getAssets()-1);
        if(fullGame)DBHandler.setAssets(p.getAssets());

        String info ="";
        Random r = new Random();
        int reference = r.nextInt(f.getNumComp());
        info+="USER: 9001: ";

        info+="The "+f.getName(reference)+" company ";

        if(f.isScam(reference)) {
            info += "is related to shady activities.";
            if (Infos.contains(info)) return addCertainInfo(reference, 9001);
            if (MainActivity.Infos.contains("There are no info tips at this point"))
                MainActivity.Infos.remove("There are no info tips at this point");
            MainActivity.Infos.add(info);
            return info;
        }

        double a = f.getCompOutlook(reference)+f.getSectorOutlook(reference)+((double)f.getRemShares(reference)/f.getTotalShares(reference))-0.5;
        if(a>0) {
            info+="share price is expected to increase";
        } else {
            info+="share price is expected to decrease";
        }
        if(MainActivity.Infos.contains("There are no info tips at this point"))MainActivity.Infos.remove("There are no info tips at this point");
        if(!MainActivity.Infos.contains(info)){
            MainActivity.Infos.add(info);
            return info;
        } else {
            return addAssetInfo(); //If the info is already given, get new info instead (recursive call)
        }
    }

    private BroadcastReceiver DayStartedMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Random random = new Random();
            for (int i = 0; i < f.getNumComp(); i++) {
                if(f.getTodaysShort(i)>0){
                    p.alterMoney(f.getTodaysShort(i)*f.getShareCurrPrince(i));
                    if(fullGame)DBHandler.setPlayerMoney(p.getMoney());
                }
            }
            if(fullGame)DBHandler.ShortSettle(time.totalDays());
            for (int i = 0; i < f.getNumComp(); i++) {
                if(f.getShortRemainingDays(i)<0){
                    f.ShortShare(i, DBHandler.getShortAmount(i), DBHandler.getShortDays(i));
                }
            }
            eventGen += random.nextInt(100);
            if(eventGen>=1000){
                generateEvent();
                eventGen=0;
            }
            if(fullGame)DBHandler.setEventGen(eventGen);
            dayOpen = true;
            UpdateCommandsUI();
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent("RingBell"));

            //Scams are resolved after Day opens
            for (int i = 0; i < f.getScamsNo(); i++) {
                switch (f.getScamType(i)){
                    case 1:     //Empty Room (set share price to 0.1, and remaining shares to -total, revenue to - total value)
                        if(f.getScamRemDays(i)==0){
                            f.Backrupt(i);//Bunctrupt company
                            if(fullGame)DBHandler.setDBCurrPrice(i, f.getShareCurrPrince(i));
                            if(fullGame)DBHandler.setCompTotValue(f.getName(i), f.getCompTotalValue(i));
                            f.removeScam(i);
                            String story = "Company "+f.getName(i)+" suddendly declared bankruptcy today.\n\nThe authorities state that the Company was found to be involved in shady activities, and the CEO was placed under arrest.";
                            editNews(55, "Company Bankrupts", story);
                        }
                        break;
                    case 2:     //Pump and Dump
                        if(f.getScamRemDays(i)<=5){
                            f.setCompOutlook(i, f.getCompOutlook(i)+5);
                        }
                        if(f.getScamRemDays(i)==0){
                            Intent intent1 = new Intent("SharesTransaction");
                            Bundle data1 = new Bundle();
                            data1.putInt("SID", i);
                            data1.putInt("amount", random.nextInt(2000)+1857);
                            data1.putInt("atPrice", f.getShareCurrPrince(i));
                            data1.putBoolean("ByPlayer", false);
                            f.setCompOutlook(i, f.getCompOutlook(i) - 25);
                            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent1);
                            f.removeScam(i);
                            String story = "A Pump and Dump scheme was executed today, leaving many stockbrokers in a tight situation, counting losses.\n\nLaw enforcement agencies are conducting an investigation to locate the person or persons responsible.";
                            editNews(40, "Stock Scheme leaves brokers in despair", story);
                        }
                        break;
                    case 3:     //Short and Distort
                        if(f.getScamRemDays(i)<=3){
                            f.setCompOutlook(i, f.getCompOutlook(i)-5);
                        }
                        if(f.getScamRemDays(i)==0){
                            f.setCompOutlook(i, f.getCompOutlook(i) + 15);
                            f.alterRemShares(i, 1581 + random.nextInt(1500));
                            if(fullGame)DBHandler.setCompOutlook(f.getName(i), f.getCompOutlook(i));
                            if(fullGame)DBHandler.setRemShares(i, f.getRemShares(i));
                            f.removeScam(i);
                            String story = "A Short and Distort scheme was executed today, leaving "+ f.getName(i) +" in a tight situation, counting losses.\n\nLaw enforcement agencies are conducting an investigation to locate the person or persons responsible.";
                            editNews(45, "A company in distress", story);
                        }
                        break;
                    case 4:     //Ponzi Scheme (set share price to 0.1, and remaining shares to -total, revenue to - total value)
                        if(f.getScamRemDays(i)==0){
                            f.Backrupt(i);//Bunctrupt company
                            if(fullGame)DBHandler.setDBCurrPrice(i, f.getShareCurrPrince(i));
                            if(fullGame)DBHandler.setCompTotValue(f.getName(i), f.getCompTotalValue(i));
                            f.removeScam(i);
                            String story = "Company "+f.getName(i)+" suddendly declared bankruptcy today.\n\nThe authorities state that the Company was found to be involved in a Ponzi Scheme, and the CEO was placed under arrest.";
                            editNews(50, "Company Bankrupts", story);
                        }
                        break;
                    case 5:     //Lawbreaker Scandal
                        if(f.getScamRemDays(i)==0){
                            int magnitude = getLinnearRN(9);

                            f.setCompOutlook(i, f.getCompOutlook(i) - magnitude * 0.1);
                            if(magnitude>3){
                                f.UpdateCompRevenue(i, -1000000*magnitude);
                            }
                            f.removeScam(i);
                            String story = "Company "+f.getName(i)+" was found to be in violation of multiple laws and regulations.\n\nThe results of the investigation, published by authorities, indicate corruption at the highest levels of the company, and the govemnment has already taken action.";
                            editNews(30+magnitude*10, "Finance world shaken", story);
                        }
                        break;
                    default:    //Do nothing, since category/type is 0, thus no Scams
                        break;
                }
            }
            Toast.makeText(MainActivity.this, "Day Started", Toast.LENGTH_SHORT).show();
        }
    };

    private void generateEvent() {
        Random random = new Random();
        int type = random.nextInt(5)+1;
        int magnitude = random.nextInt(70)+31;
        Event event = new Event(type, magnitude);
        Events.add(event);
        if(fullGame)DBHandler.addEvent(event, time.totalDays(event.getDuration()));
        alterOutlooks(event.getType(), event.getMagnitude());
    }

    public void alterOutlooks(int type, int magnitude){
        switch (type){
            case 1: //Earthquake
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Constr"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Defence"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Educ"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Entert"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Food"), -2*(double)magnitude/100);
                editNews(45+(int)Math.round((double)magnitude/10), "Earthquake", "An EarthQuake of Magnitude "+(double)magnitude/10+" has been reported.");
                break;
            case 2: //Typhoon
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Constr"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Telecom"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Oil"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Transp"), -2*(double)magnitude/100);
                editNews(50+(int)Math.floor(magnitude/20), "Typhoon", "A Typhoon of Category "+(int)Math.floor(magnitude/20)+" has been reported");
                break;
            case 3: //Explosion
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Constr"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Defence"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Tech"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Entert"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Tourism"), -2*(double)magnitude/100);
                editNews(15+magnitude, "Explosion", "An explosion has been reported. The authorities are investigating the causes");
                break;
            case 4: //Riots
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Defence"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Telecom"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Tech"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Food"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Oil"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Entert"), -2*(double)magnitude/100);
                editNews(10+(int)Math.round(magnitude/2), "Riots", "There are reports of riots in the city center. Citizens are advised to avoid the area.");
                break;
            case 5: //War
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Defence"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Tech"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Oil"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex( "Constr"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex( "Transp"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex( "Food"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex( "Telecom"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex( "Entert"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex( "Educ"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex( "Tourism"), -2*(double)magnitude/100);
                editNews(150, "WAR", "An invasion on our territory signals the start of a War. All armed forces have mobilized to deal with the invasion.");
                break;
            default: //Unknown event, assume eartquake
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Constr"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Defence"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Educ"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Entert"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Food"), -2*(double)magnitude/100);
                editNews(45 + (int) Math.round((double) magnitude / 10), "Earthquake", "An EarthQuake of Magnitude " + (double) magnitude / 10 + " has been reported.");
        }
    }

    public static int getLinnearRN(int maxSize){
        //Get a linearly multiplied random number
        int randomMultiplier = maxSize * (maxSize + 1) / 2;
        Random r=new Random();
        int randomInt = r.nextInt(randomMultiplier);

        int linearRN = 0;
        for(int i=maxSize; randomInt >= 0; i--){
            randomInt -= i;
            linearRN++;
        }

        return linearRN;
    }

    private BroadcastReceiver LeveledUp = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            p.setLevel(p.getLevel()+1);
            DBHandler.setLevel(p.getLevel());
            long priceMoney=0;
            int prizeAssets=p.getAssets();
            //TODO add tutorial calls here
            switch (p.getLevel()){
                case 2: {
                    priceMoney=-25000;
                    prizeAssets+=2;
                    setUpGGEs(state, 1);
                    break;
                }
                case 3: {
                    priceMoney=-120000;
                    prizeAssets+=2;
                    break;
                }
                case 4: {
                    priceMoney=-250000;
                    prizeAssets+=3;
                    break;
                }
                case 5: {
                    priceMoney=-500000;
                    prizeAssets+=4;
                    break;
                }
                case 6: {
                    prizeAssets+=6;
                    break;
                }
                default:
                    break;
            }

            p.alterMoney(priceMoney*100);
            p.setAssets(prizeAssets);
        }
    };

    private void callforTransactions() {
        if(!dayOpen) return;
        int temp;
        for(int i=0;i<f.getNumComp();i++) {
            if (f.getShareCurrPrince(i) != 0) {
                temp = getSharesAmount(f.getCompOutlook(i), f.getSectorOutlook(f.getCompSectorInt(i)), f.getRemShares(i), f.getTotalShares(i));
                if (temp != 0) {
                    Intent intent = new Intent("SharesTransaction");
                    Bundle data = new Bundle();
                    data.putInt("SID", i);
                    data.putInt("amount", temp);
                    data.putInt("atPrice", f.getShareCurrPrince(i));
                    data.putBoolean("ByPlayer", false);
                    intent.putExtras(data);
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                }
            }
        }
    }

    public int getSharesAmount(double C, double S, int remaining, int total){
        Random random = new Random();
        double perOwned = (double)remaining/total;

        double determinant = random.nextDouble()*2-1.5 + perOwned + (2*C+S)/10;
        boolean temp = random.nextBoolean()||(Math.abs(determinant)<0.3);

        if(temp) {
            //transact only half of the shares at a time, so as not to overwhelm user with changes
            return 0;
        } else {
            double am = Math.min(Math.abs(random.nextGaussian()), 3)*determinant*100;
            if(Math.abs(am)<=20) am = random.nextInt(50)+20;
            int amount = (int)Math.round(am);
            return amount;
        }
    }

    private BroadcastReceiver SharesTransactionedRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            int SID = data.getInt("SID");
            int amount = data.getInt("amount");
            int oldPrice = data.getInt("atPrice");
            boolean byPlayer = data.getBoolean("ByPlayer");

            int remaining = f.getRemShares(SID);
            int total = f.getTotalShares(SID);

            if(byPlayer){
                f.TransactShares(SID, amount);
                p.alterMoney(amount*oldPrice);
                if(amount>0){
                    if(fullGame)DBHandler.BuyShare(SID, amount, p.getMoney());
                } else {
                    if(fullGame)DBHandler.SellShare(SID, amount, p.getMoney());
                }
            }

            f.alterRemShares(SID, amount);
            if(fullGame)DBHandler.setRemShares(SID, f.getRemShares(SID));

            double value = (double)f.getCompTotalValue(SID)/100;
            int max = (int)Math.round( value/total );
            int newPrice = oldPrice + (int) Math.round( (1 - (double)remaining/total )*max + Math.random()*50+10 );

            f.setShareCurrPrice(SID, newPrice);
            if(fullGame)DBHandler.setDBCurrPrice(SID, newPrice);

            f.UpdateCompRevenue(SID, newPrice - oldPrice);
            if(fullGame)DBHandler.setCompRevenue(SID, f.getCompRevenue(SID));

            Intent intent1 = new Intent("SpecificPriceChange");
            Bundle data1 = new Bundle();
            data1.putInt("SID", SID);
            data1.putInt("newPrice", newPrice);
            data1.putInt("oldPrice", oldPrice);
            data1.putBoolean("PlayerOwner", f.getSharesOwned(SID)>0);
            intent1.putExtras(data1);
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent1);
        }
    };

    private BroadcastReceiver SharesShortTransactionedRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            int SID = data.getInt("SID");
            int amount = data.getInt("amount");
            int oldPrice = data.getInt("atPrice");
            boolean byPlayer = data.getBoolean("ByPlayer");
            int days = time.totalDays(data.getInt("Days"));

            f.ShortShare(SID, amount, days);
            p.alterMoney(amount * oldPrice);
            if(fullGame)DBHandler.ShortShare(SID, amount, time.totalDays(days), p.getMoney());

            Intent SharesSold = new Intent("SharesTransaction"); //To update prices
            Bundle Sdata = new Bundle();
            Sdata.putInt("SID", SID);
            Sdata.putInt("amount", (int)Math.round((double)amount / 2));
            Sdata.putInt("atPrice", oldPrice);
            Sdata.putBoolean("ByPlayer", byPlayer);
            SharesSold.putExtras(Sdata);
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(SharesSold);


        }
    };

    private BroadcastReceiver SpecificElementUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            int SID = data.getInt("SID");
            int price = data.getInt("newPrice");
            int oldPrice = data.getInt("oldPrice");
            boolean sellactive = data.getBoolean("PlayerOwner");

            String zerodigit = "";
            if(price%10==0){zerodigit="0";}
            TextView sharePrices = (TextView)findViewById(100000 + SID);
            if(price>oldPrice) {
                sharePrices.setTextColor(0xff00ff00); //Color green for price going up
            } else if (price<oldPrice) {
                sharePrices.setTextColor(0xffff0000); //Color red for price going down
            } else {
                sharePrices.setTextColor(0xffffffff); //Color white for price unchanged
            }
            sharePrices.setText(Double.toString((double)price/100) + zerodigit);

            Button Sell = (Button)findViewById(400000 + SID);
            Sell.setEnabled(sellactive & dayOpen);
            if(sellactive){
                Sell.setTextColor(0xffffffff);
            } else {
                Sell.setTextColor(0xff000000);
            }
        }
    };

    private BroadcastReceiver SoundAlteredRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playSound = !playSound;
            if(fullGame)DBHandler.setSound(playSound);
        }
    };

    public void UpdateTimeView(String str){
        TextView DT = (TextView)findViewById(R.id.DaytimeInfo);
        DT.setText(str);
    }

    public static void callScam(int cid) {
        if(f.isScam(cid)){
            f.removeScam(cid);
            if(fullGame)DBHandler.removeScam(cid);
        } else {
            p.setAssets(p.getAssets()-1);
            if(fullGame)DBHandler.setAssets(p.getAssets());
        }
    }

    private BroadcastReceiver DayEndedMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dayOpen = false;
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent("RingBell"));
            f.DayCloseShares();
            if(fullGame) {
                for (int i = 0; i <= DBHandler.getMaxSID(); i++) {
                    DBHandler.DayCloseShare(i, f.getLastClose(i));
                }
            }

            for(Event event:Events){
                event.dayEnded();
                if(event.eventEnded()){
                    alterOutlooks(event.getType(), -event.getMagnitude());
                    Events.remove(event);
                }
            }

            if(fullGame)DBHandler.ClearCompleteEvents(time.totalDays());

            if(time.totalDays()%3==0) {//Infos cleared every 2 days
                Infos.clear();
                Infos.add("There are no info tips at this point");
            }

            if((p.getMoney()+f.NetWorth())<-10000000) {
                gaming = false;
                if (p.getAssets() > 0) {
                    p.setAssets(p.getAssets() - 1);
                    if(fullGame)DBHandler.setAssets(p.getAssets());
                    p.setMoney(2500000);
                    if(fullGame)DBHandler.setPlayerMoney(p.getMoney());
                    Toast.makeText(MainActivity.this, "Used Asset to escape Bankrupty", Toast.LENGTH_SHORT).show();
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle("GAME OVER");
                    String q = "You have been arrested after amassing a tremendous amount of debt. \n\n You have lost the game.";
                    builder.setMessage(q);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            if(fullGame) {
                                DBHandler.clearData();
                                DBHandler.PrepGame(0, Company.Sectors.values());
                                f = new Finance(DBHandler, 4);
                                UpdateCentralUI();
                                p = new Gamer(DBHandler);
                                time = new Daytime(LocalBroadcastManager.getInstance(MainActivity.this.getApplicationContext()));
                                DBHandler.setEconomySize(f.calcEconomySize());
                            }
                            dialog.dismiss();
                            if(!fullGame)ExitClicked();
                        }

                    });

                    AlertDialog d = builder.create();
                    d.show();
                }
            } else {
                for (int i = 0; i < f.getNumComp(); i++) {
                    if(fullGame)DBHandler.DayCloseShare(i, f.getLastClose(i));
                }
                //Updating DB to show the NEXT day (and possibly term) than the one Just ENDED
                if (time.getDay() == 60) {
                    time.nextTerm();
                    if(fullGame)DBHandler.storeDay(1, time.getTerm());
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent("TermEnded"));
                } else {
                    if(fullGame)DBHandler.setDay(time.getDay() + 1);
                }
            }
            UpdateCommandsUI();
            gaming = true;
            if(p.getLevel()==5&(p.getMoney())/100>25000000){
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent("LevelUp"));
            }
        }
    };

    //RUN ONLY ON UI THREAD
    public void UpdateCommandsUI(){
        Button B;
        try{
            for (int i = 0; i < f.getNumComp(); i++) {
                B = (Button) findViewById(300000 + i);
                B.setEnabled(dayOpen & (p.getMoney()>0));
                B = (Button) findViewById(400000 + i);
                B.setEnabled(dayOpen & (f.getSharesOwned(i) > 0));
                if (B.isEnabled()) {
                    B.setTextColor(0xffffffff);
                } else if(p.getLevel()>=4 & f.getTodaysShort(i)==0 & dayOpen){
                    B.setEnabled(true);
                    B.setTextColor(0xffff0000); //Red Color for short positions
                }  {
                    B.setTextColor(0xff000000);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void MessagesLoad(View v){
        Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
        Bundle data = new Bundle();
        data.putBoolean("playsound", playSound);
        intent.putExtras(data);
        startActivity(intent);
    }

    public void EconomyInfoLaunch(View v){
        Intent intent = new Intent(MainActivity.this, EconomyInfoActivity.class);
        Bundle data = new Bundle();
        data.putBoolean("Sound", playSound);
        data.putLong("Economy_size", f.getEconomySize());
        data.putInt("TotalCompanies", f.getNumComp());
        data.putInt("SumOfShares", f.getSumShares());
        intent.putExtras(data);
        startActivity(intent);
    }

    private BroadcastReceiver TermEndedMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            gaming = false;

            ProgressDialog progdialog = new ProgressDialog(MainActivity.this);
            progdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progdialog.setTitle(R.string.TermEndDialogTitle);
            progdialog.setMessage(getText(R.string.TermEndDialogText));
            progdialog.setCancelable(false);
            progdialog.setMax(100);
            progdialog.setProgress(0);
            progdialog.show();

            //new companies added directly to DB (temp table for QuickGame)

            double PreveconomyOutlook=f.getSectorOutlook(0);

            int revenue, TotVal, newInv, oldPerc, newPerc, newFame;
            double divident;
            Random r = new Random();
            int newCompCounter=0;
            for (int i = 0; i < f.getNumComp(); i++) {
                revenue = f.getCompRevenue(i);
                TotVal = f.getCompTotalValue(i);

                double marketShare;
                if(fullGame)marketShare=DBHandler.getCompMarketShare(f.getName(i));
                else marketShare = Math.random()*0.5;

                revenue += (int)Math.round( (f.getEconomySize()*marketShare) / (Math.random()*135+45) );
                double investmentRes = (Math.random()*1.5+0.5)*f.getInvestment(i);          //calculating investment gains
                revenue +=(int)Math.round(investmentRes);                                   //adding investment gains to revenue
                revenue -= (int)Math.round( (Math.random()*0.05+0.05)*revenue );            //Remove upkeep costs from revenue
                revenue -= Math.max( revenue*0.2 , 0);                                      //Remove taxes from remaining revenue
                newInv = (int)Math.round(revenue*r.nextDouble()*0.2);                       //Calculating new investment based on remaining revenue

                if(fullGame)DBHandler.setCompInvest(f.getName(i), newInv);                              //store to DB
                else f.setInvestment(i, newInv);
                if(fullGame)oldPerc = DBHandler.getCompPercValue(f.getName(i));                         //Getting Percentage Change in Value
                else oldPerc = 1;
                newPerc = oldPerc + (int)Math.round((double)revenue/TotVal)*100;            //Calculating new Percentage Change in Value
                if(fullGame)DBHandler.setCompPercValue(f.getName(i), newPerc);                          //Store to DB
                if(newPerc>100 & revenue>f.getShareCurrPrince(i)*f.getTotalShares(i)){      //Decide and Calculate dividents
                    divident = newPerc*(double)f.getShareCurrPrince(i)/100;
                    p.alterMoney(Math.round(divident*f.getSharesOwned(i)));
                    revenue -= divident*f.getTotalShares(i);
                } else {
                    divident=0;
                }
                if(fullGame) {
                    newFame = 0;
                    if (divident != 0) newFame += 100;
                    newFame += DBHandler.getCompFame(f.getName(i)) * r.nextInt(newPerc);   //Calculate new Fame
                    DBHandler.setCompFame(f.getName(i), newFame);
                    DBHandler.setCompTotValue(f.getName(i), f.getCompTotalValue(i) + revenue);
                    DBHandler.setCompLastRevenue(f.getName(i), revenue);
                }
            }

            progdialog.incrementProgressBy(25);

            p.setMoney(p.getMoney() - taxes());

            if(p.getLevel()>5&((p.getMoney()+f.NetWorth())/100)>1000000000) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Congratulations");
                String q = "You are now a billionaire. \n\nYou can continue playing or start a new game. \n\nYou have completed this game in " + time.totalDays() + " days.";
                builder.setMessage(q);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog d = builder.create();
                d.show();
            }

            if(fullGame)DBHandler.setPlayerMoney(p.getMoney()); //Update player money (dividents added)

            if(fullGame) {
                for (int i = 1; i <= Company.Sectors.values().length; i++) { //Add new Companies
                    while (f.getSectorOutlook(i) > 0.6 & (f.getSectorOutlook(i) + f.getSectorOutlook(0)) > 0) {
                        String name;
                        do {
                            name = randomName();
                        } while (!f.addCompanyName(name));
                        Company c = new Company(name, Company.Sectors.values()[i - 1]);
                        newCompCounter++;
                        DBHandler.addCompany(c, f.getNumComp() + newCompCounter);
                        Share s = new Share(name, f.getNumComp() + newCompCounter, c.shareStart(), c.getTotalShares());
                        DBHandler.addShare(s);
                        f.setSectorOutlook(0, (double) c.getTotalValue() / f.getEconomySize());
                        f.setSectorOutlook(i, -(double) c.getTotalValue() / f.getEconomySize());
                    }
                    DBHandler.setOutlook(Company.Sectors.values()[i - 1].toString(), f.getBaseSectorOutlook(i));
                    DBHandler.setOutlook("economy", f.getBaseSectorOutlook(0));
                }

                int NumOfBankrupties = 0;
                for (int i = 0; i < f.getNumComp(); i++) { //Declare Bankrupties
                    if (DBHandler.getCompPercValue(f.getName(i)) < -80) {
                        DBHandler.setCompTotValue(f.getName(i), 0);
                        DBHandler.setDBCurrPrice(i, 0);
                        f.setSectorOutlook(0, -(double) f.getCompTotalValue(i) / f.getEconomySize());
                        f.setSectorOutlook(i, (double) f.getCompTotalValue(i) / f.getEconomySize());
                        NumOfBankrupties++;
                    }
                    DBHandler.setOutlook(Company.Sectors.values()[i - 1].toString(), f.getBaseSectorOutlook(i));
                    DBHandler.setOutlook("economy", f.getBaseSectorOutlook(0));
                }
            }

            progdialog.incrementProgressBy(25);

            long oldEcon = f.getEconomySize();
            if(fullGame)f = new Finance(DBHandler); //AT this point, the RAM finance tables are updated. All major alterations have been complete (adding and removing companies).

            f.resetEconomySize();
            if(fullGame)DBHandler.setEconomySize(f.getEconomySize());
            long newEcon = f.getEconomySize();

            long[] SecEconSizes = new long[Company.Sectors.values().length];
            for (int i = 0; i < SecEconSizes.length; i++) {
                SecEconSizes[i] = f.getSecEconSize(i);
            }

            double curr, newMS;
            int P, F;
            String name;
            if(fullGame) {
                for (int i = 0; i < DBHandler.getMaxSID(); i++) {
                    name = DBHandler.getDBShareName(i);
                    curr = DBHandler.getCompMarketShare(name);
                    P = DBHandler.getCompPercValue(name);
                    F = DBHandler.getCompFame(name);

                    newMS = curr * ((double) P / 100) * ((double) F / 500) + ((double) f.getCompTotalValue(i) / f.Companies[i][1]);
                    DBHandler.setCompMarketShare(name, newMS);
                }
            }
            //The market share is always positive but does not sum up to 100%, because people may use more than one company in each sector
            //plus verifications and alterations are too difficult at this point, and require direct manipulation on the DB that cannot be implemented safely on a Thread
            //the Market share is only used for Company outlook and Term revenue;

            progdialog.incrementProgressBy(15);

            double oldO = f.getSectorOutlook(0);
            double newO = r.nextDouble()*0.2*(double)(newEcon-oldEcon)/oldEcon;
            if(Math.abs(newO)>1) newO = Math.signum(newO);
            f.setSectorOutlook(0, newO);
            if(fullGame)DBHandler.setOutlook("economy", newO);


            setUpGGEs(state, newO);


            for (int i = 0; i < Company.Sectors.values().length; i++) {
                newO = f.getBaseSectorOutlook(i)*(double)SecEconSizes[i]/newEcon;
                if(Math.abs(newO)>2) newO = Math.signum(newO)*2;
                if(fullGame)DBHandler.setOutlook(Company.Sectors.values()[i].toString(), newO);
                else f.setSectorOutlook(i+1, newO);
            }

            //Set new market share
            double MS, SO, newOut;
            int nP, nrevenue;
            for (int i = 0; i < f.getNumComp(); i++) {
                //Market share*Sector outlook + percentage change/100 + Revenue/Economy Size
                if(fullGame)MS = DBHandler.getCompMarketShare(f.getName(i));
                else MS = Math.random()*0.5;
                SO = f.getSectorOutlook(f.getCompSectorInt(i) + 1);
                if(fullGame)nP = DBHandler.getCompPercValue(f.getName(i));
                else nP = 1;
                nrevenue = f.getLastRevenue(i);

                newOut = MS*SO+ ((double)nP/100) + (double)nrevenue/newEcon;
                f.setCompOutlook(i, newOut);
                if(fullGame)DBHandler.setCompOutlook(f.getName(i), newOut);
            }

            progdialog.incrementProgressBy(15);

            Random random = new Random();

            int newScams = random.nextInt(Math.round(f.getNumComp()/10));
            if(newScams<=2)newScams =1;
            if (newScams>=6) newScams =5;               //ADD 1 to 5 Scams
            int currScams;
            if(fullGame)currScams = DBHandler.getScamsNo();
            else currScams = f.getScamsNo();
            int[] ids;
            if(fullGame)ids = DBHandler.getScamSIDs();
            else ids=new int[0];
            int sid;
            int type;
            int totalDays;
            int[] days = new int[currScams+newScams];
            for (int i = 0; i < days.length; i++) {
                if(fullGame)days[i]=DBHandler.getScamResolutionDay(ids[i]);
            }
            for (int i = 0; i < newScams; i++) {
                do{
                    if(fullGame)sid = DBHandler.getRandomActiveSID();
                    else sid=random.nextInt(f.getNumComp()-1);
                } while(!f.addScam(sid));

                double e = random.nextDouble(); //5 is current total Number of Categories, from 1 to 5, see MainActivity Scam Resolution Function for details.

                if(e<0.1) type = 1;                     //Ponzi Scheme
                else if (e<=0.3) type = 2;              //Pump&Dump
                else if (e<=0.5) type = 3;              //Short&Distort
                else if (e<0.6) type = 4;               //Empty Room
                else type =5;                           //Lawbreaker Scandal

                totalDays=time.totalDays(random.nextInt(30)+25);

                if(fullGame)DBHandler.addScam(sid, type, totalDays);
                f.addScamData(sid, type, totalDays);
            }
            //term update up to here
            //call dialog to report economy size change, number of companies opened and No of companies closed

            progdialog.setProgress(100);
            progdialog.dismiss();
            gaming = true;
        }
    };

    private void setUpGGEs(EconomyState Currstate, double newEconomyOutlook) {
        switch (Currstate){
            case Normal:
                if(newEconomyOutlook>0.75) {
                    state = EconomyState.Boom;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 1);
                    }
                    editNews(100, "Economic Boom", "The goverment officials report that the economy is Booming, and great profit is to be made in the foreseable future, in all Industries.");
                } else if(newEconomyOutlook>0.5){
                    state = EconomyState.Accel;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 0.5);
                    }
                    editNews(100, "Economy Accelerates", "The goverment officials report that the future of our economy is bright, and companies can expect above average profits.");
                } else if(newEconomyOutlook<-0.5){
                    state = EconomyState.Recess;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -0.5);
                    }
                    editNews(100, "Economic Reccess", "The goverment officials report that the economy not doing very well, and companies should steer carefully to avoid losses.");
                } else if(newEconomyOutlook<-0.75){
                    state = EconomyState.Depres;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -1);
                    }
                    editNews(100, "Depression", "Economy is going from bad to worse, and goverment officials are discussing possible actions to help the economy recover.");
                } else {
                    state = EconomyState.Normal;
                }
                break;
            case Accel:
                if(newEconomyOutlook>0.75) {
                    state = EconomyState.Boom;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 0.5);
                    }
                    editNews(100, "Economic Boom", "The goverment officials report that the economy is Booming, and great profit is to be made in the foreseable future, in all Industries.");
                } else if(newEconomyOutlook>0.5){
                    state = EconomyState.Accel;
                    editNews(100, "Economy Accelerates", "The goverment officials report that the future of our economy is bright, and companies can expect above average profits.");
                } else if(newEconomyOutlook<-0.5){
                    state = EconomyState.Recess;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -1);
                    }
                    editNews(100, "Economic Reccess", "The goverment officials report that the economy not doing very well, and companies should steer carefully to avoid losses.");
                } else if(newEconomyOutlook<-0.75){
                    state = EconomyState.Depres;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -1.5);
                    }
                    editNews(100, "Depression", "Economy is going from bad to worse, and goverment officials are discussing possible actions to help the economy recover.");
                } else {
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -0.5);
                    }
                    state = EconomyState.Normal;
                }
                break;
            case Boom:
                if(newEconomyOutlook>0.75) {
                    state = EconomyState.Boom;
                    editNews(100, "Economic Boom", "The goverment officials report that the economy is Booming, and great profit is to be made in the foreseable future, in all Industries.");
                } else if(newEconomyOutlook>0.5){
                    state = EconomyState.Accel;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -0.5);
                    }
                    editNews(100, "Economy Accelerates", "The goverment officials report that the future of our economy is bright, and companies can expect above average profits.");
                } else if(newEconomyOutlook<-0.5){
                    state = EconomyState.Recess;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -1.5);
                    }
                    editNews(100, "Economic Reccess", "The goverment officials report that the economy not doing very well, and companies should steer carefully to avoid losses.");
                } else if(newEconomyOutlook<-0.75){
                    state = EconomyState.Depres;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -2);
                    }
                    editNews(100, "Depression", "Economy is going from bad to worse, and goverment officials are discussing possible actions to help the economy recover.");
                } else {
                    state = EconomyState.Normal;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -1);
                    }
                }
                break;
            case Recess:
                if(newEconomyOutlook>0.75) {
                    state = EconomyState.Boom;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 1.5);
                    }
                    editNews(100, "Economic Boom", "The goverment officials report that the economy is Booming, and great profit is to be made in the foreseable future, in all Industries.");
                } else if(newEconomyOutlook>0.5){
                    state = EconomyState.Accel;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 1);
                    }
                    editNews(100, "Economy Accelerates", "The goverment officials report that the future of our economy is bright, and companies can expect above average profits.");
                } else if(newEconomyOutlook<-0.5){
                    state = EconomyState.Recess;
                    editNews(100, "Economic Reccess", "The goverment officials report that the economy not doing very well, and companies should steer carefully to avoid losses.");
                } else if(newEconomyOutlook<-0.75){
                    state = EconomyState.Depres;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -0.5);
                    }
                    editNews(100, "Depression", "Economy is going from bad to worse, and goverment officials are discussing possible actions to help the economy recover.");
                } else {
                    state = EconomyState.Normal;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 0.5);
                    }
                }
                break;
            case Depres:
                if(newEconomyOutlook>0.75) {
                    state = EconomyState.Boom;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 2);
                    }
                    editNews(100, "Economic Boom", "The goverment officials report that the economy is Booming, and great profit is to be made in the foreseable future, in all Industries.");
                } else if(newEconomyOutlook>0.5){
                    state = EconomyState.Accel;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 1.5);
                    }
                    editNews(100, "Economy Accelerates", "The goverment officials report that the future of our economy is bright, and companies can expect above average profits.");
                } else if(newEconomyOutlook<-0.5){
                    state = EconomyState.Recess;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 0.5);
                    }
                    editNews(100, "Economic Reccess", "The goverment officials report that the economy not doing very well, and companies should steer carefully to avoid losses.");
                } else if(newEconomyOutlook<-0.75){
                    state = EconomyState.Depres;
                    editNews(100, "Depression", "Economy is going from bad to worse, and goverment officials are discussing possible actions to help the economy recover.");
                } else {
                    state = EconomyState.Normal;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 1);
                    }
                }
                break;
        }

    }

    private long taxes() {
        long tax1 = Math.round((p.getLevel()*0.05)*p.getMoney());
        long tax2 = Math.max(0, (p.getLevel()-2)*1250000);
        int[] Upkeep = {50000, 125000, 750000, 1250000, 2500000, 5000000};

        return tax1+tax2+Upkeep[p.getLevel()];
    }


    //Button And Menu Selections


    @Override
    protected void onDestroy() {
        upd.interrupt();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(DayStartedMessageRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(DayEndedMessageRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(TermEndedMessageRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(SoundAlteredRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(SharesTransactionedRec);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(SpecificElementUpdate);
        super.onDestroy();
    }

    public void PlayerButtonClick(View v) {
        Intent intent = new Intent(MainActivity.this, PlayerInfoActivity.class);
        Bundle data = new Bundle();
        data.putParcelable("DT", time);
        data.putBoolean("playSound", playSound);
        data.putLong("Pmoney", p.getMoney());
        data.putInt("level", p.getLevel());
        data.putInt("assets", p.getAssets());
        data.putInt("next", getNextLevelPreq());
        data.putLong("NetWorth", f.NetWorth());
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickInfo(View v){

        Intent intent = new Intent(this, InfoActivity.class );
        Bundle data = new Bundle();
        data.putStringArrayList("Info", Infos);
        data.putLong("Pmoney", p.getMoney());
        data.putInt("Plevel", p.getLevel());
        data.putInt("Passets", p.getAssets());
        intent.putExtras(data);
        startActivity(intent);
    }

    public void UpdateTopBar(TextView player, TextView daytime){
        long money = p.getMoney();
        int level = p.getLevel();
        int assets = p.getAssets();
        String TBPlayer = "Lvl "+level+": $"+Double.toString((double)money/100)+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());
    }

    public void BuyClick(View v){
        Intent intent = new Intent(this, BuyActivity.class);
        Bundle data = new Bundle();
        int SID = v.getId() - 300000;
        data.putInt("SID", SID );
        data.putParcelable("DT", time);
        data.putString("name", f.getName(SID));
        data.putInt("Sprice", f.getShareCurrPrince(SID));
        data.putInt("owned", f.getSharesOwned(SID));
        data.putLong("Pmoney", p.getMoney());
        data.putInt("level", p.getLevel());
        data.putInt("assets", p.getAssets());
        data.putInt("total", f.getTotalShares(SID));
        data.putInt("lastClose", f.getLastClose(SID));
        intent.putExtras(data);
        startActivity(intent);
    }

    public void SellClick(View v){
        Intent intent = new Intent(this, SellActivity.class);
        Bundle data = new Bundle();
        int SID = v.getId() - 400000;
        data.putInt("SID", SID );
        data.putParcelable("DT", time);
        data.putString("name", f.getName(SID));
        data.putInt("Sprice", f.getShareCurrPrince(SID));
        data.putInt("owned", f.getSharesOwned(SID));
        data.putLong("Pmoney", p.getMoney());
        data.putInt("level", p.getLevel());
        data.putInt("assets", p.getAssets());
        data.putInt("total", f.getTotalShares(SID));
        data.putInt("lastClose", f.getLastClose(SID));
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickPrice(View v){
        Intent intent = new Intent(this, ShareActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", (v.getId() - 100000) );
        data.putParcelable("DT", time);
        data.putLong("Pmoney", p.getMoney());
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickName(View v){
        Intent intent = new Intent(this, ShareActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", (v.getId() - 200000) );
        data.putParcelable("DT", time);
        data.putLong("Pmoney", p.getMoney());
        intent.putExtras(data);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            case R.id.menu_Exit:
                ExitClicked();
                break;
            case R.id.menu_sound:
                try {
                    soundplayer.stop();
                } catch (NullPointerException e){
                }
                playSound = !playSound;
                if(fullGame)DBHandler.setSound(playSound);
                item.setChecked(playSound);
                break;
            case R.id.menu_NewGame:
                SelectNewGame();
                break;
            case R.id.About:
                LoadCredits();
                break;
            case R.id.QuickGame:
                StartQuickGame();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void LoadCredits(){
        String[] Credits = new String[2];
        Credits[0] = "Credits";
        Credits[1] = "This game is the first work created by an independent developer.\nI hope you enjoyed it.\n\nImages come from free stock images sites (like Pixabay).\n\nBell sound by Recorded by Brylon Terry (I only kept the last 5 seconds).\n\nThis game is and will remain free.\n\nIf you liked it, please leave a good review.";
        Intent intent = new Intent(MainActivity.this, NewsActivity.class);
        Bundle data = new Bundle();
        data.putStringArray("NewsArray", Credits);
        intent.putExtras(data);
        startActivity(intent);
    }

    private void StartQuickGame(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quick Game");
        String q = "The quick game option is useful for seeing what a level 5 player is like, and enjoy the full game, without fear of altering the saved data. It is not recomended for new players. Proceed?";
        builder.setMessage(q);

        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                gaming = false;
                DBHandler.close();
                fullGame = false;
                f = new Finance(4);
                p = new Gamer(500000000, 5, 20, 1000);
                UpdateCentralUI();
                UpdateTopBar(topBarPlayer, topBarDaytime);
                dialog.dismiss();
                gaming = true;
            }

        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void ExitClicked() {
        if(fullGame)DBHandler.close();
        upd.interrupt();
        soundplayer.release();
        MainActivity.this.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private void SelectNewGame(){
        if(fullGame) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("New Game");
            String q = "Proceeding will erase all of your data and progress except your full assets. \n\nYou will start again at level 1 with $10000 and nothing owned. \n\nYou will also lose any part-gained assets. \n\nAre you sure you want to proceed?";
            builder.setMessage(q);

            builder.setPositiveButton("New Game", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    gaming = false;
                    int assets = DBHandler.getAssets();
                    DBHandler.clearData();
                    DBHandler.PrepGame(assets, Company.Sectors.values());
                    f = new Finance(DBHandler, 5);
                    UpdateCentralUI();
                    p = new Gamer(DBHandler);
                    time = new Daytime(LocalBroadcastManager.getInstance(MainActivity.this.getApplicationContext()));
                    DBHandler.setEconomySize(f.calcEconomySize());
                    dialog.dismiss();
                    gaming = true;
                }

            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Toast.makeText(MainActivity.this, "Not possible in Quick Game", Toast.LENGTH_LONG).show();
        }
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
}

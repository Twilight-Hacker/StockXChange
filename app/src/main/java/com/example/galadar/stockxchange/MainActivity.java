package com.example.galadar.stockxchange;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
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
import java.util.Random;
import android.os.Handler;

/*
Specific View IDs for editing textViews
SID: Share id: share identifier im Memory and DB, 0 to NumberOfCompanies-1
Share Name View: 200000+SID
Share Price View: 100000+SID
Share Buy Button: 300000+SID
Share Sell Button: 400000+SID
 */

public class MainActivity extends AppCompatActivity {

    static Finance f;
    public static final int[] nextLevel = {0, 125000, 620000, 1250000, 5500000, 25000000, 0}; //To be printed as is, not /100.
    ProgressDialog progdialog;
    static Gamer p;
    Runnable r;
    static MemoryDB DBHandler;
    static Daytime time;
    static TextView topBarPlayer;
    static TextView topBarDaytime;
    Handler BgHandler;
    static int eventGen;
    static ArrayList<Event> Events = new ArrayList<>();
    static boolean playSound;
    static boolean dayOpen = false;
    static boolean gaming = false;
    static double infoGen;
    static long nextInvite;
    static ArrayList<String> Infos = new ArrayList<>();
    static MediaPlayer soundplayer;
    static ArrayList<Meeting> meetingsList;
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

    class UpdateTerm extends AsyncTask<Object, Integer, Object> {

        @Override
        protected void onPreExecute() {
            progdialog = new ProgressDialog(MainActivity.this);
            progdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            String t = getString(R.string.termEnd) +" "+Integer.toString(time.getTerm()+1);
            progdialog.setTitle(t);
            progdialog.setMessage(getText(R.string.TermEndDialogText));
            progdialog.setCancelable(false);
            progdialog.setMax(100);
            progdialog.setProgress(0);
            progdialog.show();

            if (p.getLevel() > 5 & ((p.getMoney() + f.NetWorth()) / 100) > 1000000000) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Congratulations");
                String q = "You are now a billionaire. \n\nYou can continue playing or start a new game. \n\nYou have completed this game in " + time.totalDays() + " days.";
                //Store High Score Here, and transmit to Google
                builder.setMessage(q);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog d = builder.create();
                d.show();
            }

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer[] values) {
            int increment = values[0];
            progdialog.incrementProgressBy(increment);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object o) {
            long[] result = (long[])o;
            long tax = result[0];
            int NumOfBankrupties = (int) result[1];
            int newCompCounter = (int) result[2];
            long oldEcon = result[3];
            long playerDivident = result[4];

            time.nextTerm();
            if (fullGame) {
                DBHandler.setTerm(time.getTerm());
                DBHandler.setDay(time.getDay());
            }

            UpdateCentralUI();
            UpdateTopBar(topBarPlayer, topBarDaytime);

            progdialog.setProgress(100);
            progdialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            String out;
            if (f.getBaseSectorOutlook(0) > 0) out = "positive";
            else out = "negative";
            builder.setTitle("Term Report");
            String zerodigit1, zerodigit2;
            if (tax % 10 == 0) zerodigit1 = "0";
            else zerodigit1 = "";
            zerodigit2 = "0";
            String q = "In this term, " + NumOfBankrupties + " companies declared bunkrupty, " + newCompCounter + " companies entered the stock Exchange, the Economy size changed by $" + Long.toString(f.getEconSize2()-oldEcon) + ", and the economic outlook for the new term is " + out + ".\n\nYou paid $" + Double.toString((double) tax / 100) + zerodigit1 + " in taxes and got $" + Double.toString((double) playerDivident / 100) + zerodigit2 + " from dividents.";
            builder.setMessage(q);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent("DayReset");
                    dialog.dismiss();
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(i);
                    gaming = true;
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
            super.onPostExecute(o);
        }

        @Override
        protected void onCancelled() {
            progdialog.dismiss();
            super.onCancelled();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            Random r = new Random();

            //Get Economy Sizes
            double PreveconomyOutlook = f.getBaseSectorOutlook(0);
            long Size1 = f.calcEconSize1(); //Share Econ Size
            long Size2 = f.calcEconSize2(); //Company Econ Size
            long PSize1, PSize2;
            if(fullGame) {
                PSize1 = DBHandler.getEconomySize1();
                PSize2 = DBHandler.getEconomySize2();
                DBHandler.setEconomySize1(Size1);
                DBHandler.setEconomySize2(Size2);
            } else {
                PSize1 = r.nextLong()*2;
                PSize2 = r.nextLong()*4;
            }

            //New Economic Outlook
            long FullSize=Size1+Size2;
            long PFullSize = PSize1+PSize2;
            double NewEconOutlook = f.getBaseSectorOutlook(0)+( (FullSize)/(PFullSize) -1.0);
            f.setSectorOutlook(0, NewEconOutlook);

            //set up GGEs before determining sector outlooks, because it might alter economic outlook
            setUpGGEs(state, NewEconOutlook);

            //New Sector Outlooks
            double NewOut;
            for (int i = 1; i < f.getNumOfOutlooks(); i++) {
                NewOut = NewEconOutlook+r.nextDouble()-0.3; //random represents demand, Previous Sector Outlook canceled
                f.setSectorOutlook(i, NewOut);
            }

            //Store to DB
            if(fullGame){
                DBHandler.setOutlook("economy", NewEconOutlook);
                for (int i = 0; i < Company.Sectors.values().length; i++) {
                    DBHandler.setOutlook(Company.Sectors.values()[i].toString(), f.getBaseSectorOutlook(i + 1));
                }
            }

            long[] AvgRev = new long[Company.Sectors.values().length];

            for (int i = 0; i < AvgRev.length; i++) {
                AvgRev[i] = f.getSecAvgRev(i);
            }

            Integer[] values=new Integer[1];
            int revenue, TotVal, newInv, oldPerc, newPerc, newFame;
            int divident;
            double newOut;
            long playerDivident = 0;
            for (int i = 0; i < f.getNumComp(); i++) {
                TotVal = f.getCompTotalValue(i);
                if(TotVal==0)continue;
                revenue = f.getCompRevenue(i);
                newFame = 0;
                newFame += revenue-AvgRev[f.getCompSectorInt(i)];                                   //Set New Fame, part 1
                if(Math.abs(newFame)>150)newFame=150*(int)Math.signum(newFame);
                long investmentRes = Math.round( (PreveconomyOutlook+1)*f.getInvestment(i) );       //calculating investment gains
                revenue += (int) investmentRes;                                                     //add investment returns to revenue
                revenue -= (int) Math.max(revenue * 0.18, 0);                                       //Remove taxes from remaining revenue

                if (revenue > 0)
                    newInv = (int)Math.round(revenue * (double)(r.nextInt(42)+33)/100);             //Calculating new investment based on remaining revenue, from 1/3 to 3/4
                else newInv = 0;

                revenue -= newInv;                                                                  //remove invested money from revenue

                if (fullGame)
                    DBHandler.setCompInvest(f.getName(i), newInv);                                  //store to DB
                else f.setInvestment(i, newInv);

                if (fullGame)
                    oldPerc = DBHandler.getCompPercValue(f.getName(i));                             //Getting Percentage Change in Value
                else oldPerc = 0;

                newPerc = (int)Math.round(((double)(TotVal+revenue)/TotVal)*100);                   //Calculating new Percentage Change in Value
                newPerc += oldPerc;

                if (fullGame)
                    DBHandler.setCompPercValue(f.getName(i), newPerc);                              //Store to DB

                if (newPerc > 0 & revenue > 200*f.getTotalShares(i)) {                              //Decide dividends, give if percentage value positive and revenue more than twice the total shares
                    divident = f.Getdivident(i, revenue);                                           //Give as dividend 1% of share price, with a min of $1
                    p.alterMoney(Math.round(divident * f.getSharesOwned(i)));
                    playerDivident += divident * f.getSharesOwned(i);
                    revenue -= divident * f.getTotalShares(i);
                } else {
                    divident = 0;
                }

                if (divident != 0) newFame += 100;
                if (fullGame) newFame += DBHandler.getCompFame(f.getName(i));                       //Calculate new Fame, part 2

                f.setLastRevenue(i, revenue);
                revenue = Math.round(revenue/100);                                             //because total value does not use float digits
                f.setCompTotalValue(i, TotVal+revenue);                                             //Update total value

                if(fullGame){                                                                       //store new values to DB
                    DBHandler.setCompFame(f.getName(i), newFame);
                    DBHandler.setCompTotValue(f.getName(i), f.getCompTotalValue(i));
                    DBHandler.setCompLastRevenue(f.getName(i), revenue);
                }

                newOut = (revenue/TotVal) + (newFame/1000) + (newPerc/100) + ((double)(newInv-revenue)/revenue);   //Get New Company Outlook
                newOut += f.getCompOutlook(i);

                if(fullGame)DBHandler.setOutlook(f.getName(i), newOut);
                else f.setCompOutlook(i, newOut);

                if (fullGame) {
                    DBHandler.setRemShares(i, f.getTotalShares(i) / 2);                       //reset remaining prshares for price determinant
                    DBHandler.setCompRevenue(i, 0);                                                 //reset company revenue
                } else {
                    f.resetRemShares(i);
                    f.ResetCompRevenue(i);
                }
            }

            values[0]=25;
            publishProgress(values);

            long prevWorth;

            if (fullGame) prevWorth = DBHandler.getPrevNetWorth();
            else prevWorth = f.NetWorth() / 2;

            long tax = taxes(f.NetWorth() + p.getMoney() - prevWorth);
            p.setMoney(p.getMoney() - tax);

            if (fullGame) DBHandler.setPrevNetWorth(f.NetWorth());

            if (fullGame) DBHandler.setPlayerMoney(p.getMoney()); //Update player money (dividents added)

            int newCompCounter =0;
            int NumOfBankrupties = 0;

            if (fullGame) {                             //ADDING-REMOVING Companies

                int sid;
                for (int i = 1; i <= Company.Sectors.values().length; i++) { //Add new Companies
                    while (f.getSectorOutlook(i) > 0.5 & (f.getSectorOutlook(i) + f.getSectorOutlook(0)) > 0) {
                        String name;
                        do {
                            name = Finance.randomName();
                        } while (!f.addCompanyName(name));
                        Company c = new Company(name, Company.Sectors.values()[i - 1]);
                        newCompCounter++;
                        sid = DBHandler.getMaxSID() + 1;
                        Share s = new Share(name, sid, c.shareStart(), c.getTotalShares());
                        DBHandler.addCompany(c, sid);
                        DBHandler.addShare(s);
                        f.alterSectorOutlook(0, (double) c.getTotalValue() / f.getSecEconSize(f.getCompSectorInt(i)));
                        f.alterSectorOutlook(i, -(double) c.getTotalValue() / f.getSecEconSize(f.getCompSectorInt(i)));
                    }
                    DBHandler.setOutlook(Company.Sectors.values()[i - 1].toString(), f.getBaseSectorOutlook(i));
                }
                DBHandler.setOutlook("economy", f.getBaseSectorOutlook(0));
            }

            values[0]=25;
            publishProgress(values);

            if(fullGame){

                for (int i = 0; i < f.getNumComp(); i++) { //Declare Bankrupties
                    if (DBHandler.getCompPercValue(f.getName(i)) < -80 || DBHandler.getCompTotalValue(f.getName(i)) == 0) {
                        f.removeCompanyName(f.getName(i));
                        DBHandler.bankrupt(f.getName(i));
                        f.alterSectorOutlook(0, -(double) f.getCompTotalValue(i) / f.getSecEconSize(f.getCompSectorInt(i)));
                        f.alterSectorOutlook(f.getCompSectorInt(i)+1, (double) f.getCompTotalValue(i) / f.getSecEconSize(f.getCompSectorInt(i)));
                        NumOfBankrupties++;
                    }
                }

                for (int i = 0; i < Company.Sectors.values().length; i++) {
                    DBHandler.setOutlook(Company.Sectors.values()[i].toString(), f.getBaseSectorOutlook(i + 1));
                }
                DBHandler.setOutlook("economy", f.getBaseSectorOutlook(0));

                f.resetAllNames();

                int a = 0;
                int ID, oldSID;
                String name;
                int max=DBHandler.getMaxSID();
                for (int i = 0; i <= max; i++) { //Update ALL SIDs in DB
                    name = DBHandler.getDBShareName(i);
                    if (name.equalsIgnoreCase("")) continue;
                    oldSID = DBHandler.getCompanyCID(name);
                    if (oldSID < 0) continue;
                    ID = DBHandler.getDBSharePrimaryID(name);
                    if (ID < 0) continue;
                    DBHandler.UpdateSID(ID, oldSID, a);
                    a++;
                }
            }

            if (fullGame) {
                f = null;
                f = new Finance(DBHandler); //Reload Finance
            }

            long[] SecValue = new long[Company.Sectors.values().length];
            for (int i = 0; i <SecValue.length; i++) {
                SecValue[i] = f.getSectorValue(i);
            }

            double newMS;
            if (fullGame) {
                for (int i = 0; i < DBHandler.getMaxSID(); i++) {
                    newMS = (double)DBHandler.getCompFame(f.getName(i))/1000 + f.getCompTotalValue(i)/SecValue[f.getCompSectorInt(i)];
                    if(newMS<=0.01) newMS=0.012;
                    DBHandler.setCompMarketShare(f.getName(i), newMS);
                }
            }
            //The market share is always positive but does not sum up to 100%, because people may use more than one company in each sector
            //plus verifications and alterations are too difficult at this point, and require direct manipulation on the DB that cannot be implemented safely on a Thread
            //the Market share is only used for Term revenue;

            values[0]=25;
            publishProgress(values);

            int newScams = r.nextInt(6)+2;
            int sid;
            int type;
            int totalDays;
            int currentDay = time.totalDays();

            for (int i = 0; i < newScams; i++) {
                sid = f.getRandomActiveSID();

                double e = r.nextDouble(); //5 is current total Number of Categories, from 1 to 5, see MainActivity Scam Resolution Function for details.

                if (e < 0.05) type = 1;                    //Ponzi Scheme
                else if (e <= 0.3) type = 2;              //Pump&Dump
                else if (e <= 0.5) type = 3;              //Short&Distort
                else if (e < 0.55) type = 4;              //Empty Room
                else type = 5;                           //Lawbreaker Scandal

                totalDays = time.totalDays(r.nextInt(25) + 25);

                f.addScamData(sid, type, totalDays-currentDay);
                if (fullGame) DBHandler.addScam(sid, type, totalDays);
            }
            //term update up to here
            //call dialog to report economy size change, number of companies opened and No of companies closed

            values[0]=25;
            publishProgress(values);

            return new long[]{tax, NumOfBankrupties, newCompCounter, PSize2, playerDivident};
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DBHandler = new MemoryDB(this);
        fullGame = true;

        MeetingXMLParser parser = new MeetingXMLParser();
        meetingsList = parser.parse(getApplicationContext().getResources().openRawResource(R.raw.meetings));
        soundplayer = MediaPlayer.create(MainActivity.this, R.raw.bell);

        setTitle(getString(R.string.app_name));

        if (DBHandler.getMaxSID() < 1) {
            time = new Daytime(LocalBroadcastManager.getInstance(MainActivity.this.getApplicationContext()));
            DBHandler.PrepGame(0, 0, Company.Sectors.values(), 1);
            f = new Finance(DBHandler, 5);
            p = new Gamer(DBHandler);
        } else {
            time = new Daytime(LocalBroadcastManager.getInstance(MainActivity.this.getApplicationContext()), DBHandler.getTerm(), DBHandler.getDay());
            f = new Finance(DBHandler);
            p = new Gamer(DBHandler.getPlayerMoney(), DBHandler.getLevel(), DBHandler.getAssets(), DBHandler.getFame());
        }

        playSound = DBHandler.PlaySound();
        infoGen = 0;
        eventGen = DBHandler.getEventGen();
        Infos.add(getString(R.string.noInfo));
        resetNews();

        state = getEconomyState(f.getSectorOutlook(0));

        switch (state) { //SetUp Current GGE
            case Boom:
                for (int i = 1; i < f.getNumOfOutlooks(); i++) {
                    f.setSectorEventOutlook(i, 1);
                }
                break;
            case Accel:
                for (int i = 1; i < f.getNumOfOutlooks(); i++) {
                    f.setSectorEventOutlook(i, 0.5);
                }
                break;
            case Recess:
                for (int i = 1; i < f.getNumOfOutlooks(); i++) {
                    f.setSectorEventOutlook(i, -0.5);
                }
                break;
            case Depres:
                for (int i = 1; i < f.getNumOfOutlooks(); i++) {
                    f.setSectorEventOutlook(i, -1);
                }
                break;
            default:
                break;
        }

        if (fullGame) Events = DBHandler.retrieveEvents(time.totalDays());
        else {
            Events = new ArrayList<>();
        }

        if (Events.size() != 0) {
            for (Event event : Events) {
                alterOutlooks(event.getType(), event.getMagnitude());
            }
        }

        //Handler to forward time
        BgHandler = new Handler(Looper.getMainLooper());

        r = new Runnable() {
            @Override
            public void run() {
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent("TimeForwarded"));
                //TODO set this to 10000
                BgHandler.postDelayed(this, 2000);
            }
        };

        //from here on is layout controls so set layout to main
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);


        topBarPlayer = (TextView) findViewById(R.id.PlayerDataInfo);
        topBarDaytime = (TextView) findViewById(R.id.DaytimeInfo);

        if (fullGame) {
            nextInvite = DBHandler.getNextInviteTime();
            if (nextInvite == 0) {
                nextInvite = Math.round(System.currentTimeMillis() / 1000);
                nextInvite += 82800;
                DBHandler.setNextInviteTime(nextInvite);
            } else {
                long temp = Math.round(System.currentTimeMillis() / 1000);
                if (temp >= nextInvite) {
                    callInvite(p.getLevel());
                    nextInvite = temp + 82800;
                    DBHandler.setNextInviteTime(nextInvite);
                }
            }
        } else nextInvite = 0;

        UpdateTopBar(topBarPlayer, topBarDaytime);
        UpdateCentralUI();

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
                resetNews();
                TextView shareprice;
                for (int i = 0; i < f.getNumComp(); i++) {
                    shareprice = (TextView) findViewById(100000 + i);
                    shareprice.setTextColor(0xffffffff);
                }
                callforMeetings();
            }
        }, new IntentFilter("DayReset"));

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (playSound) {
                    soundplayer.start();
                }
            }
        }, new IntentFilter("RingBell"));

        LocalBroadcastManager.getInstance(this).registerReceiver(AdvanceTime, new IntentFilter("TimeForwarded"));

        callforMeetings();

        gaming = true;
        startRepTask();
    }

    private void startRepTask(){
        r.run();
    }

    private void stopRepTask(){
        BgHandler.removeCallbacks(r);
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
        News[0] = getString(R.string.NoNewsTitle);
        News[1] = getString(R.string.NoNewsBody);
        NewsPriority = 1;
    }

    public void editNews(int priority, String title, String body){
        if(priority>NewsPriority){
            News[0]="";
            if(NewsPriority>1)News[0]+=R.string.extra;
            News[0]+=title;
            News[1]=body;
            NewsPriority=priority;
            NewsLoad(new View(MainActivity.this));
        }
    }

    private void callInvite(int level) {
        final int cost = level*(int)Math.round(Math.random() * 300 + 700);
        final int reward = level*(int)Math.round(Math.random() *3 + 7);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.InvitTitle));
        String q = getString(R.string.invite1)+Integer.toString(cost)+getString(R.string.invite2)+Double.toString((double)reward/100)+getString(R.string.invite3);
        builder.setMessage(q);

        builder.setPositiveButton(getString(R.string.inviteAccept), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (fullGame) {
                    DBHandler.incAssets((double) reward / 100);
                    p.setAssets(DBHandler.getAssets());
                    p.alterMoney(cost * 100);
                    DBHandler.setPlayerMoney(p.getMoney());
                    UpdateTopBar(topBarPlayer, topBarDaytime);
                }
                dialog.dismiss();
            }

        });

        builder.setNegativeButton(getString(R.string.InviteDecline), new DialogInterface.OnClickListener() {

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
        data.putStringArray("NewsArray", News);
        intent.putExtras(data);
        startActivity(intent);
    }

    private void callforMeetings(){
        if(!fullGame)return;
        gaming=false;

        Meeting today = getMeetingbyDay(time.totalDays());
        if(today!=null){
            LaunchMeeting(today.getMeetingTitle(), today.getMeetingSpeech());
        }

        gaming=true;
    }

    private void LaunchMeeting(String title, ArrayList<String> speech) {
        Intent intent = new Intent(MainActivity.this, MeetingActivity.class);
        Bundle data = new Bundle();
        data.putStringArrayList("speech", speech);
        data.putString("title", title);
        intent.putExtras(data);
        startActivity(intent);
    }


    private void UpdateCentralUI() {
        //ScrollView centermain = (ScrollView)findViewById(R.id.scroll);
        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.Main_CentralLayout);
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
            Buy.setText(getString(R.string.Buy_Button));
            if(dayOpen & (p.getMoney()>0)) {
                Buy.setEnabled(true);
                Buy.setTextColor(0xffffffff);
            } else if(dayOpen & (p.getLevel()>=4)){
                Buy.setEnabled(true);
                Buy.setTextColor(0xffff0000);
            } else {
                Buy.setEnabled(false);
                Buy.setTextColor(0xff000000);
            }
            Button Sell = (Button)shareData.findViewById(R.id.SellButton);
            Sell.setId(400000 + i);
            Sell.setText(getString(R.string.Sell_Button));
            if((f.getSharesOwned(i)>0) & dayOpen) {
                Sell.setEnabled(true);
                Sell.setTextColor(0xffffffff);
            } else if(p.getLevel()>=4 & !f.isShorted(i) & dayOpen){
                Sell.setEnabled(true);
                Sell.setTextColor(0xffff0000); //Red Color for short positions
            } else {
                Sell.setEnabled(false);
                Sell.setTextColor(0xff000000);
            }

            parentLayout.addView(shareData);
        }
        //centermain.addView(parentLayout);
        //setContentView(R.layout.activity_main);
    }

    private BroadcastReceiver AdvanceTime = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if(gaming) {
                time.increment(10);
                UpdateTopBar(topBarPlayer, topBarDaytime);
                callforTransactions();
                callInfoGen();
            }
        }
    };

    private void callInfoGen() {
        infoGen += (0.05+Math.random()*0.05)*(1+p.getAssets());
        if(infoGen>=1){
            String str=getNewInfo();
            if(MainActivity.Infos.contains(getString(R.string.noInfo)))MainActivity.Infos.remove(getString(R.string.noInfo));
            if(!MainActivity.Infos.contains(str))MainActivity.Infos.add(str);
            else callInfoGen();
            //Toast.makeText(MainActivity.this, getString(R.string.NewInfo), Toast.LENGTH_SHORT).show();
            infoGen=0;
        }
    }

    private String getNewInfo() {
        String info="";

        Random r = new Random();
        int user = 1000+r.nextInt(5000);
        int type;
        double truthlimit = (6-p.getLevel())*0.06;
        if(r.nextDouble()<0.25) type=1;
        else type=0; //0: share, 1: Scams.
        boolean truth = r.nextDouble() >= truthlimit;

        int reference = r.nextInt(f.getNumComp());
        info+=getString(R.string.InfoUser)+Integer.toString(user)+": ";
        info+= getString(R.string.Info1, f.getName(reference)); //"The "+f.getName(reference)+" company ";

        switch (type){

            case 1:
                if(truth){
                    if(f.isScam(reference)) {
                        info+=" "+getString(R.string.InfoIlligalPos);
                    } else {
                        info+=" "+getString(R.string.InfoIlligalNeg);
                    }
                } else { //Just the inverse of the above, because the info is a lie
                    if(f.isScam(reference)) {
                        info+=" "+getString(R.string.InfoIlligalNeg);
                    } else {
                        info+=" "+getString(R.string.InfoIlligalPos);
                    }
                }
                break;

            default:
                double perLeft = (double)f.getRemShares(reference)/f.getTotalShares(reference);
                double offer = ( (double)f.getShareCurrPrince(reference)*0.01 ) / ( (double)f.getCompTotalValue(reference)/f.getTotalShares(reference) );
                double demand = f.getCompOutlook(reference)*2 + f.getSectorOutlook(f.getCompSectorInt(reference)+1);
                double a = getDeterminant(perLeft, offer, demand); //f.getCompOutlook(reference)+f.getSectorOutlook(f.getCompSectorInt(reference)+1)+((double)f.getRemShares(reference)/f.getTotalShares(reference))-0.5;
                if(truth){
                    if(a>0) {
                        info+=" "+getString(R.string.InfoShareUp);
                    } else {
                        info+=" "+ getString(R.string.InfoShareDown);
                    }
                } else { //Just the inverse of the above, because the info is a lie
                    if(a<=0) {
                        info+=" "+getString(R.string.InfoShareUp);
                    } else {
                        info+=" "+ getString(R.string.InfoShareDown);
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
        info+="USER "+user+": ";

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

    public static String addAssetInfo(Context context){
        //These info are always true.
        //This removes an asset from DB and player
        //This automatically adds the info to Infos List, and then returns it;

        p.setAssets(p.getAssets()-1);
        if(fullGame)DBHandler.setAssets(p.getAssets());

        String info ="";
        Random r = new Random();
        int reference = r.nextInt(f.getNumComp());
        info+= context.getString(R.string.InfoUser)+"9001: "; //"USER 9001: ";

        info+=context.getString(R.string.Info1, f.getName(reference));

        if(f.isScam(reference)) {
            info += context.getString(R.string.InfoIlligalPos);
            if (Infos.contains(info)) return addCertainInfo(reference, 9001);
            if (MainActivity.Infos.contains(context.getString(R.string.noInfo)))
                MainActivity.Infos.remove(context.getString(R.string.noInfo));
            MainActivity.Infos.add(info);
            return info;
        }

        double a = f.getCompOutlook(reference)+f.getSectorOutlook(f.getCompSectorInt(reference)+1)+((double)f.getRemShares(reference)/f.getTotalShares(reference))-0.5;
        if(a>0) {
            info+=context.getString(R.string.InfoShareUp);
        } else {
            info+=context.getString(R.string.InfoShareDown);
        }
        if (MainActivity.Infos.contains(context.getString(R.string.noInfo))) MainActivity.Infos.remove(context.getString(R.string.noInfo));

        if(!MainActivity.Infos.contains(info)){
            MainActivity.Infos.add(info);
            return info;
        } else {
            return addAssetInfo(context); //If the info is already given, get new info instead (recursive call)
        }
    }

    private BroadcastReceiver DayStartedMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Random random = new Random();

            for (int i = 0; i < f.getNumComp(); i++) {
                if(f.getShortRemainingDays(i)==0){
                    p.alterMoney(f.getPosShortAmount(i) * f.getShareCurrPrince(i));
                    f.clearShort(i);
                }
            }

            if(fullGame)DBHandler.setPlayerMoney(p.getMoney());

            UpdateTopBar(topBarPlayer, topBarDaytime);

            if(fullGame)DBHandler.ShortSettle(time.totalDays());

            int level = p.getLevel();
            eventGen += random.nextInt(100)*(level-2)*(level-1);
            if(eventGen>=1000){
                if(level>=3)generateEvent(); //RGEs start at level 3
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
                            f.Backrupt(i);//Banctrupt company
                            if(fullGame)DBHandler.setDBCurrPrice(i, f.getShareCurrPrince(i));
                            if(fullGame)DBHandler.setCompTotValue(f.getName(i), f.getCompTotalValue(i));
                            f.removeScam(i);
                            String story = getString(R.string.NewsBunkrupty, f.getName(i));
                            editNews(55, getString(R.string.NewsBunkruptyTitle), story);
                        }
                        break;
                    case 2:     //Pump and Dump
                        if(f.getScamRemDays(i)<=5){
                            f.setCompOutlook(i, f.getCompOutlook(i)+0.5);
                        }
                        if(f.getScamRemDays(i)==0){
                            Intent intent1 = new Intent("SharesTransaction");
                            Bundle data1 = new Bundle();
                            data1.putInt("SID", i);
                            data1.putInt("amount", -(random.nextInt(4000)+1857));
                            int droppedPrice = 4*(f.getShareCurrPrince(i)/5); //This is to further drop the new price
                            data1.putInt("atPrice", droppedPrice);
                            data1.putBoolean("ByPlayer", false);
                            f.setCompOutlook(i, f.getCompOutlook(i) - 3.5); //Because 6 pumps in total
                            intent1.putExtras(data1);
                            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent1);
                            f.removeScam(i);
                            String story = getString(R.string.NewsPumpDump, f.getName(i));
                            editNews(40, getString(R.string.NewsPumpDumpTitle), story);
                            f.resetRemShares(i);
                        }
                        break;
                    case 3:     //Short and Distort
                        if(f.getScamRemDays(i)<=3){
                            f.setCompOutlook(i, f.getCompOutlook(i)-0.8);
                        }
                        if(f.getScamRemDays(i)==0){
                            f.setCompOutlook(i, f.getCompOutlook(i) + 2.5);
                            f.alterRemShares(i, 1581 + random.nextInt(1500));
                            if(fullGame)DBHandler.setCompOutlook(f.getName(i), f.getCompOutlook(i));
                            if(fullGame)DBHandler.setRemShares(i, f.getRemShares(i));
                            f.removeScam(i);
                            String story = getString(R.string.NewsShortDistort, f.getName(i));
                            editNews(45, getString(R.string.NewsShortDistortTitle), story);
                        }
                        break;
                    case 4:     //Ponzi Scheme (set share price to 0.1, and remaining shares to -total, revenue to - total value)
                        if(f.getScamRemDays(i)==0){
                            f.Backrupt(i);//Bunctrupt company
                            if(fullGame)DBHandler.setDBCurrPrice(i, f.getShareCurrPrince(i));
                            if(fullGame)DBHandler.setCompTotValue(f.getName(i), f.getCompTotalValue(i));
                            f.removeScam(i);
                            String story = getString(R.string.NewsPonzi, f.getName(i));
                            editNews(50, getString(R.string.NewsBunkruptyTitle), story);
                        }
                        break;
                    case 5:     //Lawbreaker Scandal
                        if(f.getScamRemDays(i)==0){
                            int magnitude = getLinnearRN(14)+1;

                            f.setCompOutlook(i, f.getCompOutlook(i) - magnitude * 0.1);
                            if(magnitude>4) f.UpdateCompRevenue(i, -1000000*magnitude);
                            f.removeScam(i);
                            if(fullGame){
                                DBHandler.setCompOutlook(f.getName(i), f.getCompOutlook(i));
                                DBHandler.setCompRevenue(i, f.getCompRevenue(i));
                            }
                            String story = getString(R.string.NewsScandal, f.getName(i));
                            editNews(30+magnitude*10, getString(R.string.NewsScandalTitle), story);
                        }
                        break;
                    default:    //Do nothing, since category/type is 0, thus no Scams
                        break;
                }
            }
            Toast.makeText(MainActivity.this, getString(R.string.ToastDayStart), Toast.LENGTH_SHORT).show();
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
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Food"), -2 * (double) magnitude / 100);
                editNews(45+(int)Math.round((double)magnitude/10), getString(R.string.NewsQuakeTitle), getString(R.string.NewsQuake, (double)magnitude/10));
                break;
            case 2: //Typhoon
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Constr"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Telecom"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Oil"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Transp"), -2 * (double) magnitude / 100);
                editNews(50+(int)Math.floor(magnitude/20), getString(R.string.NewsTyphoonTitle), getString(R.string.NewsTyphoon, (int)Math.round((double) magnitude / 20)));
                break;
            case 3: //Explosion
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Constr"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Defence"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Tech"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Entert"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Tourism"), -2*(double)magnitude/100);
                editNews(15+magnitude, getString(R.string.NewsExplosionTitle), getString(R.string.NewsExplosion));
                break;
            case 4: //Riots
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Defence"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Telecom"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Tech"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Food"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Oil"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Entert"), -2*(double)magnitude/100);
                editNews(10+Math.round(magnitude/2), getString(R.string.NewsRiotsTitle), getString(R.string.NewsRiots));
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
                editNews(150, getString(R.string.NewsWarTitle), getString(R.string.NewsWar));
                break;
            default: //Unknown event, assume eartquake
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Constr"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Defence"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Educ"), 2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Entert"), -2*(double)magnitude/100);
                f.setSectorEventOutlook(f.getSectorOutlookIndex("Food"), -2*(double)magnitude/100);
                editNews(45+(int)Math.round((double)magnitude/10), getString(R.string.NewsQuakeTitle), getString(R.string.NewsQuake, (double)magnitude/10));
        }
    }

    public static int getLinnearRN(int maxSize){
        //Get a linearly multiplied random number
        int randomMultiplier = maxSize * (maxSize + 1) / 2;
        Random r=new Random();
        int randomInt = r.nextInt(randomMultiplier);

        int linearRN = 1;
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
            if(fullGame)DBHandler.setLevel(p.getLevel());
            long priceMoney=0;
            int prizeAssets=p.getAssets();
            switch (p.getLevel()){
                case 2: {
                    priceMoney=25000;
                    prizeAssets+=2;
                    setUpGGEs(state, 1);
                    Meeting meeting = getMeetingbyDay(-2);
                    LaunchMeeting(meeting.getMeetingTitle(), meeting.getMeetingSpeech());
                    break;
                }
                case 3: {
                    priceMoney=120000;
                    prizeAssets+=2;
                    Meeting meeting = getMeetingbyDay(-3);
                    LaunchMeeting(meeting.getMeetingTitle(), meeting.getMeetingSpeech());
                    break;
                }
                case 4: {
                    priceMoney=250000;
                    prizeAssets+=3;
                    Meeting meeting = getMeetingbyDay(-4);
                    LaunchMeeting(meeting.getMeetingTitle(), meeting.getMeetingSpeech());
                    break;
                }
                case 5: {
                    priceMoney=500000;
                    prizeAssets+=4;
//                    Meeting meeting = getMeetingbyDay(-5);
//                    LaunchMeeting(meeting.getMeetingTitle(), meeting.getMeetingSpeech());
                    break;
                }
                case 6: {
                    prizeAssets+=6;
//                    Meeting meeting = getMeetingbyDay(-6);
//                    LaunchMeeting(meeting.getMeetingTitle(), meeting.getMeetingSpeech());
                    break;
                }
                default:
                    break;
            }

            p.alterMoney(priceMoney*100);
            p.setAssets(prizeAssets);
            if(fullGame){
                DBHandler.setAssets(p.getAssets());
                DBHandler.setPlayerMoney(p.getMoney());
            }
            UpdateTopBar(topBarPlayer, topBarDaytime);
        }
    };

    private void callforTransactions() {
        if(!dayOpen) return;
        int temp;
        for(int i=0;i<f.getNumComp();i++) {
            if (f.getCompTotalValue(i) != 0) {
                double pricePer = ( (double)f.getShareCurrPrince(i)*0.01 ) / ( (double)f.getCompTotalValue(i)/f.getTotalShares(i) ) ; //Current price over Average value
                temp = getSharesAmount(f.getCompOutlook(i), f.getSectorOutlook(f.getCompSectorInt(i) + 1), f.getRemShares(i), f.getTotalShares(i), pricePer);
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

    public int getSharesAmount(double C, double S, int remaining, int total, double offer){
        Random random = new Random();
        double perLeft = (double)remaining/total;

        double demand = 2*C+S;

        double determinant = getDeterminant(perLeft, offer, demand); //higher price->sell, higher outlook->buy, higher perLeft->sell , perLeft base is 0.5
        boolean buy = (determinant>0)^(random.nextDouble()<0.9);  //if perOwned >0, 90% buy, else 90% sell
        if(perLeft<=0.02) buy = false;
        else if(perLeft>=0.98) buy = true;
        determinant = Math.abs(determinant);
        boolean temp = (random.nextDouble()<0.8)^(determinant<0.1); //80% chance of transaction if determinant>0.1, 20% otherwise

        if(!temp) {
            return 0;
        } else {
            double am = random.nextDouble()*50*determinant;
            if(Math.abs(am)<=50) am = random.nextInt(49)+52;
            int amount = (int)Math.round(am);
            if(!buy) amount = 0-amount; //decide buy sell here
            return amount;
        }
    }

    private double getDeterminant(double perLeft, double offer, double demand){
        return perLeft - (offer/demand);
    }

    private BroadcastReceiver SharesTransactionedRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            int SID = data.getInt("SID");
            int amount = data.getInt("amount");
            int oldPrice = data.getInt("atPrice");
            boolean byPlayer = data.getBoolean("ByPlayer");

            int total = f.getTotalShares(SID);

            if(byPlayer){
                f.TransactShares(SID, amount);
                p.alterMoney(amount*oldPrice);
                if(fullGame)DBHandler.TransactShare(SID, f.getSharesOwned(SID), p.getMoney());

                UpdateCommandsUI();
            }

            f.alterRemShares(SID, amount);
            int remaining = f.getRemShares(SID);
            if(fullGame)DBHandler.setRemShares(SID, remaining);

            double value = (double)f.getCompTotalValue(SID)*100;
            int avg = (int)Math.round( value/total );
            int base = f.getLastClose(SID);

            int newPrice = base + (int) Math.round( ( (double)amount/(remaining+1) )*(avg/10) + (Math.random()*2-1)*30 );
            if(newPrice<=20) newPrice=20+(int)Math.round(Math.random() * 30);
            else if(newPrice>2.1*avg)newPrice = 2*avg;

            f.setShareCurrPrice(SID, newPrice);
            if(fullGame)DBHandler.setDBCurrPrice(SID, newPrice);

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
            int days = data.getInt("Days");

            int total = f.getTotalShares(SID);

            f.ShortShare(SID, amount, days);
            p.alterMoney(amount * oldPrice);
            if(fullGame)DBHandler.ShortShare(SID, amount, time.totalDays(days), p.getMoney());

            amount = Math.round(amount/2);
            f.alterRemShares(SID, amount);
            int remaining = f.getRemShares(SID);
            if(fullGame)DBHandler.setRemShares(SID, remaining);

            UpdateCommandsUI();

            double value = (double)f.getCompTotalValue(SID)/100;
            int avg = (int)Math.round( value/total );
            int base = f.getLastClose(SID);

            int newPrice = base + (int) Math.round( ( (double)amount/(remaining+1) )*(avg/10) + (Math.random()*2-1)*30 );
            if(newPrice<=20) newPrice=20+(int)Math.round(Math.random() * 30);
            else if(newPrice>2.1*avg)newPrice = 2*avg;


            f.setShareCurrPrice(SID, newPrice);
            if(fullGame)DBHandler.setDBCurrPrice(SID, newPrice);

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

    private BroadcastReceiver SpecificElementUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            int SID = data.getInt("SID");
            int price = data.getInt("newPrice");
            int oldPrice = data.getInt("oldPrice");
            boolean playerOwner = data.getBoolean("PlayerOwner");

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
            if(playerOwner & dayOpen){
                Sell.setEnabled(true);
                Sell.setTextColor(0xffffffff);
            } else if(p.getLevel()>=4 & !f.isShorted(SID)& dayOpen){
                Sell.setEnabled(true);
                Sell.setTextColor(0xffff0000);
            } else {
                Sell.setEnabled(false);
                Sell.setTextColor(0xff000000);
            }
        }
    };

    private BroadcastReceiver SoundAlteredRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(playSound) {
                if (soundplayer.isPlaying()) soundplayer.stop();
                soundplayer.release();
            }
            playSound = !playSound;
            if(playSound)        soundplayer = MediaPlayer.create(MainActivity.this, R.raw.bell);
            if(fullGame)DBHandler.setSound(playSound);
        }
    };

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

            double upper, lower;

            switch (getEconomyState()){
                case Normal:
                    upper = 0.6;
                    lower = -0.4;
                    break;
                case Accel:
                    upper = 0.8;
                    lower = -0.2;
                    break;
                case Boom:
                    upper = 1.0;
                    lower = -0.1;
                    break;
                case Recess:
                    upper = 0.4;
                    lower = -0.6;
                    break;
                default: //Assume depression (to avoid might not have been initialized error)
                    upper = 0.2;
                    lower = -0.9;
                    break;
            }

            long marketSize = f.getMarketSize();
            for (int i = 0; i < f.getNumComp(); i++) { //Add revenue
                if(f.getCompTotalValue(i)==0)continue; //Exclude bunkrupt companies
                upper+=f.getCompOutlook(i);
                lower+=f.getCompOutlook(i);
                f.UpdateCompRevenue(i, (int)Math.round(marketSize*f.getMarketShare(i)*(RangedRandom(upper, lower)) ));
            }

            f.DayCloseShares();
            if(fullGame) {
                for (int i = 0; i < DBHandler.getMaxSID(); i++) {
                    DBHandler.DayCloseShare(i, f.getLastClose(i));
                    DBHandler.setCompRevenue(i, f.getCompRevenue(i));
                }
            }

            for(Event event:Events){
                    event.dayEnded();
                if(event.eventEnded()) {
                    alterOutlooks(event.getType(), -event.getMagnitude());
                }
            }

            if(fullGame)DBHandler.ClearCompleteEvents(time.totalDays());

            if(time.totalDays()%2==0) {//Infos cleared every 2 days
                Infos.clear();
                Infos.add(getString(R.string.noInfo));
            }

            if(p.getLevel()==5&(p.getMoney())/100>25000000){
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent("LevelUp"));
            }

            if((p.getMoney()+f.NetWorth())<-10000000) {
                gaming = false;
                if (p.getAssets() > 0) {
                    p.setAssets(p.getAssets() - 1);
                    if(fullGame)DBHandler.setAssets(p.getAssets());
                    p.setMoney(2500000);
                    if(fullGame)DBHandler.setPlayerMoney(p.getMoney());
                    Toast.makeText(MainActivity.this, getString(R.string.AvoidBankrupty), Toast.LENGTH_SHORT).show();
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle(getString(R.string.GameOver));
                    builder.setMessage(getString(R.string.GameOverMessage));

                    builder.setPositiveButton(getString(R.string.MessageOK), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            if(fullGame) {
                                long timeInv = DBHandler.getNextInviteTime();
                                DBHandler.clearData();
                                DBHandler.PrepGame(timeInv, 0, Company.Sectors.values(), 1);
                                f = new Finance(DBHandler, 4);
                                UpdateCentralUI();
                                p = new Gamer(DBHandler);
                                time = new Daytime(LocalBroadcastManager.getInstance(MainActivity.this.getApplicationContext()));
                                DBHandler.setEconomySize1(f.getEconSize1());
                                DBHandler.setEconomySize2(f.getEconSize2());
                                dialog.dismiss();
                            } else ExitClicked();
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
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent("TermEnded"));
                } else {
                    if(fullGame)DBHandler.setDay(time.getDay() + 1);
                }
            }
            UpdateCommandsUI();
            gaming = true;
        }
    };

    //RUN ONLY ON UI THREAD
    public void UpdateCommandsUI(){
        Button B;
        try{
            for (int i = 0; i < f.getNumComp(); i++) {
                B = (Button) findViewById(300000 + i); //Buy Button
                if(dayOpen & (p.getMoney()>0)) {
                    B.setEnabled(true);
                    B.setTextColor(0xffffffff);
                } else if(dayOpen & (p.getLevel()>= 4)){
                    B.setEnabled(true);
                    B.setTextColor(0xffff0000);
                } else {
                    B.setEnabled(false);
                    B.setTextColor(0xff000000);
                }

                B = (Button) findViewById(400000 + i); //Sell Button
                if (dayOpen & (f.getSharesOwned(i) > 0)) {
                    B.setEnabled(true);
                    B.setTextColor(0xffffffff);
                } else if(p.getLevel()>=4 & !f.isShorted(i) & dayOpen){
                    B.setEnabled(true);
                    B.setTextColor(0xffff0000); //Red Color for short positions
                } else {
                    B.setEnabled(false);
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
        data.putLong("Pmoney", p.getMoney());
        data.putInt("level", p.getLevel());
        data.putInt("assets", p.getAssets());
        intent.putExtras(data);
        startActivity(intent);
    }

    public void EconomyInfoLaunch(View v){
        Intent intent = new Intent(MainActivity.this, EconomyInfoActivity.class);
        Bundle data = new Bundle();
        data.putBoolean("Sound", playSound);
        data.putLong("Economy_size", f.getFullEconSize());
        data.putInt("TotalCompanies", f.getNumComp());
        data.putInt("SumOfShares", f.getSumShares());
        data.putLong("Pmoney", p.getMoney());
        data.putInt("level", p.getLevel());
        data.putInt("assets", p.getAssets());
        intent.putExtras(data);
        startActivity(intent);
    }

    private BroadcastReceiver TermEndedMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            gaming = false;
            new UpdateTerm().execute();
        }
    };

    private void setUpGGEs(EconomyState Currstate, double newEconomyOutlook) {
        if(p.getLevel()<2)return; //GGEs start at Level 2;
        switch (Currstate){
            case Normal:
                if(newEconomyOutlook>0.75) {
                    state = EconomyState.Boom;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 1);
                    }
                    editNews(100, getString(R.string.NewsBoomTitle), getString(R.string.NewsBoom));
                } else if(newEconomyOutlook>0.5){
                    state = EconomyState.Accel;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 0.5);
                    }
                    editNews(100, getString(R.string.NewsAccelTitle), getString(R.string.NewsAccel));
                } else if(newEconomyOutlook<-0.5){
                    state = EconomyState.Recess;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -0.5);
                    }
                    editNews(100, getString(R.string.NewsRecessTitle), getString(R.string.NewsRecess));
                } else if(newEconomyOutlook<-0.75){
                    state = EconomyState.Depres;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -1);
                    }
                    editNews(100, getString(R.string.NewsDepressionTitle), getString(R.string.NewsDepression));
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
                    editNews(100, getString(R.string.NewsBoomTitle), getString(R.string.NewsBoom));
                } else if(newEconomyOutlook>0.5){
                    state = EconomyState.Accel;
                    editNews(100, getString(R.string.NewsAccelTitle), getString(R.string.NewsAccel));
                } else if(newEconomyOutlook<-0.5){
                    state = EconomyState.Recess;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -1);
                    }
                    editNews(100, getString(R.string.NewsRecessTitle), getString(R.string.NewsRecess));
                } else if(newEconomyOutlook<-0.75){
                    state = EconomyState.Depres;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -1.5);
                    }
                    editNews(100, getString(R.string.NewsDepressionTitle), getString(R.string.NewsDepression));
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
                    editNews(100, getString(R.string.NewsBoomTitle), getString(R.string.NewsBoom));
                } else if(newEconomyOutlook>0.5){
                    state = EconomyState.Accel;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -0.5);
                    }
                    editNews(100, getString(R.string.NewsAccelTitle), getString(R.string.NewsAccel));
                } else if(newEconomyOutlook<-0.5){
                    state = EconomyState.Recess;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -1.5);
                    }
                    editNews(100, getString(R.string.NewsRecessTitle), getString(R.string.NewsRecess));
                } else if(newEconomyOutlook<-0.75){
                    state = EconomyState.Depres;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -2);
                    }
                    editNews(100, getString(R.string.NewsDepressionTitle), getString(R.string.NewsDepression));
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
                    editNews(100, getString(R.string.NewsBoomTitle), getString(R.string.NewsBoom));
                } else if(newEconomyOutlook>0.5){
                    state = EconomyState.Accel;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 1);
                    }
                    editNews(100, getString(R.string.NewsAccelTitle), getString(R.string.NewsAccel));
                } else if(newEconomyOutlook<-0.5){
                    state = EconomyState.Recess;
                    editNews(100, getString(R.string.NewsRecessTitle), getString(R.string.NewsRecess));
                } else if(newEconomyOutlook<-0.75){
                    state = EconomyState.Depres;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, -0.5);
                    }
                    editNews(100, getString(R.string.NewsDepressionTitle), getString(R.string.NewsDepression));
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
                    editNews(100, getString(R.string.NewsBoomTitle), getString(R.string.NewsBoom));
                } else if(newEconomyOutlook>0.5){
                    state = EconomyState.Accel;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 1.5);
                    }
                    editNews(100, getString(R.string.NewsAccelTitle), getString(R.string.NewsAccel));
                } else if(newEconomyOutlook<-0.5){
                    state = EconomyState.Recess;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 0.5);
                    }
                    editNews(100, getString(R.string.NewsRecessTitle), getString(R.string.NewsRecess));
                } else if(newEconomyOutlook<-0.75){
                    state = EconomyState.Depres;
                    editNews(100, getString(R.string.NewsDepressionTitle), getString(R.string.NewsDepression));
                } else {
                    state = EconomyState.Normal;
                    for(int i=1;i<f.getNumOfOutlooks();i++){
                        f.setSectorEventOutlook(i, 1);
                    }
                }
                break;
        }

    }

    public Meeting getMeetingbyDay(int day){
        //Denote level by giving negative day
        //Denote PGE meetings by giving day 0
        for (int i = 0; i < meetingsList.size(); i++) {
            if(meetingsList.get(i).getDay()==day) return meetingsList.get(i);
        }
        return null;
    }

/*
    public String[] getAllMeetingTitles(){
        String[] titles = new String[meetingsList.size()];

        for (int i = 0; i < meetingsList.size(); i++) {
            titles[i]=meetingsList.get(i).getMeetingTitle();
        }
        return titles;
    }
*/

    private long taxes(long diff) {
        long tax1 = Math.round((p.getLevel()*0.04)*diff);
        long tax2;
        if(p.getLevel()>2) {
            tax2 = (long)((p.getLevel() - 2) * 0.02 * p.getMoney());
        } else {
            tax2=0;
        }
        int[] Upkeep = {50000, 125000, 750000, 1250000, 2500000, 5000000};

        return tax1+tax2+Upkeep[p.getLevel()-1];
    }

    //Button And Menu Selections

    @Override
    protected void onDestroy() {
        stopRepTask();
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

        Intent intent = new Intent(MainActivity.this, InfoActivity.class );
        Bundle data = new Bundle();
        data.putLong("Pmoney", p.getMoney());
        data.putInt("Plevel", p.getLevel());
        data.putInt("Passets", p.getAssets());
        data.putStringArrayList("Info", Infos);
        data.putBoolean("playSound", playSound);
        intent.putExtras(data);
        startActivity(intent);
    }

    public void UpdateTopBar(TextView player, TextView daytime){
        long money = p.getMoney();
        int level = p.getLevel();
        int assets = p.getAssets();
        String zerodigit;
        if(money%10==0)zerodigit="0";
        else zerodigit="";
        String TBPlayer = "Lvl "+level+": $"+Double.toString((double)money/100)+zerodigit+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());
    }

    public void BuyClick(View v){
        Intent intent = new Intent(MainActivity.this, BuyActivity.class);
        Bundle data = new Bundle();
        int SID = v.getId() - 300000;
        data.putInt("SID", SID);
        data.putString("Sname", f.getName(SID));
        data.putInt("Sprice", f.getShareCurrPrince(SID));
        data.putInt("owned", f.getSharesOwned(SID));
        data.putLong("Pmoney", p.getMoney());
        data.putInt("level", p.getLevel());
        data.putInt("assets", p.getAssets());
        data.putInt("total", f.getTotalShares(SID));
        data.putInt("lastClose", f.getLastClose(SID));
        data.putInt("totalShares", f.getTotalShares(SID));
        data.putBoolean("playSound", playSound);
        intent.putExtras(data);
        startActivity(intent);
    }

    public void SellClick(View v){
        Intent intent = new Intent(MainActivity.this, SellActivity.class);
        Bundle data = new Bundle();
        int SID = v.getId() - 400000;
        data.putInt("SID", SID);
        data.putBoolean("playSound", playSound);
        data.putString("Sname", f.getName(SID));
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
        Intent intent = new Intent(MainActivity.this, ShareActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", (v.getId() - 100000));
        data.putBoolean("playSound", playSound);
        data.putBoolean("dayOpen", dayOpen);
        data.putLong("Pmoney", p.getMoney());
        data.putInt("level", p.getLevel());
        data.putInt("assets", p.getAssets());
        intent.putExtras(data);
        startActivity(intent);
    }

    public void clickName(View v){
        Intent intent = new Intent(MainActivity.this, ShareActivity.class);
        Bundle data = new Bundle();
        data.putInt("SID", (v.getId() - 200000));
        data.putBoolean("playSound", playSound);
        data.putBoolean("dayOpen", dayOpen);
        data.putLong("Pmoney", p.getMoney());
        data.putInt("level", p.getLevel());
        data.putInt("assets", p.getAssets());
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
                if(playSound) {
                    if (soundplayer.isPlaying()) soundplayer.stop();
                    soundplayer.release();
                }
                playSound = !playSound;
                if(playSound)         soundplayer = MediaPlayer.create(MainActivity.this, R.raw.bell);
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.Quit));
        builder.setMessage(getString(R.string.QuitMessage));

        builder.setPositiveButton(getString(R.string.ExitButton), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ExitClicked();
            }

        });

        builder.setNegativeButton(getString(R.string.CancelButton), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void LoadCredits(){
        String[] Credits = new String[2];
        Credits[0] = getString(R.string.CreditsTitle);
        Credits[1] = getString(R.string.Credits);
        Intent intent = new Intent(MainActivity.this, NewsActivity.class);
        Bundle data = new Bundle();
        data.putStringArray("NewsArray", Credits);
        intent.putExtras(data);
        startActivity(intent);
    }

    private void StartQuickGame(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.QuickGameTitle));
        builder.setMessage(getString(R.string.QuickGame));

        builder.setPositiveButton(getString(R.string.MessageGo), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                gaming = false;
                DBHandler.close();
                dayOpen = false;
                fullGame = false;
                f = new Finance(4);
                p = new Gamer(500000000, 5, 20, 1000);
                time = new Daytime(LocalBroadcastManager.getInstance(MainActivity.this));
                UpdateCentralUI();
                UpdateTopBar(topBarPlayer, topBarDaytime);
                dialog.dismiss();
                gaming = true;
            }

        });

        builder.setNegativeButton(getString(R.string.CancelButton), new DialogInterface.OnClickListener() {

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
        stopRepTask();
        if(playSound)soundplayer.release();
        MainActivity.this.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private void SelectNewGame(){
        if(fullGame) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.NewGameTitle));
            builder.setMessage(getString(R.string.NewGame));

            builder.setPositiveButton(getString(R.string.NewGameTitle), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    gaming = false;
                    dayOpen = false;
                    boolean a = DBHandler.PlaySound();
                    int sound;
                    if(a)sound=1;
                    else sound=0;
                    int assets = p.getAssets();
                    long timeInv = DBHandler.getNextInviteTime();
                    DBHandler.clearData();
                    DBHandler.PrepGame(timeInv, assets, Company.Sectors.values(), sound);
                    f = new Finance(DBHandler, 5);
                    p = new Gamer(DBHandler);
                    time = new Daytime(LocalBroadcastManager.getInstance(MainActivity.this.getApplicationContext()));
                    DBHandler.setEconomySize1(f.getEconSize1());
                    DBHandler.setEconomySize2(f.getEconSize2());
                    dialog.dismiss();
                    UpdateCentralUI();
                    UpdateTopBar(topBarPlayer, topBarDaytime);
                    gaming = true;
                }
            });

            builder.setNegativeButton(getString(R.string.NoButton), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.ToastNewGameImpossible), Toast.LENGTH_LONG).show();
        }
    }

    private double RangedRandom(double Upper, double Lower){
        double diff = Upper-Lower;
        return Math.random()*diff + Lower;
    }

}

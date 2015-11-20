package com.example.galadar.stockxchange;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Galadar on 28/10/2015.
 * Meeting xml parser that returns meeting array list
 */
public class MeetingXMLParser {

    ArrayList<Meeting> meetings;

    private int day;
    private String title;
    private ArrayList<String> speech;
    private String text;

/*
    public ArrayList<Meeting> getMeetings(){
        return meetings;
    }
*/

    public MeetingXMLParser(){
        meetings = new ArrayList<>();
        day = 0;
        title ="";
        speech = new ArrayList<>();
    }

    public ArrayList<Meeting> parse(InputStream is){

        try{
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);

            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, null);

            int EventType = parser.getEventType();
            speech = new ArrayList<>();

            while (EventType != XmlPullParser.END_DOCUMENT){
                String tagname = parser.getName();
                switch (EventType){
                    case XmlPullParser.START_TAG:
                        if(tagname.equalsIgnoreCase("meeting")){
                            day = 0;
                            title = "";
                            speech = new ArrayList<>();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if(tagname.equalsIgnoreCase("day")) day=Integer.parseInt(text);
                        else if(tagname.equalsIgnoreCase("title")) title=text;
                        else if(tagname.equalsIgnoreCase("part")) speech.add(text);
                        else meetings.add(new Meeting(day, title, speech));
                        break;
                    default:
                        break;
                }
                EventType = parser.next();
            }


        } catch (Exception e){
            e.printStackTrace();
        }
        return meetings;
    }
}

package com.example.galadar.stockxchange;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Galadar on 28/10/2015.
 */
public class MeetingXMLParser {
    private static final String ns = null;

    public ArrayList<Meeting> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private ArrayList<Meeting> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Meeting> entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Meeting")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private Meeting readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Meeting");
        String title = null;
        ArrayList<String> speech = null;
        int day = 0;
        String Sday;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            Sday = parser.getAttributeValue(null, "day");
            if (Sday.isEmpty()) {
                day = 0;
            } else {
                day = Integer.parseInt(Sday);
            }
            title = parser.getAttributeValue(null, "title");
            String name = parser.getName();
            while (name.equals("part")) {
                speech.add(readPart(parser));
                parser.nextToken();
                name = parser.getName();
            }
        }
        return new Meeting(day, title, speech);
    }

    private String readPart(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "part");
        String part = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "part");
        return part;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}

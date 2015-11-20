package com.example.galadar.stockxchange;

/**
 * Created by Galadar on 5/11/2015.
 * Event Object
 */
public class Event {
    private int type;
    private int magnitude;
    private int duration;

    public Event(int type, int magnitude) {
        this.type = type;
        this.magnitude = magnitude;
        this.duration = 2*(int)Math.round((double)magnitude/10);
    }

    public Event(int type, int magnitude, int duration) {
        this.type = type;
        this.magnitude = magnitude;
        this.duration = duration;
    }

    public int getType() {
        return type;
    }

    public int getMagnitude() {
        return magnitude;
    }

    public int getDuration() {
        return duration;
    }

    public void dayEnded(){
        this.duration--;
    }

    public boolean eventEnded(){
        return this.duration==0;
    }


}

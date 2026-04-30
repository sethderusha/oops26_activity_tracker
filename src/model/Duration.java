package model;

import java.io.Serializable;

public class Duration implements Serializable, Comparable<Duration> {

    private static final long serialVersionUID = 1L;

    private int hours;
    private int minutes;

    public Duration() {
        this(0, 0);
    }

    public Duration(int hours, int minutes) {
        set(hours, minutes);
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setHours(int hours) {
        set(hours, this.minutes);
    }

    public void setMinutes(int minutes) {
        set(this.hours, minutes);
    }

    public void set(int hours, int minutes) {
        if (hours < 0 || minutes < 0) {
            throw new IllegalArgumentException("Hours and minutes must be non-negative.");
        }
        int totalMinutes = (hours * 60) + minutes;
        this.hours = totalMinutes / 60;
        this.minutes = totalMinutes % 60;
    }

    public int toTotalMinutes() {
        return (hours * 60) + minutes;
    }

    @Override
    public int compareTo(Duration other) {
        if (other == null) {
            return 1;
        }
        return Integer.compare(this.toTotalMinutes(), other.toTotalMinutes());
    }

    @Override
    public String toString() {
        return hours + "h " + minutes + "m";
    }
}

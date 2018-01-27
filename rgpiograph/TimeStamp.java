package rgpiograph;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeStamp extends GregorianCalendar {



    public TimeStamp() { // returns an object corresponding to now
        super();
        LocalDateTime now = LocalDateTime.now();
        this.set(now.getYear(),
                now.getMonth().getValue() - 1, // Calendar works with months 0-11
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                now.getSecond());
    }

    public TimeStamp(int millis) {  
        super();
        this.setTimeInMillis(millis);
    }

    public Integer year() {
        return get(Calendar.YEAR);
    }

    public Integer month() {
        return get(Calendar.MONTH) + 1;
    }

    public Integer day() {
        return get(Calendar.DAY_OF_MONTH);
    }

    public String dayName() {
        String[] days = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        return days[get(Calendar.DAY_OF_WEEK) - 1];
    }

    public String dayShortName() {
        return dayName().substring(0, 3);
    }

    public Integer hour() {
        return get(Calendar.HOUR_OF_DAY);
    }

    public Integer minute() {
        return get(Calendar.MINUTE);
    }

    public Integer second() {
        return get(Calendar.SECOND);
    }

    public String dateName() {
        return this.dayName() + " "
                + this.day() + "/" + this.month() + "/" + this.year() + " "
                + this.hour() + ":" + this.minute() + ":" + this.second();
    }


    public boolean isSameDateAs(TimeStamp t) {
        return (this.year().intValue() == t.year().intValue())
                && (this.month().intValue() == t.month().intValue())
                && (this.day().intValue() == t.day().intValue());
    }

    public int isSecondsLaterThan(TimeStamp t) {
        return (hour() * 3600 + minute() * 60 + second())
                - (t.hour() * 3600 + t.minute() * 60 + t.second());
    }

    public String asLongString() {
        return dayName()
                + " " + day()
                + "/" + month()
                + "/" + year()
                + " " + hour()
                + ":" + minute()
                + " " +second()+"s";

    }

        public String asString() {
        return  day()
                + "/" + month()
                + " " + hour()
                + ":" + minute()
                + ":" +second();

    }

        public String asDashSeparatedString() {
        return  day()
                + "-" + month()
                + "-" + hour()
                + "-" + minute()
                + "-" +second();

    }
        
    static public TimeStamp stringToTimeValue(String s) {
        String[] tokens = s.split(" ");
        String dayName = tokens[0];
        String year = tokens[1];
        String month = tokens[2];
        String day = tokens[3];
        String hour = tokens[4];
        String minute = tokens[5];
        int second = 0;
        TimeStamp t = new TimeStamp();
        t.set(Integer.parseInt(year),
                Integer.parseInt(month) - 1,
                Integer.parseInt(day),
                Integer.parseInt(hour),
                Integer.parseInt(minute),
                second);
        return t;

    }
}

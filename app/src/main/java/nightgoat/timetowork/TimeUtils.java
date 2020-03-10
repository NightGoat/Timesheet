package nightgoat.timetowork;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Calendar;
import java.util.Locale;

public class TimeUtils {

    private static Calendar calendar = Calendar.getInstance();

    public static int getCurrentDay() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static int getCurrentMonth() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.MONTH);
    }

    public static int getCurrentYear() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.YEAR);
    }

    public static String getCurrentTime() {
        return String.format(Locale.getDefault(), "%02d:%02d", getCurrentHour(), getCurrentMinutes());
    }

    public static String getTime(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    public static int getCurrentHour() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getCurrentMinutes() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.MINUTE);
    }

    public static String getCurrentDate(){
        DateTimeFormatter sqlFormat = DateTimeFormat.forPattern("yyyy.MM.dd");
        return sqlFormat.print(System.currentTimeMillis());
    }

    public static String countTimeSum(String time1, String time2){
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .minimumPrintedDigits(2)
                .printZeroAlways()
                .appendHours()
                .appendLiteral(":")
                .appendMinutes()
                .toFormatter();
        Period period1 = formatter.parsePeriod(time1);
        Period period2 = formatter.parsePeriod(time2);
        Period period3 = period1.plus(period2);
        if (period3.getMinutes() >= 60){
            period3 = period3.plusHours(1).minusMinutes(60);
        }
        if (period3.getHours() >= 24) {
            period3 = period3.minusHours(24);
        }
        String result = formatter.print(period3);
        Log.d("TimeUtils", "countTimeSum: result: " + result);
        return result;
    }

    public static String getDateInNormalFormat(String sqlDate){
        DateTimeFormatter normalFormat = DateTimeFormat.forPattern("dd.MM.yyyy");
        DateTimeFormatter sqlFormat = DateTimeFormat.forPattern("yyyy.MM.dd");
        DateTime sqlDateTime = sqlFormat.parseDateTime(sqlDate);
        return normalFormat.print(sqlDateTime.getMillis());
    }

    public static String getDayOfTheWeek(String sqlDate){
        DateTimeFormatter sqlFormat = DateTimeFormat.forPattern("yyyy.MM.dd");
        DateTime sqlDateTime = sqlFormat.parseDateTime(sqlDate);
        return StringUtils.capitalize(sqlDateTime.dayOfWeek().getAsText(Locale.getDefault()));
    }
}

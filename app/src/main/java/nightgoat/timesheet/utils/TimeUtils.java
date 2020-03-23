package nightgoat.timesheet.utils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

public abstract class TimeUtils {

    private static final String TAG = TimeUtils.class.getName();
    private static Calendar calendar = Calendar.getInstance();

    public static int getCurrentMonth() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static String getMonthString(String month) {
        DateTimeFormatter sqlFormat = DateTimeFormat.forPattern("MM");
        DateTime dt = sqlFormat.parseDateTime(month);
        Timber.d("getMonthString: month: %s, string: %s",
                month,
                StringUtils.capitalize(dt.monthOfYear().getAsText(Locale.getDefault())));
        return StringUtils.capitalize(dt.monthOfYear().getAsText(Locale.getDefault()));
    }

    public static String getMonthStringShort(String month) {
        DateTimeFormatter sqlFormat = DateTimeFormat.forPattern("M");
        DateTime dt = sqlFormat.parseDateTime(month);
        return dt.toString("MMM", Locale.getDefault());
    }

    public static int getMonthInt(String month) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM").withLocale(Locale.getDefault());
        DateTime dt = formatter.parseDateTime(month);
        return dt.getMonthOfYear();
    }

    public static int getCurrentYear() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.YEAR);
    }

    public static String getCurrentYearString() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return String.valueOf(calendar.get(Calendar.YEAR));
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

    public static String getCurrentDate() {
        DateTimeFormatter sqlFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
        return sqlFormat.print(System.currentTimeMillis());
    }

    public static String getCurrentDateNormalFormat() {
        DateTimeFormatter sqlFormat = DateTimeFormat.forPattern("dd.MM.yyyy");
        return sqlFormat.print(System.currentTimeMillis());
    }

    public static String getDateInNormalFormat(String sqlDate) {
        DateTimeFormatter normalFormat = DateTimeFormat.forPattern("dd.MM.yyyy");
        DateTimeFormatter sqlFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime sqlDateTime = sqlFormat.parseDateTime(sqlDate);
        return normalFormat.print(sqlDateTime.getMillis());
    }

    public static String getDayOfTheWeek(String sqlDate) {
        DateTimeFormatter sqlFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime sqlDateTime = sqlFormat.parseDateTime(sqlDate);
        return StringUtils.capitalize(sqlDateTime.dayOfWeek().getAsText(Locale.getDefault()));
    }

    public static String getDayOfTheWeekShort(String sqlDate) {
        DateTimeFormatter sqlFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeFormatter dayOfWeekFormat = DateTimeFormat.forPattern("E");
        DateTime sqlDateTime = sqlFormat.parseDateTime(sqlDate);
        return dayOfWeekFormat.print(sqlDateTime);
    }

    public static String countTimeSum24(String time1, String time2) {
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
        if (period3.getMinutes() >= 60) {
            period3 = period3.plusHours(1).minusMinutes(60);
        }
        if (period3.getHours() >= 24) {
            period3 = period3.minusHours(24);
        }
        Timber.tag(TAG).i("time1(%s) + time2(%s) = %s", time1, time2, formatter.print(period3));
        return formatter.print(period3);
    }

    public static String countTimeSum(String time1, String time2) {
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
        if (period3.getMinutes() >= 60) {
            period3 = period3.plusHours(1).minusMinutes(60);
        }
        Timber.tag(TAG).i("time1(%s) + time2(%s) = %s", time1, time2, formatter.print(period3));
        return formatter.print(period3);
    }

    public static String countTimeDiff(String time1, String time2) {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .minimumPrintedDigits(2)
                .printZeroAlways()
                .appendHours()
                .appendLiteral(":")
                .appendMinutes()
                .toFormatter();
        DateTimeFormatter formatter2 = DateTimeFormat.forPattern("HH:mm");
        DateTime time1DT = DateTime.parse(time1, formatter2);
        DateTime time2DT = DateTime.parse(time2, formatter2);
        Period period = new Period(time1DT, time2DT);
        String result;
        if (period.getMinutes() < 0) {
            return formatter.print(period.withMinutes(period.getMinutes() * -1));
        } else
            return "+" + formatter.print(period);
    }
}

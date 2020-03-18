package nightgoat.timesheet.database;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import nightgoat.timesheet.domain.DaysRepository;

public class DaysRepositoryImpl implements DaysRepository {

    private static final String TAG = DaysRepositoryImpl.class.getName();

    private final DaysDao daysDao;

    public DaysRepositoryImpl(DaysDao daysDao) {
        this.daysDao = daysDao;
    }

    public Flowable<List<DayEntity>> getAllDays() {
        return daysDao.getAllDays();
    }

    public Completable addDay(DayEntity model) {
        return daysDao.insertDay(model);
    }

    public Completable updateDay(DayEntity model) {
        model.setTimeWorked(countComeGoneDifference(model));
        return daysDao.updateDay(model);
    }

    public Maybe<DayEntity> getDayByDayModel(String date) {
        return daysDao.getDayByDate(date);
    }

    public Completable deleteDaysWithoutTime() {
        return daysDao.deleteDaysWithoutTime();
    }

    @Override
    public Completable deleteDay(DayEntity dayEntity) {
        return daysDao.deleteDay(dayEntity);
    }

    @Override
    public Completable deleteEverything() {
        return daysDao.deleteEverything();
    }

    @Override
    public Flowable<List<DayEntity>> getDaysWorkedTimeNonNull(int month, int year) {
        return daysDao.getDaysWorkedTimeNonNull(month, year);
    }

    @Override
    public Flowable<String> getWorkedHoursSum(String month, String year) {
        Log.d("DaysRepositoryImpl", "getWorkedHoursSum: ");
        return daysDao.getWorkedHoursSum(month, year);
    }

    private String countComeGoneDifference(DayEntity dayEntity) {
        String timeCome = dayEntity.getTimeCome();
        String timeGone = dayEntity.getTimeGone();
        Log.d("DaysViewModel", "countComeGoneDifference: ");
        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
        if (timeCome != null && timeGone != null) {
            DateTime comeTimeDT = DateTime.parse(timeCome, formatter);
            DateTime goneTimeDT = DateTime.parse(timeGone, formatter);
            if (goneTimeDT.isAfter(comeTimeDT)) {
                return formatter.print(new Duration(comeTimeDT, goneTimeDT).getMillis());
            } else {
                return null;
            }
        } else {
            Log.e(TAG, "ERROR counting difference because timeCome is: " + timeCome + " and timeGone is: " + timeGone);
            return null;
        }
    }
}
package nightgoat.timesheet.database;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import nightgoat.timesheet.domain.DaysRepository;
import timber.log.Timber;

public class DaysRepositoryImpl implements DaysRepository {

    private static final String TAG = DaysRepositoryImpl.class.getName();

    private final DaysDao daysDao;

    public DaysRepositoryImpl(DaysDao daysDao) {
        this.daysDao = daysDao;
    }

    @Override
    public Flowable<List<DayEntity>> getAllDays() {
        return daysDao.getAllDays();
    }

    @Override
    public Flowable<List<DayEntity>> getAllDays(int month, int year) {
        return daysDao.getAllDays(String.format(Locale.getDefault(),"%02d", month), String.valueOf(year));
    }

    @Override
    public Completable addDay(DayEntity model) {
        return daysDao.insertDay(model);
    }

    @Override
    public Completable updateDay(DayEntity model) {
        model.setTimeWorked(countComeGoneDifference(model));
        return daysDao.updateDay(model);
    }

    @Override
    public Maybe<DayEntity> getDayByDayModel(String date) {
        return daysDao.getDayByDate(date);
    }

    @Override
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
    public Flowable<List<String>> getWorkedHoursSumList(String month, String year) {
        Timber.tag(TAG).d("getWorkedHoursSum: ");
        return daysDao.getWorkedHoursSumList(month, year);
    }

    private String countComeGoneDifference(DayEntity dayEntity) {
        String timeCome = dayEntity.getTimeCame();
        String timeGone = dayEntity.getTimeGone();
        Timber.tag(TAG).d("countComeGoneDifference: ");
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
            Timber.tag(TAG).e("ERROR counting difference because timeCome is: " + timeCome + " and timeGone is: " + timeGone);
            return null;
        }
    }
}
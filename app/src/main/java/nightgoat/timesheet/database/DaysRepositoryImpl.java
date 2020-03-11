package nightgoat.timesheet.database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
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
}
package nightgoat.timetowork.database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import nightgoat.timetowork.domain.DaysDataSourceRep;

public class DaysSourceRepImpl implements DaysDataSourceRep {

    private static final String TAG = DaysSourceRepImpl.class.getName();

    private final DaysDao daysDao;

    public DaysSourceRepImpl(DaysDao daysDao) {
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
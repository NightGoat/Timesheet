package nightgoat.timesheet.domain;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import nightgoat.timesheet.database.DayEntity;

public class Interactor {

    private DaysRepository daysDataSource;

    public Interactor(DaysRepository daysDataSource) {
        this.daysDataSource = daysDataSource;
    }

    public Flowable<List<DayEntity>> getAllDays() {
        return daysDataSource.getAllDays().subscribeOn(Schedulers.io());
    }

    public Completable addDay(DayEntity model) {
        return daysDataSource.addDay(model).subscribeOn(Schedulers.io());
    }

    public Completable updateDay(DayEntity model) {
        return daysDataSource.updateDay(model).subscribeOn(Schedulers.io());
    }

    public Maybe<DayEntity> getDayByDayModel(String date) {
        return daysDataSource.getDayByDayModel(date).subscribeOn(Schedulers.io());
    }

    public Completable deleteDaysWithoutTime() {
        return daysDataSource.deleteDaysWithoutTime().subscribeOn(Schedulers.io());
    }

    public Completable deleteDay(DayEntity dayEntity){
        return daysDataSource.deleteDay(dayEntity).subscribeOn(Schedulers.io());
    }

    public Completable deleteEverything() {
        return daysDataSource.deleteEverything().subscribeOn(Schedulers.io());
    }
}
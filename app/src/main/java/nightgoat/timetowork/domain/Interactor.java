package nightgoat.timetowork.domain;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import nightgoat.timetowork.database.DayEntity;

public class Interactor {

    private DaysDataSourceRep daysDataSource;

    public Interactor(DaysDataSourceRep daysDataSource) {
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
}

package nightgoat.timesheet.domain;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import nightgoat.timesheet.database.DayEntity;
import nightgoat.timesheet.utils.TimeUtils;

public class Interactor {

    private DaysRepository daysDataSource;
    public Interactor(DaysRepository daysDataSource) {
        this.daysDataSource = daysDataSource;
    }

    public Flowable<List<DayEntity>> getAllDays() {
        return daysDataSource.getAllDays().subscribeOn(Schedulers.io());
    }

    public Flowable<List<DayEntity>> getAllDays(int month, int year) {
        return daysDataSource.getAllDays(month, year).subscribeOn(Schedulers.io());
    }

    public Completable addDay(DayEntity model) {
        return daysDataSource.addDay(model).subscribeOn(Schedulers.io());
    }

    public Completable updateDay(DayEntity model) {
        return daysDataSource.updateDay(model).subscribeOn(Schedulers.io());
    }

    public Completable updateDayTimeOut(DayEntity model) {
            //TODO implement timeout from accident clicks
            return daysDataSource.updateDay(model).subscribeOn(Schedulers.io());
    }

    public Maybe<DayEntity> getDayEntityByDay(String date) {
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

    public Flowable<String> getWorkedHoursSum(String month, String year){
        return daysDataSource
                .getWorkedHoursSumList(month, year)
                .subscribeOn(Schedulers.io())
                .map(strings -> {
            String sum = "00:00";
            for (int i = 0; i < strings.size(); i++) {
                sum = TimeUtils.countTimeSum(sum, strings.get(i));
            }
            return sum;
        });
    }
}

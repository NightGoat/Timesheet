package nightgoat.timesheet.domain;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import nightgoat.timesheet.database.DayEntity;

public interface DaysRepository {

    Flowable<List<DayEntity>> getAllDays();

    Flowable<List<DayEntity>> getAllDays(int month, int year);

    Completable addDay(DayEntity model);

    Completable updateDay(DayEntity model);

    Maybe<DayEntity> getDayByDayModel(String date);

    Completable deleteDaysWithoutTime();

    Completable deleteDay(DayEntity model);

    Completable deleteEverything();

    Flowable<List<String>> getWorkedHoursSumList(String month, String year);

}

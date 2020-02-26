package nightgoat.timetowork.domain;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import nightgoat.timetowork.database.DayEntity;

public interface DaysDataSource {

    Flowable<List<DayEntity>> getAllDays();

    Completable addDay(DayEntity model);

    Completable updateDay(DayEntity model);

    Maybe<DayEntity> getDayByDayModel(String date);

    Completable deleteDaysWithoutTime();
}

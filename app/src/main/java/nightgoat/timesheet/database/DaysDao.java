package nightgoat.timesheet.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Dao
public interface DaysDao {

    @Query("SELECT * FROM days ORDER BY date")
    Flowable<List<DayEntity>> getAllDays();

    @Query("SELECT * FROM days WHERE strftime('%m', date) = :month AND strftime('%Y', date) = :year ORDER BY date")
    Flowable<List<DayEntity>> getAllDays(String month, String year);

    @Query("SELECT Worked_time FROM days WHERE strftime('%Y', date) = :year AND strftime('%m', date) = :month AND worked_time IS NOT NULL")
    Flowable<List<String>> getWorkedHoursSumList(String month, String year);

    @Query("SELECT * FROM days WHERE date = :date")
    Maybe<DayEntity> getDayByDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertDay(DayEntity model);

    @Update
    Completable updateDay(DayEntity model);

    @Delete
    Completable deleteDay(DayEntity model);

    @Query("DELETE FROM days WHERE came is NULL AND gone is NULL")
    Completable deleteDaysWithoutTime();

    @Query("DELETE FROM days")
    Completable deleteEverything();
}

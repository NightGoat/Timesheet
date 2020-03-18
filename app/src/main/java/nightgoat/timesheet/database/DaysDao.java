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

    @Query("SELECT * FROM days WHERE worked_time IS NOT NULL ORDER BY date")
    Flowable<List<DayEntity>> getDaysWorkedTimeNonNull();

    @Query("SELECT * FROM days ORDER BY date")
    Flowable<List<DayEntity>> getAllDays();

    @Query("SELECT * FROM days WHERE strftime('%m', date) = :month AND strftime('%Y', date) = :year AND worked_time IS NOT NULL")
    Flowable<List<DayEntity>> getDaysWorkedTimeNonNull(int month, int year);

    @Query("SELECT time(sum(strftime('%s', worked_time)), 'unixepoch') FROM days WHERE strftime('%Y', date) = :year AND strftime('%m', date) = :month")
    Flowable<String> getWorkedHoursSum(String month, String year);

    @Query("SELECT * FROM days WHERE date = :date")
    Maybe<DayEntity> getDayByDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertDay(DayEntity model);

    @Update
    Completable updateDay(DayEntity model);

    @Delete
    Completable deleteDay(DayEntity model);

    @Query("DELETE FROM days WHERE time_come is NULL AND time_gone is NULL")
    Completable deleteDaysWithoutTime();

    @Query("DELETE FROM days")
    Completable deleteEverything();
}

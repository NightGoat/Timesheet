package nightgoat.timetowork.database;

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
    Flowable<List<DayEntity>> getAllDays();

    @Query("SELECT * FROM days WHERE date = :date")
    Maybe<DayEntity> getDayByDate(String date);
//
//    @Query("SELECT * FROM days WHERE date = :date")
//    Flowable<DayEntity> getDayByDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertDay(DayEntity model);

    @Update
    Completable updateDay(DayEntity model);

    @Delete
    void deleteDay(DayEntity model);

    @Query("DELETE FROM days WHERE time_come is NULL AND time_gone is NULL")
    Completable deleteDaysWithoutTime();
}

package nightgoat.timesheet.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;


@Database(entities = {DayEntity.class},
        version = 4,
        exportSchema = false)
public abstract class DaysDatabase extends RoomDatabase {

    private static volatile DaysDatabase INSTANCE;

    public abstract DaysDao getDaysDao();

    public static DaysDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DaysDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            DaysDatabase.class,
                            "days_database.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

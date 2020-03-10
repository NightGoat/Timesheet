package nightgoat.timetowork.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;


import java.util.Objects;

@Entity(tableName = "days", indices = {@Index(value = {"date", "time_come", "time_gone"})})
public class DayEntity {

    @PrimaryKey
    @ColumnInfo(name = "date")
    @NonNull
    private String date;

    @ColumnInfo(name = "time_come")
    private String timeCome;

    @ColumnInfo(name = "time_gone")
    private String timeGone;

    @ColumnInfo(name = "worked_time")
    private String timeWorked;

    public DayEntity(@NonNull String date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DayEntity dayEntity = (DayEntity) o;
        return date.equals(dayEntity.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public String getTimeCome() {
        return timeCome;
    }

    public String getTimeGone() {
        return timeGone;
    }

    public String getTimeWorked() {
        return timeWorked;
    }

    public void setTimeWorked(String timeWorked) {
        this.timeWorked = timeWorked;
    }

    public void setDate(int day, int month, int year) {
        this.date = day+"."+month+"."+"year";
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public void setTimeCome(String timeCome) {
        this.timeCome = timeCome;
    }

    public void setTimeCome(int hour, int minute) {
        this.timeCome = hour+":"+minute;
    }

    public void setTimeGone(String timeGone) {
        this.timeGone = timeGone;
    }

    public void setTimeGone(int hour, int minute) {
        this.timeGone = hour+":"+minute;
    }
}

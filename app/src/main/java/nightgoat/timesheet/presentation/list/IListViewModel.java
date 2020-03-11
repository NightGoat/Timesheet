package nightgoat.timesheet.presentation.list;

import nightgoat.timesheet.database.DayEntity;

public interface IListViewModel {

    void deleteDay(DayEntity dayEntity);
    void setGoneTime(DayEntity day, String time);
    void setCameTime(DayEntity day, String time);
}

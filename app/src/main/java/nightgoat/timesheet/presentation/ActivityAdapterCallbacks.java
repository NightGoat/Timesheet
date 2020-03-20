package nightgoat.timesheet.presentation;

import nightgoat.timesheet.database.DayEntity;

public interface ActivityAdapterCallbacks {

    void onClickFinish(String day);
    void onClickDelete(DayEntity day);
    void onClickChip(DayEntity day, int timeType);
    void onClickCameChipClose(DayEntity day);
    void onClickGoneChipClose(DayEntity day);
}

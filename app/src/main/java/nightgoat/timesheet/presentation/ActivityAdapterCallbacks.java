package nightgoat.timesheet.presentation;

import nightgoat.timesheet.database.DayEntity;
import nightgoat.timesheet.utils.TimeType;

public interface ActivityAdapterCallbacks {

    void onClickFinish(String day);
    void onClickDelete(DayEntity day);
    void onClickChip(DayEntity day, TimeType type);
    void onClickCameChipClose(DayEntity day);
    void onClickGoneChipClose(DayEntity day);
}

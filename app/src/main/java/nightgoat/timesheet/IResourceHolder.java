package nightgoat.timesheet;

import com.ajts.androidmads.library.SQLiteToExcel;

public interface IResourceHolder {

    String getDirectory();
    SQLiteToExcel createSQLiteToExcel();
    String getTimeNeedToWorkFromPreferences();
    void setTimeNeedToWorkFromPreferences(String time);
    String getString(int resource);
}

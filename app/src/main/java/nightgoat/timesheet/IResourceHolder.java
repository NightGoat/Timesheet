package nightgoat.timesheet;

import android.content.ContentResolver;

import com.ajts.androidmads.library.SQLiteToExcel;

public interface IResourceHolder {

    String getDirectory();
    SQLiteToExcel createSQLiteToExcel();
    String getTimeNeedToWorkFromPreferences();
    String getString(int resource);
}

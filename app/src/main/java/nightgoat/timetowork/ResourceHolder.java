package nightgoat.timetowork;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.ajts.androidmads.library.SQLiteToExcel;

import java.util.Locale;
import java.util.Objects;

public class ResourceHolder implements IResourceHolder {

    private Context context;
    private SharedPreferences sharedPreferences;

    ResourceHolder(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    @Override
    public String getDirectory() {
        return Objects.requireNonNull(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)).getPath() + "/TimeToWork/";
    }

    @Override
    public SQLiteToExcel createSQLiteToExcel() {
        return new SQLiteToExcel(context, "days_database.db", getDirectory());
    }

    @Override
    public String getTimeNeedToWorkFromPreferences() {
        return sharedPreferences.getString("needToWork", "08:30");
    }

    @Override
    public void setTimeNeedToWorkFromPreferences(String time) {
        sharedPreferences
                .edit()
                .putString("needToWork", time)
                .apply();
    }

    @Override
    public String getString(int resource) {
        return context.getString(resource);
    }

}

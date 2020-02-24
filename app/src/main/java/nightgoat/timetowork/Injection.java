package nightgoat.timetowork;

import android.content.Context;

import nightgoat.timetowork.database.DaysDatabase;
import nightgoat.timetowork.database.DaysSource;
import nightgoat.timetowork.ui.ViewModelFactory;

public class Injection {

    private static DaysDataSource provideDaysDataSource(Context context) {
        DaysDatabase database = DaysDatabase.getInstance(context);
        return new DaysSource(database.getDaysDao());
    }

    public static ViewModelFactory provideViewModelFactory(Context context) {
        DaysDataSource dataSource = provideDaysDataSource(context);
        return new ViewModelFactory(dataSource);
    }
}

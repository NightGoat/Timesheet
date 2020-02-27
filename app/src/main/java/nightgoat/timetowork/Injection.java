package nightgoat.timetowork;

import android.content.Context;

import nightgoat.timetowork.database.DaysDatabase;
import nightgoat.timetowork.database.DaysSourceRepImpl;
import nightgoat.timetowork.domain.DaysDataSourceRep;
import nightgoat.timetowork.domain.Interactor;
import nightgoat.timetowork.presentation.ViewModelFactory;

public class Injection {

    private static DaysDataSourceRep provideDaysDataSource(Context context) {
        DaysDatabase database = DaysDatabase.getInstance(context);
        return new DaysSourceRepImpl(database.getDaysDao());
    }

    public static ViewModelFactory provideViewModelFactory(Context context) {
        DaysDataSourceRep dataSource = provideDaysDataSource(context);
        Interactor interactor = new Interactor(dataSource);
        return new ViewModelFactory(interactor);
    }

}

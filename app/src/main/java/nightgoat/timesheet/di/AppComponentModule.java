package nightgoat.timesheet.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nightgoat.timesheet.database.DaysDao;
import nightgoat.timesheet.database.DaysDatabase;

@Module
public class AppComponentModule {

    @Provides
    @Singleton
    DaysDatabase provideDatabase(Context context){
        return DaysDatabase.getInstance(context);
    }

    @Provides
    @Singleton
    DaysDao provideDaysDao(DaysDatabase database){
        return database.getDaysDao();
    }
}

package nightgoat.timetowork.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nightgoat.timetowork.database.DaysDao;
import nightgoat.timetowork.database.DaysDatabase;

@Module
public class AppComponentModule {

    @Provides
    @Singleton
    DaysDatabase provideDatabase(Context context){
        return DaysDatabase.getInstance(context);
    }

    @Provides
    @Singleton
    DaysDao provideRepository(DaysDatabase database){
        return database.getDaysDao();
    }
}

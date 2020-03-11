package nightgoat.timesheet.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nightgoat.timesheet.database.DaysDao;
import nightgoat.timesheet.database.DaysRepositoryImpl;
import nightgoat.timesheet.domain.DaysRepository;

@Module
public class DataModule {

    @Provides
    @Singleton
    DaysRepository provideRepository(DaysDao dao){
        return new DaysRepositoryImpl(dao);
    }
}

package nightgoat.timetowork.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nightgoat.timetowork.database.DaysDao;
import nightgoat.timetowork.database.DaysRepositoryImpl;
import nightgoat.timetowork.domain.DaysRepository;

@Module
public class DataModule {

    @Provides
    @Singleton
    DaysRepository provideRepository(DaysDao dao){
        return new DaysRepositoryImpl(dao);
    }
}

package nightgoat.timesheet.di;

import dagger.Module;
import dagger.Provides;
import nightgoat.timesheet.domain.DaysRepository;
import nightgoat.timesheet.domain.Interactor;

@Module
public class InteractorModule {

    @Provides
    @ActivityScope
    Interactor provideInteractor(DaysRepository repository){
        return new Interactor(repository);
    }
}

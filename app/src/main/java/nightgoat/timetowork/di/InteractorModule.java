package nightgoat.timetowork.di;

import dagger.Module;
import dagger.Provides;
import nightgoat.timetowork.domain.DaysRepository;
import nightgoat.timetowork.domain.Interactor;

@Module
public class InteractorModule {

    @Provides
    @ActivityScope
    Interactor provideInteractor(DaysRepository repository){
        return new Interactor(repository);
    }
}

package nightgoat.timetowork.di;

import javax.inject.Singleton;

import dagger.Component;
import nightgoat.timetowork.IResourceHolder;
import nightgoat.timetowork.domain.DaysRepository;

@Component(
        modules = {
                AppComponentModule.class,
                ContextModule.class,
                DataModule.class
        }
)
@Singleton
public interface AppComponent {
    DaysRepository getRepository();
    IResourceHolder getResoursceHolder();
}

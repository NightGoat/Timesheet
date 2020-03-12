package nightgoat.timesheet.di;

import javax.inject.Singleton;

import dagger.Component;
import nightgoat.timesheet.IResourceHolder;
import nightgoat.timesheet.domain.DaysRepository;

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
    IResourceHolder getResourceHolder();
}

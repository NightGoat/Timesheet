package nightgoat.timesheet.di;

import androidx.appcompat.app.AppCompatActivity;

import dagger.BindsInstance;
import dagger.Component;
import nightgoat.timesheet.domain.Interactor;
import nightgoat.timesheet.presentation.list.ListActivity;
import nightgoat.timesheet.presentation.main.MainActivity;
import nightgoat.timesheet.presentation.settings.SettingsActivity;

@Component(modules = {
        InteractorModule.class,
        ActivityModule.class},
           dependencies = AppComponent.class)

@ActivityScope
public interface ActivityComponent {

    @Component.Builder
    interface Builder{
        @BindsInstance
        Builder setActivity(AppCompatActivity activity);
        Builder setDependencies(AppComponent dependencies);
        Builder interactorModule(InteractorModule module);
        ActivityComponent build();
    }

    void inject(MainActivity activity);

    void inject(ListActivity activity);

    void inject(SettingsActivity activity);
}
package nightgoat.timesheet.di;

import dagger.Component;
import nightgoat.timesheet.presentation.list.ListActivity;
import nightgoat.timesheet.presentation.main.MainActivity;
import nightgoat.timesheet.presentation.settings.SettingsActivity;

@Component(modules = InteractorModule.class,
        dependencies = AppComponent.class)
@ActivityScope
public interface AcitivityComponent {
    void inject(MainActivity activity);
    void inject(ListActivity activity);
    void inject(SettingsActivity activity);
}
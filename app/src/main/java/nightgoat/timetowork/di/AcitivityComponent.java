package nightgoat.timetowork.di;

import dagger.Component;
import nightgoat.timetowork.presentation.list.ListActivity;
import nightgoat.timetowork.presentation.main.MainActivity;
import nightgoat.timetowork.presentation.settings.SettingsActivity;

@Component(modules = InteractorModule.class,
        dependencies = AppComponent.class)
@ActivityScope
public interface AcitivityComponent {
    void inject(MainActivity activity);
    void inject(ListActivity activity);
    void inject(SettingsActivity activity);
}
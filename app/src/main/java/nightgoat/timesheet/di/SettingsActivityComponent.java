package nightgoat.timesheet.di;

import androidx.appcompat.app.AppCompatActivity;

import dagger.BindsInstance;
import dagger.Component;
import nightgoat.timesheet.presentation.settings.SettingsActivity;

@Component(modules = {
        InteractorModule.class,
        SettingsActivityModule.class},
        dependencies = AppComponent.class)

@ActivityScope
public interface SettingsActivityComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder setActivity(AppCompatActivity activity);
        Builder setDependencies(AppComponent dependencies);
        SettingsActivityComponent build();
    }

    void inject(SettingsActivity activity);
}
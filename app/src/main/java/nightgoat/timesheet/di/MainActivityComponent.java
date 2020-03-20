package nightgoat.timesheet.di;

import androidx.appcompat.app.AppCompatActivity;

import dagger.BindsInstance;
import dagger.Component;
import nightgoat.timesheet.presentation.main.MainActivity;

@Component(modules = {
        InteractorModule.class,
        MainActivityModule.class},
        dependencies = AppComponent.class)

@ActivityScope
public interface MainActivityComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder setActivity(AppCompatActivity activity);
        Builder setDependencies(AppComponent dependencies);
        MainActivityComponent build();
    }

    void inject(MainActivity activity);
}
package nightgoat.timesheet.di;

import androidx.appcompat.app.AppCompatActivity;

import dagger.BindsInstance;
import dagger.Component;
import nightgoat.timesheet.presentation.list.ListActivity;

@Component(modules = {
        InteractorModule.class,
        ListActivityModule.class},
        dependencies = AppComponent.class)

@ActivityScope
public interface ListActivityComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder setActivity(AppCompatActivity activity);
        Builder setDependencies(AppComponent dependencies);
        ListActivityComponent build();
    }

    void inject(ListActivity activity);
}
package nightgoat.timesheet.di;

import dagger.Component;
import nightgoat.timesheet.WidgetProvider;
import nightgoat.timesheet.WidgetThreeBtnsProvider;

@Component(modules =
        InteractorModule.class,
        dependencies = AppComponent.class)

@ActivityScope
public interface BroadcastReceiverComponent {

    void inject(WidgetProvider provider);
    void inject(WidgetThreeBtnsProvider provider);
}
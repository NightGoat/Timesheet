package nightgoat.timesheet;

import android.app.Application;

import nightgoat.timesheet.di.AppComponent;
import nightgoat.timesheet.di.AppComponentModule;
import nightgoat.timesheet.di.ContextModule;
import nightgoat.timesheet.di.DaggerAppComponent;
import nightgoat.timesheet.di.DataModule;
import timber.log.Timber;

public class App extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

        appComponent = DaggerAppComponent
                .builder()
                .appComponentModule(new AppComponentModule())
                .contextModule(new ContextModule(getApplicationContext()))
                .dataModule(new DataModule())
                .build();
        super.onCreate();
    }

    public AppComponent getAppComponent(){
        return appComponent;
    }

}

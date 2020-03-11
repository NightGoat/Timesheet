package nightgoat.timetowork;

import android.app.Application;

import nightgoat.timetowork.di.AppComponent;
import nightgoat.timetowork.di.AppComponentModule;
import nightgoat.timetowork.di.ContextModule;
import nightgoat.timetowork.di.DaggerAppComponent;
import nightgoat.timetowork.di.DataModule;

public class App extends Application {

    private AppComponent appComponent;


    @Override
    public void onCreate() {
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

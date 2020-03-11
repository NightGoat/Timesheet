package nightgoat.timesheet.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nightgoat.timesheet.IResourceHolder;
import nightgoat.timesheet.ResourceHolder;

@Module
public class ContextModule {

    private Context context;

    public ContextModule(Context context){
        this.context = context;
    }

    @Provides
    @Singleton
    Context provideContext(){
        return context;
    }

    @Provides
    @Singleton
    IResourceHolder provideResourceHolder(Context context){
        return new ResourceHolder(context);
    }
}

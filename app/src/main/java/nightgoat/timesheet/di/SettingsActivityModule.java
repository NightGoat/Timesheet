package nightgoat.timesheet.di;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import dagger.Module;
import dagger.Provides;
import nightgoat.timesheet.IResourceHolder;
import nightgoat.timesheet.domain.Interactor;
import nightgoat.timesheet.presentation.ViewModelFactory;
import nightgoat.timesheet.presentation.settings.SettingsViewModel;

@Module
class SettingsActivityModule {

    @Provides
    SettingsViewModel provideSettingsViewModel(AppCompatActivity activity, ViewModelFactory factory) {
        return new ViewModelProvider(activity, factory)
                .get(SettingsViewModel.class);
    }

    @Provides
    ViewModelFactory provideViewModelFactory(Interactor interactor, IResourceHolder holder){
        return new ViewModelFactory(interactor, holder);
    }
}

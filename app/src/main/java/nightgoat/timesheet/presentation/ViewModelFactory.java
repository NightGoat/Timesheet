package nightgoat.timesheet.presentation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import nightgoat.timesheet.domain.Interactor;
import nightgoat.timesheet.presentation.list.ListViewModel;
import nightgoat.timesheet.presentation.main.DaysViewModel;
import nightgoat.timesheet.IResourceHolder;
import nightgoat.timesheet.presentation.settings.SettingsViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final Interactor interactor;
    private final IResourceHolder resourceHolder;

    public ViewModelFactory(Interactor interactor, IResourceHolder resourceHolder) {
        this.interactor = interactor;
        this.resourceHolder = resourceHolder;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DaysViewModel.class)) {
            //noinspection unchecked
            return (T) new DaysViewModel(interactor, resourceHolder);
        }

        if (modelClass.isAssignableFrom(ListViewModel.class)) {
            //noinspection unchecked
            return (T) new ListViewModel(interactor);
        }

        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            //noinspection unchecked
            return (T) new SettingsViewModel(interactor, resourceHolder);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

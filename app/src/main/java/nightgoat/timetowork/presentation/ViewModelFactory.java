package nightgoat.timetowork.presentation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import nightgoat.timetowork.domain.DaysDataSource;
import nightgoat.timetowork.presentation.list.ListViewModel;
import nightgoat.timetowork.presentation.main.DaysViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final DaysDataSource mDataSource;

    public ViewModelFactory(DaysDataSource dataSource) {
        mDataSource = dataSource;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DaysViewModel.class)) {
            //noinspection unchecked
            return (T) new DaysViewModel(mDataSource);
        }

        if (modelClass.isAssignableFrom(ListViewModel.class)) {
            //noinspection unchecked
            return (T) new ListViewModel(mDataSource);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

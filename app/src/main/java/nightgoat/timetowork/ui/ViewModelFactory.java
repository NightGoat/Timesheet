package nightgoat.timetowork.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import nightgoat.timetowork.DaysDataSource;
import nightgoat.timetowork.ui.listActivity.ListViewModel;
import nightgoat.timetowork.ui.mainActivity.DaysViewModel;

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

package nightgoat.timetowork.presentation.list;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import nightgoat.timetowork.domain.DaysDataSource;
import nightgoat.timetowork.database.DayEntity;
import nightgoat.timetowork.domain.Interactor;

public class ListViewModel extends ViewModel implements LifecycleObserver {

    private final DaysDataSource mDataSource;
    MutableLiveData<List<DayEntity>> daysLD = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ListViewModel(DaysDataSource dataSource) {
        mDataSource = dataSource;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart() {
        compositeDisposable.add(
                mDataSource.getAllDays()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dayEntities -> daysLD.setValue(dayEntities)));

    }

    @Override
    protected void onCleared() {
        compositeDisposable.clear();
        super.onCleared();
    }
}

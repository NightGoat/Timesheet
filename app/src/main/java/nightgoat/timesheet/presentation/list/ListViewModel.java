package nightgoat.timesheet.presentation.list;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import nightgoat.timesheet.database.DayEntity;
import nightgoat.timesheet.domain.Interactor;

public class ListViewModel extends ViewModel implements LifecycleObserver, IListViewModel {

    private Interactor interactor;
    MutableLiveData<List<DayEntity>> daysLD = new MutableLiveData<>();
    private CompositeDisposable mDisposable = new CompositeDisposable();

    public ListViewModel(Interactor interactor) {
         this.interactor = interactor;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart() {
        mDisposable.add(
                interactor.getAllDays()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dayEntities -> daysLD.setValue(dayEntities)));
    }

    @Override
    protected void onCleared() {
        mDisposable.clear();
        super.onCleared();
    }

    @Override
    public void deleteDay(DayEntity dayEntity) {
        interactor.deleteDay(dayEntity).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    @Override
    public void setGoneTime(DayEntity dayEntity, String time) {
        dayEntity.setTimeGone(time);
        mDisposable.add(
                interactor.updateDay(dayEntity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());
    }

    @Override
    public void setCameTime(DayEntity dayEntity, String time) {
        dayEntity.setTimeCome(time);
        mDisposable.add(
                interactor.updateDay(dayEntity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());
    }
}

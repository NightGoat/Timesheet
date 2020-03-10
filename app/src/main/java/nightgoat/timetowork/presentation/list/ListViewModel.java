package nightgoat.timetowork.presentation.list;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import nightgoat.timetowork.database.DayEntity;
import nightgoat.timetowork.domain.Interactor;

public class ListViewModel extends ViewModel implements LifecycleObserver, IListViewModel {

    private Interactor interactor;
    MutableLiveData<List<DayEntity>> daysLD = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ListViewModel(Interactor interactor) {
         this.interactor = interactor;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart() {
        compositeDisposable.add(
                interactor.getAllDays()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dayEntities -> daysLD.setValue(dayEntities)));
    }

    @Override
    protected void onCleared() {
        compositeDisposable.clear();
        super.onCleared();
    }

    @Override
    public void deleteDay(DayEntity dayEntity) {
        interactor.deleteDay(dayEntity).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    void saveDBtoExcel(){

    }
}

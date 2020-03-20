package nightgoat.timesheet.presentation.list;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import nightgoat.timesheet.database.DayEntity;
import nightgoat.timesheet.domain.Interactor;
import nightgoat.timesheet.utils.TimeUtils;
import timber.log.Timber;

public class ListViewModel extends ViewModel implements LifecycleObserver {

    private static final String TAG = ListViewModel.class.getName();
    private Interactor interactor;
    MutableLiveData<List<DayEntity>> daysLD = new MutableLiveData<>();
    MutableLiveData<List<String>> monthLD = new MutableLiveData<>();
    MutableLiveData<List<String>> yearsLD = new MutableLiveData<>();
    private CompositeDisposable mDisposable = new CompositeDisposable();
    private Disposable disposable;

    public ListViewModel(Interactor interactor) {
        this.interactor = interactor;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart() {
        mDisposable.add(
                interactor.getAllDays()
                        .observeOn(AndroidSchedulers.mainThread()).
                        subscribe(dayEntities -> {
                                    //daysLD.setValue(dayEntities);
                                    List<String> months = new ArrayList<>();
                                    List<String> years = new ArrayList<>();
                                    Timber.d("allDays: subscribe: size: %s", dayEntities.size());

                                    for(DayEntity day: dayEntities){
                                        Timber.d("day: %s, month: %s, year: %s", day.getDate(), TimeUtils.getMonthString(day.getDate().substring(5,7)), day.getDate().substring(0,4));
                                        String year = day.getDate().substring(0,4);
                                        String month = TimeUtils.getMonthStringShort(day.getDate().substring(5,7));
                                        if (!months.contains(month)) months.add(month);
                                        if (!years.contains(year)) years.add(year);
                                    }
                                    monthLD.setValue(months);
                                    yearsLD.setValue(years);
                                    getList(TimeUtils.getCurrentMonth(), TimeUtils.getCurrentYear());
                                },
                                throwable -> Timber.tag(TAG).e(throwable)));
    }

    @Override
    protected void onCleared() {
        mDisposable.clear();
        super.onCleared();
    }

    void getList(int month, int year) {
        Timber.i("getList: month: %s, year %s", month, year);
        if (disposable != null) disposable.dispose();
        disposable = interactor.getAllDays(month, year)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dayEntities -> {
                            daysLD.setValue(dayEntities);
                            Timber.d("getList: subscribe: size: %s", dayEntities.size());
                        },
                        throwable -> Timber.tag(TAG).e(throwable));
        mDisposable.add(disposable);
    }

    void deleteDay(DayEntity dayEntity) {
        interactor.deleteDay(dayEntity).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    void setGoneTime(DayEntity dayEntity, String time) {
        dayEntity.setTimeGone(time);
        mDisposable.add(
                interactor.updateDay(dayEntity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());
    }

    void setCameTime(DayEntity dayEntity, String time) {
        dayEntity.setTimeCame(time);
        mDisposable.add(
                interactor.updateDay(dayEntity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());
    }
}

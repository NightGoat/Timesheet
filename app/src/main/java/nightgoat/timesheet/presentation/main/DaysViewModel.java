package nightgoat.timesheet.presentation.main;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableMaybeObserver;
import nightgoat.timesheet.IResourceHolder;
import nightgoat.timesheet.TimeUtils;
import nightgoat.timesheet.database.DayEntity;
import nightgoat.timesheet.domain.Interactor;

public class DaysViewModel extends ViewModel implements LifecycleObserver {

    private final String TAG = DaysViewModel.class.getName();

    MutableLiveData<String> timeComeLD = new MutableLiveData<>();
    MutableLiveData<String> timeGoneLD = new MutableLiveData<>();
    MutableLiveData<String> dateLD = new MutableLiveData<>();
    MutableLiveData<String> timeDifferenceLD = new MutableLiveData<>();
    MutableLiveData<String> timeLeftToWorkTodayLD = new MutableLiveData<>();
    MutableLiveData<String> dayOfWeek = new MutableLiveData<>();
    MutableLiveData<Integer> dayLD = new MutableLiveData<>();
    MutableLiveData<Integer> monthLD = new MutableLiveData<>();
    MutableLiveData<Integer> yearLD = new MutableLiveData<>();
    MutableLiveData<Boolean> isGoneTimeExistLD = new MutableLiveData<>();
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private List<DayEntity> days;
    private Interactor interactor;
    private Calendar calendar;
    private String date;
    private String timeCome;
    private String timeGone;
    private String timeCanGoHomeAt;
    private String currentTime = TimeUtils.getCurrentTime();
    private String timeNeedToWork;
    private String timeDifference;
    private DayEntity dayEntity;
    private IResourceHolder resourceHolder;

    public DaysViewModel(Interactor interactor, IResourceHolder resourceHolder) {
        this.interactor = interactor;
        this.resourceHolder = resourceHolder;
        calendar = Calendar.getInstance();
        mDisposable.add(Observable.interval(1, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> {
                    currentTime = TimeUtils.getCurrentTime();
                    countComeGoneDifference();
                }, e -> Log.d("DaysViewModel", "interval error: " + e.getMessage())));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart() {
        Logger.addLogAdapter(new AndroidLogAdapter());
        Log.d("DaysViewModel", "onStart: ");
        getDate();
        getDayEntity(date);
        timeNeedToWork = resourceHolder.getTimeNeedToWorkFromPreferences();
    }

    void setPreviousDay() {
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        getDate();
        getDayEntity(date);
    }

    void setNextDay() {
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        getDate();
        getDayEntity(date);
    }

    void setComeTime(String time) {
        timeCome = time;
        dayEntity.setTimeCome(timeCome);
        mDisposable.add(
                interactor.updateDay(dayEntity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                timeComeLD.setValue(timeCome);
                                countComeGoneDifference();
                                countTimeCanGoHome();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "setComeTime Error: " + e);
                            }
                        }));
    }

    void setGoneTime(String time) {
        this.timeGone = time;
        dayEntity.setTimeGone(timeGone);
        mDisposable.add(
                interactor.updateDay(dayEntity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                timeGoneLD.setValue(timeGone);
                                countComeGoneDifference();
                            }
                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "setGoneTime Error: " + e);
                            }
                        }));
        countTimeCanGoHome();
    }

    void getDayEntity(String date) {
        this.date = date;
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy.MM.dd");
        DateTime dt = dateTimeFormatter.parseDateTime(date);
        calendar.setTime(dt.toDate());
        mDisposable.add(interactor.getDayByDayModel(date)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableMaybeObserver<DayEntity>() {
                                   @Override
                                   public void onSuccess(DayEntity day) {
                                       Log.w(TAG, "Found dayEntity for: " + day.getDate());
                                       dayEntity = day;
                                       timeCome = dayEntity.getTimeCome();
                                       timeGone = dayEntity.getTimeGone();
                                       timeComeLD.setValue(timeCome);
                                       timeGoneLD.setValue(timeGone);
                                       countComeGoneDifference();
                                       countTimeCanGoHome();
                                   }

                                   @Override
                                   public void onError(Throwable e) {
                                       Log.e(TAG, "Error: " + e);
                                       addDay();
                                       timeCome = null;
                                       timeGone = null;
                                       timeComeLD.setValue(null);
                                       timeGoneLD.setValue(null);
                                       getDayEntity(date);
                                   }

                                   @Override
                                   public void onComplete() {
                                       Log.e(TAG, "Didnt found entity, adding new one");
                                       timeCome = null;
                                       timeGone = null;
                                       timeGoneLD.setValue(null);
                                       timeComeLD.setValue(null);
                                       addDay();
                                   }
                               }
                ));
        dateLD.setValue(TimeUtils.getDateInNormalFormat(date));
        dayOfWeek.setValue(TimeUtils.getDayOfTheWeek(date));
    }

    private void addDay() {
        dayEntity = new DayEntity(date);
        mDisposable.add(interactor.addDay(dayEntity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.e(TAG, "Added day");
                        timeDifferenceLD.setValue("00:00");
                        countTimeCanGoHome();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Adding day completed with Error: " + e);
                    }
                }));
    }

    private void getDate() {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        dayLD.setValue(day);
        monthLD.setValue(month);
        yearLD.setValue(year);
        date = String.format(Locale.getDefault(), "%d.%02d.%02d", year, month, day);
    }

    void deleteEmptyEntities() {
        Log.w(TAG, "Deleting all empty entities");
        interactor.deleteDaysWithoutTime()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void countComeGoneDifference() {
        Log.d("DaysViewModel", "countComeGoneDifference: ");
        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
        if (timeCome != null && timeGone != null) {
            Log.d(TAG, "counting difference for timeCome: " + timeCome + " and timeGone: " + timeGone);
            DateTime comeTimeDT = DateTime.parse(timeCome, formatter);
            DateTime goneTimeDT = DateTime.parse(timeGone, formatter);
            if (goneTimeDT.isAfter(comeTimeDT)) {
                timeDifference = formatter.print(new Duration(comeTimeDT, goneTimeDT).getMillis());
            } else {
                timeDifference = null;
            }
            dayEntity.setTimeWorked(timeDifference);
            mDisposable.add(
                    interactor.updateDay(dayEntity)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe());
        } else if (timeCome != null){
            DateTime comeTimeDT = DateTime.parse(timeCome, formatter);
            DateTime nowTimeDT = DateTime.parse(currentTime, formatter);
            if (nowTimeDT.isAfter(comeTimeDT)) {
                timeDifference = formatter.print(new Duration(comeTimeDT, nowTimeDT).getMillis());
            }
            else {
                timeDifference = null;
            }
        } else {
            Log.e(TAG, "ERROR counting difference because timeCome is: " + timeCome + " and timeGone is: " + timeGone);
            timeDifference = null;
        }
        timeDifferenceLD.setValue(timeDifference);
        countTimeLeftToWorkToday();
    }

    private void countTimeLeftToWorkToday(){
        if (timeDifference != null) {
            timeLeftToWorkTodayLD.postValue(TimeUtils.countTimeDiff(timeNeedToWork, timeDifference));
        } else {
            timeLeftToWorkTodayLD.setValue(null);
        }
    }

    private void countTimeCanGoHome(){
        Logger.d(" countTimeCanGoHome() timeCome: %s timeGone: %s timeCanGoHomeAt: %s", timeCome, timeGone, timeCanGoHomeAt);
        if (timeCome != null && timeGone == null) {
            timeCanGoHomeAt = TimeUtils.countTimeSum(timeCome, timeNeedToWork);
            timeGoneLD.setValue(timeCanGoHomeAt);
            isGoneTimeExistLD.setValue(false);
        } else if (timeCome != null) {
            isGoneTimeExistLD.setValue(true);
        } else if (timeGone != null) {
            timeComeLD.setValue(null);
            isGoneTimeExistLD.setValue(true);
        } else {
            timeGoneLD.setValue(null);
            timeComeLD.setValue(null);
            isGoneTimeExistLD.setValue(false);
        }
    }


    @Override
    protected void onCleared() {
        deleteEmptyEntities();
        mDisposable.clear();
        super.onCleared();
    }

}

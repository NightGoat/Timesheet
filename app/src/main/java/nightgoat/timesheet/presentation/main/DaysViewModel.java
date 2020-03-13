package nightgoat.timesheet.presentation.main;

import android.annotation.SuppressLint;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

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
import nightgoat.timesheet.IResourceHolder;
import nightgoat.timesheet.TimeUtils;
import nightgoat.timesheet.database.DayEntity;
import nightgoat.timesheet.domain.Interactor;
import timber.log.Timber;

public class DaysViewModel extends ViewModel implements LifecycleObserver {

    private final String TAG = DaysViewModel.class.getName();

    MutableLiveData<String> timeCameLiveData = new MutableLiveData<>();
    MutableLiveData<String> timeGoneLiveData = new MutableLiveData<>();
    MutableLiveData<String> dateLiveData = new MutableLiveData<>();
    MutableLiveData<String> timeWasOnWorkLiveData = new MutableLiveData<>();
    MutableLiveData<String> timeLeftToWorkTodayLiveData = new MutableLiveData<>();
    MutableLiveData<String> dayOfWeekLiveData = new MutableLiveData<>();
    MutableLiveData<Integer> dayLiveData = new MutableLiveData<>();
    MutableLiveData<Integer> monthLiveData = new MutableLiveData<>();
    MutableLiveData<Integer> yearLiveData = new MutableLiveData<>();
    MutableLiveData<Boolean> isGoneTimeExistLiveData = new MutableLiveData<>();
    MutableLiveData<String> workedHoursSumLiveData = new MutableLiveData<>();
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private List<DayEntity> days;
    private Interactor interactor;
    private Calendar calendar;
    private String date;
    private String timeCome;
    private String timeGone;
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
                }, e -> Timber.tag(TAG).d("interval error: %s", e.getMessage())));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart() {
        Timber.tag(TAG).d("onStart: ");
        getDate();
        timeNeedToWork = resourceHolder.getTimeNeedToWorkFromPreferences();
        mDisposable.add(
                interactor.getAllDays()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dayEntities -> {
                            days = dayEntities;
                            getDayEntity(date);

                        }, throwable -> Timber.tag(TAG).d("onStart: error with list: %s",
                                throwable.getMessage())));
    }

    void getDayEntity(String date) {
        this.date = date;
        Timber.d("getDayEntity: date: %s", date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = dateTimeFormatter.parseDateTime(date);
        calendar.setTime(dt.toDate());
        dayEntity = new DayEntity(date);
        if (days.contains(dayEntity)) {
            for (DayEntity day : days) {
                if (day.equals(dayEntity)) dayEntity = day;
            }
        } else addDay();
        timeCome = dayEntity.getTimeCame();
        timeGone = dayEntity.getTimeGone();
        timeDifference = dayEntity.getTimeWorked();
        Timber.tag(TAG).d(
                "DayEntity timeCame: %s, timeGone: %s, timeDifference: %s",
                timeCome, timeGone, timeDifference);
        timeCameLiveData.postValue(timeCome);
        timeGoneLiveData.postValue(timeGone);
        countComeGoneDifference();
        countTimeCanGoHome();
        dateLiveData.postValue(TimeUtils.getDateInNormalFormat(date));
        dayOfWeekLiveData.postValue(TimeUtils.getDayOfTheWeek(date));
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

    void setCameTime(String time) {
        dayEntity.setTimeCame(time);
        mDisposable.add(
                interactor.updateDay(dayEntity)
                        .subscribe());
    }

    void setGoneTime(String time) {
        dayEntity.setTimeGone(time);
        mDisposable.add(
                interactor.updateDay(dayEntity)
                        .subscribe());
    }

    private void addDay() {
        Timber.tag(TAG).d("addDay: %s", dayEntity.getDate());
        mDisposable.add(interactor.addDay(dayEntity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Timber.e("Added day")));
    }

    private void getDate() {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        getWorkedHoursSum(month, year);
        dayLiveData.postValue(day);
        monthLiveData.postValue(month);
        yearLiveData.postValue(year);
        date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month, day);
    }

    @SuppressLint("DefaultLocale")
    private void getWorkedHoursSum(int month, int year) {
        Timber.tag(TAG).d("getWorkedHoursSum: " + String.format("%02d", month) + " " + year);
        mDisposable.add(interactor.getWorkedHoursSum(String.format("%02d", month), String.valueOf(year))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        sum -> {
                            Timber.tag(TAG).d("getWorkedHoursSum: %s", sum);
                            workedHoursSumLiveData.setValue("Отработано в этом месяце: " + sum);
                        },
                        throwable ->
                                Timber.tag(TAG).e("getWorkedHoursSum: ERROR: %s", throwable.getMessage()),
                        () -> Timber.d("getWorkedHoursSum: onComplete")));
    }

    void deleteEmptyEntities() {
        Timber.tag(TAG).w("Deleting all empty entities");
        interactor.deleteDaysWithoutTime()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void countComeGoneDifference() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
        if (timeCome != null && timeDifference == null) {
            DateTime comeTimeDT = DateTime.parse(timeCome, formatter);
            DateTime nowTimeDT = DateTime.parse(currentTime, formatter);
            if (nowTimeDT.isAfter(comeTimeDT)) {
                timeDifference = formatter.print(new Duration(comeTimeDT, nowTimeDT).getMillis());
            }
        }
        timeWasOnWorkLiveData.postValue(timeDifference);
        countTimeLeftToWorkToday();
    }

    private void countTimeLeftToWorkToday() {
        if (timeDifference != null) {
            timeLeftToWorkTodayLiveData
                    .postValue(TimeUtils.countTimeDiff(timeNeedToWork, timeDifference));
        } else {
            timeLeftToWorkTodayLiveData.postValue(null);
        }
    }

    private void countTimeCanGoHome() {
        if (timeCome != null && timeGone == null) {
            String timeCanGoHomeAt = TimeUtils.countTimeSum(timeCome, timeNeedToWork);
            timeGoneLiveData.postValue(timeCanGoHomeAt);
            isGoneTimeExistLiveData.postValue(false);
        } else if (timeCome != null) {
            isGoneTimeExistLiveData.postValue(true);
        } else if (timeGone != null) {
            timeCameLiveData.postValue(null);
            isGoneTimeExistLiveData.postValue(true);
        } else {
            timeGoneLiveData.postValue(null);
            timeCameLiveData.postValue(null);
            isGoneTimeExistLiveData.postValue(false);
        }
    }

    @Override
    protected void onCleared() {
        deleteEmptyEntities();
        mDisposable.clear();
        super.onCleared();
    }
}

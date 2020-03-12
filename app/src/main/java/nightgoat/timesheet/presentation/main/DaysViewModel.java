package nightgoat.timesheet.presentation.main;

import android.annotation.SuppressLint;
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
    MutableLiveData<String> workedHoursSumLD = new MutableLiveData<>();
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
                }, e -> Log.d("DaysViewModel", "interval error: " + e.getMessage())));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart() {
        Logger.addLogAdapter(new AndroidLogAdapter());
        Log.d("DaysViewModel", "onStart: ");
        getDate();
        timeNeedToWork = resourceHolder.getTimeNeedToWorkFromPreferences();
        mDisposable.add(
                interactor.getAllDays()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dayEntities -> {
                            days = dayEntities;
                            getDayEntity(date);

                        }, throwable -> Log.d("DaysViewModel", "onStart: error with list: " + throwable.getMessage())));
    }

    void getDayEntity(String date) {
        this.date = date;
        Log.d("DaysViewModel", "getDayEntity: date: " + date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = dateTimeFormatter.parseDateTime(date);
        calendar.setTime(dt.toDate());
        dayEntity = new DayEntity(date);
        if (days.contains(dayEntity)) {
            for (DayEntity day : days) {
                if (day.equals(dayEntity)) dayEntity = day;
            }
        } else addDay();
        timeCome = dayEntity.getTimeCome();
        timeGone = dayEntity.getTimeGone();
        timeDifference = dayEntity.getTimeWorked();
        Log.d("DaysViewModel", "DayEntity timeCome: " + timeCome + " timeGone: " + timeGone + " timeDifference: " + timeDifference);
        timeComeLD.postValue(timeCome);
        timeGoneLD.postValue(timeGone);
        countComeGoneDifference();
        countTimeCanGoHome();
        dateLD.postValue(TimeUtils.getDateInNormalFormat(date));
        dayOfWeek.postValue(TimeUtils.getDayOfTheWeek(date));
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
        dayEntity.setTimeCome(time);
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
        Log.d("DaysViewModel", "addDay: " + dayEntity.getDate());
        mDisposable.add(interactor.addDay(dayEntity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Log.e(TAG, "Added day")));
    }

    private void getDate() {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        getWorkedHoursSum(month, year);
        dayLD.postValue(day);
        monthLD.postValue(month);
        yearLD.postValue(year);
        date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month, day);
    }

    @SuppressLint("DefaultLocale")
    private void getWorkedHoursSum(int month, int year) {
        Log.d("DaysViewModel", "getWorkedHoursSum: " + String.format("%02d", month) + " " + year);
        mDisposable.add(interactor.getWorkedHoursSum(String.format("%02d", month), String.valueOf(year))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sum -> {
                            Log.d("DaysViewModel", "getWorkedHoursSum: " + sum);
                            workedHoursSumLD.setValue("Отработано в этом месяце: " + sum);
                        },
                        throwable -> Log.e("DaysViewModel", "getWorkedHoursSum: ERROR" + throwable.getMessage()),
                        () -> Log.d("DaysViewModel", "getWorkedHoursSum: onComplete"),
                        subscription -> Log.d("DaysViewModel", "getWorkedHoursSum: subscription" + subscription.toString())));
    }

    void deleteEmptyEntities() {
        Log.w(TAG, "Deleting all empty entities");
        interactor.deleteDaysWithoutTime()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void countComeGoneDifference() {
        // Log.d("DaysViewModel", "countComeGoneDifference: ");
        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
        if (timeCome != null && timeDifference == null) {
            DateTime comeTimeDT = DateTime.parse(timeCome, formatter);
            DateTime nowTimeDT = DateTime.parse(currentTime, formatter);
            if (nowTimeDT.isAfter(comeTimeDT)) {
                timeDifference = formatter.print(new Duration(comeTimeDT, nowTimeDT).getMillis());
            }
        }
        timeDifferenceLD.postValue(timeDifference);
        countTimeLeftToWorkToday();
    }

    private void countTimeLeftToWorkToday() {
        if (timeDifference != null) {
            timeLeftToWorkTodayLD.postValue(TimeUtils.countTimeDiff(timeNeedToWork, timeDifference));
        } else {
            timeLeftToWorkTodayLD.postValue(null);
        }
    }

    private void countTimeCanGoHome() {
        // Logger.d(" countTimeCanGoHome() timeCome: %s timeGone: %s timeCanGoHomeAt: %s", timeCome, timeGone, timeCanGoHomeAt);
        if (timeCome != null && timeGone == null) {
            String timeCanGoHomeAt = TimeUtils.countTimeSum(timeCome, timeNeedToWork);
            timeGoneLD.postValue(timeCanGoHomeAt);
            isGoneTimeExistLD.postValue(false);
        } else if (timeCome != null) {
            isGoneTimeExistLD.postValue(true);
        } else if (timeGone != null) {
            timeComeLD.postValue(null);
            isGoneTimeExistLD.postValue(true);
        } else {
            timeGoneLD.postValue(null);
            timeComeLD.postValue(null);
            isGoneTimeExistLD.postValue(false);
        }
    }


    @Override
    protected void onCleared() {
        deleteEmptyEntities();
        mDisposable.clear();
        super.onCleared();
    }

}

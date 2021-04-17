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
import io.reactivex.disposables.Disposable;
import nightgoat.timesheet.IResourceHolder;
import nightgoat.timesheet.database.DayEntity;
import nightgoat.timesheet.domain.Interactor;
import nightgoat.timesheet.utils.TimeUtils;
import timber.log.Timber;

public class DaysViewModel extends ViewModel implements LifecycleObserver {

    private final String TAG = DaysViewModel.class.getName();

    private final DataContainer dataContainer;
    MutableLiveData<DataContainer> containerLiveData = new MutableLiveData<>();
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private List<DayEntity> days;
    private final Interactor interactor;
    private final Calendar calendar;
    private String date;
    private String timeCome;
    private String timeGone;
    private String currentTime = TimeUtils.getCurrentTime();
    private String timeNeedToWork;
    private String timeDifference;
    private DayEntity dayEntity;
    private final IResourceHolder resourceHolder;
    private int cachedMonth;
    private int cachedYear;
    private int month;
    private int year;
    private Disposable workedHoursDisposable;

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
        dataContainer = new DataContainer();
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
        Timber.d("getDayEntity: date: %s", date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = dateTimeFormatter.parseDateTime(date);
        calendar.setTime(dt.toDate());
        getDate();
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

        dataContainer.note = dayEntity.getNote();
        dataContainer.timeCame = timeCome;
        dataContainer.timeGone = timeGone;
        dataContainer.date = TimeUtils.getDateInNormalFormat(date);
        dataContainer.dayOfWeek = TimeUtils.getDayOfTheWeek(date);
        countComeGoneDifference();
        countTimeCanGoHome();
        containerLiveData.postValue(dataContainer);
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
        Timber.tag(TAG).d("addDay: %s, month: %d, year: %d", dayEntity.getDate(), month, year);
        mDisposable.add(interactor.addDay(dayEntity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Timber.e("Added day")));
    }

    private void getDate() {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);
        Timber.tag(TAG).i("getDate: month: %d - cached month: %d, year: %d, cached year: %d", month, cachedMonth, year, cachedYear);
        if ((cachedMonth == 0 && cachedYear == 0) || (cachedMonth != month || cachedYear != year)) {
            cachedMonth = month;
            cachedYear = year;
            getWorkedHoursSum(month, year);
            Timber.tag(TAG).i("getDate: in if statement");
        }
        dataContainer.day = day;
        dataContainer.month = month - 1;
        dataContainer.year = year;
        containerLiveData.postValue(dataContainer);
        date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month, day);
    }

    void updateNote(String text){
        dayEntity.setNote(text);
        mDisposable.add(interactor.updateDay(dayEntity).subscribe());
        dataContainer.note = text;
        containerLiveData.postValue(dataContainer);
    }

    @SuppressLint("DefaultLocale")
    private void getWorkedHoursSum(int month, int year) {
        Timber.tag(TAG).d("getWorkedHoursSum: " + String.format("%02d", month) + " " + year);
        if (workedHoursDisposable != null) workedHoursDisposable.dispose();
        workedHoursDisposable =
                interactor.getWorkedHoursSum(String.format("%02d", month), String.valueOf(year))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                sum -> {
                                    Timber.tag(TAG).i("getWorkedHoursSum: sum: %s", sum);
                                    dataContainer.workedHoursSum = sum;
                                    containerLiveData.postValue(dataContainer);
                                },
                                throwable ->
                                        Timber.tag(TAG)
                                                .e("getWorkedHoursSum: ERROR: %s", throwable.getMessage()),
                                () -> Timber.d("getWorkedHoursSum: onComplete"));
        mDisposable.add(workedHoursDisposable);
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
        dataContainer.timeWasOnWork = timeDifference;
        containerLiveData.postValue(dataContainer);
        countTimeLeftToWorkToday();
    }

    private void countTimeLeftToWorkToday() {
        if (timeDifference != null) {
            dataContainer.timeLeftToWorkToday = TimeUtils.countTimeDiff(timeNeedToWork, timeDifference);
        } else {
            dataContainer.timeLeftToWorkToday = null;
        }
        containerLiveData.postValue(dataContainer);
    }

    private void countTimeCanGoHome() {
        Timber.d("countTimeCanGoHome: timeCome: %s, timeGone: %s", timeCome, timeGone);
        if (timeCome != null && timeGone == null) {
            dataContainer.timeGone = TimeUtils.countTimeSum24(timeCome, timeNeedToWork);
            dataContainer.isGoneTimeExist = false;
        } else if (timeCome != null) {
            dataContainer.isGoneTimeExist = true;
        } else if (timeGone != null) {
            dataContainer.timeCame = null;
            dataContainer.isGoneTimeExist = true;
        } else {
            dataContainer.timeGone = null;
            dataContainer.timeCame = null;
            dataContainer.isGoneTimeExist = false;
        }
        containerLiveData.postValue(dataContainer);
    }

    @Override
    protected void onCleared() {
        deleteEmptyEntities();
        mDisposable.clear();
        super.onCleared();
    }
}

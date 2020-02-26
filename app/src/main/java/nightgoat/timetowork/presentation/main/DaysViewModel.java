package nightgoat.timetowork.presentation.main;

import android.util.Log;

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

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;
import nightgoat.timetowork.domain.DaysDataSource;
import nightgoat.timetowork.database.DayEntity;

public class DaysViewModel extends ViewModel implements LifecycleObserver {

    private final String TAG = DaysViewModel.class.getName();

    MutableLiveData<String> timeComeLD = new MutableLiveData<>();
    MutableLiveData<String> timeGoneLD = new MutableLiveData<>();
    MutableLiveData<String> dateLD = new MutableLiveData<>();
    MutableLiveData<String> timeDifferenceLD = new MutableLiveData<>();
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private List<DayEntity> days;
    private Calendar calendar;
    private int day, month, year, hourCome, hourGone, minuteCome, minuteGone;
    private String date, timeCome, timeGone, timeDifference;

    private final DaysDataSource mDataSource;
    private DayEntity dayEntity;

    public DaysViewModel(DaysDataSource dataSource) {
        mDataSource = dataSource;
        calendar = Calendar.getInstance();
        getDate();
        getDayEntity(date);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart() {

    }

    public Flowable<List<DayEntity>> getDays() {
        return mDataSource.getAllDays()
                .map(dayEntities -> {
                    days = dayEntities;
                    return dayEntities;
                });
    }

    void setPreviousDay() {
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        getDate();
        getDayEntity(date);
    }

    void setNextDay() {
        calendar.add(Calendar.DAY_OF_MONTH, +1);
        getDate();
        getDayEntity(date);
    }

    void setComeTime(String time) {
        timeCome = time;
        dayEntity.setTimeCome(timeCome);
        mDisposable.add(
                mDataSource.updateDay(dayEntity)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                timeComeLD.setValue(timeCome);
                                countComeGoneDifference();
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
                mDataSource.updateDay(dayEntity)
                        .subscribeOn(Schedulers.io())
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

    }

    void getDayEntity(String date) {
        this.date = date;
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy.MM.dd");
        DateTime dt = dateTimeFormatter.parseDateTime(date);
        calendar.setTime(dt.toDate());
        mDisposable.add(mDataSource.getDayByDayModel(date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableMaybeObserver<DayEntity>() {
                                   @Override
                                   public void onSuccess(DayEntity day) {
                                       Log.w(TAG, "Found dayEntity for: " + day.getDate());
                                       dayEntity = day;
                                       timeCome = dayEntity.getTimeCome();
                                       timeGone = dayEntity.getTimeGone();
                                       timeGoneLD.setValue(timeGone);
                                       timeComeLD.setValue(timeCome);
                                       countComeGoneDifference();
                                   }

                                   @Override
                                   public void onError(Throwable e) {
                                       Log.e(TAG, "Error: " + e);
                                       addDay();
                                       getDayEntity(date);
                                   }

                                   @Override
                                   public void onComplete() {
                                       Log.e(TAG, "Didnt found entity, adding new one");
                                       addDay();
                                       timeGoneLD.setValue("");
                                       timeComeLD.setValue("");
                                   }
                               }
                ));
        dateLD.setValue(date);

    }

    private void addDay() {
        dayEntity = new DayEntity(date);
        mDisposable.add(mDataSource.addDay(dayEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.e(TAG, "Added day");
                        timeDifferenceLD.setValue("00:00");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Adding day completed with Error: " + e);
                    }
                }));
    }

    private void getDate() {
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);
        date = String.format(Locale.getDefault(), "%d.%02d.%02d", year, month, day);
    }

    private void callForDispose() {
        Log.w(TAG, "Deleting all empty entities");
        mDataSource.deleteDaysWithoutTime()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        mDisposable.clear();
    }

    private void countComeGoneDifference() {
        if (timeCome != null && timeGone != null) {
            Log.d(TAG, "counting difference for timeCome: " + timeCome + " and timeGone: " + timeGone);
            DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
            DateTime comeTimeDT = DateTime.parse(timeCome, formatter);
            DateTime goneTimeDT = DateTime.parse(timeGone, formatter);
            if (goneTimeDT.isAfter(comeTimeDT)) {
                timeDifference = formatter.print(new Duration(comeTimeDT, goneTimeDT).getMillis());
                timeDifferenceLD.setValue(timeDifference);
            } else {
                timeDifference = null;
                timeDifferenceLD.setValue("Никаких путешествий во времени!");
            }
            dayEntity.setTimeWorked(timeDifference);
            mDisposable.add(
                    mDataSource.updateDay(dayEntity)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe());
        } else {
            Log.e(TAG, "ERROR counting difference because timeCome is: " + timeCome + " and timeGone" + timeGone);
            timeDifferenceLD.setValue("00:00");
        }
    }

    int getCurrentDay() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    int getCurrentMonth() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.MONTH);
    }

    int getCurrentYear() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.YEAR);
    }

    String getCurrentTime() {
        return String.format(Locale.getDefault(), "%02d:%02d", getCurrentHour(), getCurrentMinutes());
    }

    String getTime(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    int getCurrentHour() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    int getCurrentMinutes() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.MINUTE);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        callForDispose();
    }
}

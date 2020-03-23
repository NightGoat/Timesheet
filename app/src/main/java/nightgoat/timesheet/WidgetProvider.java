package nightgoat.timesheet;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;
import nightgoat.timesheet.database.DayEntity;
import nightgoat.timesheet.di.AppComponent;
import nightgoat.timesheet.di.DaggerBroadcastReceiverComponent;
import nightgoat.timesheet.domain.Interactor;
import nightgoat.timesheet.utils.TimeUtils;

public class WidgetProvider extends AppWidgetProvider {

    @Inject
    Interactor interactor;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views2 = new RemoteViews(context.getPackageName(), R.layout.widget);
            views2.setOnClickPendingIntent(R.id.widget2_btn_came, getPendingSelfIntent(context, "nightgoat.timesheet.action.came"));
            views2.setOnClickPendingIntent(R.id.widget2_btn_gone, getPendingSelfIntent(context, "nightgoat.timesheet.action.gone"));
            appWidgetManager.updateAppWidget(appWidgetId, views2);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppComponent component = ((App) context.getApplicationContext()).getAppComponent();
        DaggerBroadcastReceiverComponent.builder().appComponent(component).build().inject(this);
        String currentTime = TimeUtils.getCurrentTime();
        if (Objects.equals(intent.getAction(), "nightgoat.timesheet.action.came")) {
            interactor.getDayEntityByDay(TimeUtils.getCurrentDate())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableMaybeObserver<DayEntity>() {
                        @Override
                        public void onSuccess(DayEntity dayEntity) {
                            dayEntity.setTimeCame(currentTime);
                            interactor.updateDayTimeOut(dayEntity).subscribeOn(Schedulers.io()).subscribe();
                            makeToast(context.getString(R.string.came) + ": " + currentTime, context);
                        }

                        @Override
                        public void onError(Throwable e) {
                            makeToast(e.getMessage(), context);
                        }

                        @Override
                        public void onComplete() {
                            DayEntity dayEntity = new DayEntity(TimeUtils.getCurrentDate());
                            dayEntity.setTimeCame(currentTime);
                            interactor.addDay(dayEntity).subscribeOn(Schedulers.io()).subscribe();
                            makeToast(context.getString(R.string.came) + ": " + currentTime, context);
                        }
                    });
        }
        if (Objects.equals(intent.getAction(), "nightgoat.timesheet.action.gone")) {
            interactor.getDayEntityByDay(TimeUtils.getCurrentDate())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableMaybeObserver<DayEntity>() {
                        @Override
                        public void onSuccess(DayEntity dayEntity) {
                            dayEntity.setTimeGone(currentTime);
                            interactor.updateDayTimeOut(dayEntity).observeOn(AndroidSchedulers.mainThread())
                                    .subscribe();
                            makeToast(context.getString(R.string.gone) + ": " + currentTime, context);
                        }

                        @Override
                        public void onError(Throwable e) {
                            makeToast(e.getMessage(), context);
                        }

                        @Override
                        public void onComplete() {
                            DayEntity dayEntity = new DayEntity(TimeUtils.getCurrentDate());
                            dayEntity.setTimeGone(currentTime);
                            interactor.addDay(dayEntity).subscribe();
                            makeToast(context.getString(R.string.gone) + ": " + currentTime, context);
                        }
                    });
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private void makeToast(String text, Context context) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

}

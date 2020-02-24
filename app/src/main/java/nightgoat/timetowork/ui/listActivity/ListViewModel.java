package nightgoat.timetowork.ui.listActivity;

import androidx.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import nightgoat.timetowork.DaysDataSource;
import nightgoat.timetowork.database.DayEntity;

public class ListViewModel extends ViewModel {

    private final DaysDataSource mDataSource;
    private List<DayEntity> days;

    public ListViewModel(DaysDataSource dataSource) {
        mDataSource = dataSource;
    }

    Flowable<List<DayEntity>> getList() {
        return mDataSource.getAllDays()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}

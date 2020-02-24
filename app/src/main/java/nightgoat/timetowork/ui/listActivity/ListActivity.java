package nightgoat.timetowork.ui.listActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import org.reactivestreams.Subscription;

import java.util.List;

import io.reactivex.FlowableSubscriber;
import nightgoat.timetowork.Injection;
import nightgoat.timetowork.R;
import nightgoat.timetowork.database.DayEntity;
import nightgoat.timetowork.ui.ViewModelFactory;

public class ListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private List<DayEntity> days;
    private ListViewModel mViewModel;
    private ViewModelFactory mViewModelFactory;
    private ListViewAdapter adapter;
    private final String TAG = ListActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initViews();
        toolbar.setTitle(null);
        setSupportActionBar(toolbar);
        mViewModelFactory = Injection.provideViewModelFactory(this);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(ListViewModel.class);
        initList();
    }


    private void initList() {
        Log.d(TAG, "initList()");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mViewModel.getList().subscribe(new FlowableSubscriber<List<DayEntity>>() {
            @Override
            public void onSubscribe(Subscription s) {
                Log.d(TAG, "getList onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(List<DayEntity> dayEntities) {
                days = dayEntities;
                adapter = new ListViewAdapter(days);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                Log.d(TAG, "getList onNext: " + dayEntities.toString());
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG, "getList onError: " + t);
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "getList onComplete");

            }
        });

    }

    private void initViews() {
        toolbar = findViewById(R.id.list_activity_toolbar);
        recyclerView = findViewById(R.id.list_activity_recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_activity_toolbar_menu, menu);
        return true;
    }
}

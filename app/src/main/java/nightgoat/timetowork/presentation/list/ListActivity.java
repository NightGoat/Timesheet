package nightgoat.timetowork.presentation.list;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;

import java.util.List;

import nightgoat.timetowork.Injection;
import nightgoat.timetowork.R;
import nightgoat.timetowork.database.DayEntity;
import nightgoat.timetowork.presentation.ViewModelFactory;

public class ListActivity extends AppCompatActivity {

    private EditText searchEditText;
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
        setSupportActionBar(toolbar);
        initList();
        initViewModel();
        mViewModel.daysLD.observe(this, data -> adapter.changeList(data));
    }

    private void initViews() {
        toolbar = findViewById(R.id.list_activity_toolbar);
        recyclerView = findViewById(R.id.list_activity_recyclerView);
        searchEditText = findViewById(R.id.list_activity_EditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
        });
    }

    private void initList() {
        Log.d(TAG, "initList()");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new ListViewAdapter(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void initViewModel() {
        mViewModelFactory = Injection.provideViewModelFactory(this);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(ListViewModel.class);
        getLifecycle().addObserver(mViewModel);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_activity_toolbar_menu, menu);
        return true;
    }
}

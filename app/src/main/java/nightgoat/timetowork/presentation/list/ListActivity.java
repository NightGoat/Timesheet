package nightgoat.timetowork.presentation.list;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.List;

import javax.inject.Inject;

import nightgoat.timetowork.App;
import nightgoat.timetowork.IResourceHolder;
import nightgoat.timetowork.R;
import nightgoat.timetowork.database.DayEntity;
import nightgoat.timetowork.di.AppComponent;
import nightgoat.timetowork.di.DaggerAcitivityComponent;
import nightgoat.timetowork.di.InteractorModule;
import nightgoat.timetowork.domain.Interactor;
import nightgoat.timetowork.presentation.ViewModelFactory;
import nightgoat.timetowork.presentation.settings.SettingsActivity;

public class ListActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private List<DayEntity> days;
    private ListViewModel mViewModel;
    private ViewModelFactory mViewModelFactory;
    private ListViewAdapter adapter;
    private final String TAG = ListActivity.class.getName();

    @Inject
    Interactor interactor;

    @Inject
    IResourceHolder resourceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initViews();
        initToolbar();
        initViewModel();
        initList();
    }

    private void initViews() {
        toolbar = findViewById(R.id.list_activity_toolbar);
        recyclerView = findViewById(R.id.list_activity_recyclerView);
        searchEditText = findViewById(R.id.list_activity_EditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
        });
    }

    private void initToolbar() {
        toolbar.setTitle(getString(R.string.action_list));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViewModel() {
        AppComponent component = ((App)getApplication()).getAppComponent();
        DaggerAcitivityComponent.builder()
                .appComponent(component)
                .interactorModule(new InteractorModule())
                .build()
                .inject(this);
        ViewModelFactory mViewModelFactory = new ViewModelFactory(interactor, resourceHolder);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(ListViewModel.class);
        getLifecycle().addObserver(mViewModel);
        mViewModel.daysLD.observe(this, data -> adapter.changeList(data));
    }

    private void initList() {
        Log.d(TAG, "initList()");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new ListViewAdapter(mViewModel);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_activity_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            goToSettingsActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        Log.d(TAG, "Going to Settings activity");
    }
}

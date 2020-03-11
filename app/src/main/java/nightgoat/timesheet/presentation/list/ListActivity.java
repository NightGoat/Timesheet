package nightgoat.timesheet.presentation.list;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import javax.inject.Inject;

import nightgoat.timesheet.App;
import nightgoat.timesheet.IResourceHolder;
import nightgoat.timesheet.R;
import nightgoat.timesheet.databinding.ActivityListBinding;
import nightgoat.timesheet.di.AppComponent;
import nightgoat.timesheet.di.DaggerAcitivityComponent;
import nightgoat.timesheet.di.InteractorModule;
import nightgoat.timesheet.domain.Interactor;
import nightgoat.timesheet.presentation.ViewModelFactory;
import nightgoat.timesheet.presentation.ActivityForResultFinisher;
import nightgoat.timesheet.presentation.settings.SettingsActivity;

public class ListActivity extends AppCompatActivity implements ActivityForResultFinisher {

    private ActivityListBinding binding;
    private ListViewModel mViewModel;
    private ListViewAdapter adapter;
    private final String TAG = ListActivity.class.getName();

    @Inject
    Interactor interactor;

    @Inject
    IResourceHolder resourceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initToolbar();
        initViewModel();
        initList();
        initSearchTextChangedListener();
    }

    private void initSearchTextChangedListener() {
        binding.listActivityEditText.addTextChangedListener(new TextWatcher() {
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
        binding.listActivityToolbar.setTitle(getString(R.string.action_list));
        setSupportActionBar(binding.listActivityToolbar);
        binding.listActivityToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        binding.listActivityToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViewModel() {
        AppComponent component = ((App)getApplication()).getAppComponent();
        DaggerAcitivityComponent.builder()
                .appComponent(component)
                .interactorModule(new InteractorModule())
                .build()
                .inject(this);
        mViewModel = new ViewModelProvider(this, new ViewModelFactory(interactor, resourceHolder)).get(ListViewModel.class);
        getLifecycle().addObserver(mViewModel);
        mViewModel.daysLD.observe(this, data -> adapter.changeList(data));
    }

    private void initList() {
        Log.d(TAG, "initList()");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new ListViewAdapter(mViewModel, this);
        binding.listActivityRecyclerView.setLayoutManager(layoutManager);
        binding.listActivityRecyclerView.setAdapter(adapter);
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

    @Override
    public void finishActivityForResult(String day) {
        Intent intent = new Intent();
        intent.putExtra("day", day);
        setResult(RESULT_OK, intent);
        finish();
    }
}

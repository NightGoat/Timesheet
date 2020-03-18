package nightgoat.timesheet.presentation.list;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;

import javax.inject.Inject;

import nightgoat.timesheet.App;
import nightgoat.timesheet.R;
import nightgoat.timesheet.database.DayEntity;
import nightgoat.timesheet.databinding.ActivityListBinding;
import nightgoat.timesheet.di.AppComponent;
import nightgoat.timesheet.di.DaggerActivityComponent;
import nightgoat.timesheet.di.InteractorModule;
import nightgoat.timesheet.presentation.ActivityAdapterCallbacks;
import nightgoat.timesheet.presentation.settings.SettingsActivity;
import nightgoat.timesheet.utils.TimeType;
import nightgoat.timesheet.utils.TimeUtils;
import timber.log.Timber;

public class ListActivity extends AppCompatActivity implements ActivityAdapterCallbacks  {

    private ActivityListBinding binding;
    private ListAdapter adapter;

    @SuppressWarnings("unused")
    private final static String TAG = ListActivity.class.getName();

    private String month, year;

    @Inject
    ListViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initToolbar();
        initViewModel();
        initList();
        initSearchTextChangedListener();
        initViewModelObservations();
    }

    private void initViewModelObservations() {
        mViewModel.monthLD.observe(this, months -> {
            binding.listChipGroupMonth.removeAllViews();
            Timber.d("monthLD: size: %d", months.size());
            for (String month: months) {
                Chip chip = new Chip(this);
                chip.setText(month);
                chip.setCheckable(true);
                if (TimeUtils.getMonthInt(month) == TimeUtils.getCurrentMonth()) {
                    this.month = month;
                    chip.setChecked(true);
                }
                binding.listChipGroupMonth.addView(chip);
                chip.setOnClickListener(v -> {
                    this.month = month;
                    binding.listChipGroupMonth.clearCheck();
                    chip.setChecked(true);
                    mViewModel.getList(TimeUtils.getMonthInt(month), Integer.parseInt(year));
                });
            }
        });

        mViewModel.yearsLD.observe(this, years -> {
            binding.listChipGroupYear.removeAllViews();
            Timber.d("yearsLD: size: %d", years.size());
            for (String year: years) {
                Chip chip = new Chip(this);
                chip.setText(year);
                chip.setCheckable(true);
                if (year.equalsIgnoreCase(String.valueOf(TimeUtils.getCurrentYear()))) {
                    this.year = year;
                    chip.setChecked(true);
                }
                binding.listChipGroupYear.addView(chip);
                chip.setOnClickListener(v -> {
                    this.year = year;
                    binding.listChipGroupYear.clearCheck();
                    chip.setChecked(true);
                    mViewModel.getList(TimeUtils.getMonthInt(month), Integer.parseInt(year));
                });
            }
        });
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
        binding.listToolbar.setTitle(getString(R.string.action_list));
        setSupportActionBar(binding.listToolbar);
        binding.listToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        binding.listToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViewModel() {
        AppComponent component = ((App)getApplication()).getAppComponent();
        DaggerActivityComponent.builder()
                .setActivity(this)
                .setDependencies(component)
                .interactorModule(new InteractorModule())
                .build()
                .inject(this);
        getLifecycle().addObserver(mViewModel);
        mViewModel.daysLD.observe(this, data -> adapter.changeList(data));
    }

    private void initList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new ListAdapter(this);
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
    }

    @Override
    public void onClickFinish(String day) {
        Intent intent = new Intent();
        intent.putExtra("day", day);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClickDelete(DayEntity day) {
        mViewModel.deleteDay(day);
    }

    @Override
    public void onClickChip(DayEntity day, int timeType) {
        showTimePickerDialog(day, timeType);
    }


    @Override
    public void onClickCameChipClose(DayEntity day) {
        mViewModel.setCameTime(day, null);
    }

    @Override
    public void onClickGoneChipClose(DayEntity day) {
        mViewModel.setGoneTime(day, null);
    }

    private void showTimePickerDialog(DayEntity day, int type){
        TimePickerDialog tpd = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfDay) -> {
                        switch (type) {
                            case TimeType.CAME:
                                mViewModel.setCameTime(day, TimeUtils.getTime(hourOfDay, minuteOfDay));
                                break;
                            case TimeType.GONE:
                                mViewModel.setGoneTime(day, TimeUtils.getTime(hourOfDay, minuteOfDay));
                                break;
                        }
        },
                TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
        tpd.show();
    }


}

package nightgoat.timesheet.presentation.list;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;

import java.util.Set;

import javax.inject.Inject;

import nightgoat.timesheet.App;
import nightgoat.timesheet.R;
import nightgoat.timesheet.database.DayEntity;
import nightgoat.timesheet.databinding.ActivityListBinding;
import nightgoat.timesheet.di.AppComponent;
import nightgoat.timesheet.di.DaggerListActivityComponent;
import nightgoat.timesheet.presentation.ActivityAdapterCallbacks;
import nightgoat.timesheet.presentation.settings.SettingsActivity;
import nightgoat.timesheet.utils.DateType;
import nightgoat.timesheet.utils.mTextWatcher;
import nightgoat.timesheet.utils.TimeType;
import nightgoat.timesheet.utils.TimeUtils;
import timber.log.Timber;

public class ListActivity extends AppCompatActivity implements ActivityAdapterCallbacks {

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
        initCloseSearchBtnClickListener();
    }

    private void initCloseSearchBtnClickListener() {
        binding.listCloseSearch.setOnClickListener(v -> {
            binding.listActivityEditText.setText("");
            binding.listSearchLayout.setVisibility(View.GONE);
        });
    }

    private void initViewModelObservations() {
        mViewModel.monthLD.observe(this, months -> {
            binding.listChipGroupMonth.removeAllViews();
            Timber.d("monthLD: size: %d", months.size());
            createChips(months, DateType.MONTH);
        });

        mViewModel.yearsLD.observe(this, years -> {
            binding.listChipGroupYear.removeAllViews();
            Timber.d("yearsLD: size: %d", years.size());
            createChips(years, DateType.YEAR);
        });
    }

    private void createChips(Set<String> array, DateType type) {
        for (String s : array) {
            Chip chip = new Chip(this);
            chip.setText(s);
            chip.setCheckable(true);

            switch (type) {
                case YEAR:
                    if (s.equals(TimeUtils.getCurrentYearString())) {
                        this.year = s;
                        chip.setChecked(true);
                    }
                    chip.setOnClickListener(v -> {
                        this.year = s;
                        binding.listChipGroupYear.clearCheck();
                        chip.setChecked(true);
                        mViewModel.getList(TimeUtils.getMonthInt(month), Integer.parseInt(year));
                    });
                    binding.listChipGroupYear.addView(chip);
                    break;
                case MONTH:
                    if (TimeUtils.getMonthInt(s) == TimeUtils.getCurrentMonth()) {
                        this.month = s;
                        chip.setChecked(true);
                    }
                    chip.setOnClickListener(v -> {
                        this.month = s;
                        binding.listChipGroupMonth.clearCheck();
                        chip.setChecked(true);
                        mViewModel.getList(TimeUtils.getMonthInt(month), Integer.parseInt(year));
                    });
                    binding.listChipGroupMonth.addView(chip);
                    break;
            }
        }
    }

    private void initSearchTextChangedListener() {
        binding.listActivityEditText.addTextChangedListener(new mTextWatcher(){
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
        AppComponent component = ((App) getApplication()).getAppComponent();
        DaggerListActivityComponent.builder()
                .setActivity(this)
                .setDependencies(component)
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
        switch (item.getItemId()) {
            case R.id.action_settings:
                goToSettingsActivity();
                break;
            case R.id.action_search:
                if (binding.listSearchLayout.getVisibility() == View.GONE)
                    binding.listSearchLayout.setVisibility(View.VISIBLE);
                else {
                    binding.listActivityEditText.setText("");
                    binding.listSearchLayout.setVisibility(View.GONE);
                }
                break;
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
    public void onClickChip(DayEntity day, TimeType type) {
        showTimePickerDialog(day, type);
    }

    @Override
    public void onClickCameChipClose(DayEntity day) {
        mViewModel.setCameTime(day, null);
    }

    @Override
    public void onClickGoneChipClose(DayEntity day) {
        mViewModel.setGoneTime(day, null);
    }

    private void showTimePickerDialog(DayEntity day, TimeType type) {
        TimePickerDialog tpd = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfDay) -> {
                    switch (type) {
                        case CAME:
                            mViewModel.setCameTime(day, TimeUtils.getTime(hourOfDay, minuteOfDay));
                            break;
                        case GONE:
                            mViewModel.setGoneTime(day, TimeUtils.getTime(hourOfDay, minuteOfDay));
                            break;
                    }
                },
                TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
        tpd.show();
    }


}

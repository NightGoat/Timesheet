package nightgoat.timesheet.presentation.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import java.util.Locale;

import javax.inject.Inject;

import nightgoat.timesheet.App;
import nightgoat.timesheet.IResourceHolder;
import nightgoat.timesheet.R;
import nightgoat.timesheet.TimeUtils;
import nightgoat.timesheet.databinding.ActivityMainBinding;
import nightgoat.timesheet.di.AppComponent;
import nightgoat.timesheet.di.DaggerAcitivityComponent;
import nightgoat.timesheet.di.InteractorModule;
import nightgoat.timesheet.domain.Interactor;
import nightgoat.timesheet.presentation.list.ListActivity;
import nightgoat.timesheet.presentation.ViewModelFactory;
import nightgoat.timesheet.presentation.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    @Inject
    Interactor interactor;
    @Inject
    IResourceHolder resourceHolder;

    private ActivityMainBinding binding;
    private DaysViewModel mViewModel;
    private GestureDetector gestureDetector;
    private Integer day, month, year;

    private static final String TAG = MainActivity.class.getName();
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        initViewModel();
        gestureDetector = new GestureDetector(this, this);
        //Пришел:
        mViewModel.timeComeLD.observe(this, data -> binding.comeET.setText(data));
        //Ушел
        mViewModel.timeGoneLD.observe(this, data -> binding.goneET.setText(data));
        //Дата
        mViewModel.dateLD.observe(this, data -> binding.dayET.setText(data));
        //День недели
        mViewModel.dayOfWeek.observe(this, data -> binding.dayTIL.setHint(data));
        //Был на работе
        mViewModel.timeDifferenceLD.observe(this, data -> binding.wasOnWorkET.setText(data));
        //Осталось работать
        mViewModel.timeLeftToWorkTodayLD.observe(this, data -> binding.leftTimeET.setText(data));
        //День, месяц, год для DatePickerDialog
        mViewModel.dayLD.observe(this, data -> day = data);
        mViewModel.monthLD.observe(this, data -> month = data - 1);
        mViewModel.yearLD.observe(this, data -> year = data);
        mViewModel.isGoneTimeExistLD.observe(this, isGoneTimeExist -> {
            binding.goneTIL.setEndIconVisible(isGoneTimeExist);
            if (isGoneTimeExist) {
                binding.goneET.setTextColor(getResources().getColor(R.color.colorAccent));
                binding.wasOnWorkET.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                binding.goneET.setTextColor(getResources().getColor(R.color.colorLightGrey));
                binding.wasOnWorkET.setTextColor(getResources().getColor(R.color.colorLightGrey));
            }
        });
        mViewModel.workedHoursSumLD.observe(this, data -> binding.workedHoursSum.setText(data));

        initLeftArrowClickListener();
        initRightArrowClickListener();
        initComeBtnClickListener();
        initGoneBtnClickListener();
        initComeETClickListener();
        initGoneETClickListener();
        initDayETClickListener();
        initComeTILEndIconClickListener();
        initGoneTILEndIconClickListener();
    }

    private void initGoneTILEndIconClickListener() {
        binding.goneTIL.setEndIconOnClickListener(v -> mViewModel.setGoneTime(null));
    }

    private void initComeTILEndIconClickListener() {
        binding.comeTIL.setEndIconOnClickListener(v -> mViewModel.setComeTime(null));
    }

    private void initViewModel() {
        AppComponent component = ((App) getApplication()).getAppComponent();
        DaggerAcitivityComponent.builder()
                .appComponent(component)
                .interactorModule(new InteractorModule())
                .build()
                .inject(this);

        mViewModel = new ViewModelProvider(this, new ViewModelFactory(interactor, resourceHolder)).get(DaysViewModel.class);
        getLifecycle().addObserver(mViewModel);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void initLeftArrowClickListener() {
        binding.leftBtn.setOnClickListener(v -> mViewModel.setPreviousDay());
    }

    public void initRightArrowClickListener() {
        binding.rightBtn.setOnClickListener(v -> mViewModel.setNextDay());
    }

    public void initComeBtnClickListener() {
        binding.buttonCome.setOnClickListener(v -> mViewModel.setComeTime(TimeUtils.getCurrentTime()));
    }

    public void initGoneBtnClickListener() {
        binding.buttonGone.setOnClickListener(v -> mViewModel.setGoneTime(TimeUtils.getCurrentTime()));
    }

    public void initComeETClickListener() {
        binding.comeET.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) ->
                            mViewModel.setComeTime(TimeUtils.getTime(hourOfDay, minuteOfDay))
                    , TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
            tpd.show();
        });
    }

    public void initGoneETClickListener() {
        binding.goneET.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) ->
                            mViewModel.setGoneTime(TimeUtils.getTime(hourOfDay, minuteOfDay)),
                    TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
            tpd.show();
        });
    }

    private void initDayETClickListener() {
        binding.dayET.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%d.%02d.%02d", year, month + 1, dayOfMonth);
            mViewModel.getDayEntity(date);
        }, year, month, day);
        datePickerDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_list) {
            goToListActivity();
            return true;
        } else if (id == R.id.action_today) {
            mViewModel.getDayEntity(TimeUtils.getCurrentDate());
        } else goToSettingsActivity();
        return super.onOptionsItemSelected(item);
    }

    private void goToListActivity() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivityForResult(intent, 1);
        Log.d(TAG, "Going to List activity");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            mViewModel.getDayEntity(data.getStringExtra("day"));
        }
    }

    private void goToSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        Log.d(TAG, "Going to Settings activity");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d(TAG, "onShowPress: " + e.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        mViewModel.setPreviousDay();
                    } else {
                        mViewModel.setNextDay();
                    }
                    result = true;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPause() {
        mViewModel.deleteEmptyEntities();
        super.onPause();
    }
}


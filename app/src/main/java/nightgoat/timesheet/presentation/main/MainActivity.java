package nightgoat.timesheet.presentation.main;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import javax.inject.Inject;

import nightgoat.timesheet.App;
import nightgoat.timesheet.R;
import nightgoat.timesheet.databinding.ActivityMainBinding;
import nightgoat.timesheet.di.AppComponent;
import nightgoat.timesheet.di.DaggerActivityComponent;
import nightgoat.timesheet.di.InteractorModule;
import nightgoat.timesheet.presentation.list.ListActivity;
import nightgoat.timesheet.presentation.settings.SettingsActivity;
import nightgoat.timesheet.utils.TimeUtils;

public class MainActivity extends AppCompatActivity {

    @Inject
    DaysViewModel mViewModel;

    private ActivityMainBinding binding;
    private GestureDetector gestureDetector;
    private Integer day, month, year;

    @SuppressWarnings("unused")
    private static final String TAG = MainActivity.class.getName();

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.activityMainToolbar);
        initViewModel();
        gestureDetector = new GestureDetector(this, new MyGestureListener());
        initViewModelObservations();
        initPreviousDayBtnClickListener();
        initNextDayBtnClickListener();
        initCameBtnClickListener();
        initGoneBtnClickListener();
        initCameEditTextClickListener();
        initGoneEditTextClickListener();
        initDayEditTextClickListener();
        initCameTextInputLayoutEndIconClickListener();
        initGoneTextInputLayoutEndIconClickListener();
    }

    private void initViewModel() {
        AppComponent component = ((App) getApplication()).getAppComponent();
        DaggerActivityComponent.builder()
                .setActivity(this)
                .setDependencies(component)
                .interactorModule(new InteractorModule())
                .build()
                .inject(this);
        getLifecycle().addObserver(mViewModel);
    }

    private void initViewModelObservations() {
        mViewModel.timeCameLiveData.observe(this, data ->
                binding.activityMainCameTextInputEditText.setText(data));                            //Пришел:
        mViewModel.timeGoneLiveData.observe(this, data ->
                binding.activityMainGoneTextInputEditText.setText(data));                            //Ушел
        mViewModel.dateLiveData.observe(this, data -> {                                        //Дата
                    if (data.equals(TimeUtils.getCurrentDateNormalFormat()))
                        binding.activityMainDayTextInputEditText
                                .setTextColor(getResources().getColor(R.color.colorPrimary));
                    else binding.activityMainDayTextInputEditText
                            .setTextColor(getResources().getColor(R.color.colorGrey));
                    binding.activityMainDayTextInputEditText.setText(data);
                }
        );
        mViewModel.dayOfWeekLiveData.observe(this, data ->
                binding.activityMainDayTextInputLayout.setHint(data));                               //День недели
        mViewModel.timeWasOnWorkLiveData.observe(this, data ->
                binding.activityMainTimeWasOnWorkTextInputEditText.setText(data));                   //Был на работе
        mViewModel.timeLeftToWorkTodayLiveData.observe(this, data ->
                binding.activityMainTimeLeftToWorkTextInputEditText.setText(data));                  //Осталось работать
        mViewModel.dayLiveData.observe(this, data -> day = data);                              //День,  для DatePickerDialog
        mViewModel.monthLiveData.observe(this, data -> month = data - 1);                      //Месяц для DatePickerDialog
        mViewModel.yearLiveData.observe(this, data -> year = data);                            // Год для DatePickerDialog
        mViewModel.isGoneTimeExistLiveData.observe(this, isGoneTimeExist -> {
            binding.activityMainGoneTextInputLayout.setEndIconVisible(isGoneTimeExist);
            if (isGoneTimeExist) {
                binding.activityMainGoneTextInputEditText
                        .setTextColor(getResources().getColor(R.color.colorAccent));
                binding.activityMainTimeWasOnWorkTextInputEditText
                        .setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                binding.activityMainGoneTextInputEditText
                        .setTextColor(getResources().getColor(R.color.colorLightGrey));
                binding.activityMainTimeWasOnWorkTextInputEditText
                        .setTextColor(getResources().getColor(R.color.colorLightGrey));
            }
        });
        mViewModel.workedHoursSumLiveData.observe(this, data ->
                binding.activityMainWorkedHoursSumTextView2.setText(data));
    }

    private void initGoneTextInputLayoutEndIconClickListener() {
        binding.activityMainGoneTextInputLayout
                .setEndIconOnClickListener(v -> mViewModel.setGoneTime(null));
    }

    private void initCameTextInputLayoutEndIconClickListener() {
        binding.activityMainCameTextInputLayout
                .setEndIconOnClickListener(v -> mViewModel.setCameTime(null));
    }

    public void initPreviousDayBtnClickListener() {
        binding.activityMainPreviousDayBtn.setOnClickListener(v -> mViewModel.setPreviousDay());
    }

    public void initNextDayBtnClickListener() {
        binding.activityMainNextDayBtn.setOnClickListener(v -> mViewModel.setNextDay());
    }

    public void initCameBtnClickListener() {
        binding.activityMainCameBtn.setOnClickListener(v ->
                mViewModel.setCameTime(TimeUtils.getCurrentTime()));
    }

    public void initGoneBtnClickListener() {
        binding.activityMainGoneBtn.setOnClickListener(v ->
                mViewModel.setGoneTime(TimeUtils.getCurrentTime()));
    }

    public void initCameEditTextClickListener() {
        binding.activityMainCameTextInputEditText.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) ->
                            mViewModel.setCameTime(TimeUtils.getTime(hourOfDay, minuteOfDay))
                    , TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
            tpd.show();
        });
    }

    public void initGoneEditTextClickListener() {
        binding.activityMainGoneTextInputEditText.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) ->
                            mViewModel.setGoneTime(TimeUtils.getTime(hourOfDay, minuteOfDay)),
                    TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
            tpd.show();
        });
    }

    private void initDayEditTextClickListener() {
        binding.activityMainDayTextInputEditText.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog =
                new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                    String date = String.format(
                            Locale.getDefault(),
                            "%d-%02d-%02d", year, month + 1, dayOfMonth);
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
        switch (item.getItemId()) {
            case R.id.action_list:
                goToListActivity();
                break;
            case R.id.action_today:
                mViewModel.getDayEntity(TimeUtils.getCurrentDate());
                break;
            case R.id.action_settings:
                goToSettingsActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToListActivity() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivityForResult(intent, 1);
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
    }


    @Override
    protected void onPause() {
        mViewModel.deleteEmptyEntities();
        super.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        return gestureDetector.onTouchEvent(event);
    }


    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
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
    }
}



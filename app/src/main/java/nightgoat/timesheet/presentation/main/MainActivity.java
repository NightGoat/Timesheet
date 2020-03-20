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
import nightgoat.timesheet.di.DaggerMainActivityComponent;
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
        setSupportActionBar(binding.mainToolbar);
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
        DaggerMainActivityComponent.builder()
                .setActivity(this)
                .setDependencies(component)
                .build()
                .inject(this);
        getLifecycle().addObserver(mViewModel);
    }

    private void initViewModelObservations() {
        mViewModel.containerLiveData.observe(this, dataContainer -> {
            binding.mainEditCame.setText(dataContainer.timeCame);
            binding.mainEditGone.setText(dataContainer.timeGone);
            if (dataContainer.date != null && dataContainer.date.equals(TimeUtils.getCurrentDateNormalFormat()))
                binding.mainEditDate
                        .setTextColor(getResources().getColor(R.color.colorPrimary));
            else binding.mainEditDate
                    .setTextColor(getResources().getColor(R.color.colorGrey));
            binding.mainEditDate.setText(dataContainer.date);
            binding.mainTextInputLayoutDate.setHint(dataContainer.dayOfWeek);
            binding.mainEditTimeWasOnWork.setText(dataContainer.timeWasOnWork);
            binding.mainEditTimeLeftToWork.setText(dataContainer.timeLeftToWorkToday);
            day = dataContainer.day;
            month = dataContainer.month;
            year = dataContainer.year;
            if (dataContainer.isGoneTimeExist != null) {
                binding.mainTextInputLayoutGone.setEndIconVisible(dataContainer.isGoneTimeExist);
                if (dataContainer.isGoneTimeExist) {
                    binding.mainEditGone
                            .setTextColor(getResources().getColor(R.color.colorAccent));
                    binding.mainEditTimeWasOnWork
                            .setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    binding.mainEditGone
                            .setTextColor(getResources().getColor(R.color.colorLightGrey));
                    binding.mainEditTimeWasOnWork
                            .setTextColor(getResources().getColor(R.color.colorLightGrey));
                }
            }
            binding.mainTextWorkedHoursSumValue.setText(dataContainer.workedHoursSum);
        });
    }

    private void initGoneTextInputLayoutEndIconClickListener() {
        binding.mainTextInputLayoutGone
                .setEndIconOnClickListener(v -> mViewModel.setGoneTime(null));
    }

    private void initCameTextInputLayoutEndIconClickListener() {
        binding.mainTextInputLayoutCame
                .setEndIconOnClickListener(v -> mViewModel.setCameTime(null));
    }

    public void initPreviousDayBtnClickListener() {
        binding.mainBtnPreviousDay.setOnClickListener(v -> mViewModel.setPreviousDay());
    }

    public void initNextDayBtnClickListener() {
        binding.mainBtnNextDay.setOnClickListener(v -> mViewModel.setNextDay());
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
        binding.mainEditCame.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) ->
                            mViewModel.setCameTime(TimeUtils.getTime(hourOfDay, minuteOfDay))
                    , TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
            tpd.show();
        });
    }

    public void initGoneEditTextClickListener() {
        binding.mainEditGone.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) ->
                            mViewModel.setGoneTime(TimeUtils.getTime(hourOfDay, minuteOfDay)),
                    TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
            tpd.show();
        });
    }

    private void initDayEditTextClickListener() {
        binding.mainEditDate.setOnClickListener(v -> showDatePickerDialog());
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



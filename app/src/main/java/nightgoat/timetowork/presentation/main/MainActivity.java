package nightgoat.timetowork.presentation.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MotionEventCompat;
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
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

import nightgoat.timetowork.Injection;
import nightgoat.timetowork.R;
import nightgoat.timetowork.TimeUtils;
import nightgoat.timetowork.presentation.list.ListActivity;
import nightgoat.timetowork.presentation.ViewModelFactory;
import nightgoat.timetowork.presentation.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {
    private ImageButton leftArrowIB, rightArrowIB;
    private MaterialButton comeBtn, goneBtn;
    private DaysViewModel mViewModel;
    private Toolbar toolbar;
    private GestureDetector gestureDetector;
    private TextInputEditText comeET, goneET, wasOnWorkET, dayET;
    private TextInputLayout comeTIL, goneTIL, dayTIL;
    private Integer day, month, year;

    private static final String TAG = MainActivity.class.getName();
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setSupportActionBar(toolbar);
        initViewModel();
        gestureDetector = new GestureDetector(this, this);
        //Пришел:
        mViewModel.timeComeLD.observe(this, data -> comeET.setText(data));
        //Ушел
        mViewModel.timeGoneLD.observe(this, data -> goneET.setText(data));
        //Дата
        mViewModel.dateLD.observe(this, data -> dayET.setText(data));
        //День недели
        mViewModel.dayOfWeek.observe(this, data -> dayTIL.setHint(data));
        //Был на работе
        mViewModel.timeDifferenceLD.observe(this, data -> wasOnWorkET.setText(data));
        //День, месяц, год для DatePickerDialog
        mViewModel.dayLD.observe(this, data -> day = data);
        mViewModel.monthLD.observe(this, data -> month = data - 1);
        mViewModel.yearLD.observe(this, data -> year = data);
        mViewModel.isGoneTimeExistLD.observe(this, isGoneTimeExist -> {
            goneTIL.setEndIconVisible(isGoneTimeExist);
            if (isGoneTimeExist) goneET.setTextColor(getResources().getColor(R.color.colorAccent));
            else goneET.setTextColor(getResources().getColor(R.color.colorGrey));
        });

        initLeftArrowClickListener();
        initRightArrowClickListener();
        initComeBtnClickListener();
        initGoneBtnClickListener();
        initComeTextViewClickListener();
        initGoneTextViewClickListener();
        initDayTextViewClickListener();
        initComeTILEndIconClickListener();
        initGoneTILEndIconClickListener();
    }

    private void initGoneTILEndIconClickListener() {
        goneTIL.setEndIconOnClickListener(v -> mViewModel.setGoneTime(null));
    }

    private void initComeTILEndIconClickListener() {
        comeTIL.setEndIconOnClickListener(v -> mViewModel.setComeTime(null));
    }

    private void initViews() {
        leftArrowIB = findViewById(R.id.left_btn);
        rightArrowIB = findViewById(R.id.right_btn);
        dayET = findViewById(R.id.dayET);
        dayTIL = findViewById(R.id.dayTIL);
        wasOnWorkET = findViewById(R.id.wasOnWorkET);
        wasOnWorkET.setEnabled(false);
        comeET = findViewById(R.id.comeET);
        goneET = findViewById(R.id.goneET);
        comeBtn = findViewById(R.id.button_come);
        goneBtn = findViewById(R.id.button_gone);
        toolbar = findViewById(R.id.toolbar);
        comeTIL = findViewById(R.id.comeTIL);
        goneTIL = findViewById(R.id.goneTIL);
    }

    private void initViewModel() {
        ViewModelFactory mViewModelFactory = Injection.provideViewModelFactory(getApplicationContext());
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(DaysViewModel.class);
        getLifecycle().addObserver(mViewModel);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void initLeftArrowClickListener() {
        leftArrowIB.setOnClickListener(v -> mViewModel.setPreviousDay());
    }

    public void initRightArrowClickListener() {
        rightArrowIB.setOnClickListener(v -> mViewModel.setNextDay());
    }

    public void initComeBtnClickListener() {
        comeBtn.setOnClickListener(v -> mViewModel.setComeTime(TimeUtils.getCurrentTime()));
    }

    public void initGoneBtnClickListener() {
        goneBtn.setOnClickListener(v -> mViewModel.setGoneTime(TimeUtils.getCurrentTime()));
    }

    public void initComeTextViewClickListener() {
        comeET.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) ->
                            mViewModel.setComeTime(TimeUtils.getTime(hourOfDay, minuteOfDay))
                    , TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
            tpd.show();
        });
    }

    public void initGoneTextViewClickListener() {
        goneET.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) ->
                            mViewModel.setGoneTime(TimeUtils.getTime(hourOfDay, minuteOfDay)),
                    TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
            tpd.show();
        });
    }

    private void initDayTextViewClickListener() {
        dayET.setOnClickListener(v -> showDatePickerDialog());
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
        } else if(id == R.id.action_today) {
            mViewModel.getDayEntity(TimeUtils.getCurrentDate());
        } else goToSettingsActivity();
        return super.onOptionsItemSelected(item);
    }

    private void goToListActivity() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
        Log.d(TAG, "Going to List activity");
    }

    private void goToSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        Log.d(TAG, "Going to Settings activity");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
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


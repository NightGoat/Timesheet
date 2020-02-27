package nightgoat.timetowork.presentation.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.Locale;

import nightgoat.timetowork.Injection;
import nightgoat.timetowork.R;
import nightgoat.timetowork.presentation.list.ListActivity;
import nightgoat.timetowork.presentation.ViewModelFactory;

public class MainActivity extends AppCompatActivity {
    private ImageButton leftArrowIB, rightArrowIB;
    private TextView dayTV, leftHoursTV, comeTV, goneTV;
    private MaterialButton comeBtn, goneBtn;
    private String date;
    private DaysViewModel mViewModel;
    private ViewModelFactory mViewModelFactory;
    private Calendar calendar = Calendar.getInstance();
    private Toolbar toolbar;

    private static final String TAG = MainActivity.class.getName();

    private void initViews() {
        leftArrowIB = findViewById(R.id.left_btn);
        rightArrowIB = findViewById(R.id.right_btn);
        dayTV = findViewById(R.id.day_text);
        leftHoursTV = findViewById(R.id.time_left_today_TV);
        comeTV = findViewById(R.id.come_today_TV);
        goneTV = findViewById(R.id.gone_today_TV);
        comeBtn = findViewById(R.id.button_come);
        goneBtn = findViewById(R.id.button_gone);
        toolbar = findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setSupportActionBar(toolbar);
        initViewModel();

        mViewModel.timeComeLD.observe(this, data -> comeTV.setText(data));
        mViewModel.timeGoneLD.observe(this, data -> goneTV.setText(data));
        mViewModel.dateLD.observe(this, data -> dayTV.setText(data));
        mViewModel.timeDifferenceLD.observe(this, data -> leftHoursTV.setText(data));

        initLeftArrowClickListener();
        initRightArrowClickListener();
        initComeBtnClickListener();
        initGoneBtnClickListener();
        initComeTextViewClickListener();
        initGoneTextViewClickListener();
        initDayTextViewClickListener();
    }

    private void initViewModel() {
        mViewModelFactory = Injection.provideViewModelFactory(this);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(DaysViewModel.class);
        getLifecycle().addObserver(mViewModel);
    }

    public void initLeftArrowClickListener() {
        leftArrowIB.setOnClickListener(v -> mViewModel.setPreviousDay());
    }

    public void initRightArrowClickListener() {
        rightArrowIB.setOnClickListener(v -> mViewModel.setNextDay());
    }

    public void initComeBtnClickListener() {
        comeBtn.setOnClickListener(v -> mViewModel.setComeTime(mViewModel.getCurrentTime()));
    }

    public void initGoneBtnClickListener() {
        goneBtn.setOnClickListener(v -> mViewModel.setGoneTime(mViewModel.getCurrentTime()));
    }

    public void initComeTextViewClickListener() {
        comeTV.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) ->
                            mViewModel.setComeTime(mViewModel.getTime(hourOfDay, minuteOfDay))
                    , mViewModel.getCurrentHour(), mViewModel.getCurrentMinutes(), true);
            tpd.show();
        });
    }

    public void initGoneTextViewClickListener() {
        goneTV.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) ->
                            mViewModel.setGoneTime(mViewModel.getTime(hourOfDay, minuteOfDay)),
                    mViewModel.getCurrentHour(), mViewModel.getCurrentMinutes(), true);
            tpd.show();
        });
    }

    private void initDayTextViewClickListener() {
        dayTV.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String date = String.format(Locale.getDefault(), "%d.%02d.%02d", year, month + 1, dayOfMonth);
                mViewModel.getDayEntity(date);
            }, mViewModel.getCurrentYear(), mViewModel.getCurrentMonth(), mViewModel.getCurrentDay());
            datePickerDialog.show();
        });
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
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
            Log.d(TAG, "Going to another activity");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}


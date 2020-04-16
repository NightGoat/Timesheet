package nightgoat.timesheet.presentation.main;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import javax.inject.Inject;

import it.sephiroth.android.library.xtooltip.ClosePolicy;
import it.sephiroth.android.library.xtooltip.Tooltip;
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
    private String note;

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
        initCameTextClickListener();
        initGoneTextClickListener();
        initDayTextClickListener();
        initCameDeleteBtnClickListener();
        initGoneDeleteBtnClickListener();
        registerForContextMenu(binding.mainTextNote);
    }

    private void initNoteTextClickListener() {
          Intent intent = new Intent(this, NoteActivity.class);
          intent.putExtra("date", binding.mainTextDate.getText().toString());
          intent.putExtra("dayOfWeek", binding.mainTextDayOfWeek.getText().toString());
          intent.putExtra("noteText", binding.mainTextNote.getText().toString());
          startActivityForResult(intent, 2);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.main_text_note) {
            menu.add(0, 1, 0, getString(R.string.edit));
            menu.add(0, 0, 1, getString(R.string.delete));
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                mViewModel.updateNote("");
                break;
            case 1:
                initNoteTextClickListener();
                break;
        }
        return super.onContextItemSelected(item);
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
            if (dataContainer.timeCame != null) {
                binding.mainBtnDeleteCame.setVisibility(View.VISIBLE);
                binding.mainTextCameValue.setText(dataContainer.timeCame);
            } else {
                binding.mainBtnDeleteCame.setVisibility(View.INVISIBLE);
                binding.mainTextCameValue.setText(null);
            }
            binding.mainTextGoneValue.setText(dataContainer.timeGone);
            if (dataContainer.date != null && dataContainer.date.equals(TimeUtils.getCurrentDateNormalFormat()))
                binding.mainTextDate
                        .setTextColor(getResources().getColor(R.color.colorPrimary));
            else binding.mainTextDate
                    .setTextColor(getResources().getColor(R.color.colorGrey));
            binding.mainTextDate.setText(dataContainer.date);
            binding.mainTextDayOfWeek.setText(dataContainer.dayOfWeek);
            binding.mainTextTimeWasOnWork.setText(dataContainer.timeWasOnWork);
            binding.mainTextTimeLeftToWork.setText(dataContainer.timeLeftToWorkToday);
            day = dataContainer.day;
            month = dataContainer.month;
            year = dataContainer.year;
            if (dataContainer.isGoneTimeExist != null) {
                if (dataContainer.isGoneTimeExist) {
                    binding.mainBtnDeleteGone.setVisibility(View.VISIBLE);
                    binding.mainTextGoneValue
                            .setTextColor(getResources().getColor(R.color.colorAccent));
                    binding.mainTextTimeWasOnWork
                            .setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    binding.mainBtnDeleteGone.setVisibility(View.INVISIBLE);
                    binding.mainTextGoneValue
                            .setTextColor(getResources().getColor(R.color.colorLightGrey));
                    binding.mainTextTimeWasOnWork
                            .setTextColor(getResources().getColor(R.color.colorLightGrey));
                }
            }
            binding.mainTextWorkedHoursSumValue.setText(dataContainer.workedHoursSum);
            note = dataContainer.note;
            binding.mainTextNote.setText(note);
        });
    }

    private void initGoneDeleteBtnClickListener() {
        binding.mainBtnDeleteGone
                .setOnClickListener(v -> mViewModel.setGoneTime(null));
    }

    private void initCameDeleteBtnClickListener() {
        binding.mainBtnDeleteCame
                .setOnClickListener(v -> mViewModel.setCameTime(null));
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

    public void initCameTextClickListener() {
        binding.mainTextCameValue.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) ->
                            mViewModel.setCameTime(TimeUtils.getTime(hourOfDay, minuteOfDay))
                    , TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
            tpd.show();
        });
    }

    public void initGoneTextClickListener() {
        binding.mainTextGoneValue.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) ->
                            mViewModel.setGoneTime(TimeUtils.getTime(hourOfDay, minuteOfDay)),
                    TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
            tpd.show();
        });
    }

    private void initDayTextClickListener() {
        binding.mainLayoutDate.setOnClickListener(v -> showDatePickerDialog());
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
            case R.id.action_wtf:
                ClosePolicy.Builder closePolicyBuilder = new ClosePolicy.Builder();
                ClosePolicy closePolicy = closePolicyBuilder.outside(true).build();
                Tooltip cameTooltip = createTooltip(binding.mainIconCame, R.string.wtfCame, closePolicy);
                cameTooltip.show(binding.mainLinearLayoutCame, Tooltip.Gravity.RIGHT, true);
                Tooltip goneTooltip = createTooltip(binding.mainIconGone, R.string.wtfGone, closePolicy);
                goneTooltip.show(binding.mainLinearLayoutGone, Tooltip.Gravity.RIGHT, true);
                Tooltip TimeWasOnWorkTooltip = createTooltip(binding.mainIconTimeWasOnWork, R.string.wtfTimeWasOnWork, closePolicy);
                TimeWasOnWorkTooltip.show(binding.mainLinearLayoutTimeWasOnWork, Tooltip.Gravity.RIGHT, true);
                Tooltip TimeLeftToWorkTooltip = createTooltip(binding.mainIconTimeLeftToWork, R.string.wtfTimeLeftToWork, closePolicy);
                TimeLeftToWorkTooltip.show(binding.mainLinearLayoutTimeLeftToWork, Tooltip.Gravity.RIGHT, true);
                Tooltip WorkedHoursMonthTooltip = createTooltip(binding.mainIconWorkedHoursMonth, R.string.wtfWorkedHours, closePolicy);
                WorkedHoursMonthTooltip.show(binding.mainLinearLayoutWorkedHours, Tooltip.Gravity.RIGHT, true);
                Tooltip NoteTooltip = createTooltip(binding.mainIconWorkedNote, R.string.wtfNote, closePolicy);
                NoteTooltip.show(binding.mainLinearLayoutNote, Tooltip.Gravity.RIGHT, true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Tooltip createTooltip(View anchor, int textId, ClosePolicy closePolicy){
        return new Tooltip.Builder(this)
                .anchor(anchor, 0, 0, false)
                .text(getString(textId))
                .closePolicy(closePolicy)
                .create();
    }

    private void goToListActivity() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            switch (requestCode) {
                case 1:
                    mViewModel.getDayEntity(data.getStringExtra("day"));
                    break;
                case 2:
                    mViewModel.updateNote(data.getStringExtra("noteText"));
            }
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



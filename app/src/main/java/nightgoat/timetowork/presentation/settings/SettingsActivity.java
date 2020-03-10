package nightgoat.timetowork.presentation.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import nightgoat.timetowork.BuildConfig;
import nightgoat.timetowork.Injection;
import nightgoat.timetowork.R;
import nightgoat.timetowork.presentation.ViewModelFactory;

public class SettingsActivity extends AppCompatActivity implements ISnackBarMaker {

    private Toolbar toolbar;
    private TextView needToBeOnWork_TV;
    private MaterialButton saveDBtoExcel_Btn, cleanDB_Btn, openExcel_Btn;
    private SettingsViewModel mViewModel;
    private ProgressBar progressBar;
    private String directory_path ;
    private File excelFile;
    private static final int WRITE_REQUEST_CODE = 43;

    private final String TAG = SettingsActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        initViewModel();
        initToolbar();
        initNeedToBeOnWorkListener();
        initCleanDBListener();
        mViewModel.isOpenExcelFileBtnEnabled.observe(this, aBoolean -> openExcel_Btn.setEnabled(aBoolean));
        mViewModel.isProgressBarVisible.observe(this, aBoolean -> {
            if (aBoolean) progressBar.setVisibility(View.VISIBLE);
            else progressBar.setVisibility(View.INVISIBLE);
        });
        mViewModel.excelFileLD.observe(this, file -> excelFile = file);
        directory_path = Objects.requireNonNull(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)).getPath() + "/TimeToWork/";
        saveDBtoExcel_Btn.setOnClickListener(v -> saveDBtoExcel());
        openExcel_Btn.setOnClickListener(v -> openExcelFile());
        mViewModel.checkIsExcelFileExists();
    }

    void saveDBtoExcel() {
        if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
        mViewModel.saveDBtoExcel();
        } else {
            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            saveDBtoExcel();
        }
    }

    void openExcelFile(){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(SettingsActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    excelFile);
            intent.setDataAndType(uri, "application/vnd.ms-excel");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Нет приложения для открытия файла", Toast.LENGTH_SHORT).show();
        }
    }

    private void initCleanDBListener() {
        cleanDB_Btn.setOnClickListener(v -> {
            final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setMessage("Удалить все записи?")
                    .setPositiveButton("Да", (dialog, which) -> mViewModel.deleteEverything())
                    .setNegativeButton("Отмена", ((dialog, which) -> dialog.cancel()))
                    .create()
                    .show();
        });
    }

    private void initNeedToBeOnWorkListener() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        needToBeOnWork_TV.setText(sharedPreferences.getString("needToWork", "8:30"));
        needToBeOnWork_TV.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) -> {
                        String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfDay);
                        sharedPreferences
                                .edit()
                                .putString("needToWork", time)
                                .apply();
                        needToBeOnWork_TV.setText(time);
                    }
                    , 8, 30, true);
            tpd.show();
        });

    }

    private void initToolbar() {
        toolbar.setTitle(getString(R.string.action_settings));
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViewModel() {
        ViewModelFactory mViewModelFactory = Injection.provideViewModelFactory(getApplicationContext());
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(SettingsViewModel.class);
        getLifecycle().addObserver(mViewModel);
        mViewModel.setSnackBarMaker(this);
    }

    private void initViews() {
        toolbar = findViewById(R.id.settings_activity_toolbar);
        needToBeOnWork_TV = findViewById(R.id.settings_needToBeOnWork_value_TV);
        saveDBtoExcel_Btn = findViewById(R.id.settings_saveDBtoExcel_Btn);
        cleanDB_Btn = findViewById(R.id.settings_cleanDB_Btn);
        progressBar = findViewById(R.id.settings_progressBar);
        openExcel_Btn = findViewById(R.id.settings_openExcel_Btn);
    }

    @Override
    public void createSnackBar(String text) {
        Snackbar.make(needToBeOnWork_TV,
                 text, Snackbar.LENGTH_LONG)
                .setAction("Открыть", v -> openExcelFile()).show();
    }
}

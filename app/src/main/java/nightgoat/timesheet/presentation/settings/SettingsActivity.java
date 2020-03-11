package nightgoat.timesheet.presentation.settings;

import androidx.appcompat.app.AppCompatActivity;
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
import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.Locale;

import javax.inject.Inject;

import nightgoat.timesheet.App;
import nightgoat.timesheet.BuildConfig;
import nightgoat.timesheet.IResourceHolder;
import nightgoat.timesheet.R;
import nightgoat.timesheet.databinding.ActivitySettingsBinding;
import nightgoat.timesheet.di.AppComponent;
import nightgoat.timesheet.di.DaggerAcitivityComponent;
import nightgoat.timesheet.di.InteractorModule;
import nightgoat.timesheet.domain.Interactor;
import nightgoat.timesheet.presentation.ISnackBarMaker;
import nightgoat.timesheet.presentation.ViewModelFactory;

public class SettingsActivity extends AppCompatActivity implements ISnackBarMaker {

    private ActivitySettingsBinding binding;
    private SettingsViewModel mViewModel;
    private File excelFile;
    private final String TAG = SettingsActivity.class.getName();

    @Inject
    Interactor interactor;

    @Inject
    IResourceHolder resourceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViewModel();
        initToolbar();
        initNeedToBeOnWorkListener();
        initCleanDBListener();

        mViewModel.isOpenExcelFileBtnEnabled.observe(this, aBoolean -> binding.settingsOpenExcelBtn.setEnabled(aBoolean));
        mViewModel.isProgressBarVisible.observe(this, aBoolean -> {
            if (aBoolean) binding.settingsProgressBar.setVisibility(View.VISIBLE);
            else binding.settingsProgressBar.setVisibility(View.INVISIBLE);
        });
        mViewModel.excelFileLD.observe(this, file -> excelFile = file);
        binding.settingsSaveDBtoExcelBtn.setOnClickListener(v -> saveDBtoExcel());
        binding.settingsOpenExcelBtn.setOnClickListener(v -> openExcelFile());
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
            Toast.makeText(this, getString(R.string.noExcelApp), Toast.LENGTH_SHORT).show();
        }
    }

    private void initCleanDBListener() {
        binding.settingsCleanDBBtn.setOnClickListener(v -> {
            final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setMessage(getString(R.string.deleteDB))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> mViewModel.deleteEverything())
                    .setNegativeButton(getString(R.string.cancel), ((dialog, which) -> dialog.cancel()))
                    .create()
                    .show();
        });
    }

    private void initNeedToBeOnWorkListener() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        binding.settingsNeedToBeOnWorkValueTV.setText(sharedPreferences.getString("needToWork", "8:30"));
        binding.settingsNeedToBeOnWorkValueTV.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfDay) -> {
                        String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfDay);
                        sharedPreferences
                                .edit()
                                .putString("needToWork", time)
                                .apply();
                        binding.settingsNeedToBeOnWorkValueTV.setText(time);
                    }
                    , 8, 30, true);
            tpd.show();
        });
    }

    private void initToolbar() {
        binding.settingsToolbar.setTitle(getString(R.string.action_settings));
        binding.settingsToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        setSupportActionBar(binding.settingsToolbar);
        binding.settingsToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViewModel() {
        AppComponent component = ((App)getApplication()).getAppComponent();
        DaggerAcitivityComponent.builder()
                .appComponent(component)
                .interactorModule(new InteractorModule())
                .build()
                .inject(this);
        ViewModelFactory mViewModelFactory = new ViewModelFactory(interactor, resourceHolder);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(SettingsViewModel.class);
        getLifecycle().addObserver(mViewModel);
        mViewModel.setSnackBarMaker(this);
    }

    @Override
    public void createSnackBar(String text) {
        Snackbar.make(binding.settingsNeedToBeOnWorkValueTV,
                 text, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.open), v -> openExcelFile()).show();
    }
}

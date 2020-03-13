package nightgoat.timesheet.presentation.settings;

import android.util.Log;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ajts.androidmads.library.SQLiteToExcel;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import nightgoat.timesheet.IResourceHolder;
import nightgoat.timesheet.R;
import nightgoat.timesheet.domain.Interactor;
import nightgoat.timesheet.presentation.ISnackBarMaker;
import timber.log.Timber;

public class SettingsViewModel extends ViewModel implements LifecycleObserver {

    private final String TAG = SettingsViewModel.class.getName();

    MutableLiveData<Boolean> isProgressBarVisible = new MutableLiveData<>();
    MutableLiveData<Boolean> isOpenExcelFileBtnEnabled = new MutableLiveData<>();
    MutableLiveData<File> excelFileLD = new MutableLiveData<>();

    private Interactor interactor;
    private IResourceHolder resourceHolder;
    private ISnackBarMaker snackBarMaker;

    private String directory_path;
    private final String filename = "TimeToWork.xls";

    public SettingsViewModel(Interactor interactor, IResourceHolder resourceHolder) {
        this.interactor = interactor;
        this.resourceHolder = resourceHolder;
        directory_path = resourceHolder.getDirectory();
    }

    void setSnackBarMaker(ISnackBarMaker snackBarMaker) {
        this.snackBarMaker = snackBarMaker;
    }

    void deleteEverything(){
        interactor.deleteEverything().observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    void saveDBtoExcel() {
            File directory = new File(directory_path);
            if (!directory.exists()) {
                Timber.tag(TAG).v("File Created%s", String.valueOf(directory.mkdirs()));
            }
            SQLiteToExcel sqLiteToExcel = resourceHolder.createSQLiteToExcel();
            sqLiteToExcel.exportAllTables(filename, new SQLiteToExcel.ExportListener() {
                @Override
                public void onStart() {
                    isProgressBarVisible.setValue(true);
                }

                @Override
                public void onCompleted(String filePath) {
                    snackBarMaker.createSnackBar(resourceHolder.getString(R.string.saved) + " " + filePath);
                    isProgressBarVisible.setValue(false);
                    isOpenExcelFileBtnEnabled.setValue(true);
                    checkIsExcelFileExists();
                }

                @Override
                public void onError(Exception e) {
                    snackBarMaker.createSnackBar(e.getMessage());
                    isOpenExcelFileBtnEnabled.setValue(false);
                    isProgressBarVisible.setValue(false);
                }
            });
    }

    void checkIsExcelFileExists() {
        File excelFile = new File(directory_path + filename);
        if (excelFile.exists()) {
            isOpenExcelFileBtnEnabled.setValue(true);
            excelFileLD.setValue(excelFile);
        } else {
            isOpenExcelFileBtnEnabled.setValue(false);
        }
    }
}

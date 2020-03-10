package nightgoat.timetowork.presentation.settings;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import com.ajts.androidmads.library.SQLiteToExcel;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import nightgoat.timetowork.IResourceHolder;
import nightgoat.timetowork.domain.Interactor;

public class SettingsViewModel extends ViewModel implements LifecycleObserver {

    MutableLiveData<String> needToWork = new MutableLiveData<>();
    MutableLiveData<Boolean> isProgressBarVisible = new MutableLiveData<>();
    MutableLiveData<Boolean> isOpenExcelFileBtnEnabled = new MutableLiveData<>();
    MutableLiveData<File> excelFileLD = new MutableLiveData<>();

    private Interactor interactor;
    private IResourceHolder resourceHolder;
    private ISnackBarMaker snackBarMaker;
    private File excelFile;

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

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart() {

    }

    void deleteEverything(){
        interactor.deleteEverything().observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    void saveDBtoExcel() {
            File directory = new File(directory_path);
            if (!directory.exists()) {
                Log.v("File Created", String.valueOf(directory.mkdirs()));
            }
            SQLiteToExcel sqLiteToExcel = resourceHolder.createSQLiteToExcel();
            sqLiteToExcel.exportAllTables(filename, new SQLiteToExcel.ExportListener() {
                @Override
                public void onStart() {
                    Log.d("SettingsViewModel", "onStart: ");
                    isProgressBarVisible.setValue(true);
                }

                @Override
                public void onCompleted(String filePath) {
                    snackBarMaker.createSnackBar("Успешно сохранено в: " + filePath);
                    isProgressBarVisible.setValue(false);
                    isOpenExcelFileBtnEnabled.setValue(true);
                    checkIsExcelFileExists();
                    Log.d("SettingsViewModel", "onCompleted: ");
                }

                @Override
                public void onError(Exception e) {
                    Log.d("SettingsViewModel", "onError: " + e.getMessage());
                    snackBarMaker.createSnackBar(e.getMessage());
                    isOpenExcelFileBtnEnabled.setValue(false);
                    isProgressBarVisible.setValue(false);
                }
            });
    }

    void checkIsExcelFileExists() {
        excelFile = new File(directory_path+filename);
        if (excelFile.exists()) {
            isOpenExcelFileBtnEnabled.setValue(true);
            excelFileLD.setValue(excelFile);
            Log.d("SettingsViewModel", "checkIsExcelFileExists: " + directory_path + filename + " exists");
        } else {
            isOpenExcelFileBtnEnabled.setValue(false);
            Log.d("SettingsViewModel", "checkIsExcelFileExists: " + directory_path + filename + " don't exists");
        }
    }
}

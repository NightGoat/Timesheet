package nightgoat.timesheet.presentation.list;

import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import nightgoat.timesheet.R;
import nightgoat.timesheet.TimeUtils;
import nightgoat.timesheet.database.DayEntity;
import nightgoat.timesheet.presentation.ActivityForResultFinisher;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<DayEntity> data = new ArrayList<>();
    private List<DayEntity> sourceData = new ArrayList<>();
    private IListViewModel mViewModel;
    private ActivityForResultFinisher activity;

    ListAdapter(IListViewModel viewModel, ActivityForResultFinisher activity) {
        this.mViewModel = viewModel;
        this.activity = activity;
    }

    void changeList(List<DayEntity> list) {
        sourceData.clear();
        sourceData.addAll(list);
        filter("");
    }

    void filter(String query) {
        data.clear();
        for (DayEntity dayEntity : sourceData) {
            if (dayEntity.getDate().toLowerCase().contains(query.toLowerCase()))
                data.add(dayEntity);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_day, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView cardDateTV;
        private Chip chipCame, chipGone, chipWas;
        private ImageButton deleteBtn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDateTV = itemView.findViewById(R.id.card_date_textView);
            chipCame = itemView.findViewById(R.id.chipCame);
            chipGone = itemView.findViewById(R.id.chipGone);
            chipWas = itemView.findViewById(R.id.chipWas);
            deleteBtn = itemView.findViewById(R.id.card_delete);
        }

        void bind(DayEntity day) {
            String dateTitle = TimeUtils.getDateInNormalFormat(day.getDate()) + " " + TimeUtils.getDayOfTheWeek(day.getDate());
            cardDateTV.setText(dateTitle);
            this.itemView.setOnClickListener(v -> activity.finishActivityForResult(day.getDate()));
            chipCame.setText(day.getTimeCome());
            chipGone.setText(day.getTimeGone());
            chipWas.setText(day.getTimeWorked());
            chipGone.setOnClickListener(v -> {
                TimePickerDialog tpd = new TimePickerDialog(this.itemView.getContext(),
                        (view, hourOfDay, minuteOfDay) ->
                                mViewModel.setGoneTime(day, TimeUtils.getTime(hourOfDay, minuteOfDay)),
                        TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
                tpd.show();
            });
            chipCame.setOnClickListener(v -> {
                TimePickerDialog tpd = new TimePickerDialog(this.itemView.getContext(),
                        (view, hourOfDay, minuteOfDay) ->
                                mViewModel.setCameTime(day, TimeUtils.getTime(hourOfDay, minuteOfDay)),
                        TimeUtils.getCurrentHour(), TimeUtils.getCurrentMinutes(), true);
                tpd.show();
            });
            chipCame.setOnCloseIconClickListener(v -> mViewModel.setCameTime(day, null));
            chipGone.setOnCloseIconClickListener(v -> mViewModel.setGoneTime(day, null));
            deleteBtn.setOnClickListener(v -> mViewModel.deleteDay(day));
        }
    }
}

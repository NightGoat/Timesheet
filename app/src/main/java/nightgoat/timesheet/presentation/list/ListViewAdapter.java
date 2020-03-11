package nightgoat.timesheet.presentation.list;

import android.app.TimePickerDialog;
import android.content.Intent;
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
import nightgoat.timesheet.databinding.CardDayBinding;
import nightgoat.timesheet.presentation.ActivityForResultFinisher;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> {

    private List<DayEntity> data = new ArrayList<>();
    private List<DayEntity> sourceData = new ArrayList<>();
    private IListViewModel mViewModel;
    private ActivityForResultFinisher activity;

    ListViewAdapter(IListViewModel viewModel, ActivityForResultFinisher activity) {
        this.mViewModel = viewModel;
        this.activity = activity;
    }

    void changeList(List<DayEntity> list) {
        sourceData.clear();
        sourceData.addAll(list);
        filter("");
    }

    void filter(String query){
        data.clear();
        for (DayEntity dayEntity: sourceData) {
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
            String timeCame, timeGone, timeWas;
            String dateTitle = TimeUtils.getDateInNormalFormat(day.getDate()) + " " + TimeUtils.getDayOfTheWeek(day.getDate());
            cardDateTV.setText(dateTitle);
            this.itemView.setOnClickListener(v -> activity.finishActivityForResult(day.getDate()));
            if ((timeCame = day.getTimeCome()) != null) chipCame.setText(timeCame);
            if ((timeGone = day.getTimeGone()) != null) chipGone.setText(timeGone);
            if ((timeWas = day.getTimeWorked()) != null) chipWas.setText(timeWas);
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
            deleteBtn.setOnClickListener(v -> mViewModel.deleteDay(day));
        }
    }
}
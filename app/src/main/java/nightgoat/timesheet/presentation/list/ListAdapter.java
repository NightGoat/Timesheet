package nightgoat.timesheet.presentation.list;

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
import nightgoat.timesheet.database.DayEntity;
import nightgoat.timesheet.presentation.ActivityAdapterCallbacks;
import nightgoat.timesheet.utils.TimeType;
import nightgoat.timesheet.utils.TimeUtils;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private final List<DayEntity> data = new ArrayList<>();
    private final List<DayEntity> sourceData = new ArrayList<>();
    private final ActivityAdapterCallbacks activity;

    ListAdapter(ActivityAdapterCallbacks activity) {
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
            boolean isDateExist = TimeUtils.getDateInNormalFormat(dayEntity.getDate())
                    .toLowerCase()
                    .contains(query.toLowerCase());
            if (isDateExist) {
                data.add(dayEntity);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_day_linear, parent, false));
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

        private final TextView cardDateTV;
        private final TextView cardNote;
        private final Chip chipCame;
        private final Chip chipGone;
        private final Chip chipWas;
        private final ImageButton deleteBtn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDateTV = itemView.findViewById(R.id.card_date_textView);
            chipCame = itemView.findViewById(R.id.chipCame);
            chipGone = itemView.findViewById(R.id.chipGone);
            chipWas = itemView.findViewById(R.id.chipWas);
            deleteBtn = itemView.findViewById(R.id.card_delete);
            cardNote = itemView.findViewById(R.id.card_text_note);
        }

        void bind(DayEntity day) {
            String dateTitle = day.getDate().substring(8, 10) + " " + TimeUtils.getDayOfTheWeek(day.getDate());
            cardDateTV.setText(dateTitle);
            this.itemView.setOnClickListener(v -> activity.onClickFinish(day.getDate()));
            chipCame.setCloseIconVisible(day.getTimeCame() != null);
            chipCame.setText(day.getTimeCame());
            chipGone.setCloseIconVisible(day.getTimeGone() != null);
            chipGone.setText(day.getTimeGone());
            chipWas.setText(day.getTimeWorked());
            chipGone.setOnClickListener(v -> activity.onClickChip(day, TimeType.GONE));
            chipCame.setOnClickListener(v -> activity.onClickChip(day, TimeType.CAME));
            chipCame.setOnCloseIconClickListener(v -> activity.onClickCameChipClose(day));
            chipGone.setOnCloseIconClickListener(v -> activity.onClickGoneChipClose(day));
            deleteBtn.setOnClickListener(v -> activity.onClickDelete(day));
            if (day.getNote() != null && !day.getNote().isEmpty()) cardNote.setText(day.getNote());
            else cardNote.setVisibility(View.GONE);
        }
    }
}

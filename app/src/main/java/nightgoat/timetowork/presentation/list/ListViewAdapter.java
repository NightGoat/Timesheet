package nightgoat.timetowork.presentation.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import nightgoat.timetowork.R;
import nightgoat.timetowork.database.DayEntity;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> {

    private List<DayEntity> data = new ArrayList<>();
    private List<DayEntity> sourceData = new ArrayList<>();

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
        private TextView cardWorkedTimeTV;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardDateTV = itemView.findViewById(R.id.card_date_textView);
            cardWorkedTimeTV = itemView.findViewById(R.id.card_timeWorked_textView);
        }

        void bind(DayEntity day) {
            cardDateTV.setText(day.getDate());
            cardWorkedTimeTV.setText(String.format(itemView.getContext().getString(R.string.spentTimeToday) + " %s", day.getTimeWorked()));
        }
    }
}

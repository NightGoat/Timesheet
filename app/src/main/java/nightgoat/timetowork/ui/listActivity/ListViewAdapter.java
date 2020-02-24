package nightgoat.timetowork.ui.listActivity;

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

    ListViewAdapter(List<DayEntity> data) {
        if (data != null ) {
            this.data = data;
        }
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
        holder.cardDateTV.setText(data.get(position).getDate());
        holder.cardWorkedTimeTV.setText(String.format(holder.itemView.getContext().getString(R.string.spentTimeToday) + " %s", data.get(position).getTimeWorked()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView cardDateTV;
        TextView cardWorkedTimeTV;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDateTV = itemView.findViewById(R.id.card_date_textView);
            cardWorkedTimeTV = itemView.findViewById(R.id.card_timeWorked_textView);
        }
    }
}

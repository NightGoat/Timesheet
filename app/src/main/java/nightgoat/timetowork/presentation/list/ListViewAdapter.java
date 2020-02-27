package nightgoat.timetowork.presentation.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import nightgoat.timetowork.Injection;
import nightgoat.timetowork.R;
import nightgoat.timetowork.database.DayEntity;
import nightgoat.timetowork.presentation.ViewModelFactory;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> {

    private List<DayEntity> data = new ArrayList<>();
    private List<DayEntity> sourceData = new ArrayList<>();
    private ViewModelFactory mViewModelFactory;
    private ListViewModel mViewModel;

    ListViewAdapter(Context context) {
        mViewModelFactory = Injection.provideViewModelFactory(context);
        mViewModel = new ViewModelProvider((ViewModelStoreOwner) context, mViewModelFactory).get(ListViewModel.class);
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
        private TextView cardWorkedTimeTV;
        private ImageButton deleteBtn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDateTV = itemView.findViewById(R.id.card_date_textView);
            cardWorkedTimeTV = itemView.findViewById(R.id.card_timeWorked_textView);
            deleteBtn = itemView.findViewById(R.id.card_delete);
        }

        void bind(DayEntity day) {
            cardDateTV.setText(day.getDate());
            cardWorkedTimeTV.setText(String.format(itemView.getContext().getString(R.string.spentTimeToday) + " %s", day.getTimeWorked()));
            deleteBtn.setOnClickListener(v -> mViewModel.deleteDay(day));
        }
    }
}

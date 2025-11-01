package com.nebula.editor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class FormatAdapter extends RecyclerView.Adapter<FormatAdapter.FormatViewHolder> {

    private List<FormatItem> formatItems;
    private FormatSelectionListener listener;

    public FormatAdapter(List<FormatItem> formatItems, FormatSelectionListener listener) {
        this.formatItems = formatItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FormatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.format_item, parent, false);
        return new FormatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FormatViewHolder holder, int position) {
        FormatItem item = formatItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return formatItems.size();
    }

    static class FormatViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView tvAspect;
        private TextView tvName;

        public FormatViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.format_card);
            tvAspect = itemView.findViewById(R.id.tv_aspect);
            tvName = itemView.findViewById(R.id.tv_format_name);
        }

        public void bind(FormatItem item, FormatSelectionListener listener) {
            tvAspect.setText(item.getAspectRatio());
            tvName.setText(item.getName());
            
            // Update selection state
            cardView.setStrokeWidth(item.isSelected() ? 4 : 0);
            cardView.setStrokeColor(itemView.getContext()
                    .getResources().getColor(R.color.nebula_purple));
            
            itemView.setOnClickListener(v -> {
                listener.onFormatSelected(item);
            });
        }
    }

    public interface FormatSelectionListener {
        void onFormatSelected(FormatItem format);
    }
}

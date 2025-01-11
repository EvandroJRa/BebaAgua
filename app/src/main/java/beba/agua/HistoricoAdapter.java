package beba.agua;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.ViewHolder> {

    private List<HistoricoModel> historicoList;

    public HistoricoAdapter(List<HistoricoModel> historicoList) {
        this.historicoList = historicoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historico, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoricoModel item = historicoList.get(position);
        holder.textoData.setText(item.getData());
        holder.textoQuantidade.setText(String.format("%.0f ml", item.getQuantidade()));
        holder.textoMeta.setText(String.format("%.0f ml", item.getMetaDiaria()));
    }

    @Override
    public int getItemCount() {
        return historicoList.size();
    }

    public void adicionarRegistros(List<HistoricoModel> novosRegistros) {
        historicoList.addAll(novosRegistros);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textoData, textoQuantidade, textoMeta;

        public ViewHolder(View itemView) {
            super(itemView);
            textoData = itemView.findViewById(R.id.textoData);
            textoQuantidade = itemView.findViewById(R.id.textoQuantidade);
            textoMeta = itemView.findViewById(R.id.textoMeta);
        }
    }
}

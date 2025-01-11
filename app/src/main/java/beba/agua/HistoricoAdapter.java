package beba.agua;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.ViewHolder> {

    private Context context;
    private List<HistoricoModel> historicoList;

    public HistoricoAdapter(Context context, List<HistoricoModel> historicoList) {
        this.context = context;
        this.historicoList = historicoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_historico, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HistoricoModel item = historicoList.get(position);

        holder.textoData.setText(item.getData());
        holder.textoQuantidade.setText(String.format("%.0f ml", item.getQuantidade()));
        holder.textoMeta.setText(String.format("%.0f ml", item.getMetaDiaria()));

        int progresso = (int) ((item.getQuantidade() / item.getMetaDiaria()) * 100);
        holder.progressBar.setProgress(progresso);

        holder.iconeMetaAtingida.setVisibility(
                item.getQuantidade() >= item.getMetaDiaria() ? View.VISIBLE : View.GONE
        );
    }

    @Override
    public int getItemCount() {
        return historicoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textoData, textoQuantidade, textoMeta;
        ProgressBar progressBar;
        View iconeMetaAtingida;

        public ViewHolder(View itemView) {
            super(itemView);
            textoData = itemView.findViewById(R.id.textoData);
            textoQuantidade = itemView.findViewById(R.id.textoQuantidade);
            textoMeta = itemView.findViewById(R.id.textoMeta);
            progressBar = itemView.findViewById(R.id.progressoConsumo);
            iconeMetaAtingida = itemView.findViewById(R.id.iconeMetaAtingida);
        }
    }
}

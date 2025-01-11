package beba.agua;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder> {

    private List<HistoricoModel> historicoList;

    public HistoricoAdapter(List<HistoricoModel> historicoList) {
        this.historicoList = new ArrayList<>(historicoList); // Garante que não altera a lista original
    }

    @NonNull
    @Override
    public HistoricoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historico, parent, false);
        return new HistoricoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoricoViewHolder holder, int position) {
        HistoricoModel historico = historicoList.get(position);

        // Formata a data para exibição no card
        holder.textoData.setText(formatarData(historico.getData()));

        // Define os valores de quantidade e meta
        holder.textoQuantidade.setText(String.format("%.0f ml", historico.getQuantidade()));
        holder.textoMeta.setText(String.format("Meta: %.0f ml", historico.getMetaDiaria()));

        // Calcula e define o progresso no ProgressBar
        int progresso = (int) ((historico.getQuantidade() / historico.getMetaDiaria()) * 100);
        holder.progressoConsumo.setProgress(progresso);

        // Exibe ícone com base na meta atingida ou não
        if (historico.getQuantidade() >= historico.getMetaDiaria()) {
            holder.iconeMetaAtingida.setImageResource(R.drawable.ic_meta_atingida);
            holder.iconeMetaAtingida.setVisibility(View.VISIBLE);
        } else {
            holder.iconeMetaAtingida.setImageResource(R.drawable.ic_meta_nao_atingida);
            holder.iconeMetaAtingida.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return historicoList.size();
    }

    // **Novo método para adicionar registros sem recriar o Adapter**
    public void adicionarRegistros(List<HistoricoModel> novosRegistros) {
        int startPosition = historicoList.size();
        historicoList.addAll(novosRegistros);
        notifyItemRangeInserted(startPosition, novosRegistros.size());
    }

    // **Formata a data para o padrão "12 de Janeiro de 2025"**
    private String formatarData(String data) {
        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat formatoSaida = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
            Date date = formatoEntrada.parse(data);
            return formatoSaida.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return data; // Retorna a data original caso ocorra erro
        }
    }

    public static class HistoricoViewHolder extends RecyclerView.ViewHolder {
        TextView textoData, textoQuantidade, textoMeta;
        ProgressBar progressoConsumo;
        ImageView iconeMetaAtingida;  // ⬅️ Certifique-se de que é um ImageView

        public HistoricoViewHolder(View itemView) {
            super(itemView);
            textoData = itemView.findViewById(R.id.textoData);
            textoQuantidade = itemView.findViewById(R.id.textoQuantidade);
            textoMeta = itemView.findViewById(R.id.textoMeta);
            progressoConsumo = itemView.findViewById(R.id.progressoConsumo);
            iconeMetaAtingida = itemView.findViewById(R.id.iconeMetaAtingida); // ⬅️ Certifique-se de que este ID existe no XML
        }
    }
}

package beba.agua;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistoricoActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistorico;
    private HistoricoAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<HistoricoModel> historicoList;
    private static final String TAG = "HistoricoActivity";

    private boolean isLoading = false;
    private int offset = 0;
    private static final int LIMITE_PAGINA = 10; // 🔹 Carregar 10 registros por vez

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        recyclerViewHistorico = findViewById(R.id.recyclerViewHistorico);
        dbHelper = new DatabaseHelper(this);

        historicoList = new ArrayList<>();
        adapter = new HistoricoAdapter(historicoList);
        recyclerViewHistorico.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistorico.setAdapter(adapter);

        carregarMaisHistorico(); // 🔹 Carrega os primeiros registros
        dbHelper.inserirDadosFicticios(); //dados para teste

        recyclerViewHistorico.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == historicoList.size() - 1) {
                    carregarMaisHistorico();
                }
            }
        });
    }

    private void carregarMaisHistorico() {
        isLoading = true;

        List<HistoricoModel> novosRegistros = dbHelper.obterHistoricoPaginado(offset, LIMITE_PAGINA);
        if (!novosRegistros.isEmpty()) {
            historicoList.addAll(novosRegistros);
            adapter.adicionarRegistros(novosRegistros);
            offset += novosRegistros.size();
        } else {
            Log.d(TAG, "🚫 Nenhum novo registro encontrado.");
        }

        isLoading = false;
    }
}

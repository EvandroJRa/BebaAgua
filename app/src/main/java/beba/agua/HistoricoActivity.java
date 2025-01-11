package beba.agua;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;


public class HistoricoActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistorico;
    private HistoricoAdapter historicoAdapter;
    private DatabaseHelper dbHelper;
    private static final String TAG = "HistoricoActivity";

    private int offset = 0;
    private static final int LIMITE_PAGINACAO = 50; // Número de registros por página

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        // Define Status Bar preta para esta tela
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        recyclerViewHistorico = findViewById(R.id.recyclerViewHistorico);
        recyclerViewHistorico.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new DatabaseHelper(this);

        // inserede dados para  testar
//        if (dbHelper.isBancoVazio()) { //Insere dados apenas se o banco estiver vazio
//            dbHelper.inserirDadosFicticios();
//        }

        carregarHistorico();
    }

    private void carregarHistorico() {
        List<HistoricoModel> historicoList = dbHelper.obterHistoricoPaginado(offset, LIMITE_PAGINACAO);

        if (!historicoList.isEmpty()) {
            if (historicoAdapter == null) {
                historicoAdapter = new HistoricoAdapter(historicoList);
                recyclerViewHistorico.setAdapter(historicoAdapter);
            } else {
                historicoAdapter.adicionarRegistros(historicoList); // Método para adicionar mais registros ao adapter
            }
            Log.d(TAG, "✅ Histórico carregado com " + historicoList.size() + " registros.");
        } else {
            Log.d(TAG, "⚠️ Nenhum histórico encontrado.");
        }
    }
}




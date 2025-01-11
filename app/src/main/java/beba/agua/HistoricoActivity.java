package beba.agua;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class HistoricoActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistorico;
    private TextView textoSemHistorico;
    private DatabaseHelper dbHelper;
    private static final String TAG = "HistoricoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        recyclerViewHistorico = findViewById(R.id.recyclerViewHistorico);
        textoSemHistorico = findViewById(R.id.textoSemHistorico);
        dbHelper = new DatabaseHelper(this);

        recyclerViewHistorico.setLayoutManager(new LinearLayoutManager(this));

        // Verifica se o banco de dados está vazio e, se estiver, insere dados ficticios
        if (dbHelper.isBancoVazio()) {
            dbHelper.inserirDadosFicticios();
        }

        carregarHistorico();
    }

    private void carregarHistorico() {
        Cursor cursor = dbHelper.obterHistorico();

        if (cursor != null && cursor.getCount() > 0) {
            List<HistoricoModel> historicoList = new ArrayList<>();

            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndexOrThrow("data"));
                double quantidade = cursor.getDouble(cursor.getColumnIndexOrThrow("quantidade"));
                double metaDiaria = cursor.getDouble(cursor.getColumnIndexOrThrow("metaDiaria"));
                historicoList.add(new HistoricoModel(data, quantidade, metaDiaria));
            }
            cursor.close();

            HistoricoAdapter adapter = new HistoricoAdapter(this, historicoList);
            recyclerViewHistorico.setAdapter(adapter);
            textoSemHistorico.setVisibility(TextView.GONE);
            Log.d(TAG, "✅ Histórico carregado com " + historicoList.size() + " registros.");
        } else {
            textoSemHistorico.setVisibility(TextView.VISIBLE);
            recyclerViewHistorico.setAdapter(null);
            Log.d(TAG, "⚠️ Nenhum histórico encontrado.");
        }
    }
}



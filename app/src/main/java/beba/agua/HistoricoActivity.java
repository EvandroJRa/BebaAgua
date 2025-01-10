package beba.agua;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HistoricoActivity extends AppCompatActivity {

    private ListView listViewHistorico;
    private TextView textoSemHistorico;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        listViewHistorico = findViewById(R.id.listViewHistorico);
        textoSemHistorico = findViewById(R.id.textoSemHistorico);
        dbHelper = new DatabaseHelper(this);

        carregarHistorico(); // 🔄 Chama o método para exibir os dados
    }

    private void carregarHistorico() {
        Cursor cursor = dbHelper.obterHistorico();

        if (cursor != null && cursor.getCount() > 0) {
            String[] from = new String[]{"data", "quantidade", "metaDiaria"};
            int[] to = new int[]{R.id.textoData, R.id.textoQuantidade, R.id.textoMeta};

            // 🔹 Adapter atualizado para evitar erro da coluna '_id'
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this, R.layout.item_historico, cursor, from, to, 0
            );

            listViewHistorico.setAdapter(adapter);
            textoSemHistorico.setVisibility(TextView.GONE);
            Log.d("HistoricoActivity", "✅ Histórico carregado com " + cursor.getCount() + " registros.");
        } else {
            textoSemHistorico.setVisibility(TextView.VISIBLE);
            listViewHistorico.setAdapter(null);
            Log.d("HistoricoActivity", "⚠️ Nenhum histórico encontrado.");
        }
    }
}

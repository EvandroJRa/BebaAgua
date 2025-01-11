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
    private static final String TAG = "HistoricoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        listViewHistorico = findViewById(R.id.listViewHistorico);
        textoSemHistorico = findViewById(R.id.textoSemHistorico);
        dbHelper = new DatabaseHelper(this);

        carregarHistorico();
    }

    private void carregarHistorico() {
        Cursor cursor = dbHelper.obterHistorico();

        if (cursor != null && cursor.getCount() > 0) {
            String[] from = new String[]{"data", "quantidade", "metaDiaria"};
            int[] to = new int[]{R.id.textoData, R.id.textoQuantidade, R.id.textoMeta};

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this, R.layout.item_historico, cursor, from, to, 0
            ) {
                @Override
                public void setViewText(TextView v, String text) {
                    try {
                        // 🎯 Obtém o índice da coluna diretamente pelo nome
                        if (v.getId() == R.id.textoQuantidade) {
                            double quantidade = cursor.getDouble(cursor.getColumnIndexOrThrow("quantidade"));
                            text = String.format("%.0f ml", quantidade);
                        } else if (v.getId() == R.id.textoMeta) {
                            double metaDiaria = cursor.getDouble(cursor.getColumnIndexOrThrow("metaDiaria"));
                            text = String.format("%.0f ml", metaDiaria);
                        }

                        super.setViewText(v, text);
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Erro ao formatar valor: " + e.getMessage());
                        v.setText("0 ml");
                    }
                }
            };

            listViewHistorico.setAdapter(adapter);
            textoSemHistorico.setVisibility(TextView.GONE);
            Log.d(TAG, "✅ Histórico carregado com " + cursor.getCount() + " registros.");
        } else {
            textoSemHistorico.setVisibility(TextView.VISIBLE);
            listViewHistorico.setAdapter(null);
            Log.d(TAG, "⚠️ Nenhum histórico encontrado.");
        }
    }
}

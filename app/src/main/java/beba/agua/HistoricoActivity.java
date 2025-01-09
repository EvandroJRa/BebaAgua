package beba.agua;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HistoricoActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView listViewHistorico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        dbHelper = new DatabaseHelper(this);
        listViewHistorico = findViewById(R.id.listViewHistorico);

        carregarHistorico();
    }

    private void carregarHistorico() {
        Cursor cursor = dbHelper.obterHistorico();

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "Nenhum histórico encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] from = {"data", "quantidade", "metaDiaria"};
        int[] to = {R.id.textoData, R.id.textoQuantidade, R.id.textoMeta};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this, R.layout.item_historico, cursor, from, to, 0
        );

        listViewHistorico.setAdapter(adapter);
    }
}

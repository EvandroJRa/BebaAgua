package beba.agua;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.content.pm.ActivityInfo;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
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
    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);
        // Trava a orientação da tela em retrato (vertical)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Adiciona SwipeGestureListener para navegar entre telas
        gestureDetector = new GestureDetectorCompat(this, new SwipeGestureListener(this, null, LembretesActivity.class));

        View layout = findViewById(android.R.id.content);
        layout.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

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

    // Mudança de tela com swip <---- ---->
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector != null && gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
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




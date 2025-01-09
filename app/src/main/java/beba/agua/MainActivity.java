package beba.agua;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editTextMeta;
    private Button botaoSalvarMeta, botao100ml, botao150ml, botao250ml, botao500ml, botao600ml, botao750ml, botaoHistorico;
    private TextView textoStatus;
    private ProgressBar barraProgresso;

    private double consumoAtual = 0.0;
    private double metaDiaria = 0.0;

    private DatabaseHelper dbHelper;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String META_DIARIA_KEY = "metaDiaria";
    private static final String CONSUMO_ATUAL_KEY = "consumoAtual";
    private static final int REQUEST_CODE_NOTIFICACAO = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        inicializarComponentes();
        configurarListeners();
        carregarMetaDiaria();
        atualizarInterface();

        solicitarPermissaoNotificacoes(); // 🔔 Garantindo que a permissão seja solicitada corretamente

        Button botaoAbrirLembretes;
        botaoAbrirLembretes = findViewById(R.id.botaoAbrirLembretes);
        botaoAbrirLembretes.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LembretesActivity.class);
            startActivity(intent);
        });


    }

    // 🔔 **Solicita permissão para notificações no Android 13+**
    private void solicitarPermissaoNotificacoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICACAO);
            }
        }
    }

    private void inicializarComponentes() {
        editTextMeta = findViewById(R.id.editTextNumber);
        botaoSalvarMeta = findViewById(R.id.botaoSalvarMeta);
        textoStatus = findViewById(R.id.textoStatus);
        barraProgresso = findViewById(R.id.barraProgresso);
        botaoHistorico = findViewById(R.id.botaoHistorico);

        botao100ml = findViewById(R.id.botao100ml);
        botao150ml = findViewById(R.id.botao150ml);
        botao250ml = findViewById(R.id.botao250ml);
        botao500ml = findViewById(R.id.botao500ml);
        botao600ml = findViewById(R.id.botao600ml);
        botao750ml = findViewById(R.id.botao750ml);

        botaoHistorico.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, HistoricoActivity.class);
            startActivity(intent);
        });
    }

    private void configurarListeners() {
        botaoSalvarMeta.setOnClickListener(view -> salvarMetaDiaria());

        botao100ml.setOnClickListener(view -> adicionarConsumo(100));
        botao150ml.setOnClickListener(view -> adicionarConsumo(150));
        botao250ml.setOnClickListener(view -> adicionarConsumo(250));
        botao500ml.setOnClickListener(view -> adicionarConsumo(500));
        botao600ml.setOnClickListener(view -> adicionarConsumo(600));
        botao750ml.setOnClickListener(view -> adicionarConsumo(750));
    }

    private void salvarMetaDiaria() {
        String metaString = editTextMeta.getText().toString();
        if (metaString.isEmpty()) {
            Toast.makeText(this, "Insira uma meta diária", Toast.LENGTH_SHORT).show();
            return;
        }

        metaDiaria = Double.parseDouble(metaString);
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putFloat(META_DIARIA_KEY, (float) metaDiaria)
                .apply();

        botaoSalvarMeta.setEnabled(false);
        atualizarInterface();
    }

    private void carregarMetaDiaria() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        metaDiaria = prefs.getFloat(META_DIARIA_KEY, 2000.0f);
        consumoAtual = prefs.getFloat(CONSUMO_ATUAL_KEY, 0.0f);
    }

    private void adicionarConsumo(double quantidade) {
        String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 🔄 Atualiza no banco de dados
        dbHelper.registrarConsumo(dataAtual, quantidade, metaDiaria);

        // 🔄 Atualiza a interface
        consumoAtual += quantidade;
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putFloat(CONSUMO_ATUAL_KEY, (float) consumoAtual);
        editor.apply();

        atualizarInterface();
    }

    private void atualizarInterface() {
        textoStatus.setText(String.format(Locale.getDefault(), "%.0f ml / %.0f ml", consumoAtual, metaDiaria));

        if (metaDiaria > 0) {
            int progresso = (int) ((consumoAtual / metaDiaria) * 100);
            barraProgresso.setProgress(progresso);
        } else {
            barraProgresso.setProgress(0);
        }
    }
}

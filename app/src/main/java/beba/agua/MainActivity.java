package beba.agua;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
    private Button botaoSalvarMeta, botao100ml, botao150ml, botao250ml, botao500ml, botao600ml, botao750ml, botaoHistorico, botaoAbrirLembretes;
    private TextView textoStatus;
    private ProgressBar barraProgresso;
    private double consumoAtual = 0.0;
    private double metaDiaria = 0.0;
    private DatabaseHelper dbHelper;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String META_DIARIA_KEY = "metaDiaria";
    private static final String CONSUMO_ATUAL_KEY = "consumoAtual";
    private static final String ULTIMA_DATA_KEY = "ultimaData";
    private static final int REQUEST_CODE_NOTIFICACAO = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DatabaseHelper(this);

        inicializarComponentes();
        configurarListeners();
        carregarMetaDiaria();

        if (isNovoDia()) {
            resetarConsumoDiario();
        }

        atualizarInterface();
        solicitarPermissaoNotificacoes();
    }

    // 🔹 **Solicita permissão para notificações no Android 13+**
    private void solicitarPermissaoNotificacoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICACAO);
                Log.d("MainActivity", "🔔 Solicitando permissão de notificação.");
            } else {
                Log.d("MainActivity", "✅ Permissão de notificação já concedida.");
            }
        }
    }

    // 🔹 **Inicializa componentes da UI**
    private void inicializarComponentes() {
        editTextMeta = findViewById(R.id.editTextNumber);
        botaoSalvarMeta = findViewById(R.id.botaoSalvarMeta);
        textoStatus = findViewById(R.id.textoStatus);
        barraProgresso = findViewById(R.id.barraProgresso);
        botaoHistorico = findViewById(R.id.botaoHistorico);
        botaoAbrirLembretes = findViewById(R.id.botaoAbrirLembretes);

        botao100ml = findViewById(R.id.botao100ml);
        botao150ml = findViewById(R.id.botao150ml);
        botao250ml = findViewById(R.id.botao250ml);
        botao500ml = findViewById(R.id.botao500ml);
        botao600ml = findViewById(R.id.botao600ml);
        botao750ml = findViewById(R.id.botao750ml);

        botaoSalvarMeta.setEnabled(false);

        botaoHistorico.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, HistoricoActivity.class));
        });

        botaoAbrirLembretes.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, LembretesActivity.class));
        });
    }

    // 🔹 **Configura eventos de clique**
    private void configurarListeners() {
        botaoSalvarMeta.setOnClickListener(view -> salvarMetaDiaria());

        botao100ml.setOnClickListener(view -> adicionarConsumo(100));
        botao150ml.setOnClickListener(view -> adicionarConsumo(150));
        botao250ml.setOnClickListener(view -> adicionarConsumo(250));
        botao500ml.setOnClickListener(view -> adicionarConsumo(500));
        botao600ml.setOnClickListener(view -> adicionarConsumo(600));
        botao750ml.setOnClickListener(view -> adicionarConsumo(750));
    }

    // 🔹 **Salva a meta diária no SharedPreferences**
    private void salvarMetaDiaria() {
        String metaString = editTextMeta.getText().toString();
        if (metaString.isEmpty()) {
            Toast.makeText(this, "Insira uma meta diária", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            metaDiaria = Double.parseDouble(metaString);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putFloat(META_DIARIA_KEY, (float) metaDiaria)
                    .apply();

            botaoSalvarMeta.setEnabled(false);
            atualizarInterface();
            Log.d("MainActivity", "📌 Nova meta salva: " + metaDiaria + "ml");
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido para a meta", Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "⚠ Erro ao salvar meta", e);
        }
    }

    // 🔹 **Carrega a meta e consumo atual do SharedPreferences**
    private void carregarMetaDiaria() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        metaDiaria = prefs.getFloat(META_DIARIA_KEY, 2000.0f);
        consumoAtual = prefs.getFloat(CONSUMO_ATUAL_KEY, 0.0f);
    }

    // 🔹 **Adiciona o consumo de água**
    private void adicionarConsumo(double quantidade) {
        String dataAtual = obterDataAtual();
        dbHelper.registrarConsumo(dataAtual, consumoAtual + quantidade, metaDiaria);

        consumoAtual += quantidade;
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putFloat(CONSUMO_ATUAL_KEY, (float) consumoAtual)
                .apply();

        atualizarInterface();

        if (consumoAtual >= metaDiaria) {
            Toast.makeText(this, "Parabéns, Meta concluída!", Toast.LENGTH_LONG).show();
            desativarBotoesConsumo();
        }
    }

    // 🔹 **Atualiza a interface**
    private void atualizarInterface() {
        textoStatus.setText(String.format(Locale.getDefault(), "%.0f ml / %.0f ml", consumoAtual, metaDiaria));
        int progresso = (int) ((consumoAtual / metaDiaria) * 100);
        barraProgresso.setProgress(metaDiaria > 0 ? progresso : 0);
    }

    // 🔹 **Verifica se é um novo dia**
    private boolean isNovoDia() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String ultimaData = prefs.getString(ULTIMA_DATA_KEY, "");
        String dataAtual = obterDataAtual();

        if (!ultimaData.equals(dataAtual)) {
            prefs.edit().putString(ULTIMA_DATA_KEY, dataAtual).apply();
            return true;
        }
        return false;
    }

    // 🔹 **Reseta o consumo ao iniciar um novo dia**
    private void resetarConsumoDiario() {
        consumoAtual = 0;
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putFloat(CONSUMO_ATUAL_KEY, 0)
                .apply();
        atualizarInterface();
        botaoSalvarMeta.setEnabled(true);
        Log.d("MainActivity", "🆕 Novo dia! Consumo resetado.");
    }

    private String obterDataAtual() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void desativarBotoesConsumo() {
        botao100ml.setEnabled(false);
        botao150ml.setEnabled(false);
        botao250ml.setEnabled(false);
        botao500ml.setEnabled(false);
        botao600ml.setEnabled(false);
        botao750ml.setEnabled(false);
        botaoSalvarMeta.setEnabled(false);
    }
}

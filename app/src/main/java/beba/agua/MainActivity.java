package beba.agua;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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
    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Log.d("MainActivity", "🟢 App iniciado, verificando status do dia...");

        inicializarComponentes();
        configurarListeners();
        carregarMetaDiaria();
        atualizarInterface();
        solicitarPermissaoNotificacoes();
        verificarESolicitarPermissaoAlarme();

        // Adiciona SwipeGestureListener para navegar entre telas
        gestureDetector = new GestureDetectorCompat(this, new SwipeGestureListener(this, LembretesActivity.class, null));

        View layout = findViewById(android.R.id.content);
        layout.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Define Status Bar azul para esta tela
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }

        dbHelper = new DatabaseHelper(this);

        if (isNovoDia()) {
            Log.d("MainActivity", "🌅 Novo dia detectado! Resetando metas e reagendando lembretes...");
            resetarConsumoDiario();  // Reseta a meta diária
            LembretesActivity.reagendarLembretes(this); // 🔥 Reagenda os lembretes
        }

        if (isNovoDia()) {
            Log.d("MainActivity", "🌅 Novo dia detectado! Resetando metas e reagendando lembretes...");
            resetarConsumoDiario();
            LembretesActivity.reagendarLembretes(this);
        }
        verificarMetaDiaria();
        atualizarInterface();
    }

    // Mudança de tela com swip <---- ---->
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector != null && gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
    //Obter data atual
    private String obterDataAtual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    //**Solicita permissão para notificações no Android 13+**
    private void solicitarPermissaoNotificacoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                Log.d("MainActivity", "🔔 Solicitando permissão de notificação.");
            } else {
                Log.d("MainActivity", "✅ Permissão de notificação já concedida.");
            }
        }
    }
    //Verificar permissão
    private void verificarESolicitarPermissaoAlarme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
            SharedPreferences prefs = getSharedPreferences("ConfigApp", MODE_PRIVATE);
            boolean jaSolicitou = prefs.getBoolean("PERMISSAO_ALARME_SOLICITADA", false);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms() && !jaSolicitou) {
                Log.w("MainActivity", "⚠️ Permissão de alarme exato ainda não concedida! Solicitando...");

                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);

                // Salva no SharedPreferences que a permissão já foi solicitada
                prefs.edit().putBoolean("PERMISSAO_ALARME_SOLICITADA", true).apply();
            } else {
                Log.d("MainActivity", "✅ Permissão de alarme exato já concedida ou já foi solicitada antes.");
            }
        }
    }

    //**Inicializa componentes da UI**
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

    //**Configura eventos de clique**
    private void configurarListeners() {
        botaoSalvarMeta.setOnClickListener(view -> salvarMetaDiaria());

        botao100ml.setOnClickListener(view -> adicionarConsumo(100));
        botao150ml.setOnClickListener(view -> adicionarConsumo(150));
        botao250ml.setOnClickListener(view -> adicionarConsumo(250));
        botao500ml.setOnClickListener(view -> adicionarConsumo(500));
        botao600ml.setOnClickListener(view -> adicionarConsumo(600));
        botao750ml.setOnClickListener(view -> adicionarConsumo(750));
    }

    //Salva a meta diária no SharedPreferences
    private void salvarMetaDiaria() {
        String metaString = editTextMeta.getText().toString().trim();
        if (metaString.isEmpty()) {
            Toast.makeText(this, "Insira uma meta diária ex: 2000ml = 2L", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            metaDiaria = Double.parseDouble(metaString);

            // 🔥 Salvar a meta e a data no SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putFloat(META_DIARIA_KEY, (float) metaDiaria);
            editor.putString(ULTIMA_DATA_KEY, obterDataAtual()); // Salva a data da meta definida
            editor.apply();

            // 🔥 Atualizar no Banco de Dados
            String dataAtual = obterDataAtual();
            dbHelper.atualizarMetaDiaria(dataAtual, metaDiaria);

            // 🔒 Desativar o campo e o botão após salvar
            editTextMeta.setEnabled(false);
            botaoSalvarMeta.setEnabled(false);
            Toast.makeText(this, "Meta definida com sucesso!", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "📌 Nova meta definida: " + metaDiaria + "ml. Campo e botão desativados.");
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido para a meta", Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "⚠ Erro ao salvar meta", e);
        }
    }


    // **Carrega a meta e consumo atual do SharedPreferences**
    private void carregarMetaDiaria() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        metaDiaria = prefs.getFloat(META_DIARIA_KEY, 2000.0f);
        consumoAtual = prefs.getFloat(CONSUMO_ATUAL_KEY, 0.0f);
    }

    // **Adiciona o consumo de água**
    private void adicionarConsumo(double quantidade) {
        String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Atualiza no banco de dados
        dbHelper.registrarConsumo(dataAtual, quantidade, metaDiaria);

        // Atualiza a interface
        consumoAtual += quantidade;
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putFloat(CONSUMO_ATUAL_KEY, (float) consumoAtual);

        if (consumoAtual >= metaDiaria) {
            Toast.makeText(this, "🎉 Parabéns! Meta concluída!", Toast.LENGTH_LONG).show();
            desativarBotoesConsumo(); // 🔥 Desativa os botões de consumo
            editor.putBoolean("META_CONCLUIDA_HOJE", true); // Salva que a meta foi concluída hoje
            editor.putBoolean("META_CONCLUIDA_ONTEM", true); // Para ser usada no próximo dia
            Log.d(TAG, "🎯 Meta concluída! Lembretes desativados.");
        }

        editor.apply();
        atualizarInterface();
    }

    // Atualiza a interface
    private void atualizarInterface() {
        textoStatus.setText(String.format(Locale.getDefault(), "%.0f ml / %.0f ml", consumoAtual, metaDiaria));
        int progresso = (int) ((consumoAtual / metaDiaria) * 100);
        barraProgresso.setProgress(metaDiaria > 0 ? progresso : 0);
    }
    //Verificar meta diária
    private void verificarMetaDiaria() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String ultimaDataSalva = prefs.getString(ULTIMA_DATA_KEY, ""); // Data da última meta salva
        String dataAtual = obterDataAtual(); // Data do dia atual
        float metaSalva = prefs.getFloat(META_DIARIA_KEY, 0.0f); // Recupera a meta salva

        Log.d("MainActivity", "🔍 Verificando data: Última - " + ultimaDataSalva + " | Atual - " + dataAtual);

        // 🚀 Se for um novo dia, redefinir a meta e ativar o campo e botão
        if (!dataAtual.equals(ultimaDataSalva)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("META_CONCLUIDA_HOJE", false);
            editor.putString(ULTIMA_DATA_KEY, dataAtual);
            editor.putFloat(META_DIARIA_KEY, 0.0f); // Resetar a meta diária
            editor.apply();

            // 🔥 Libera o campo e o botão para nova edição
            editTextMeta.setEnabled(true);
            botaoSalvarMeta.setEnabled(true);
            editTextMeta.setText(""); // Limpa o campo para nova inserção

            Log.d("MainActivity", "🟢 Novo dia detectado! Campo de meta e botão ativados.");
        } else {
            // 🚨 Se já houver uma meta definida, desativa o campo e o botão
            if (metaSalva > 0) {
                editTextMeta.setEnabled(false);
                botaoSalvarMeta.setEnabled(false);
                editTextMeta.setText(String.valueOf(metaSalva)); // Exibe a meta salva
                Log.d("MainActivity", "🔒 Meta já definida hoje: " + metaSalva + "ml. Campo e botão desativados.");
            } else {
                editTextMeta.setEnabled(true);
                botaoSalvarMeta.setEnabled(true);
                Log.d("MainActivity", "🟢 Nenhuma meta definida hoje. Campo e botão ativados.");
            }
        }
    }


    //Verifica se é um novo dia
    private boolean isNovoDia() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String ultimaData = prefs.getString(ULTIMA_DATA_KEY, "");
        String dataAtual = obterDataAtual();

        if (!dataAtual.equals(ultimaData)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(ULTIMA_DATA_KEY, dataAtual);
            editor.putBoolean("META_CONCLUIDA_HOJE", false);
            editor.putFloat(META_DIARIA_KEY, 0.0f); // Resetar a meta
            editor.apply();

            Log.d("MainActivity", "🔄 Novo dia detectado! Resetando dados do usuário.");
            return true;
        }
        return false;
    }


    // **Reseta o consumo ao iniciar um novo dia**
    private void resetarConsumoDiario() {
        consumoAtual = 0;
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putFloat(CONSUMO_ATUAL_KEY, 0)
                .apply();
        atualizarInterface();
        ativarBotoesConsumo(); // 🔥 Reativa os botões
        Log.d("MainActivity", "🌅 Novo dia! Consumo resetado e botões ativados.");
    }

    private void definirEstadoBotoesConsumo(boolean ativar) {
        botao100ml.setEnabled(ativar);
        botao150ml.setEnabled(ativar);
        botao250ml.setEnabled(ativar);
        botao500ml.setEnabled(ativar);
        botao600ml.setEnabled(ativar);
        botao750ml.setEnabled(ativar);
        botaoSalvarMeta.setEnabled(ativar);

        Log.d("MainActivity", ativar ? "✅ Botões ativados." : "🚫 Botões desativados.");
    }
    // Método para desativar botões
    private void desativarBotoesConsumo() {
        definirEstadoBotoesConsumo(false);
    }
    // Método para ativar botões
    private void ativarBotoesConsumo() {
        definirEstadoBotoesConsumo(true);
    }
}

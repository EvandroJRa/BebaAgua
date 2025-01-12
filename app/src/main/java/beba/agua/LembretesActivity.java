package beba.agua;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LembretesActivity extends AppCompatActivity implements DatabaseHelper.LembretesListener {

    private static final String TAG = "LembretesActivity";

    private TimePicker timePicker;
    private RadioGroup radioGroupFrequencia;
    private EditText editTextMensagem;
    private Switch switchLembrete;
    private Button botaoSalvarLembretes;
    private RadioButton radioButton30Min, radioButton1Hora, radioButton2Horas;
    private static final String PREFS_NAME = "LembreteConfig";
    private static final String KEY_MENSAGEM = "mensagemLembrete";
    private static final String KEY_HORA = "horaLembrete";
    private static final String KEY_MINUTO = "minutoLembrete";
    private static final String KEY_NOTIFICACAO = "notificacaoAtivada";
    private static final String KEY_INTERVALO = "lembrete_intervalo";
    private static final String KEY_META_CONCLUIDA = "META_CONCLUIDA";
    private static final String KEY_ULTIMA_DATA_META = "ULTIMA_DATA_META";
    private DatabaseHelper dbHelper;
    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lembretes);

        //gestureDetector = new GestureDetectorCompat(this, new GestureListener(this));

        Log.d(TAG, "🟢 Tela de lembretes carregada com sucesso.");

        // 🔥 Verifica e solicita permissão de alarmes exatos diretamente
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "⚠️ Permissão de alarmes exatos necessária.");
                solicitarPermissaoAlarmesExatos(this); // 🔥 Método chamado corretamente
            }
        }
        // Inicializa o dbHelper
        dbHelper = new DatabaseHelper(this);

        // Garante que um novo dia seja iniciado corretamente
        if (dbHelper != null) {
            dbHelper.iniciarNovoDia();
            dbHelper.setLembretesListener(this);
        } else {
            Log.e(TAG, "❌ ERRO: dbHelper está NULL ao tentar iniciar novo dia!");
        }

        // Configuração da Status Bar preta para esta tela
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        // Inicializa os componentes e configurações
        inicializarComponentes();
        carregarConfiguracoes();
        configurarListeners();
        verificarMetaEAtualizarBotao();
        resetarEstadoMetaSeNovoDia();
        solicitarPermissaoAlarme();
        verificarPermissaoAlarme();

        Log.d(TAG, "🟢 Tela de lembretes carregada com sucesso.");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }


    private void inicializarComponentes() {
        timePicker = findViewById(R.id.timePicker);
        radioGroupFrequencia = findViewById(R.id.radioGroupFrequencia);
        editTextMensagem = findViewById(R.id.editTextMensagem);
        switchLembrete = findViewById(R.id.switch1lembete);
        botaoSalvarLembretes = findViewById(R.id.botaoSalvarLembretes);
        radioButton30Min = findViewById(R.id.radioButton30Min);
        radioButton1Hora = findViewById(R.id.radioButton1Hora);
        radioButton2Horas = findViewById(R.id.radioButton2Horas);
        timePicker.setIs24HourView(true);
    }

    private void carregarConfiguracoes() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editTextMensagem.setText(prefs.getString(KEY_MENSAGEM, "Hora de beber água!"));
        switchLembrete.setChecked(prefs.getBoolean(KEY_NOTIFICACAO, true));

        int intervaloMinutos = prefs.getInt(KEY_INTERVALO, -1);
        if (intervaloMinutos == 30) radioButton30Min.setChecked(true);
        else if (intervaloMinutos == 60) radioButton1Hora.setChecked(true);
        else if (intervaloMinutos == 120) radioButton2Horas.setChecked(true);

        int hora = prefs.getInt(KEY_HORA, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        int minuto = prefs.getInt(KEY_MINUTO, Calendar.getInstance().get(Calendar.MINUTE));
        timePicker.setHour(hora);
        timePicker.setMinute(minuto);

        Log.d(TAG, "🔄 Configurações carregadas.");
    }

    private void configurarListeners() {
        botaoSalvarLembretes.setOnClickListener(view -> {
            Log.d(TAG, "💾 Botão SALVAR pressionado!");
            salvarConfiguracoes();

            if (switchLembrete.isChecked()) {
                agendarLembretes();
                Toast.makeText(this, "✅ Lembretes ativados com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                cancelarNotificacoes();
                Toast.makeText(this, "🚫 Lembretes desativados!", Toast.LENGTH_SHORT).show();
            }
        });

        radioGroupFrequencia.setOnCheckedChangeListener((group, checkedId) -> salvarConfiguracoes());
    }

    private void solicitarPermissaoAlarme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "Ative a permissão de Alarmes.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void verificarPermissaoAlarme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "❌ Permissão de alarme exato NÃO concedida!");
            }
        }
    }
    public void solicitarPermissaoAlarmesExatos(Context context) {
        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 🔥 ADICIONA A FLAG NECESSÁRIA
        context.startActivity(intent); // ✅ AGORA FUNCIONA SEM CRASHAR
        Log.w(TAG, "⚠️ Solicitando permissão para agendar alarmes exatos.");
    }

    private void agendarLembretes() {
        salvarConfiguracoes();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int intervaloMinutos = prefs.getInt(KEY_INTERVALO, 30);
        long intervaloMillis = TimeUnit.MINUTES.toMillis(intervaloMinutos);

        int hora = timePicker.getHour();
        int minuto = timePicker.getMinute();
        String mensagem = editTextMensagem.getText().toString();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.MINUTE, 1);
        }

        Intent intent = new Intent(this, LembreteReceiver.class);
        intent.putExtra("mensagem", mensagem);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervaloMillis, pendingIntent);

        Log.d(TAG, "✅ Lembrete AGENDADO para " + hora + ":" + minuto + " e será repetido a cada " + intervaloMinutos + " minutos.");
    }

    //Método para reagendar lembretes após reinício do dispositivo
    public static void reagendarLembretes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean notificacaoAtivada = prefs.getBoolean(KEY_NOTIFICACAO, true);

        if (!notificacaoAtivada) {
            Log.d(TAG, "🔕 Lembretes desativados, nada será reativado.");
            return;
        }

        String mensagem = prefs.getString(KEY_MENSAGEM, "Hora de beber água!");
        int hora = prefs.getInt(KEY_HORA, 8);
        int minuto = prefs.getInt(KEY_MINUTO, 0);
        int intervaloMinutos = prefs.getInt(KEY_INTERVALO, 30);
        long intervaloMillis = TimeUnit.MINUTES.toMillis(intervaloMinutos);

        Log.d(TAG, "🔄 Reagendando lembrete para " + hora + ":" + minuto + " a cada " + intervaloMinutos + " minutos.");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(context, LembreteReceiver.class);
        intent.putExtra("mensagem", mensagem);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 1, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervaloMillis, pendingIntent);

        Log.d(TAG, "✅ Lembrete reagendado com sucesso!");
    }

    private void cancelarNotificacoes() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(this, LembreteReceiver.class), PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "❌ Lembretes cancelados.");
        }
    }

    private void verificarMetaEAtualizarBotao() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean metaConcluida = prefs.getBoolean(KEY_META_CONCLUIDA, false);
        botaoSalvarLembretes.setEnabled(!metaConcluida);
    }

    private void resetarEstadoMetaSeNovoDia() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String ultimaDataSalva = prefs.getString("ULTIMA_DATA_META", "");

        // Obtém a data atual no formato "YYYY-MM-DD"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dataAtual = sdf.format(new Date());

        if (!dataAtual.equals(ultimaDataSalva)) {
            // 🚀 **Novo dia detectado!** Resetar progresso da meta
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_META_CONCLUIDA, false); // Marca a meta como NÃO concluída
            editor.putString("ULTIMA_DATA_META", dataAtual); // Atualiza para a nova data
            editor.apply();

            // 🔄 Também reseta o banco de dados para um novo dia
            dbHelper.iniciarNovoDia();

            // ✅ **Reativa o botão de salvar lembretes**
            botaoSalvarLembretes.setEnabled(true);

            Log.d(TAG, "🔄 Novo dia detectado! Meta resetada e botão de salvar reativado.");
        } else {
            Log.d(TAG, "📅 Mesmo dia detectado, nenhum reset necessário.");
        }
    }

    private void salvarConfiguracoes() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Obtém o horário do TimePicker
        int hora = timePicker.getHour();
        int minuto = timePicker.getMinute();

        // Obtém o intervalo de lembrete salvo no RadioButton selecionado
        int intervaloMinutos = 30; // Padrão 30 minutos
        if (radioButton1Hora.isChecked()) {
            intervaloMinutos = 60;
        } else if (radioButton2Horas.isChecked()) {
            intervaloMinutos = 120;
        }

        // Salva os valores no SharedPreferences
        editor.putInt(KEY_HORA, hora);
        editor.putInt(KEY_MINUTO, minuto);
        editor.putInt(KEY_INTERVALO, intervaloMinutos);
        editor.putString(KEY_MENSAGEM, editTextMensagem.getText().toString());
        editor.putBoolean(KEY_NOTIFICACAO, switchLembrete.isChecked());

        editor.apply(); // Aplica as alterações

        Log.d(TAG, "💾 Configurações salvas: Hora: " + hora + " Minuto: " + minuto +
                " Intervalo: " + intervaloMinutos + " minutos. Notificações: " + switchLembrete.isChecked());
    }
    @Override
    public void verificarEAtualizarLembretes() {
        Log.d(TAG, "🎯 Meta atingida! Cancelando lembretes até o próximo dia.");

        // Salva no SharedPreferences que a meta foi concluída
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_META_CONCLUIDA, true);
        editor.apply();

        cancelarNotificacoes();
        verificarMetaEAtualizarBotao(); // Garante que o botão seja atualizado imediatamente
        Toast.makeText(this, "Meta diária concluída! Lembretes pausados até amanhã.", Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        salvarConfiguracoes();
        Log.d(TAG, "💾 Configurações salvas ao sair da tela.");
    }
}

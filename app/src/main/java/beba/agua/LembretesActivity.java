package beba.agua;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class LembretesActivity extends AppCompatActivity {

    private static final String TAG = "LembretesActivity";

    private TimePicker timePicker;
    private RadioGroup radioGroupFrequencia;
    private EditText editTextMensagem;
    private Switch switchLembrete;
    private ImageButton botaoConfigurarNotificacoes;
    private Button botaoSalvarLembretes;

    private static final String PREFS_NAME = "LembreteConfig";
    private static final String KEY_MENSAGEM = "mensagemLembrete";
    private static final String KEY_FREQUENCIA = "frequenciaLembrete";
    private static final String KEY_HORA = "horaLembrete";
    private static final String KEY_MINUTO = "minutoLembrete";
    static final String KEY_NOTIFICACAO = "notificacaoAtivada";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lembretes);

        inicializarComponentes();
        carregarConfiguracoes();
        configurarListeners();
        solicitarPermissaoAlarmes();

        Log.d(TAG, "🟢 Tela de lembretes carregada com sucesso.");
    }

    //solicitar permissão
    private void solicitarPermissaoAlarmes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    private void inicializarComponentes() {
        timePicker = findViewById(R.id.timePicker);
        radioGroupFrequencia = findViewById(R.id.radioGroupFrequencia);
        editTextMensagem = findViewById(R.id.editTextMensagem);
        switchLembrete = findViewById(R.id.switch1lembete);
        botaoSalvarLembretes = findViewById(R.id.botaoSalvarLembretes);
        botaoConfigurarNotificacoes = findViewById(R.id.botaoConfigurarNotificacoes);

        timePicker.setIs24HourView(true);
    }

    private void configurarListeners() {
        botaoSalvarLembretes.setOnClickListener(view -> {
            if (switchLembrete.isChecked()) {
                agendarLembretes();
            } else {
                cancelarNotificacoes();
            }
        });

        botaoConfigurarNotificacoes.setOnClickListener(view -> abrirConfiguracoesNotificacoes());

        switchLembrete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                cancelarNotificacoes();
            }
        });
    }

    private void abrirConfiguracoesNotificacoes() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        startActivity(intent);
    }

    private void carregarConfiguracoes() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        editTextMensagem.setText(prefs.getString(KEY_MENSAGEM, "Hora de beber água!"));
        switchLembrete.setChecked(prefs.getBoolean(KEY_NOTIFICACAO, true));

        int radioSelecionado = prefs.getInt(KEY_FREQUENCIA, R.id.radioButton1Hora);
        radioGroupFrequencia.check(radioSelecionado);

        int hora = prefs.getInt(KEY_HORA, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        int minuto = prefs.getInt(KEY_MINUTO, Calendar.getInstance().get(Calendar.MINUTE));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(hora);
            timePicker.setMinute(minuto);
        } else {
            timePicker.setCurrentHour(hora);
            timePicker.setCurrentMinute(minuto);
        }

        Log.d(TAG, "🔄 Configurações carregadas.");
    }

    private void agendarLembretes() {
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    boolean notificacaoAtivada = prefs.getBoolean(KEY_NOTIFICACAO, true);

    if (!notificacaoAtivada) {
        Log.d(TAG, "❌ Notificações estão desativadas. Nenhum lembrete será agendado.");
        return;
    }

    String mensagem = prefs.getString(KEY_MENSAGEM, "Hora de beber água!");
    int radioSelecionado = prefs.getInt(KEY_FREQUENCIA, R.id.radioButton1Hora);
    int hora = prefs.getInt(KEY_HORA, 8);  // Padrão: 08:00
    int minuto = prefs.getInt(KEY_MINUTO, 0);

    int intervaloMinutos = (radioSelecionado == R.id.radioButton30Min) ? 30 :
            (radioSelecionado == R.id.radioButton2Horas) ? 120 : 60;

    Log.d(TAG, "🔄 Agendando lembrete para " + hora + ":" + minuto + " a cada " + intervaloMinutos + " minutos.");

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, hora);
    calendar.set(Calendar.MINUTE, minuto);
    calendar.set(Calendar.SECOND, 0);

    if (calendar.before(Calendar.getInstance())) {
        calendar.add(Calendar.DAY_OF_MONTH, 1);
    }

    Intent intent = new Intent(this, LembreteReceiver.class);
    intent.setAction("beba.agua.LembreteReceiver");
    intent.putExtra("mensagem", mensagem);

    PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );

    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    long intervaloMillis = TimeUnit.MINUTES.toMillis(intervaloMinutos);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervaloMillis, pendingIntent);

    Log.d(TAG, "✅ Lembrete agendado com sucesso!");
}
    private void salvarConfiguracoes() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_MENSAGEM, editTextMensagem.getText().toString());
        editor.putBoolean(KEY_NOTIFICACAO, switchLembrete.isChecked());
        editor.putInt(KEY_FREQUENCIA, radioGroupFrequencia.getCheckedRadioButtonId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editor.putInt(KEY_HORA, timePicker.getHour());
            editor.putInt(KEY_MINUTO, timePicker.getMinute());
        } else {
            editor.putInt(KEY_HORA, timePicker.getCurrentHour());
            editor.putInt(KEY_MINUTO, timePicker.getCurrentMinute());
        }

        editor.apply();
        Log.d(TAG, "💾 Configurações salvas.");
    }

    private int obterFrequenciaSelecionada() {
        int radioSelecionado = radioGroupFrequencia.getCheckedRadioButtonId();
        if (radioSelecionado == R.id.radioButton30Min) return 30;
        else if (radioSelecionado == R.id.radioButton2Horas) return 120;
        else return 60;
    }
    //Cancelar Notificação
    private void cancelarNotificacoes() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(this, 0, new Intent(this, LembreteReceiver.class), PendingIntent.FLAG_IMMUTABLE));
        Toast.makeText(this, "Lembretes Desativados", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "❌ Lembretes cancelados.");
    }

    //Cancelar Lembretes
    public static void cancelarLembretes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean lembretesAtivados = prefs.getBoolean(KEY_NOTIFICACAO, true); // 🔹 Verifica se estavam ativos

        if (lembretesAtivados) { // 🔥 Apenas salva se estavam ativados antes de cancelar
            prefs.edit().putBoolean("LEMBRETES_FORAM_ATIVADOS", true).apply();
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, new Intent(context, LembreteReceiver.class), PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);

        prefs.edit().putBoolean(KEY_NOTIFICACAO, false).apply(); // 🔹 Atualiza estado no SharedPreferences
        Log.d(TAG, "❌ Lembretes foram cancelados pelo sistema.");
    }


    public static void reagendarLembretes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean notificacaoAtivada = prefs.getBoolean(KEY_NOTIFICACAO, true);

        if (!notificacaoAtivada) {
            Log.d(TAG, "🔕 Lembretes desativados, nada será reativado.");
            return;
        }

        String mensagem = prefs.getString(KEY_MENSAGEM, "Hora de beber água!");
        int radioSelecionado = prefs.getInt(KEY_FREQUENCIA, R.id.radioButton1Hora);
        int hora = prefs.getInt(KEY_HORA, 8);
        int minuto = prefs.getInt(KEY_MINUTO, 0);

        int intervaloMinutos = (radioSelecionado == R.id.radioButton30Min) ? 30 :
                (radioSelecionado == R.id.radioButton2Horas) ? 120 : 60;

        Log.d(TAG, "🔄 Reagendando lembrete para " + hora + ":" + minuto + " a cada " + intervaloMinutos + " minutos.");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(context, LembreteReceiver.class);
        intent.setAction("beba.agua.LembreteReceiver");
        intent.putExtra("mensagem", mensagem);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervaloMillis = TimeUnit.MINUTES.toMillis(intervaloMinutos);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        Log.d(TAG, "✅ Lembrete reagendado com sucesso!");
    }
}

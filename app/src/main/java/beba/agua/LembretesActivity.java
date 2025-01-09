package beba.agua;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class LembretesActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private RadioGroup radioGroupFrequencia;
    private EditText editTextMensagem;
    private Switch switchLembrete;
    private ImageButton botaoConfigurarNotificacoes;
    private Button botaoSalvarLembretes;

    private static final String TAG = "LembretesActivity";
    private static final String PREFS_NAME = "LembreteConfig";
    private static final String KEY_MENSAGEM = "mensagemLembrete";
    private static final String KEY_FREQUENCIA = "frequenciaLembrete";
    private static final String KEY_HORA = "horaLembrete";
    private static final String KEY_MINUTO = "minutoLembrete";
    private static final String KEY_NOTIFICACAO = "notificacaoAtivada";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lembretes);

        Log.d(TAG, "🟢 Tela de lembretes aberta.");

        inicializarComponentes();
        configurarTimePicker();
        carregarConfiguracoes();
        configurarListeners();
    }

    private void inicializarComponentes() {
        timePicker = findViewById(R.id.timePicker);
        radioGroupFrequencia = findViewById(R.id.radioGroupFrequencia);
        editTextMensagem = findViewById(R.id.editTextMensagem);
        switchLembrete = findViewById(R.id.switch1lembete);
        botaoSalvarLembretes = findViewById(R.id.botaoSalvarLembretes);
        botaoConfigurarNotificacoes = findViewById(R.id.botaoConfigurarNotificacoes);
    }

    private void configurarTimePicker() {
        timePicker.setIs24HourView(true);
        Calendar now = Calendar.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(now.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(now.get(Calendar.MINUTE));
        } else {
            timePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(now.get(Calendar.MINUTE));
        }
        Log.d(TAG, "🕒 TimePicker configurado com a hora atual.");
    }

    private void configurarListeners() {
        botaoSalvarLembretes.setOnClickListener(view -> {
            Log.d(TAG, "📌 Botão SALVAR LEMBRETES clicado.");
            if (switchLembrete.isChecked()) {
                agendarLembretes();
            } else {
                cancelarNotificacoes();
            }
        });

        botaoConfigurarNotificacoes.setOnClickListener(view -> {
            Log.d(TAG, "🔔 Botão CONFIGURAR NOTIFICAÇÕES clicado.");
            abrirConfiguracoesNotificacoes();
        });

        switchLembrete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "🔄 Switch de lembrete alterado: " + (isChecked ? "ATIVADO" : "DESATIVADO"));
            if (!isChecked) {
                cancelarNotificacoes();
            }
        });

        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            Log.d(TAG, "🕒 TimePicker alterado para: " + hourOfDay + ":" + minute);
        });

        radioGroupFrequencia.setOnCheckedChangeListener((group, checkedId) -> {
            Log.d(TAG, "🔁 Frequência do lembrete alterada: " + obterFrequenciaSelecionada() + " minutos.");
        });

        atualizarVisibilidadeBotaoNotificacoes();
    }

    private void abrirConfiguracoesNotificacoes() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        startActivity(intent);
    }

    private void atualizarVisibilidadeBotaoNotificacoes() {
        boolean notificacoesAtivadas = NotificationManagerCompat.from(this).areNotificationsEnabled();
        botaoConfigurarNotificacoes.setVisibility(notificacoesAtivadas ? View.GONE : View.VISIBLE);
        Log.d(TAG, "🔔 Status das notificações: " + (notificacoesAtivadas ? "ATIVADAS" : "DESATIVADAS"));
    }

    private void agendarLembretes() {
        int hora = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? timePicker.getHour() : timePicker.getCurrentHour();
        int minuto = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? timePicker.getMinute() : timePicker.getCurrentMinute();
        String mensagem = editTextMensagem.getText().toString();

        int intervaloMinutos = obterFrequenciaSelecionada();
        long intervaloMillis = TimeUnit.MINUTES.toMillis(intervaloMinutos);

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
                this, 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervaloMillis, pendingIntent);


        Log.d(TAG, "✅ Lembrete AGENDADO para " + hora + ":" + minuto + " com intervalo de " + intervaloMinutos + " minutos.");
        Toast.makeText(this, "**Lembrete Agendado**!", Toast.LENGTH_SHORT).show();
        salvarConfiguracoes();
    }

    private void cancelarNotificacoes() {
        Intent intent = new Intent(this, LembreteReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        Toast.makeText(this, "Lembretes Desativados", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "❌ Lembretes CANCELADOS.");
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
        Log.d(TAG, "💾 Configurações SALVAS!");
    }

    private void carregarConfiguracoes() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        editTextMensagem.setText(prefs.getString(KEY_MENSAGEM, "Hora de se hidratar!"));
        switchLembrete.setChecked(prefs.getBoolean(KEY_NOTIFICACAO, true));

        int radioSelecionado = prefs.getInt(KEY_FREQUENCIA, R.id.radioButton1Hora);
        radioGroupFrequencia.check(radioSelecionado);

        int hora = prefs.getInt(KEY_HORA, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        int minuto = prefs.getInt(KEY_MINUTO, Calendar.getInstance().get(Calendar.MINUTE));

        timePicker.setHour(hora);
        timePicker.setMinute(minuto);

        Log.d(TAG, "✅ Configurações CARREGADAS!");
    }

    private int obterFrequenciaSelecionada() {
        int radioSelecionado = radioGroupFrequencia.getCheckedRadioButtonId();
        if (radioSelecionado == R.id.radioButton30Min) return 30;
        else if (radioSelecionado == R.id.radioButton2Horas) return 120;
        else return 60;
    }
}

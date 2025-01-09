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

        // 🔹 Inicializando os componentes
        timePicker = findViewById(R.id.timePicker);
        radioGroupFrequencia = findViewById(R.id.radioGroupFrequencia);
        editTextMensagem = findViewById(R.id.editTextMensagem);
        switchLembrete = findViewById(R.id.switch1lembete);
        botaoSalvarLembretes = findViewById(R.id.botaoSalvarLembretes);
        botaoConfigurarNotificacoes = findViewById(R.id.botaoConfigurarNotificacoes);

        // 🔹 Configuração inicial do TimePicker (compatível com versões antigas)
        timePicker.setIs24HourView(true);
        Calendar now = Calendar.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(now.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(now.get(Calendar.MINUTE));
        } else {
            timePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(now.get(Calendar.MINUTE));
        }

        // 🔹 Carregar configurações salvas do usuário
        carregarConfiguracoes();

        // 🔹 Listener para salvar lembretes
        botaoSalvarLembretes.setOnClickListener(view -> {
            if (switchLembrete.isChecked()) {
                agendarLembretes();
            } else {
                cancelarNotificacoes();
            }
        });

        // 🔹 Botão para configurar notificações
        atualizarVisibilidadeBotaoNotificacoes();
        botaoConfigurarNotificacoes.setOnClickListener(view -> abrirConfiguracoesNotificacoes());

        // 🔹 Listener para ativar/desativar lembretes via Switch
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

    private void atualizarVisibilidadeBotaoNotificacoes() {
        boolean notificacoesAtivadas = NotificationManagerCompat.from(this).areNotificationsEnabled();
        botaoConfigurarNotificacoes.setVisibility(notificacoesAtivadas ? View.GONE : View.VISIBLE);
    }

    // 🔹 **Agendar lembretes no horário definido**
    private void agendarLembretes() {
        int hora, minuto;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hora = timePicker.getHour();
            minuto = timePicker.getMinute();
        } else {
            hora = timePicker.getCurrentHour();
            minuto = timePicker.getCurrentMinute();
        }

        String mensagem = editTextMensagem.getText().toString();
        int intervaloMinutos = obterFrequenciaSelecionada();
        long intervaloMillis = TimeUnit.MINUTES.toMillis(intervaloMinutos); // 🔥 Corrigido para tempo correto

        Log.d("LembretesActivity", "--->> Intervalo definido: " + intervaloMinutos + " minutos");

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

        Toast.makeText(this, "Lembrete Agendado!", Toast.LENGTH_SHORT).show();
        Log.d("LembretesActivity", "--->>> Lembrete agendado para " + hora + ":" + minuto + " com intervalo de " + intervaloMinutos + " minutos");

        salvarConfiguracoes();
    }

    // 🔹 **Salvar configurações do usuário no SharedPreferences**
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
        Log.d("Config", "------>>>> Configurações salvas!");
    }

    // 🔹 **Carregar configurações do usuário do SharedPreferences**
    private void carregarConfiguracoes() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        editTextMensagem.setText(prefs.getString(KEY_MENSAGEM, "Hora de se hidratar!"));
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
    }

    private int obterFrequenciaSelecionada() {
        int radioSelecionado = radioGroupFrequencia.getCheckedRadioButtonId();
        if (radioSelecionado == R.id.radioButton30Min) return 30;
        else if (radioSelecionado == R.id.radioButton2Horas) return 120;
        else return 60;
    }

    private void cancelarNotificacoes() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(this, 0, new Intent(this, LembreteReceiver.class), PendingIntent.FLAG_IMMUTABLE));
        Toast.makeText(this, "Lembretes Desativados", Toast.LENGTH_SHORT).show();
    }
}

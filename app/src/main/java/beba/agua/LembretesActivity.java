package beba.agua;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
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
    private ImageButton botaoConfigurarNotificacoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lembretes);

        timePicker = findViewById(R.id.timePicker);
        radioGroupFrequencia = findViewById(R.id.radioGroupFrequencia);
        editTextMensagem = findViewById(R.id.editTextMensagem);
        Button botaoSalvarLembretes = findViewById(R.id.botaoSalvarLembretes);
        botaoConfigurarNotificacoes = findViewById(R.id.botaoConfigurarNotificacoes);

        // Configurações iniciais do TimePicker
        timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(this));
        Calendar now = Calendar.getInstance();
        timePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(now.get(Calendar.MINUTE));

        // Configura o botão para salvar lembretes
        botaoSalvarLembretes.setOnClickListener(view -> agendarLembretes());

        // Configura o botão de notificações
        atualizarVisibilidadeBotaoNotificacoes();
        botaoConfigurarNotificacoes.setOnClickListener(view -> abrirConfiguracoesNotificacoes());
    }

    private void abrirConfiguracoesNotificacoes() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            atualizarVisibilidadeBotaoNotificacoes();
        }
    }

    private void atualizarVisibilidadeBotaoNotificacoes() {
        boolean notificacoesAtivadas = NotificationManagerCompat.from(this).areNotificationsEnabled();
        botaoConfigurarNotificacoes.setVisibility(notificacoesAtivadas ? View.GONE : View.VISIBLE);
    }

    private void agendarLembretes() {

        // Define a frequência dos lembretes para 1 minuto (ignora RadioGroup)

        long intervalo = TimeUnit.MINUTES.toMillis(1); // 1 minuto em milissegundos

        Log.d("LembretesActivity", "----->>Intervalo de agendamento: " + intervalo);

        int hora = timePicker.getCurrentHour();
        int minuto = timePicker.getCurrentMinute();
        String mensagem = editTextMensagem.getText().toString().trim();

//        if (mensagem.isEmpty()) {
//            Toast.makeText(this, "Por favor, insira uma mensagem para o lembrete.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Define a frequência com base no RadioGroup
//        long intervalo = obterFrequenciaSelecionada();
//        if (intervalo == 0) {
//            Toast.makeText(this, "Selecione uma frequência para o lembrete.", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // Configura o horário inicial do lembrete
        Calendar horarioLembrete = Calendar.getInstance();
        horarioLembrete.set(Calendar.HOUR_OF_DAY, hora);
        horarioLembrete.set(Calendar.MINUTE, minuto);
        horarioLembrete.set(Calendar.SECOND, 0);

        // Se o horário já passou, agenda para o próximo dia
        if (horarioLembrete.before(Calendar.getInstance())) {
            horarioLembrete.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Cria o Intent para o BroadcastReceiver
        Intent intent = new Intent(this, LembreteReceiver.class);
        intent.setAction("beba.agua.LembreteReceiver");
        intent.putExtra("mensagem", mensagem);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        Log.d("LembretesActivity", "---->>>Agendando lembrete...");

        // Configura o AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    horarioLembrete.getTimeInMillis(),
                    intervalo,
                    pendingIntent
            );
            Toast.makeText(this, "Lembrete agendado com sucesso!", Toast.LENGTH_SHORT).show();

            Log.d("LembretesActivity", "---->>>Lembrete agendado: " + horarioLembrete.getTime());
        } else {
            Toast.makeText(this, "----->>>Erro ao agendar o lembrete.", Toast.LENGTH_SHORT).show();
            Log.e("LembretesActivity", "---->>>AlarmManager não disponível.");
        }

        finish();
    }

    private long obterFrequenciaSelecionada() {
        int selecionadoId = radioGroupFrequencia.getCheckedRadioButtonId();
        if (selecionadoId == R.id.radioButton1Hora) {
            return TimeUnit.HOURS.toMillis(1);
        } else if (selecionadoId == R.id.radioButton2Horas) {
            return TimeUnit.HOURS.toMillis(2);
        } else if (selecionadoId == R.id.radioButton30Min) {
            return TimeUnit.MINUTES.toMillis(30);
        }
        return 0;
    }
}

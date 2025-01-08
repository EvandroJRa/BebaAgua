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
        int hora = timePicker.getCurrentHour();
        int minuto = timePicker.getCurrentMinute();
        String mensagem = editTextMensagem.getText().toString();

        // Verifica se as notificações estão habilitadas
        Switch switchNotificacoes = findViewById(R.id.switch1lembete);
        if (!switchNotificacoes.isChecked()) {
            // Cancela notificações
            cancelarNotificacoes();
            Toast.makeText(this, "Notificações desabilitadas!", Toast.LENGTH_SHORT).show();
            Log.d("LembretesActivity", "---->>Notificações desabilitadas pelo usuário.");
            return;
        }

        // Define a frequência dos lembretes (baseado no RadioGroup Frequência)
        long intervalo = TimeUnit.MINUTES.toMillis(1); // Valor padrão 1 HOURS
        if (radioGroupFrequencia.getCheckedRadioButtonId() == R.id.radioButton30Min) {
            intervalo = TimeUnit.MINUTES.toMillis(30); // VOLTAR PARA 30 APOS FINALIZAR OS TESTES
        } else if (radioGroupFrequencia.getCheckedRadioButtonId() == R.id.radioButton2Horas) {
            intervalo = TimeUnit.HOURS.toMillis(2);
        }

        Log.d("LembretesActivity", "--->>>Intervalo definido para: " + intervalo + " milissegundos");

        // Configura o calendário
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Cria o Intent e o PendingIntent
        Intent intent = new Intent(this, LembreteReceiver.class);
        intent.setAction("beba.agua.LembreteReceiver");
        intent.putExtra("mensagem", mensagem);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Agenda o alarme
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalo, pendingIntent);

        Toast.makeText(this, "Lembrete agendado com sucesso!", Toast.LENGTH_SHORT).show();
        Log.d("LembretesActivity", "----->>>Lembrete agendado com sucesso!");
        finish();
    }

    // Método para cancelar notificações
    private void cancelarNotificacoes() {
        Intent intent = new Intent(this, LembreteReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}


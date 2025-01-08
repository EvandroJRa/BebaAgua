package beba.agua;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
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
    private ImageButton botaoConfigurarNotificacoes; // Declaração da variável aqui

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lembretes);

        timePicker = findViewById(R.id.timePicker);
        // Define a hora atual no TimePicker
        timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(this)); // Define o formato de 24 horas ou AM/PM
        timePicker.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(Calendar.getInstance().get(Calendar.MINUTE));
        radioGroupFrequencia = findViewById(R.id.radioGroupFrequencia);
        editTextMensagem = findViewById(R.id.editTextMensagem);
        Button botaoSalvarLembretes = findViewById(R.id.botaoSalvarLembretes);
        botaoSalvarLembretes.setOnClickListener(view -> agendarLembretes());

        // Botão de configuração de notificação
        botaoConfigurarNotificacoes = findViewById(R.id.botaoConfigurarNotificacoes);

        // Verifica o estado da notificação e atualiza a visibilidade do botão
        atualizarVisibilidadeBotaoNotificacoes();

        botaoConfigurarNotificacoes.setOnClickListener(view -> {
            // Abre a tela de configurações de notificações do aplicativo
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
            startActivity(intent);

            // Sobrescreve o método onActivityResult para verificar o estado das notificações
            // quando o usuário retornar das configurações de notificações
            startActivityForResult(intent, 1); // 1 é um código de solicitação arbitrário
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) { // Verifica se é o resultado da activity de configurações
            // Atualiza a visibilidade do botão após o retorno
            atualizarVisibilidadeBotaoNotificacoes();
        }
    }

    private void atualizarVisibilidadeBotaoNotificacoes() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        boolean areNotificationsEnabled = notificationManager.areNotificationsEnabled();

        if (areNotificationsEnabled) {
            botaoConfigurarNotificacoes.setVisibility(View.GONE);
        } else {
            botaoConfigurarNotificacoes.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) { // Verifica se é a resposta da nossa solicitação
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // A permissão foi concedida
                // ... seu código para agendar o lembrete ...
            } else {
                // A permissão foi negada
                Toast.makeText(this, "Permissão de notificações negada", Toast.LENGTH_SHORT).show();

            }
        }
    }
    private void agendarLembretes() {
        int hora = timePicker.getCurrentHour();
        int minuto = timePicker.getCurrentMinute();
        String mensagem = editTextMensagem.getText().toString();

        // Define a frequência dos lembretes
//        long intervalo = 0;
//        int radioButtonId = radioGroupFrequencia.getCheckedRadioButtonId();
//        if (radioButtonId == R.id.radioButton1Hora) {
//            //intervalo = TimeUnit.HOURS.toMillis(1); // 1 hora em milissegundos
//            intervalo = TimeUnit.MINUTES.toMillis(1); // 1 minuto em milissegundos
//        } else if (radioButtonId == R.id.radioButton2Horas) {
//            intervalo = TimeUnit.MINUTES.toMillis(2); // 2 minutoS em milissegundos
//            //intervalo = TimeUnit.HOURS.toMillis(2); // 2 horas em milissegundos
//        }
        // Define a frequência dos lembretes para 1 minuto (ignora RadioGroup)
        long intervalo = TimeUnit.MINUTES.toMillis(1);

        Log.d("LembretesActivity", "------>>>Intervalo definido para: " + intervalo + " milissegundos"); // Log para o intervalo


        // Cria um Calendar com o horário definido pelo usuário
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, 0);

        // Se o horário definido já passou, adiciona um dia ao Calendar
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Cria um Intent para o BroadcastReceiver
        Intent intent = new Intent(this, LembreteReceiver.class);

        intent.setAction("beba.agua.LembreteReceiver"); // Define a ação explicitamente
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Cria um PendingIntent com o sinalizador FLAG_IMMUTABLE
        //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentMainActivity, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d("LembretesActivity", "--------->Agendando lembrete...");

        // Agenda o alarme com o AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalo, pendingIntent);


        Toast.makeText(this, "Lembrete Agendado!", Toast.LENGTH_SHORT).show();
        Log.d("LembretesActivity", "----->Lembrete agendado!");

        finish();
    }

}
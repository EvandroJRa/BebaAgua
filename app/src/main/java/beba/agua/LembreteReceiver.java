package beba.agua;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class LembreteReceiver extends BroadcastReceiver {

    private static final String TAG = "LembreteReceiver";
    private static final String CHANNEL_ID = "beba_agua_channel";
    private static final String PREFS_NAME = "LembreteConfig";
    private static final String KEY_FREQUENCIA = "frequenciaLembrete";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "🚀 LembreteReceiver foi acionado!");
        Log.d(TAG, "📢 Lembrete recebido! Exibindo notificação...");

        String mensagem = intent.getStringExtra("mensagem");
        if (mensagem == null || mensagem.isEmpty()) {
            mensagem = "Hora de beber água! 💧";
        }

        criarCanalDeNotificacao(context);
        exibirNotificacao(context, mensagem);

        // ✅ Agendar próxima execução do alarme para repetir conforme a frequência definida
        reagendarLembrete(context);
    }

    private void criarCanalDeNotificacao(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                CharSequence nome = "Lembretes de Hidratação";
                String descricao = "Notificações para lembrar de beber água.";
                int importancia = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, nome, importancia);
                channel.setDescription(descricao);

                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Canal de notificação criado com sucesso.");
            } else {
                Log.d(TAG, "🔄 Canal de notificação já existente.");
            }
        }
    }

    private void exibirNotificacao(Context context, String mensagem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "⚠️ Permissão de notificação não concedida! Notificação bloqueada.");
                return; // Não exibir notificação sem permissão
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_gota)
                .setContentTitle("🚰 Hidratação Importante!")
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1001, builder.build());

        Log.d(TAG, "✅ Notificação exibida com sucesso.");
    }

    private void reagendarLembrete(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int radioSelecionado = prefs.getInt(KEY_FREQUENCIA, R.id.radioButton1Hora); // Frequência padrão: 1 hora

        int intervaloMinutos = (radioSelecionado == R.id.radioButton30Min) ? 30 :
                (radioSelecionado == R.id.radioButton2Horas) ? 120 : 60; // 1h padrão

        long intervaloMillis = TimeUnit.MINUTES.toMillis(intervaloMinutos);

        Log.d(TAG, "🔄 Reagendando lembrete para daqui a " + intervaloMinutos + " minutos.");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, intervaloMinutos);

        Intent intent = new Intent(context, LembreteReceiver.class);
        intent.setAction("beba.agua.LembreteReceiver");
        intent.putExtra("mensagem", "Hora de beber água! 💧");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d(TAG, "🕒 Novo lembrete agendado para: " + calendar.getTime());
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        Log.d(TAG, "✅ Lembrete reagendado com sucesso!");
    }
}

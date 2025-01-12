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
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.Calendar;


public class LembreteReceiver extends BroadcastReceiver {

    private static final String TAG = "LembreteReceiver";
    private static final String CHANNEL_ID = "beba_agua_channel";
    private static final String PREFS_NAME = "LembreteConfig";
    private static final String KEY_FREQUENCIA = "frequenciaLembrete";
    private static final String KEY_MENSAGEM = "mensagemLembrete";
    private static final String KEY_NOTIFICACAO = "notificacaoAtivada";

    private int obterFrequenciaSelecionada(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int radioSelecionado = prefs.getInt(KEY_FREQUENCIA, R.id.radioButton1Hora);

        if (radioSelecionado == R.id.radioButton30Min) return 30;
        else if (radioSelecionado == R.id.radioButton2Horas) return 120;
        else return 60; // Padrão: 1 hora
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "🚀 LembreteReceiver acionado! Verificando notificação...");

        // 🔥 Solicita permissão de alarmes exatos se necessário
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "⚠️ Permissão de alarmes exatos ainda não concedida!");
                solicitarPermissaoAlarmesExatos(context);
                return;
            }
        }

        // 🔥 Obtém a mensagem do intent (ou define um padrão)
        String mensagem = intent.getStringExtra("mensagem");
        if (mensagem == null || mensagem.isEmpty()) {
            mensagem = "Hora de beber água! 💧";
        }

        // 🔹 Criar canal de notificação e exibir
        criarCanalDeNotificacao(context);
        exibirNotificacao(context, mensagem);

        // 🔄 Reagendar lembrete automaticamente
        reagendarLembrete(context);
    }

    private void solicitarPermissaoAlarmesExatos(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 🚀 Adiciona a flag necessária
                context.startActivity(intent);

                Log.w(TAG, "⚠️ Requesting exact alarm permission...");
                Toast.makeText(context, "Ative a permissão de alarmes exatos nas configurações.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void criarCanalDeNotificacao(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Lembretes de Hidratação",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notificações para lembrar de beber água.");
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Canal de notificação criado.");
            } else {
                Log.d(TAG, "🔄 Canal de notificação já existe.");
            }
        }
    }

    private void exibirNotificacao(Context context, String mensagem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "⚠️ Permissão de notificação não concedida! Notificação bloqueada.");
                return;
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Hidratação Importante!")
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1001, builder.build());

        Log.d(TAG, "✅ Notificação exibida com sucesso!");
    }

    private void reagendarLembrete(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int intervaloMinutos = obterFrequenciaSelecionada(context);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, intervaloMinutos);

        Log.d(TAG, "🔄 Reagendando lembrete para daqui a " + intervaloMinutos + " minutos.");

        Intent intent = new Intent(context, LembreteReceiver.class);
        intent.setAction("beba.agua.LembreteReceiver");
        intent.putExtra("mensagem", "Hora de beber água! 💧");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 1, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        Log.d(TAG, "✅ Novo lembrete agendado para " + calendar.getTime());
    }
}

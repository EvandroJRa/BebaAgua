package beba.agua;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class LembreteReceiver extends BroadcastReceiver {

    private static final String TAG = "LembreteReceiver";
    private static final String CHANNEL_ID = "beba_agua_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "📢 Lembrete recebido! Tentando exibir notificação...");

        String mensagem = intent.getStringExtra("mensagem");
        if (mensagem == null || mensagem.isEmpty()) {
            mensagem = "Hora de beber água! 💧";
        }

        criarCanalDeNotificacao(context);
        exibirNotificacao(context, mensagem);
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
}

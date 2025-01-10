import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.Manifest;
import android.app.PendingIntent;

public class LembreteReceiver extends BroadcastReceiver {
    private static final String TAG = "LembreteReceiver";
    private static final String CHANNEL_ID = "LEMBRETES_CANAL";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "📢 Lembrete recebido! Tentando exibir notificação...");

        if (intent.getAction() != null && intent.getAction().equals("beba.agua.LembreteReceiver")) {
            String mensagem = intent.getStringExtra("mensagem");

            // Criar canal antes de exibir a notificação
            criarCanalDeNotificacao(context);
            exibirNotificacao(context, mensagem);
        }
    }

    private void criarCanalDeNotificacao(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Verifica se o dispositivo está no Android 8.0+
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Verifica se o canal já existe
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                CharSequence nome = "Lembretes de Hidratação";
                String descricao = "Notificações para lembrar você de beber água";
                int importancia = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel canal = new NotificationChannel(CHANNEL_ID, nome, importancia);
                canal.setDescription(descricao);
                canal.enableVibration(true);
                canal.enableLights(true);

                notificationManager.createNotificationChannel(canal);
                Log.d(TAG, "✅ Canal de notificação criado com sucesso.");
            } else {
                Log.d(TAG, "📌 Canal de notificação já existente.");
            }
        }
    }

    private void exibirNotificacao(Context context, String mensagem) {
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
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "❌ Permissão de notificação não concedida.");
            return;
        }

        notificationManager.notify(1001, builder.build());
        Log.d(TAG, "✅ Notificação exibida com sucesso.");
    }
}

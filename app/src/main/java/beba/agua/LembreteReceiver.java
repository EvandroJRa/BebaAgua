package beba.agua;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class LembreteReceiver extends BroadcastReceiver {

    private static final String TAG = "LembreteReceiver";
    private static final String CANAL_ID = "canal_lembretes";
    private static final int NOTIFICACAO_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "---->>>>Lembrete recebido!");

        String mensagem = intent.getStringExtra("mensagem");
        if (mensagem == null || mensagem.isEmpty()) {
            mensagem = "Hora de beber água!";
        }

        // Cria o canal de notificação, se necessário
        criarCanalNotificacao(context);

        // Cria um Intent para abrir a MainActivity ao clicar na notificação
        Intent intentMainActivity = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intentMainActivity,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Configura a notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(R.drawable.icon_gota) // Ícone da notificação
                .setContentTitle("Lembrete de hidratação")
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Remove a notificação ao clicar

        // Exibe a notificação
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(NOTIFICACAO_ID, builder.build());
            Log.d(TAG, "Notificação exibida com sucesso.");
        } catch (Exception e) {
            Log.e(TAG, "---->>>>Erro ao exibir a notificação: " + e.getMessage(), e);
        }
    }

    private void criarCanalNotificacao(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence nome = "Lembretes de Hidratação";
            String descricao = "Canal para notificações de lembretes de água";
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel canal = new NotificationChannel(CANAL_ID, nome, importancia);
            canal.setDescription(descricao);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(canal);
                Log.d(TAG, "Canal de notificação criado.");
            } else {
                Log.e(TAG, "----->>>Erro ao criar o canal de notificação: NotificationManager é nulo.");
            }
        }
    }
}

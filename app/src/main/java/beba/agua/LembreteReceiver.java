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

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("LembreteReceiver", "--------->Lembrete recebido!");

        String mensagem = intent.getStringExtra("mensagem");
        if (mensagem == null || mensagem.isEmpty()) {
            mensagem = "Hora de beber água!";
        }

        // Cria um canal de notificação (para Android 8.0 ou superior)
        criarCanalNotificacao(context);

        // Cria um Intent para abrir a MainActivity quando a notificação for clicada
        Intent intentMainActivity = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);

        // Cria a notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "canal_lembretes")
                .setSmallIcon(R.drawable.icon_gota) // Substitua pelo ícone da sua notificação
                .setContentTitle("Lembrete de hidratação")
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Remove a notificação quando clicada

        // Exibe a notificação
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }

    private void criarCanalNotificacao(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence nome = "Lembretes de água";
            String descricao = "Canal para lembretes de beber água";
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel canal = new NotificationChannel("canal_lembretes", nome, importancia);
            canal.setDescription(descricao);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }
    }
}
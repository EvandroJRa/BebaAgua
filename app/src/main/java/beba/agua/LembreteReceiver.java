package beba.agua;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class LembreteReceiver extends BroadcastReceiver {

    private static final String TAG = "LembreteReceiver";
    private static final String CANAL_ID = "canal_lembretes";
    private static final int NOTIFICACAO_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "---->>>> Lembrete recebido! Tentando exibir notificação...");

        String mensagem = intent.getStringExtra("mensagem");
        if (mensagem == null || mensagem.isEmpty()) {
            mensagem = "Hora de beber água!";
        }

        // Criar o canal de notificação (para Android 8.0 ou superior)
        criarCanalNotificacao(context);

        // Criar Intent para abrir a MainActivity ao clicar na notificação
        Intent intentMainActivity = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intentMainActivity,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Criar a notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(R.drawable.icon_gota) // Ícone da notificação
                .setContentTitle("Lembrete de hidratação")
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Alta prioridade para garantir que apareça
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Remove a notificação ao clicar

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // 🔍 **Verificação de permissão (Android 13+)**
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "⚠️ Permissão de notificação não concedida! Notificação não será exibida.");
                Toast.makeText(context, "Ative as notificações para receber lembretes!", Toast.LENGTH_LONG).show();
                return; // **Sai do método para evitar a SecurityException**
            }
        }

        try {
            notificationManager.notify(NOTIFICACAO_ID, builder.build());
            Log.d(TAG, "✅ Notificação exibida com sucesso.");
        } catch (Exception e) {
            Log.e(TAG, "---->>>> Erro ao exibir a notificação: " + e.getMessage(), e);
        }
    }

    private void criarCanalNotificacao(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence nome = "Lembretes de Hidratação";
            String descricao = "Canal para notificações de lembretes de água";
            int importancia = NotificationManager.IMPORTANCE_HIGH; // ALTA prioridade

            NotificationChannel canal = new NotificationChannel(CANAL_ID, nome, importancia);
            canal.setDescription(descricao);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(canal);
                Log.d(TAG, "✅ Canal de notificação criado com sucesso.");
            } else {
                Log.e(TAG, "----->>> Erro ao criar o canal de notificação: NotificationManager é nulo.");
            }
        }
    }
}

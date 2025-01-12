package beba.agua;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private static final String PREFS_NAME = "LembreteConfig"; // Nome correto do SharedPreferences
    private static final String KEY_NOTIFICACAO = "notificacaoAtivada"; // Chave correta

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "🔄 Dispositivo reiniciado! Verificando lembretes...");

            // 🔹 Obtém SharedPreferences corretamente
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean lembreteAtivo = prefs.getBoolean(KEY_NOTIFICACAO, false);

            // 🔹 Log para verificar se a chave foi lida corretamente
            Log.d(TAG, "📌 Estado da chave '" + KEY_NOTIFICACAO + "': " + lembreteAtivo);

            if (lembreteAtivo) {
                Log.d(TAG, "✅ Reagendando lembretes após reinicialização.");
                LembretesActivity.reagendarLembretes(context);
            } else {
                Log.d(TAG, "🔕 Nenhum lembrete estava ativado antes do reboot.");
            }
        }
    }
}

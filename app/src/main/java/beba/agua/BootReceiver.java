package beba.agua;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "🔄 Dispositivo reiniciado! Verificando lembretes...");

            SharedPreferences prefs = context.getSharedPreferences("LembreteConfig", Context.MODE_PRIVATE);
            boolean lembreteAtivo = prefs.getBoolean("notificacaoAtivada", false);

            if (lembreteAtivo) {
                Log.d(TAG, "✅ Reagendando lembretes após reinicialização.");
                LembretesActivity.reagendarLembretes(context);
            } else {
                Log.d(TAG, "🔕 Nenhum lembrete estava ativado antes do reboot.");
            }
        }
    }
}

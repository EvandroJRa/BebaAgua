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
            Log.d(TAG, "📢 Dispositivo reiniciado! Reativando lembretes...");

            // Reagendar lembretes após reinicialização
            LembretesActivity.reagendarLembretes(context);
        }
    }
}

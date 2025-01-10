package beba.agua;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("BootReceiver", "🔄 Dispositivo reiniciado! Verificando permissão...");

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                Log.d("BootReceiver", "✅ Permissão concedida, reagendando lembretes...");
                LembretesActivity.reagendarLembretes(context);
            } else {
                Log.e("BootReceiver", "🚨 Permissão SCHEDULE_EXACT_ALARM não concedida!");
            }
        }
    }
}


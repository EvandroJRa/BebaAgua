package beba.agua;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class LembretesActivity extends AppCompatActivity {

    private static final String TAG = "LembretesActivity";

    private TimePicker timePicker;
    private RadioGroup radioGroupFrequencia;
    private EditText editTextMensagem;
    private Switch switchLembrete;
    private Button botaoSalvarLembretes;

    private static final String PREFS_NAME = "LembreteConfig";
    private static final String KEY_MENSAGEM = "mensagemLembrete";
    private static final String KEY_FREQUENCIA = "frequenciaLembrete";
    private static final String KEY_HORA = "horaLembrete";
    private static final String KEY_MINUTO = "minutoLembrete";
    static final String KEY_NOTIFICACAO = "notificacaoAtivada";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lembretes);

        inicializarComponentes();
        carregarConfiguracoes();
        configurarListeners();
        salvarConfiguracoes();
        solicitarPermissaoAlarme();
        verificarPermissaoAlarme();
        solicitarPermissaoAlarmesExatos();
        obterFrequenciaSelecionada();


        Log.d(TAG, "🟢 Tela de lembretes carregada com sucesso.");
    }

    // 🔹 Solicita permissão para alarmes exatos
    private void solicitarPermissaoAlarme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "⚠️ Permissão de alarme exato não concedida! Abrindo configurações...");

                // Verifica se já foi solicitado antes para evitar múltiplos prompts
                SharedPreferences prefs = getSharedPreferences("ConfigApp", MODE_PRIVATE);
                boolean jaSolicitou = prefs.getBoolean("PERMISSAO_ALARME_SOLICITADA", false);

                if (!jaSolicitou) {
                    // Abre a tela de configurações do aplicativo
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);

                    // Salva que já solicitamos a permissão para não perguntar novamente
                    prefs.edit().putBoolean("PERMISSAO_ALARME_SOLICITADA", true).apply();

                    Toast.makeText(this, "Ative a permissão de Alarmes e Lembretes nas configurações do app.", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d(TAG, "✅ Permissão de alarme exato já concedida.");
            }
        }
    }
    // 🔹 **Solicita permissão para alarmes exatos no Android 12+ (API 31+)**
    private void solicitarPermissaoAlarmesExatos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                Log.w(TAG, "⚠️ Solicitando permissão para agendar alarmes exatos.");
            } else {
                Log.d(TAG, "✅ Permissão para alarmes exatos já concedida.");
            }
        }
    }

    // 🔹 Verifica se a permissão foi concedida
    private void verificarPermissaoAlarme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "❌ Permissão SCHEDULE_EXACT_ALARM ainda não foi concedida!");

                new AlertDialog.Builder(this)
                        .setTitle("Permissão Necessária")
                        .setMessage("Para que os lembretes funcionem corretamente, ative a permissão de alarme exato nas configurações do sistema.")
                        .setPositiveButton("Abrir Configurações", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            } else {
                Log.d(TAG, "✅ Permissão SCHEDULE_EXACT_ALARM concedida.");
            }
        }
    }

    private void inicializarComponentes() {
        timePicker = findViewById(R.id.timePicker);
        radioGroupFrequencia = findViewById(R.id.radioGroupFrequencia);
        editTextMensagem = findViewById(R.id.editTextMensagem);
        switchLembrete = findViewById(R.id.switch1lembete);
        botaoSalvarLembretes = findViewById(R.id.botaoSalvarLembretes);

        timePicker.setIs24HourView(true);
    }

    private void configurarListeners() {
        botaoSalvarLembretes.setOnClickListener(view -> {
            Log.d(TAG, "💾 Botão SALVAR pressionado!");

            if (switchLembrete.isChecked()) {
                agendarLembretes(); // 🔥 Agora não cancela antes!
                Toast.makeText(this, "Lembretes configurados com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                cancelarNotificacoes();
                Toast.makeText(this, "Lembretes desativados!", Toast.LENGTH_SHORT).show();
            }
        });

        switchLembrete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                Log.d(TAG, "🔕 Lembretes DESATIVADOS pelo usuário.");
                cancelarNotificacoes();
            } else {
                Log.d(TAG, "🔔 Lembretes ATIVADOS pelo usuário.");
            }
        });

        radioGroupFrequencia.setOnCheckedChangeListener((group, checkedId) -> {
            int frequencia = obterFrequenciaSelecionada();
            Log.d(TAG, "⏰ Frequência do lembrete alterada para " + frequencia + " minutos.");
        });
    }

    private void carregarConfiguracoes() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        editTextMensagem.setText(prefs.getString(KEY_MENSAGEM, "Hora de beber água!"));
        switchLembrete.setChecked(prefs.getBoolean(KEY_NOTIFICACAO, true));

        int radioSelecionado = prefs.getInt(KEY_FREQUENCIA, R.id.radioButton1Hora);
        radioGroupFrequencia.check(radioSelecionado);

        int hora = prefs.getInt(KEY_HORA, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        int minuto = prefs.getInt(KEY_MINUTO, Calendar.getInstance().get(Calendar.MINUTE));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(hora);
            timePicker.setMinute(minuto);
        } else {
            timePicker.setCurrentHour(hora);
            timePicker.setCurrentMinute(minuto);
        }

        Log.d(TAG, "🔄 Configurações carregadas.");
    }

    private void agendarLembretes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "❌ ERRO: Permissão de alarme exato não concedida! Alarme não será agendado.");
                return;
            }
        }

        int hora = timePicker.getHour();
        int minuto = timePicker.getMinute();
        int intervaloMinutos = obterFrequenciaSelecionada();
        long intervaloMillis = TimeUnit.MINUTES.toMillis(intervaloMinutos);
        String mensagem = editTextMensagem.getText().toString();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(this, LembreteReceiver.class);
        intent.setAction("beba.agua.LembreteReceiver");
        intent.putExtra("mensagem", mensagem);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                intervaloMillis,
                pendingIntent);

        salvarConfiguracoes(); // Salvar SOMENTE após sucesso no agendamento

        Log.d(TAG, "✅ Lembrete AGENDADO para " + hora + ":" + minuto + " e será repetido a cada " + intervaloMinutos + " minutos.");
    }


    private void salvarConfiguracoes() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_MENSAGEM, editTextMensagem.getText().toString());
        editor.putBoolean(KEY_NOTIFICACAO, switchLembrete.isChecked());
        editor.putInt(KEY_FREQUENCIA, radioGroupFrequencia.getCheckedRadioButtonId());
        editor.putInt(KEY_HORA, timePicker.getHour());
        editor.putInt(KEY_MINUTO, timePicker.getMinute());

        editor.apply();
        Log.d(TAG, "💾 Configurações salvas com sucesso.");
    }

    private int obterFrequenciaSelecionada() {
        int radioSelecionado = radioGroupFrequencia.getCheckedRadioButtonId();
        if (radioSelecionado == R.id.radioButton30Min) return 30;
        else if (radioSelecionado == R.id.radioButton2Horas) return 120;
        else return 60; // Padrão: 1 hora
    }


    public static void reagendarLembretes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean notificacaoAtivada = prefs.getBoolean(KEY_NOTIFICACAO, true);

        if (!notificacaoAtivada) {
            Log.d(TAG, "🔕 Lembretes desativados, nada será reativado.");
            return;
        }

        String mensagem = prefs.getString(KEY_MENSAGEM, "Hora de beber água!");
        int radioSelecionado = prefs.getInt(KEY_FREQUENCIA, R.id.radioButton1Hora);
        int hora = prefs.getInt(KEY_HORA, 8);
        int minuto = prefs.getInt(KEY_MINUTO, 0);

        int intervaloMinutos = (radioSelecionado == R.id.radioButton30Min) ? 30 :
                (radioSelecionado == R.id.radioButton2Horas) ? 120 : 60;

        Log.d(TAG, "🔄 Reagendando lembrete para " + hora + ":" + minuto + " a cada " + intervaloMinutos + " minutos.");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(context, LembreteReceiver.class);
        intent.setAction("beba.agua.LembreteReceiver");
        intent.putExtra("mensagem", mensagem);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervaloMillis = TimeUnit.MINUTES.toMillis(intervaloMinutos);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            Log.d(TAG, "🕒 Lembrete realmente AGENDADO para: " + calendar.getTime());
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        Log.d(TAG, "✅ Lembrete reagendado com sucesso!");
    }

    //Cancelar Lembretes
    public static void cancelarLembretes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean lembretesAtivados = prefs.getBoolean(KEY_NOTIFICACAO, true); // 🔹 Verifica se estavam ativos

        if (lembretesAtivados) { // 🔥 Apenas salva se estavam ativados antes de cancelar
            prefs.edit().putBoolean("LEMBRETES_FORAM_ATIVADOS", true).apply();
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, new Intent(context, LembreteReceiver.class), PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);

        prefs.edit().putBoolean(KEY_NOTIFICACAO, false).apply(); // 🔹 Atualiza estado no SharedPreferences
        Log.d(TAG, "❌ Lembretes foram cancelados pelo sistema.");
        Toast.makeText(context, "Lembretes Desativados", Toast.LENGTH_SHORT).show();
    }

    //Cancelar Notificação
    private void cancelarNotificacoes() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(this, LembreteReceiver.class), PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "❌ Lembretes cancelados.");
        } else {
            Log.d(TAG, "⚠️ Nenhum lembrete ativo para cancelar.");
        }
    }

}

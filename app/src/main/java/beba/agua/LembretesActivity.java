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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.Calendar;

import java.util.concurrent.TimeUnit;

public class LembretesActivity extends AppCompatActivity implements DatabaseHelper.LembretesListener {

    private static final String TAG = "LembretesActivity";

    private TimePicker timePicker;
    private RadioGroup radioGroupFrequencia;
    private EditText editTextMensagem;
    private Switch switchLembrete;
    private Button botaoSalvarLembretes;

    private RadioButton radioButton30Min;
    private RadioButton radioButton1Hora;
    private RadioButton radioButton2Horas;
    //private RadioGroup radioGroupFrequencia;


    private static final String PREFS_NAME = "LembreteConfig";
    private static final String KEY_MENSAGEM = "mensagemLembrete";
    private static final String KEY_FREQUENCIA = "frequenciaLembrete";
    private static final String KEY_HORA = "horaLembrete";
    private static final String KEY_MINUTO = "minutoLembrete";
    static final String KEY_NOTIFICACAO = "notificacaoAtivada";
    private static final String KEY_INTERVALO = "lembrete_intervalo"; // Chave para armazenar o intervalo dos lembretes

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lembretes);

        // Define Status Bar preta para esta tela
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        // Inicializa os componentes da interface
        radioButton30Min = findViewById(R.id.radioButton30Min);
        radioButton1Hora = findViewById(R.id.radioButton1Hora);
        radioButton2Horas = findViewById(R.id.radioButton2Horas);
        radioGroupFrequencia = findViewById(R.id.radioGroupFrequencia);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean primeiroAcesso = prefs.getBoolean("KEY_PRIMEIRO_ACESSO", true); // Default: true
        int intervaloMinutos = prefs.getInt(KEY_INTERVALO, -1); // -1 indica que não há valor salvo

        if (primeiroAcesso || intervaloMinutos == -1) {
            // Se for o primeiro acesso, desativa os RadioButtons
            radioButton30Min.setEnabled(false);
            radioButton1Hora.setEnabled(false);
            radioButton2Horas.setEnabled(false);

            Log.d(TAG, "🚀 Primeiro acesso: RadioButtons desativados aguardando interação.");
        } else {
            // Atualiza a seleção dos RadioButtons conforme o valor salvo
            if (intervaloMinutos == 30) {
                radioButton30Min.setChecked(true);
            } else if (intervaloMinutos == 60) {
                radioButton1Hora.setChecked(true);
            } else if (intervaloMinutos == 120) {
                radioButton2Horas.setChecked(true);
            }
        }

        // Listener para quando o usuário interagir pela primeira vez
        radioGroupFrequencia.setOnCheckedChangeListener((group, checkedId) -> {
            salvarConfiguracoes();
            int novoIntervalo = 30; // Padrão: 30 minutos

            if (checkedId == R.id.radioButton1Hora) {
                novoIntervalo = 60;
            } else if (checkedId == R.id.radioButton2Horas) {
                novoIntervalo = 120;
            }

            // Habilita os RadioButtons após a primeira interação
            if (primeiroAcesso) {
                radioButton30Min.setEnabled(true);
                radioButton1Hora.setEnabled(true);
                radioButton2Horas.setEnabled(true);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("KEY_PRIMEIRO_ACESSO", false); // Define que o usuário já interagiu
                editor.apply();
                Log.d(TAG, "✅ Primeiro acesso concluído: RadioButtons habilitados.");
            }

            // Salva a seleção no SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_INTERVALO, novoIntervalo);
            editor.apply();

            Log.d(TAG, "✅ Novo intervalo de lembrete salvo: " + novoIntervalo + " minutos");
        });

        // Exibe no Logcat o horário salvo do alarme
        Log.d(TAG, "🕒 Tela de lembretes aberta! Intervalo salvo: " + (intervaloMinutos == -1 ? "Nenhum definido" : intervaloMinutos + " min"));

        // Chama os métodos para inicializar os componentes e configurações
        inicializarComponentes();
        carregarConfiguracoes();
        configurarListeners();

        // Inicializa o banco de dados e define o listener
        dbHelper = new DatabaseHelper(this);
        dbHelper.setLembretesListener(this);

        // Solicitação e verificação de permissões de alarmes exatos
        solicitarPermissaoAlarme();
        verificarPermissaoAlarme();
        solicitarPermissaoAlarmesExatos();

        Log.d(TAG, "🟢 Tela de lembretes carregada com sucesso.");
    }

    private void inicializarComponentes() {
        timePicker = findViewById(R.id.timePicker);
        radioGroupFrequencia = findViewById(R.id.radioGroupFrequencia);
        editTextMensagem = findViewById(R.id.editTextMensagem);
        switchLembrete = findViewById(R.id.switch1lembete);
        botaoSalvarLembretes = findViewById(R.id.botaoSalvarLembretes);

        timePicker.setIs24HourView(true);
    }
    //**Solicita permissão para alarmes exatos (Android 12+)**
    private void solicitarPermissaoAlarme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "⚠️ Permissão de alarme exato não concedida! Abrindo configurações...");

                SharedPreferences prefs = getSharedPreferences("ConfigApp", MODE_PRIVATE);
                boolean jaSolicitou = prefs.getBoolean("PERMISSAO_ALARME_SOLICITADA", false);

                if (!jaSolicitou) {
                    // Abre a tela de configurações do aplicativo
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);

                    prefs.edit().putBoolean("PERMISSAO_ALARME_SOLICITADA", true).apply();
                    Toast.makeText(this, "Ative a permissão de Alarmes nas configurações do app.", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d(TAG, "✅ Permissão de alarme exato já concedida.");
            }
        }
    }

    //**Solicita explicitamente a permissão SCHEDULE_EXACT_ALARM no Android 12+**
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
    //**Verifica se a permissão foi concedida**
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
    private void configurarListeners() {
        botaoSalvarLembretes.setOnClickListener(view -> {
            Log.d(TAG, "💾 Botão SALVAR pressionado!");

            if (switchLembrete.isChecked()) {
                agendarLembretes();
                Toast.makeText(this, "✅ Lembretes ativados com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                cancelarNotificacoes();
                Toast.makeText(this, "🚫 Lembretes desativados!", Toast.LENGTH_SHORT).show();
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
        salvarConfiguracoes();
        Log.d(TAG, "✅ Lembrete agendado. Configurações salvas.");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "❌ Permissão de alarme exato não concedida! Alarme não será agendado.");
                return;
            }
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Obtém o intervalo salvo no SharedPreferences
        int intervaloMinutos = prefs.getInt(KEY_INTERVALO, 30); // Padrão: 30 minutos
        long intervaloMillis = TimeUnit.MINUTES.toMillis(intervaloMinutos);

        int hora = timePicker.getHour();
        int minuto = timePicker.getMinute();
        String mensagem = editTextMensagem.getText().toString();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.MINUTE, 1); // Ajusta para 1 minuto depois se o horário já passou
        }

        Intent intent = new Intent(this, LembreteReceiver.class);
        intent.setAction("beba.agua.LembreteReceiver");
        intent.putExtra("mensagem", mensagem);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                intervaloMillis,
                pendingIntent);

        salvarConfiguracoes(); // Certifica-se de salvar a configuração atual no SharedPreferences

        Log.d(TAG, "✅ Lembrete AGENDADO para " + hora + ":" + minuto + " e será repetido a cada " + intervaloMinutos + " minutos.");
    }

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
    private int obterFrequenciaSelecionada() {
        int radioSelecionado = radioGroupFrequencia.getCheckedRadioButtonId();
        if (radioSelecionado == R.id.radioButton30Min) return 30;
        else if (radioSelecionado == R.id.radioButton2Horas) return 120;
        else return 60;
    }

    // Método para Reagendar os Lembretes**
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

        // 🔹Define a frequência com base no RadioButton selecionado
        int intervaloMinutos = (radioSelecionado == R.id.radioButton30Min) ? 30 :
                (radioSelecionado == R.id.radioButton2Horas) ? 120 : 60;

        Log.d(TAG, "🔄 Reagendando lembrete para " + hora + ":" + minuto + " a cada " + intervaloMinutos + " minutos.");

        // Configura o horário do primeiro lembrete
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, 0);

        // Se a hora já passou, agendar para o próximo dia
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(context, LembreteReceiver.class);
        intent.setAction("beba.agua.LembreteReceiver");
        intent.putExtra("mensagem", mensagem);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 1, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
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
    @Override
    public void verificarEAtualizarLembretes() {
        Log.d(TAG, "🎯 Meta atingida! Cancelando lembretes até o próximo dia.");
        cancelarNotificacoes();
        Toast.makeText(this, "Meta diária concluída! Lembretes pausados até amanhã.", Toast.LENGTH_LONG).show();
    }
    //Salvar configurações
    private void salvarConfiguracoes() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Obtém o horário do TimePicker
        int hora = timePicker.getHour();
        int minuto = timePicker.getMinute();

        // Obtém o intervalo de lembrete salvo no RadioButton selecionado
        int intervaloMinutos = 30; // Padrão
        if (radioButton1Hora.isChecked()) {
            intervaloMinutos = 60;
        } else if (radioButton2Horas.isChecked()) {
            intervaloMinutos = 120;
        }

        // Salva os valores no SharedPreferences
        editor.putInt(KEY_HORA, hora);
        editor.putInt(KEY_MINUTO, minuto);
        editor.putInt(KEY_INTERVALO, intervaloMinutos);
        editor.apply(); // Aplica as alterações

        Log.d(TAG, "💾 Configurações salvas: Hora: " + hora + " Minuto: " + minuto + " Intervalo: " + intervaloMinutos + " minutos.");
    }
    @Override
    protected void onPause() {
        super.onPause();
        salvarConfiguracoes(); // Garante que as configurações são salvas ao sair da tela
        Log.d(TAG, "💾 Configurações salvas ao sair da tela.");
    }
}

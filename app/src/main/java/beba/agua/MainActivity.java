package beba.agua;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText editTextMeta;
    private Button botaoSalvarMeta;
    private TextView textoStatus;
    private ProgressBar barraProgresso;
    private Button botao100ml, botao150ml, botao250ml, botao500ml, botao600ml, botao750ml;

    private double consumoAtual = 0.0;
    private double metaDiaria = 0.0;

    // Nome da chave para armazenar a meta no SharedPreferences
    private static final String META_DIARIA_KEY = "metaDiaria";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtém referências aos elementos da interface do usuário
        editTextMeta = findViewById(R.id.editTextNumber);
        botaoSalvarMeta = findViewById(R.id.botaoSalvarMeta);
        textoStatus = findViewById(R.id.textoStatus);
        barraProgresso = findViewById(R.id.barraProgresso);
        botao100ml = findViewById(R.id.botao100ml);
        botao150ml = findViewById(R.id.botao150ml);
        botao250ml = findViewById(R.id.botao250ml);
        botao500ml = findViewById(R.id.botao500ml);
        botao600ml = findViewById(R.id.botao600ml);
        botao750ml = findViewById(R.id.botao750ml);

        // Carrega a meta diária do SharedPreferences
        carregarMetaDiaria();

        // Configura o listener para o botão "Salvar Meta"
        botaoSalvarMeta.setOnClickListener(view -> {
            salvarMetaDiaria();
        });

        // Configura os listeners para os botões de adição de água
        botao100ml.setOnClickListener(view -> adicionarConsumo(100));
        botao150ml.setOnClickListener(view -> adicionarConsumo(150));
        botao250ml.setOnClickListener(view -> adicionarConsumo(250));
        botao500ml.setOnClickListener(view -> adicionarConsumo(500));
        botao600ml.setOnClickListener(view -> adicionarConsumo(600));
        botao750ml.setOnClickListener(view -> adicionarConsumo(750));

        // Atualiza a interface inicial
        atualizarInterface();
    }

    // Salva a meta diária no SharedPreferences
    private void salvarMetaDiaria() {
        String metaString = editTextMeta.getText().toString();
        if (metaString.isEmpty()) {
            Toast.makeText(this, "Insira uma meta diária", Toast.LENGTH_SHORT).show();
            return;
        }

        metaDiaria = Double.parseDouble(metaString);

        // Salva a meta usando SharedPreferences
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
                .putFloat(META_DIARIA_KEY, (float) metaDiaria)
                .apply();

        atualizarInterface();
    }

    // Carrega a meta diária do SharedPreferences
    private void carregarMetaDiaria() {
        metaDiaria = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getFloat(META_DIARIA_KEY, 0);
    }

    // Adiciona a quantidade de água ao consumo atual
    private void adicionarConsumo(double quantidade) {
        consumoAtual += quantidade;
        atualizarInterface();
    }

    // Atualiza o texto de status e a barra de progresso
    private void atualizarInterface() {
        textoStatus.setText(String.format("%.0f ml / %.0f ml", consumoAtual, metaDiaria));

        if (metaDiaria > 0) {
            int progresso = (int) ((consumoAtual / metaDiaria) * 100);
            barraProgresso.setProgress(progresso);
        } else {
            barraProgresso.setProgress(0);
        }
    }
}

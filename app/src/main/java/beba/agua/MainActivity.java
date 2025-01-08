package beba.agua;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

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

        Button botaoAbrirLembretes = findViewById(R.id.botaoAbrirLembretes);
        botaoAbrirLembretes.setOnClickListener(view -> {
            // Cria um Intent para abrir a LembretesActivity
            Intent intent = new Intent(MainActivity.this, LembretesActivity.class);
            startActivity(intent);
        });

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

        // Abrir teclado numérico ao clicar no EditText
        editTextMeta.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(editTextMeta, InputMethodManager.SHOW_IMPLICIT);
        });

        // Configura o TextWatcher para o EditText da meta
        editTextMeta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Não é necessário implementar este método
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Não é necessário implementar este método
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Remove caracteres não numéricos, exceto ponto e vírgula
                String texto = s.toString().replaceAll("[^0-9.,]", "");
                // Substitui vírgula por ponto
                texto = texto.replace(",", ".");

                // Define o texto formatado no EditText
                editTextMeta.removeTextChangedListener(this);
                editTextMeta.setText(texto);
                editTextMeta.setSelection(texto.length());
                editTextMeta.addTextChangedListener(this);
            }
        });

        // Configura o listener para o botão "Salvar Meta"
        botaoSalvarMeta.setOnClickListener(view -> {
            salvarMetaDiaria();
        });

        // Desabilita os botões de adição de água inicialmente
        botao100ml.setEnabled(false);
        botao150ml.setEnabled(false);
        botao250ml.setEnabled(false);
        botao500ml.setEnabled(false);
        botao600ml.setEnabled(false);
        botao750ml.setEnabled(false);

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

        // Remove caracteres não numéricos, exceto ponto e vírgula
        metaString = metaString.replaceAll("[^0-9.,]", "");
        // Substitui vírgula por ponto
        metaString = metaString.replace(",", ".");

        try {
            metaDiaria = Double.parseDouble(metaString); // Salva a meta em ml

            // Salva a meta usando SharedPreferences
            getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
                    .putFloat(META_DIARIA_KEY, (float) metaDiaria)
                    .apply();

            // Desabilita o botão "Salvar Meta" após salvar
            botaoSalvarMeta.setEnabled(false);

            // Habilita os botões de adição de água após salvar a meta
            botao100ml.setEnabled(true);
            botao150ml.setEnabled(true);
            botao250ml.setEnabled(true);
            botao500ml.setEnabled(true);
            botao600ml.setEnabled(true);
            botao750ml.setEnabled(true);

            atualizarInterface();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido para a meta", Toast.LENGTH_SHORT).show();
        }
    }
    // Carrega a meta diária do SharedPreferences
    private void carregarMetaDiaria() {
        // ... (código existente) ...
    }
    // Adiciona a quantidade de água ao consumo atual
    private void adicionarConsumo(double quantidade) {
        consumoAtual += quantidade; // Atualiza o consumo primeiro

        // Verifica se a meta foi atingida
        if (consumoAtual >= metaDiaria) {
            Toast.makeText(MainActivity.this, "Parabéns, Meta concluída!", Toast.LENGTH_SHORT).show();
            // Desabilita os botões de adição de água
            botao100ml.setEnabled(false);
            botao150ml.setEnabled(false);
            botao250ml.setEnabled(false);
            botao500ml.setEnabled(false);
            botao600ml.setEnabled(false);
            botao750ml.setEnabled(false);
        }
        atualizarInterface();
    }

    // Atualiza o texto de status e a barra de progresso
    private void atualizarInterface() {
        textoStatus.setText(String.format(Locale.getDefault(), "%.0f ml / %.0f ml", consumoAtual, metaDiaria));

        if (metaDiaria > 0) {
            int progresso = (int) ((consumoAtual / metaDiaria) * 100);
            barraProgresso.setProgress(progresso);
        } else {
            barraProgresso.setProgress(0);
        }
    }
}
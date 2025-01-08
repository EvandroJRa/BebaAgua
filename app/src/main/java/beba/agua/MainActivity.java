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
    private Button botaoSalvarMeta, botao100ml, botao150ml, botao250ml, botao500ml, botao600ml, botao750ml;
    private TextView textoStatus;
    private ProgressBar barraProgresso;

    private double consumoAtual = 0.0;
    private double metaDiaria = 0.0;

    private static final String META_DIARIA_KEY = "metaDiaria";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializarComponentes();
        configurarListeners();
        carregarMetaDiaria();
        atualizarInterface();
    }

    private void inicializarComponentes() {
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

        Button botaoAbrirLembretes = findViewById(R.id.botaoAbrirLembretes);
        botaoAbrirLembretes.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LembretesActivity.class);
            startActivity(intent);
        });

        setBotaoEstado(false);
    }

    private void configurarListeners() {
        editTextMeta.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(editTextMeta, InputMethodManager.SHOW_IMPLICIT);
        });

        editTextMeta.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String texto = s.toString().replaceAll("[^\\d.,]", "").replace(",", ".");
                editTextMeta.removeTextChangedListener(this);
                editTextMeta.setText(texto);
                editTextMeta.setSelection(texto.length());
                editTextMeta.addTextChangedListener(this);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        botaoSalvarMeta.setOnClickListener(view -> salvarMetaDiaria());

        botao100ml.setOnClickListener(view -> adicionarConsumo(100));
        botao150ml.setOnClickListener(view -> adicionarConsumo(150));
        botao250ml.setOnClickListener(view -> adicionarConsumo(250));
        botao500ml.setOnClickListener(view -> adicionarConsumo(500));
        botao600ml.setOnClickListener(view -> adicionarConsumo(600));
        botao750ml.setOnClickListener(view -> adicionarConsumo(750));
    }

    private void salvarMetaDiaria() {
        String metaString = editTextMeta.getText().toString();
        if (metaString.isEmpty()) {
            Toast.makeText(this, "Insira uma meta diária", Toast.LENGTH_SHORT).show();
            return;
        }

        metaString = metaString.replaceAll("[^\\d.,]", "").replace(",", ".");
        try {
            metaDiaria = Double.parseDouble(metaString);

            getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
                    .putFloat(META_DIARIA_KEY, (float) metaDiaria)
                    .apply();

            botaoSalvarMeta.setEnabled(false);
            setBotaoEstado(true);
            atualizarInterface();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido para a meta", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarMetaDiaria() {
        metaDiaria = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getFloat(META_DIARIA_KEY, 0.0f);

        if (metaDiaria > 0) {
            botaoSalvarMeta.setEnabled(false);
            setBotaoEstado(true);
        }
    }

    private void adicionarConsumo(double quantidade) {
        consumoAtual += quantidade;

        if (consumoAtual >= metaDiaria) {
            Toast.makeText(this, "Parabéns, Meta concluída!", Toast.LENGTH_LONG).show();
            setBotaoEstado(false);
        }
        atualizarInterface();
    }

    private void atualizarInterface() {
        textoStatus.setText(String.format(Locale.getDefault(), "%.0f ml / %.0f ml", consumoAtual, metaDiaria));

        if (metaDiaria > 0) {
            int progresso = (int) ((consumoAtual / metaDiaria) * 100);
            barraProgresso.setProgress(progresso);
        } else {
            barraProgresso.setProgress(0);
        }
    }

    private void setBotaoEstado(boolean estado) {
        botao100ml.setEnabled(estado);
        botao150ml.setEnabled(estado);
        botao250ml.setEnabled(estado);
        botao500ml.setEnabled(estado);
        botao600ml.setEnabled(estado);
        botao750ml.setEnabled(estado);
    }
}

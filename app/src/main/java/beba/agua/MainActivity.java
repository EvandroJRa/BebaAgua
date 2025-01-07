package beba.agua;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Variáveis para os componentes da UI
    private ImageView iconapp;
    private TextView textoBemVindo;
    private TextView campoMetaDiaria;
    private EditText editTextNumber;
    private TextView textoConsumoAtual;
    private TextView textoStatus;
    private Button botaoRegistrar;
    private ProgressBar barraProgresso;

    // Outras variáveis
    private int metaDiaria = 0;
    private int consumoAtual = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referenciar os componentes da UI
        iconapp = findViewById(R.id.iconapp);
        textoBemVindo = findViewById(R.id.textoBemVindo);
        campoMetaDiaria = findViewById(R.id.campoMetaDiaria);
        editTextNumber = findViewById(R.id.editTextNumber);
        textoConsumoAtual = findViewById(R.id.textoConsumoAtual);
        textoStatus = findViewById(R.id.textoStatus);
        botaoRegistrar = findViewById(R.id.botaoRegistrar);
        barraProgresso = findViewById(R.id.barraProgresso);

        editTextNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        metaDiaria = Integer.parseInt(editTextNumber.getText().toString());
                    } catch (NumberFormatException e) {
                        metaDiaria = 0;
                        editTextNumber.setText("0");
                        //Toast.makeText(MainActivity.this, "Insira um valor válido", Toast.LENGTH_SHORT).show();
                    }
                    textoStatus.setText(consumoAtual + " ml / " + metaDiaria + " ml");
                    barraProgresso.setMax(metaDiaria);
                }
            }
        });

        botaoRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Registrar Consumo");
                builder.setMessage("Insira a quantidade de água consumida (em ml):");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            int quantidade = Integer.parseInt(input.getText().toString());

                            if (quantidade > 0) {
                                consumoAtual += quantidade;
                                textoStatus.setText(consumoAtual + " ml / " + metaDiaria + " ml");
                                barraProgresso.setProgress(consumoAtual);

                                if (consumoAtual >= metaDiaria) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Parabéns");
                                    builder.setMessage("Você atingiu a meta diária de consumo de água");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    AlertDialog dialogParabens = builder.create();
                                    dialogParabens.show();
                                }

                            } else {
                                Toast.makeText(MainActivity.this, "Insira um valor maior que zero", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(MainActivity.this, "Valor inválido", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
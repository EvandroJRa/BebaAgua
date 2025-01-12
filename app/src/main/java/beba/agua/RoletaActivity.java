package beba.agua;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RoletaActivity extends AppCompatActivity {
    private ImageView imageRoleta;
    private Button botaoGirar;
    private TextView textoDica;
    private Random random;
    private List<String> listaDicas;
    private static final String PREFS_NAME = "DicasPreferences";
    private static final String KEY_ULTIMAS_DICAS = "ultimas_dicas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roleta);

        imageRoleta = findViewById(R.id.imageRoleta);
        botaoGirar = findViewById(R.id.botaoGirar);
        textoDica = findViewById(R.id.textoDica);
        random = new Random();

        carregarDicas();

        botaoGirar.setOnClickListener(view -> girarRoleta());
    }
    private void carregarDicas() {
        listaDicas = new ArrayList<>();
        listaDicas.add("Beba um copo de água ao acordar para ativar seu metabolismo.");
        listaDicas.add("Evite bebidas açucaradas, elas podem causar desidratação.");
        listaDicas.add("Sempre leve uma garrafa d'água com você durante o dia.");
        listaDicas.add("Hidrate-se antes de dormir para um sono mais saudável.");
        listaDicas.add("A água melhora a digestão e reduz a retenção de líquidos.");
        listaDicas.add("Beber água antes das refeições ajuda no controle do apetite.");
        listaDicas.add("A hidratação é essencial para o funcionamento do cérebro.");
        listaDicas.add("Se sentir sede, seu corpo já está desidratado.");
        listaDicas.add("Mantenha um cronograma de ingestão de água.");
        listaDicas.add("Frutas e vegetais também são ótimas fontes de hidratação.");

        // Você pode adicionar mais 90 dicas aqui!
    }

    private void girarRoleta() {
        int numeroSorteado = random.nextInt(10);
        int anguloFinal = (numeroSorteado * 36) + 720; // Gira a roleta e para no número sorteado

        ObjectAnimator animacao = ObjectAnimator.ofFloat(imageRoleta, "rotation", 0f, anguloFinal);
        animacao.setDuration(2000);
        animacao.setInterpolator(new DecelerateInterpolator());
        animacao.start();

        botaoGirar.setEnabled(false);

        imageRoleta.postDelayed(() -> {
            botaoGirar.setEnabled(true);
            String dica = obterDicaValida();
            textoDica.setText(dica);
            salvarDicaNoHistorico(dica);
        }, 2500);
    }

    private String obterDicaValida() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        List<String> dicasRecentes = new ArrayList<>(prefs.getStringSet(KEY_ULTIMAS_DICAS, new HashSet<>()));

        Collections.shuffle(listaDicas);

        for (String dica : listaDicas) {
            if (!dicasRecentes.contains(dica)) {
                return dica;
            }
        }

        return listaDicas.get(0);
    }

    private void salvarDicaNoHistorico(String dica) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> dicasRecentes = new HashSet<>(prefs.getStringSet(KEY_ULTIMAS_DICAS, new HashSet<>()));
        dicasRecentes.add(dica);

        if (dicasRecentes.size() > 10) { // Mantém um histórico de apenas 10 dicas
            Iterator<String> iterator = dicasRecentes.iterator();
            iterator.next();
            iterator.remove();
        }

        editor.putStringSet(KEY_ULTIMAS_DICAS, dicasRecentes);
        editor.apply();
    }
}

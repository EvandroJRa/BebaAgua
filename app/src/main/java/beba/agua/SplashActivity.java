package beba.agua;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000; // Tempo de exibição em milissegundos (2.5 segundos)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar();
        setContentView(R.layout.activity_splash);

        // Aguarda um tempo antes de abrir a MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish(); // Fecha a SplashActivity para não voltar nela ao pressionar "Voltar"
        }, SPLASH_TIME_OUT);
    }
}

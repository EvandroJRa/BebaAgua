//package beba.agua;
//
//import android.content.Context;
//import android.content.Intent;
//import android.view.GestureDetector;
//import android.view.MotionEvent;
//import androidx.annotation.NonNull;
//import androidx.core.view.GestureDetectorCompat;
//
//public class GestureListener extends GestureDetector.SimpleOnGestureListener {
//
//    private final Context context;
//
//    public GestureListener(Context context) {
//        this.context = context;
//    }
//
//    private static final int SWIPE_THRESHOLD = 100;  // Distância mínima do swipe
//    private static final int SWIPE_VELOCITY_THRESHOLD = 100;  // Velocidade mínima do swipe
//
//    @Override
//    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
//        float diffX = e2.getX() - e1.getX();
//        float diffY = e2.getY() - e1.getY();
//
//        if (Math.abs(diffX) > Math.abs(diffY)) {
//            // Gesto Horizontal
//            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//                if (diffX > 0) {
//                    abrirHistorico(); // Swipe para a direita (→)
//                } else {
//                    abrirLembretes(); // Swipe para a esquerda (←)
//                }
//                return true;
//            }
//        } else {
//            // Gesto Vertical
//            if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
//                if (diffY > 0) {
//                    abrirConfiguracoes(); // Swipe para baixo (↓)
//                } else {
//                    abrirRoleta(); // Swipe para cima (↑)
//                }
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private void abrirHistorico() {
//        Intent intent = new Intent(context, HistoricoActivity.class);
//        context.startActivity(intent);
//    }
//
//    private void abrirLembretes() {
//        Intent intent = new Intent(context, LembretesActivity.class);
//        context.startActivity(intent);
//    }
//
//    private void abrirRoleta() {
//        Intent intent = new Intent(context, RoletaActivity.class);
//        context.startActivity(intent);
//    }
//
//    private void abrirConfiguracoes() {
//        Intent intent = new Intent(context, ConfiguracoesActivity.class);
//        context.startActivity(intent);
//    }
//}

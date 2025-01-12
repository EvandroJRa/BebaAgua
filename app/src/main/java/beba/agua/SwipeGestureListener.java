package beba.agua;

import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.core.view.GestureDetectorCompat;

public class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private final Context context;
    private final Class<?> esquerdaActivity;
    private final Class<?> direitaActivity;

    public SwipeGestureListener(Context context, Class<?> esquerdaActivity, Class<?> direitaActivity) {
        this.context = context;
        this.esquerdaActivity = esquerdaActivity;
        this.direitaActivity = direitaActivity;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffX = e2.getX() - e1.getX();

        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffX > 0) {
                // Swipe para a direita (volta para Activity definida)
                if (direitaActivity != null) {
                    context.startActivity(new Intent(context, direitaActivity));
                }
            } else {
                // Swipe para a esquerda (vai para a próxima Activity)
                if (esquerdaActivity != null) {
                    context.startActivity(new Intent(context, esquerdaActivity));
                }
            }
            return true;
        }
        return false;
    }
}

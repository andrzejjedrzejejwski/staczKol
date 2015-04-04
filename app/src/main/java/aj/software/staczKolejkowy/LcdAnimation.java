package aj.software.staczKolejkowy;

import android.os.AsyncTask;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aj.software.staczKolejkowy.core.App;

public class LcdAnimation extends AsyncTask<String, String, Void> {

    private int index = 0;
    private TextView lcd;
    private String currentNumber;
    private App app;

    private static final Logger LOG = LoggerFactory.getLogger(LcdAnimation.class.getSimpleName());

    public LcdAnimation(App app, TextView lcd, String currentNumber) {
        this.lcd = lcd;
        this.currentNumber = currentNumber;
        this.app = app;
    }

    @Override
    protected void onPreExecute() {
        app.setLcdAnimating(true);
        lcd.setText("");
    }

    @Override
    protected Void doInBackground(String... params) {
        sleep(1250);
        for(int i = 0; i<currentNumber.length(); i++) {
            publishProgress(currentNumber.substring(index, index + 1));
            index++;
            sleep(350);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        lcd.setText(lcd.getText() + values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        sleep(300);
        final ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.03f, 1.0f, 1.03f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(75);
        scaleAnimation.setInterpolator(new DecelerateInterpolator());
        scaleAnimation.setRepeatCount(9);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        app.setLcdAnimating(false);
        lcd.startAnimation(scaleAnimation);
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

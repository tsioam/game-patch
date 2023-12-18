package app.cloudgame.web.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.webkit.ValueCallback;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.Locale;

import app.cloudgame.web.Configuration;
import app.cloudgame.web.pages.SettingsPageKt;

public class WebContainer extends LinearLayout {

    private float currentMouseX = 0;
    private float currentMouseY = 0;
    private float mouseSpeed = 1.f;
    private boolean enableUserMouseSpeed;

    public void setWebView(GameView webView) {
        this.webView = webView;
    }

    private GameView webView;

    public WebContainer(Context context) {
        super(context);
        initParams();
    }

    public WebContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initParams();
    }

    public WebContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams();
    }

    private void initParams() {
        mouseSpeed = SettingsPageKt.getMouseSpeed(Configuration.getConfiguration().getMouseSpeedLevel());
        enableUserMouseSpeed = mouseSpeed != 1.f;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (((event.getSource() & InputDevice.SOURCE_MOUSE)  != 0) && !hasPointerCapture()) {
            currentMouseX = event.getX();
            currentMouseY = event.getY();
        }
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchCapturedPointerEvent(MotionEvent event) {
        if (hasPointerCapture()) {
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE || event.getActionMasked() == MotionEvent.ACTION_HOVER_MOVE) {
                if (enableUserMouseSpeed) {
                    float x = event.getX() * mouseSpeed;
                    float y = event.getY() * mouseSpeed;
                    String script = String.format(Locale.ENGLISH, "window.EVAL_MOVEMENT_CB(%f,%f)", x, y);
                    evaluateJavascript(script, null);
                } else {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    String script = String.format(Locale.ENGLISH, "window.EVAL_MOVEMENT_CB(%d,%d)", x, y);
                    evaluateJavascript(script, null);
                }
            } else {
                event.setSource(InputDevice.SOURCE_MOUSE);
                event.setLocation(currentMouseX, currentMouseY);
                return super.dispatchGenericPointerEvent(event);
            }
            return true;
        }
        return super.dispatchCapturedPointerEvent(event);
    }

    @Override
    protected boolean dispatchGenericPointerEvent(MotionEvent event) {
        return super.dispatchGenericPointerEvent(event);
    }

    @Override
    public void onPointerCaptureChange(boolean hasCapture) {
        super.onPointerCaptureChange(hasCapture);
        if (webView == null) {
            return;
        }
        JSBridge jsBridge = webView.getJsBridge();
        if (jsBridge == null) {
            return;
        }
        String script = String.format("window.POINTER_LOCK_CHANGE_CB(%s, %s)", hasCapture? "true": "false", jsBridge.getGson().toJson(jsBridge.getLastLockEleId()));
        evaluateJavascript(script, null);
    }

    public void notifyLockStateWithCallback(boolean hasCapture, ValueCallback<String> cb) {
        JSBridge jsBridge = webView.getJsBridge();
        String script = String.format("window.POINTER_LOCK_CHANGE_CB(%s, %s)", hasCapture? "true": "false", jsBridge.getGson().toJson(jsBridge.getLastLockEleId()));
        evaluateJavascript(script, cb);
    }

    private void evaluateJavascript(String source, ValueCallback<String> cb) {
        if (webView != null) {
            Log.d("eval", source);
            webView.evaluateJavascript(source, cb);
        }
    }
}

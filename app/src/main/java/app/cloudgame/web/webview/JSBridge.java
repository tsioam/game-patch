package app.cloudgame.web.webview;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;

public class JSBridge {

    private static final String TAG = "GameViewJsBridge";

    private final GameView mWebView;
    private final Gson mGson;
    private static final String SUCCESS = "{\"code\": 0}";
    private String lastLockEleId = "";

    public JSBridge(GameView webView) {
        this.mWebView = webView;
        this.mGson = new Gson();
    }

    @JavascriptInterface
    public void evalMethod(String method, String params, String callbackId) {
        if ("toast".equals(method)) {
            runInUiThread(() -> {
                Toast.makeText(mWebView.getContext(), params, Toast.LENGTH_LONG).show();
                evalCallback(callbackId, "ok");
            });
        } else if ("requestPointerLock".equals(method)) {
            requestPointerLock(callbackId, params);
        } else if ("exitPointerLock".equals(method)) {
            exitPointerLock(callbackId, params);
        }
    }

    private void evalCallback(String callbackId, String result) {
        if (TextUtils.isEmpty(callbackId)) {
            return;
        }
        String script = String.format("window.CG_EVAL_CALLBACK(%s, %s)", mGson.toJson(callbackId), mGson.toJson(result));
        mWebView.evaluateJavascript(script, null);
    }

    private void runInUiThread(Runnable runnable) {
        mWebView.getContainer().post(runnable);
    }

    private void requestPointerLock(String callbackId, String eleId) {
        Log.d(TAG, "request pointer lock");
        runInUiThread(() -> {
            lastLockEleId = eleId;
            mWebView.getContainer().requestPointerCapture();
            evalCallback(callbackId, SUCCESS);
        });
    }

    private void exitPointerLock(String callbackId, String eleId) {
        Log.d(TAG, "request exit pointer lock");
        runInUiThread(() -> {
            mWebView.getContainer().releasePointerCapture();
            evalCallback(callbackId, SUCCESS);
        });
    }

    public String getLastLockEleId() {
        return lastLockEleId;
    }

    public Gson getGson() {
        return mGson;
    }
}

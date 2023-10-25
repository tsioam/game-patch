package app.cloudgame.web;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Choreographer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;
import android.window.OnBackInvokedDispatcher;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Locale;

import app.cloudgame.web.webview.GameView;
import app.cloudgame.web.webview.IWebPageCallback;
import app.cloudgame.web.webview.UtilsKt;

public class WebActivity extends Activity implements IWebPageCallback {

    public static final String START_SCRIPT = "page_start_scripts";
    public static final String LOADED_SCRIPT = "page_load_scripts";
    public static final String URL = "url";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupActivity();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadWeb();
    }

    private void enableImmersiveMode() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void setupActivity() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        window.setAttributes(layoutParams);
        enableImmersiveMode();
    }

    private void loadWeb() {
        GameView view = findViewById(R.id.mouse_view);
        if (view == null) {
            return;
        }
        webView = view;
        view.setWebCallback(this);
        view.setOnClickListener(l -> {
            view.requestPointerCapture();
        });
        String url = getIntent().getStringExtra("url");
        if (TextUtils.isEmpty(url)) {
            url = Configuration.DEFAULT_URL;
        }
        view.loadUrl(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onWebPageLoadEnd(WebView webView, String url) {
        evalScripts(START_SCRIPT);
    }

    @Override
    public void onWebPageLoadStart(WebView webView, String url) {
        evalScripts(LOADED_SCRIPT);
    }

    private void evalScripts(String lifeCycleKey) {
        String scripts = getIntent().getStringExtra(lifeCycleKey);
        if (!TextUtils.isEmpty(scripts)) {
            Gson gson = new Gson();
            try {
                List<String> scriptList = gson.fromJson(scripts, new TypeToken<List<String>>(){}.getType());
                for (String script : scriptList) {
                    String scriptData = Configuration.getConfiguration().getScript(script);
                    if (!TextUtils.isEmpty(scriptData)) {
                        webView.evaluateJavascript(scriptData, null);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
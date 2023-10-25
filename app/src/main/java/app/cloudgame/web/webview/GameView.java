package app.cloudgame.web.webview;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.WeakHashMap;

import app.cloudgame.web.Configuration;

public class GameView extends WebView {

    private boolean hasSetup = false;
    private JSBridge jsBridge;
    private float currentMouseX = 0;
    private float currentMouseY = 0;
    private IWebPageCallback webCallback;

    public GameView(@NonNull Context context) {
        super(context);
        setup();
    }

    public GameView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public GameView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public GameView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }

    public void setWebCallback(IWebPageCallback webCallback) {
        this.webCallback = webCallback;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setup() {
        if (hasSetup) {
            return;
        }
        hasSetup = true;
        setFocusableInTouchMode(true);
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);

        setDesktopMode(true);
        setWebViewClient(new GameWebViewClient());
        setWebChromeClient(new ChromeWebViewClient());

        jsBridge = new JSBridge(this);
        addJavascriptInterface(jsBridge, "CG_BRIDGE");
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (((event.getSource() & InputDevice.SOURCE_MOUSE)  != 0) && event.getActionMasked() == MotionEvent.ACTION_MOVE && !hasPointerCapture()) {
            currentMouseX = event.getX();
            currentMouseY = event.getY();
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onCapturedPointerEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE || motionEvent.getActionMasked() == MotionEvent.ACTION_HOVER_MOVE) {
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            String script = String.format(Locale.ENGLISH, "window.EVAL_MOVEMENT_CB(%d,%d)", x, y);
            evaluateJavascript(script, null);
        } else {
            motionEvent.setSource(InputDevice.SOURCE_MOUSE);
            motionEvent.setLocation(currentMouseX, currentMouseY);
            return super.onGenericMotionEvent(motionEvent);
        }
        return true;
    }

    @Override
    public void onPointerCaptureChange(boolean hasCapture) {
        super.onPointerCaptureChange(hasCapture);
        String script = String.format("window.POINTER_LOCK_CHANGE_CB(%s, %s)", hasCapture? "true": "false", jsBridge.getGson().toJson(jsBridge.getLastLockEleId()));
        String TAG = "GameView";
        Log.d(TAG, script);
        evaluateJavascript(script, null);
    }

    public void setDesktopMode(boolean enabled) {
        getSettings().setUserAgentString(Configuration.getConfiguration().getUserAgent());
        getSettings().setUseWideViewPort(enabled);
        getSettings().setLoadWithOverviewMode(enabled);
        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(false);

        setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        setScrollbarFadingEnabled(false);
        setInitialScale(1);
        if (isAttachedToWindow()) {
            reload();
        }
    }

    private static class GameWebViewClient extends WebViewClient {

        private GameWebViewClient() {
            super();
            setWebContentsDebuggingEnabled(true);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (view instanceof GameView) {
                GameView gameView = (GameView) view;
                if (gameView.webCallback != null) {
                    gameView.webCallback.onWebPageLoadStart(view, url);
                }
            }
            String[] scripts = Configuration.getConfiguration().getPageStartScripts();
            for (String script: scripts) {
                view.evaluateJavascript(script, null);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (view instanceof GameView) {
                GameView gameView = (GameView) view;
                if (gameView.webCallback != null) {
                    gameView.webCallback.onWebPageLoadEnd(view, url);
                }
            }
        }
    }

    private static class ChromeWebViewClient extends WebChromeClient {
        private final WeakHashMap<WebView,Dialog> mDialogWeakMap = new WeakHashMap<>();

        @SuppressLint("SetJavaScriptEnabled")
        private static void setupWindowWebView(WebView webView, Dialog dialog) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    String scheme =request.getUrl().getScheme();
                    if ("https".equals(scheme) || "http".equals(scheme)) {
                        return false;
                    }
                    return super.shouldOverrideUrlLoading(view, request);
                }
            });

            webView.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onCloseWindow(WebView window) {
                    if (window == webView && dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            if (resultMsg != null && resultMsg.obj instanceof WebView.WebViewTransport) {
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                WebView webView = new WebView(view.getContext());
                Dialog dialog = new Dialog(view.getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                setupWindowWebView(webView, dialog);
                mDialogWeakMap.put(webView, dialog);
                dialog.setContentView(webView);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                Window window = dialog.getWindow();
                if (window != null) {
                    lp.copyFrom(dialog.getWindow().getAttributes());
                    DisplayMetrics dm = view.getContext().getResources().getDisplayMetrics();
                    lp.width = (int) (dm.widthPixels * 0.7);
                    lp.height = (int) (dm.heightPixels * 0.9);
                    lp.horizontalMargin = 0;
                    lp.verticalMargin = 0;
                    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
                }
                transport.setWebView(webView);
                resultMsg.sendToTarget();
                dialog.show();
                if (window != null) {
                    window.setAttributes(lp);
                }
            }
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            Dialog dialog = mDialogWeakMap.get(window);
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }
}

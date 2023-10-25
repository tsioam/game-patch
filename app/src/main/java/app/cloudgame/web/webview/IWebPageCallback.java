package app.cloudgame.web.webview;

import android.webkit.WebView;

public interface IWebPageCallback {
    void onWebPageLoadEnd(WebView webView, String url);
    void onWebPageLoadStart(WebView webView, String url);
}

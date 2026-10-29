package com.jinhj.reading;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** GitHub Pages의 독서 체크리스트 웹앱을 그대로 띄우고, 위젯과 데이터를 주고받는 브리지 */
public class MainActivity extends Activity {
    static final String URL = "https://jinhj423.github.io/reading-tracker/";

    private WebView web;
    private String pendingAction;
    private boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        web.addJavascriptInterface(new Bridge(), "AndroidWidget");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                loaded = true;
                runPending();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri u = req.getUrl();
                if (u.toString().startsWith(URL)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                return true;
            }
        });
        setContentView(web);

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        ReminderReceiver.schedule(this);

        pendingAction = getIntent().getStringExtra("action");
        web.loadUrl(URL + "#today");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        pendingAction = intent.getStringExtra("action");
        if (loaded) runPending();
    }

    private void runPending() {
        String a = pendingAction;
        pendingAction = null;
        String arg = a == null ? "" : a;
        // 위젯 버튼(start/stop) 처리 + 앱으로 돌아올 때 화면/위젯 갱신
        web.evaluateJavascript("window.nativeAction && window.nativeAction('" + arg + "')", null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loaded) web.evaluateJavascript("window.nativeAction && window.nativeAction('')", null);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }

    class Bridge {
        @JavascriptInterface
        public void update(String json) {
            getSharedPreferences("widget", MODE_PRIVATE).edit().putString("data", json).apply();
            ReadingWidget.refreshAll(MainActivity.this);
        }

        @JavascriptInterface
        public String version() {
            return "2";
        }

        /** 매일 알림 시간 (0~23시, -1이면 끔) */
        @JavascriptInterface
        public void setReminder(int hour) {
            getSharedPreferences("widget", MODE_PRIVATE).edit().putInt("remindHour", hour).apply();
            ReminderReceiver.schedule(MainActivity.this);
        }
    }
}

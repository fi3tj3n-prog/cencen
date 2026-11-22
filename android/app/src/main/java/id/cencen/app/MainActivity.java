package id.cencen.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> fileCb;
    private static final int PICK = 7;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.parseColor("#1a0f1f"));
        getWindow().setNavigationBarColor(Color.parseColor("#1a0f1f"));
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#1a0f1f"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fileCb != null) fileCb.onReceiveValue(null);
                fileCb = cb;
                try { startActivityForResult(p.createIntent(), PICK); }
                catch (Exception e) { fileCb = null; return false; }
                return true;
            }
        });
        setContentView(web);
        if (saved != null) web.restoreState(saved);
        else web.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == PICK && fileCb != null) {
            fileCb.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data));
            fileCb = null;
        } else super.onActivityResult(req, res, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); web.saveState(out); }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("(function(){var o=document.querySelector('.sheet.open');if(o){o.classList.remove('open');return 1}var h=document.querySelector('.view.on');if(h&&h.id!=='v-home'){document.querySelector('[data-go=home]').click();return 1}return 0})()",
            r -> { if (!"1".equals(r)) MainActivity.super.onBackPressed(); });
    }
}

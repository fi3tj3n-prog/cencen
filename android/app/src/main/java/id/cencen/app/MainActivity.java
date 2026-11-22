package id.cencen.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.widget.Toast;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> fileCb;
    private static final int PICK = 7, SAVE = 8;
    private String pendingText;

    public class Bridge {
        @JavascriptInterface
        public void saveFile(String name, String text) {
            runOnUiThread(() -> {
                pendingText = text;
                Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("application/json");
                i.putExtra(Intent.EXTRA_TITLE, name);
                try { startActivityForResult(i, SAVE); }
                catch (Exception e) { Toast.makeText(MainActivity.this, "Tidak bisa membuka penyimpanan", Toast.LENGTH_LONG).show(); }
            });
        }
    }

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
        web.addJavascriptInterface(new Bridge(), "CencenAndroid");
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
        } else if (req == SAVE) {
            if (res == RESULT_OK && data != null && data.getData() != null && pendingText != null) {
                try (OutputStream os = getContentResolver().openOutputStream(data.getData())) {
                    os.write(pendingText.getBytes(StandardCharsets.UTF_8));
                    Toast.makeText(this, "Cadangan tersimpan", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Gagal menyimpan cadangan", Toast.LENGTH_LONG).show();
                }
            }
            pendingText = null;
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

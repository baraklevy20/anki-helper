package levy.barak.ankihelper;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;

public class ForvoActivity extends Activity {
    public class DownloadHandler implements DownloadListener {
        WebView forvoWebView;

        public DownloadHandler(WebView forvoWebView) {
            this.forvoWebView = forvoWebView;
        }

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            // Delete existing file
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                    "anki_helper/anki_helper_sound" + TranslateActivity.currentWord + ".mp3");

            if (file.exists()) {
                file.delete();
            }

            // Download it
            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(url));
            request.addRequestHeader("Cookie", CookieManager.getInstance().getCookie("forvo.com"));
            request.allowScanningByMediaScanner();
            //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // vibrates when the download is completed
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "anki_helper/anki_helper_sound" + TranslateActivity.currentWord + ".mp3");
            DownloadManager dm = (DownloadManager) forvoWebView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            dm.enqueue(request);
            TranslateActivity.currentWord++;
            getSharedPreferences(TranslateActivity.PREFERENCES, MODE_PRIVATE).
                    edit().
                    putInt(TranslateActivity.PREFERENCES_CURRENT_WORD, TranslateActivity.currentWord).
                    commit();
            startActivity(new Intent(forvoWebView.getContext(), TranslateActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forvo);

        final WebView forvoWebView = findViewById(R.id.forvoWebView);

        forvoWebView.setDownloadListener(new DownloadHandler(forvoWebView));



        WebSettings settings = forvoWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        forvoWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString().toLowerCase();
                String word = TranslateActivity.getGermanWordWithoutPrefix().toLowerCase();

                Log.i("requestedUrl", request.getUrl().toString());
                return !(
                        url.endsWith("de/" + word + "/") ||
                        url.endsWith("/login/") ||
                        url.contains("download") ||
                        url.endsWith("/word/" + word + "/")
                );
            }
        });

        forvoWebView.loadUrl("https://forvo.com/word/de/" + TranslateActivity.getGermanWordWithoutPrefix() + "/");
        CookieManager.getInstance().setAcceptCookie(true);
    }
}

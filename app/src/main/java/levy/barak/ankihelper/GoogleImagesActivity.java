package levy.barak.ankihelper;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class GoogleImagesActivity extends Activity {
    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void catchHref(String href) throws UnsupportedEncodingException {
            String[] parts = href.split("\\?");
            String[] attributes = href.split("&");
            String imageUrl = java.net.URLDecoder.decode(attributes[0].split("=")[1], "UTF-8");
            TranslateActivity.getCorrectPreferences(mContext).edit().putString(GoogleTranslateActivity.SHARED_IMAGE_SRC, imageUrl).commit();

            Toast.makeText(mContext, TranslateActivity.getCorrectPreferences(mContext).getString(GoogleTranslateActivity.SHARED_IMAGE_SRC, ""), Toast.LENGTH_SHORT).show();

            startActivity(new Intent(mContext, ForvoActivity.class));

            new Thread(() -> {
                // Delete existing file
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        "/anki_helper/anki_helper_image" + TranslateActivity.currentWord + imageUrl.substring(imageUrl.lastIndexOf('.')));

                if (file.exists()) {
                    file.delete();
                }

                // Download it
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
                request.allowScanningByMediaScanner();
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        "anki_helper/anki_helper_image" + TranslateActivity.currentWord + imageUrl.substring(imageUrl.lastIndexOf('.')));
                DownloadManager dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                dm.enqueue(request);
            }).start();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_images);

        final WebView googleImagesWebView = findViewById(R.id.googleImagesWebView);

        googleImagesWebView.addJavascriptInterface(new WebAppInterface(this), "Android");

        WebSettings settings = googleImagesWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        googleImagesWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                googleImagesWebView.evaluateJavascript(
                        "(" +
                                "   function() {" +
                                "       document.addEventListener('click', function(e) {\n" +
                                "           Android.catchHref(e.target.parentNode.getAttribute('href'));\n" +
                                "       });" +
                                "   }" +
                                ")();",
                        null);
            }
        });

        googleImagesWebView.loadUrl(
                "http://www.google.de/search?q=" +
                TranslateActivity.getGermanWordWithoutPrefix() +
                "&hl=de&tbo=d&site=imghp&tbm=isch"
        );


    }
}

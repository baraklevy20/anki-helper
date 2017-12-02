package levy.barak.ankihelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class GoogleTranslateActivity extends Activity {
    public static final String GERMAN_WORD = "GERMAN_WORD";
    public static final String SHARED_IMAGE_SRC = "IMAGE_SRC";
    public static final String SHARED_IPA_SRC = "IPA_VALUE";

    public class WebAppInterface {
        private Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void catchGermanWord(String germanWord) {
            TranslateActivity.germanWord = germanWord;
            TranslateActivity.getCorrectPreferences(mContext).edit().putString(GERMAN_WORD, germanWord).commit();

            startActivity(new Intent(mContext, GoogleImagesActivity.class));

            new Thread(() -> {
                final StringBuilder builder = new StringBuilder();

                try {
                    Document doc = Jsoup.connect("https://www.collinsdictionary.com/dictionary/german-english/" + TranslateActivity.getGermanWordWithoutPrefix().toLowerCase()).get();
                    Elements pronounciations = doc.select(".pron");
                    String pronounciation = pronounciations.first().text();
                    pronounciation = pronounciation.substring(1, pronounciation.length() - 1);
                    TranslateActivity.getCorrectPreferences(mContext).edit().putString(SHARED_IPA_SRC, pronounciation).commit();
                } catch (IOException e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }
            }).start();

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_translate);

        final WebView googleTranslateEditView = findViewById(R.id.googleTranslateWebView);
        googleTranslateEditView.addJavascriptInterface(new WebAppInterface(this), "Android");

        WebSettings settings = googleTranslateEditView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        googleTranslateEditView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                googleTranslateEditView.evaluateJavascript(
                        "(" +
                                "   function() {" +
                                "       document.addEventListener('click', function(e) {\n" +
                                "       var currentNode = e.target;" +
                                "       while (currentNode.className.split(' ').indexOf('gt-baf-entry-clickable') < 0)" +
                                "       currentNode = currentNode.parentNode;" +
                                // childNodes[0] = der/die/das
                                "       var result = currentNode.firstChild.firstChild.childNodes[0].innerHTML;" +
                                "       if (currentNode.firstChild.firstChild.childNodes.length == 2) result += ' ' + currentNode.firstChild.firstChild.childNodes[1].innerHTML;" +
                                "           Android.catchGermanWord(result);\n" +
                                "       });" +
                                "   }" +
                                ")();",
                        null);
            }
        });

        googleTranslateEditView.loadUrl("https://translate.google.com/m/translate#en/de/" + TranslateActivity.englishWord);
    }
}

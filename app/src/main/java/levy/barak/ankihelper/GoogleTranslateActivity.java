package levy.barak.ankihelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import levy.barak.ankihelper.utils.FileUtils;
import levy.barak.ankihelper.utils.GermanUtils;

public class GoogleTranslateActivity extends Activity {
    public class WebAppInterface {
        private GoogleTranslateActivity mContext;

        WebAppInterface(GoogleTranslateActivity c) {
            mContext = c;
        }

        @JavascriptInterface
        public void catchGermanWord(String germanWord, String type) {
            AnkiHelperApplication.currentWord.type = GermanUtils.WordCategory.valueOf(type.substring(0, type.length() - 1).toUpperCase()); // Remove the '\n'

            // Set the german word to either capitalized or lower cased
            AnkiHelperApplication.currentWord.germanWord = AnkiHelperApplication.currentWord.type.isUppercase() ?
                    germanWord.substring(0, 1).toUpperCase() + germanWord.substring(1) :
                    germanWord.toLowerCase();

            startActivity(new Intent(mContext, GoogleImagesActivity.class));

            new Thread(() -> {
                // Get additional information about the word
                try {
                    getWordInformationFromWiki(TranslateActivity.getGermanWordWithoutPrefix());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        }

        public void getWordInformationFromWiki(String word) throws IOException {
            Connection connection = Jsoup.connect("https://de.wiktionary.org/wiki/" + word);
            Document doc = connection.get();

            Elements ipas = doc.select(".ipa");

            if (ipas.size() == 0) {
                mContext.runOnUiThread(() -> Toast.makeText(mContext, "Couldn't find an IPA. Wrong word perhaps?", Toast.LENGTH_LONG).show());
                return;
            }

            AnkiHelperApplication.currentWord.ipa = ipas.first().text();

            Elements wordInASentences = doc.select("[title=Verwendungsbeispielsätze]").first().nextElementSibling().children();

            for (int i = 0; i < wordInASentences.size(); i++) {
                AnkiHelperApplication.currentWord.wordInASentences.add(wordInASentences.get(i).html());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_translate);

        final WebView googleTranslateEditView = (WebView) findViewById(R.id.googleTranslateWebView);
        googleTranslateEditView.addJavascriptInterface(new WebAppInterface(this), "Android");

        WebSettings settings = googleTranslateEditView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        googleTranslateEditView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.i("requestedUrl", request.getUrl().toString());
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                //mIsInTranslate = true;
                googleTranslateEditView.evaluateJavascript(FileUtils.getFileContent(googleTranslateEditView.getContext(), "googleTranslate.js"), null);
            }
        });

        googleTranslateEditView.loadUrl("https://translate.google.com/m/translate#en/de/" + AnkiHelperApplication.currentWord.englishWord);
    }
}

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
            AnkiHelperApplication.currentWord.germanWord = germanWord;
            AnkiHelperApplication.currentWord.type = GermanUtils.translateTypes(type.substring(0, type.length() - 1)); // Remove the '\n'
            startActivity(new Intent(mContext, GoogleImagesActivity.class));

            new Thread(() -> {
                final StringBuilder builder = new StringBuilder();

                // Search for the word
                try {
                    getWordInfoFromWiki(TranslateActivity.getGermanWordWithoutPrefix());
                } catch (IOException e) {
                    // If there are issues, try the lower case version
                    try {
                        getWordInfoFromWiki(TranslateActivity.getGermanWordWithoutPrefix().toLowerCase());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }).start();

        }

        public void getWordInfoFromWiki(String word) throws IOException {
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

//            @Override
//            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//                //Log.i("requestedUrl23", request.getUrl());
//
//                Log.i("requestedUrl2", request.getUrl().toString());
////                if (request.getUrl().toString().startsWith("https://translate.google.com/translate_a/single")) {
////                    if (!mIsInTranslate) {
////                        mIsInTranslate = true;
////                    }
////                    else {
////                        return new WebResourceResponse("", "", null);
////                    }
////                }
//
//                if (mIsInTranslate) {
//                    return new WebResourceResponse("", "", null);
//                }
//
//                return null;
//            }

            public void onPageFinished(WebView view, String url) {
                //mIsInTranslate = true;
                googleTranslateEditView.evaluateJavascript(FileUtils.getFileContent(googleTranslateEditView.getContext(), "googleTranslate.js"), null);
            }
        });

        googleTranslateEditView.loadUrl("https://translate.google.com/m/translate#en/de/" + AnkiHelperApplication.currentWord.englishWord);
    }
}

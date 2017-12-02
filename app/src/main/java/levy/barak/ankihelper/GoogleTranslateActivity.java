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
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import levy.barak.ankihelper.levy.barak.ankihelper.utils.WebUtils;

public class GoogleTranslateActivity extends Activity {
    public static final String GERMAN_WORD = "GERMAN_WORD";
    public static final String SHARED_IMAGE_SRC = "IMAGE_SRC";
    public static final String SHARED_IPA_SRC = "IPA_VALUE";

    private boolean mIsInTranslate = false;

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
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.i("requestedUrl", request.getUrl().toString());
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                //Log.i("requestedUrl23", request.getUrl());

                Log.i("requestedUrl2", request.getUrl().toString());
//                if (request.getUrl().toString().startsWith("https://translate.google.com/translate_a/single")) {
//                    if (!mIsInTranslate) {
//                        mIsInTranslate = true;
//                    }
//                    else {
//                        return new WebResourceResponse("", "", null);
//                    }
//                }

                if (mIsInTranslate) {
                    return new WebResourceResponse("", "", null);
                }

                return null;
            }

            public void onPageFinished(WebView view, String url) {
                mIsInTranslate = true;
                //googleTranslateEditView.evaluateJavascript(WebUtils.getJavascript(googleTranslateEditView.getContext(), "googleTranslate.js"), null);
            }
        });

        googleTranslateEditView.loadUrl("https://translate.google.com/m/translate#en/de/" + TranslateActivity.englishWord);
    }
}
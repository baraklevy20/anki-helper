package levy.barak.ankihelper.vocabulary_screen;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import levy.barak.ankihelper.AnkiHelperApplication;
import levy.barak.ankihelper.R;
import levy.barak.ankihelper.utils.FileUtils;
import levy.barak.ankihelper.utils.GermanUtils;

/**
 * Created by baraklev on 2/9/2018.
 */

public class GoogleTranslateFragment extends Fragment {
    public class WebAppInterface {
        private GoogleTranslateFragment mContext;

        WebAppInterface(GoogleTranslateFragment c) {
            mContext = c;
        }

        @JavascriptInterface
        public void catchGermanWord(String germanWord, String type) {
            AnkiHelperApplication.currentWord.type = GermanUtils.WordCategory.valueOf(type.substring(0, type.length() - 1).toUpperCase()); // Remove the '\n'

            // Set the german word to either capitalized or lower cased
            if (AnkiHelperApplication.currentWord.type.isUppercase()) {
                String[] split = germanWord.split(" ");

                if (split.length == 2) {
                    AnkiHelperApplication.currentWord.germanWord = split[0] + " " +
                            split[1].substring(0, 1).toUpperCase() + split[1].substring(1);
                }
                else {
                    AnkiHelperApplication.currentWord.germanWord = split[0].substring(0, 1).toUpperCase() + split[0].substring(1);
                }
            }
            else {
                AnkiHelperApplication.currentWord.germanWord = germanWord.toLowerCase();
            }

            // Move to the google images activity
            moveToNextScreen();

            new Thread(() -> {
                // Get additional information about the word
                try {
                    getWordInformationFromWiki(GermanUtils.getGermanWordWithoutPrefix());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        public void moveToNextScreen() {
            GoogleImagesFragment newFragment = new GoogleImagesFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            transaction.replace(R.id.fragmentsContainer, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        public void getWordInformationFromWiki(String word) throws IOException {
            Connection connection = Jsoup.connect("https://de.wiktionary.org/wiki/" + word);
            Document doc = connection.get();

            Elements ipas = doc.select(".ipa");

            if (ipas.size() == 0) {
                mContext.getActivity().runOnUiThread(() -> Toast.makeText(mContext.getActivity(), "Couldn't find an IPA. Wrong word perhaps?", Toast.LENGTH_LONG).show());
                return;
            }

            AnkiHelperApplication.currentWord.ipa = ipas.first().text();

            Elements wordInASentences = doc.select("[title=Verwendungsbeispielsätze]").first().nextElementSibling().children();

            for (int i = 0; i < wordInASentences.size(); i++) {
                AnkiHelperApplication.currentWord.wordInASentences.add(wordInASentences.get(i).html());
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_vocabulary_google_translate, container, false);

        final WebView googleTranslateEditView = (WebView) fragment.findViewById(R.id.googleTranslateWebView);
        googleTranslateEditView.addJavascriptInterface(new WebAppInterface(this), "Android");

        WebSettings settings = googleTranslateEditView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        googleTranslateEditView.setWebViewClient(new WebViewClient() {
            int resourcesLoaded = 0;

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);

                // We only attach the script AFTER we load the first resource which is the main URL
                if (resourcesLoaded == 1) {
                    googleTranslateEditView.evaluateJavascript(FileUtils.getFileContent(googleTranslateEditView.getContext(), "googleTranslate.js"), null);
                }

                resourcesLoaded++;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.i("requestedUrl", request.getUrl().toString());
                return true;
            }
        });

        googleTranslateEditView.loadUrl("https://translate.google.com/m/translate#en/de/" + AnkiHelperApplication.currentWord.englishWord);

        return fragment;
    }
}

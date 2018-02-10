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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import levy.barak.ankihelper.AnkiHelperApplication;
import levy.barak.ankihelper.R;
import levy.barak.ankihelper.languages.GermanLanguage;
import levy.barak.ankihelper.utils.FileUtils;

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
        public void catchGoogleTranslateWord(String googleTranslateWord, String wordCategory) {
            String lowerCaseCategory = wordCategory.substring(0, wordCategory.length() - 1).toLowerCase(); // Remove the '\n'
            AnkiHelperApplication.currentWord.translatedWordCategory = AnkiHelperApplication.language.wordCategoriesTranslations.get(lowerCaseCategory);
            AnkiHelperApplication.currentWord.secondLanguageWord = AnkiHelperApplication.language.parseGoogleTranslateWord(googleTranslateWord, lowerCaseCategory);

            // Move to the google images activity
            moveToNextScreen();

            new Thread(() -> {
                // Get additional information about the word
                try {
                    getWordInformationFromWiki(AnkiHelperApplication.language.getMajorWordPart());
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

            Element examplesElement = doc.select("[title=Verwendungsbeispielsätze]").first();

            // If there are any examples
            if (examplesElement != null) {
                // Read them
                Elements wordInASentences = examplesElement.nextElementSibling().children();

                // Clear before usage so if the user has chosen a different translation,
                // it would get the sentences from the latest translation.
                AnkiHelperApplication.currentWord.wordInASentences.clear();

                for (int i = 0; i < wordInASentences.size(); i++) {
                    AnkiHelperApplication.currentWord.wordInASentences.add(wordInASentences.get(i).html());
                }
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

        googleTranslateEditView.loadUrl("https://translate.google.com/m/translate#en/" +
                AnkiHelperApplication.language.getLanguageCode() + "/" +
                AnkiHelperApplication.currentWord.firstLanguageWord);

        return fragment;
    }
}

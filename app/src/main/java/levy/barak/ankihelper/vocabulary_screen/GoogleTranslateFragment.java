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

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

import levy.barak.ankihelper.AnkiHelperApplication;
import levy.barak.ankihelper.R;
import levy.barak.ankihelper.anki.Word;
import levy.barak.ankihelper.utils.FileUtils;

/**
 * Created by baraklev on 2/9/2018.
 */

public class GoogleTranslateFragment extends Fragment {
    public static final String FIRST_LANGUAGE_TO_SECOND_LANGUAGE = "levy.barak.ankihelper.first_to_second_language";
    private boolean isFirstToSecondLanguage;

    public class WebAppInterface {
        private Fragment mContext;

        WebAppInterface(Fragment c) {
            mContext = c;
        }

        @JavascriptInterface
        public void catchGoogleTranslateWord(String googleTranslateWord, String wordCategory) {
            String trimmedWordCategory = wordCategory.substring(0, wordCategory.length() - 1); // Remove the '\n'

            AnkiHelperApplication.currentWord.wordCategory = Word.WordCategory.valueOf(trimmedWordCategory.toUpperCase());

            if (isFirstToSecondLanguage) {
                AnkiHelperApplication.currentWord.secondLanguageWord =
                        AnkiHelperApplication.language.parseGoogleTranslateWord(googleTranslateWord, AnkiHelperApplication.currentWord.wordCategory);
            }
            else {
                AnkiHelperApplication.currentWord.firstLanguageWord = googleTranslateWord;
            }

            // Move to the google images activity
            moveToNextScreen();

            new Thread(() -> {
                // Get additional information about the word
                try {
                    getWordInformationFromWiki(AnkiHelperApplication.language.getSearchableWord());
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
            Connection connection = Jsoup.connect("https://" + AnkiHelperApplication.language.getWiktionaryLanguageCode() + ".wiktionary.org/wiki/" + word);
            AnkiHelperApplication.language.getInformationFromWiktionary(mContext, connection.get(), isFirstToSecondLanguage);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_vocabulary_google_translate, container, false);
        isFirstToSecondLanguage = getActivity().getIntent().getBooleanExtra(FIRST_LANGUAGE_TO_SECOND_LANGUAGE, true);

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

        String first = isFirstToSecondLanguage ? "en" : AnkiHelperApplication.language.getGoogleTranslateLanguageCode();
        String second = isFirstToSecondLanguage ? AnkiHelperApplication.language.getGoogleTranslateLanguageCode() : "en";
        String wordToTranslate = isFirstToSecondLanguage ? AnkiHelperApplication.currentWord.firstLanguageWord.toLowerCase()
                : AnkiHelperApplication.currentWord.secondLanguageWord;

        googleTranslateEditView.loadUrl("https://translate.google.com/m/translate#" + first + "/" +
                second + "/" +
                wordToTranslate);

        return fragment;
    }
}

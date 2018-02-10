package levy.barak.ankihelper.vocabulary_screen;

import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import levy.barak.ankihelper.AnkiHelperApplication;
import levy.barak.ankihelper.R;

public class ForvoFragment extends Fragment {
    public class DownloadHandler implements DownloadListener {
        WebView forvoWebView;

        public DownloadHandler(WebView forvoWebView) {
            this.forvoWebView = forvoWebView;
        }

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            String downloadName = "anki_helper_sound_" + AnkiHelperApplication.currentWord.id + "_" + AnkiHelperApplication.currentWord.soundsUrl.size();

            // Download it
            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(url));
            request.addRequestHeader("Cookie", CookieManager.getInstance().getCookie("forvo.com"));
            request.allowScanningByMediaScanner();
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "anki_helper/" + downloadName);
            DownloadManager dm = (DownloadManager) forvoWebView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            dm.enqueue(request);

            // Get the additional information
            AnkiHelperApplication.currentWord.additionalInformation =
                    ((EditText)getActivity().findViewById(R.id.vocabularyAdditionalInfoEditText)).getText().toString();

            // Add the sound and write the word
            AnkiHelperApplication.currentWord.soundsUrl.add(downloadName);
            AnkiHelperApplication.allWords.add(AnkiHelperApplication.currentWord);
            AnkiHelperApplication.writeWords();

            startActivity(new Intent(forvoWebView.getContext(), VocabularyListActivity.class));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_vocabulary_forvo, container, false);

        final WebView forvoWebView = (WebView) fragment.findViewById(R.id.forvoWebView);
        forvoWebView.setDownloadListener(new DownloadHandler(forvoWebView));

        WebSettings settings = forvoWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        forvoWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                try {
                    String url = java.net.URLDecoder.decode(request.getUrl().toString().toLowerCase(), "UTF-8");
                    String word = AnkiHelperApplication.language.getMajorWordPart().toLowerCase();

                    Log.i("requestedUrl", url);

                    return !(
                            url.endsWith("de/" + word + "/") ||
                                    url.endsWith("/login/") ||
                                    url.contains("download") ||
                                    url.endsWith("/word/" + word + "/")
                    );
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getActivity(), "Couldn't decode the URL in Forvo", Toast.LENGTH_LONG).show();
                return false;
            }
        });

        forvoWebView.loadUrl("https://forvo.com/word/de/" + AnkiHelperApplication.language.getMajorWordPart() + "/");
        CookieManager.getInstance().setAcceptCookie(true);

        return fragment;
    }
}

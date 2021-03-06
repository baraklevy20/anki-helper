package levy.barak.ankihelper.common_screens;

import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import levy.barak.ankihelper.AnkiHelperApp;
import levy.barak.ankihelper.R;
import levy.barak.ankihelper.grammar_screen.GrammarFormSentenceFragment;
import levy.barak.ankihelper.utils.FileUtils;
import levy.barak.ankihelper.vocabulary_screen.ForvoFragment;

public class GoogleImagesFragment extends Fragment {
    public static final int PICK_IMAGE = 0;
    public static final String ACTIVITY_SOURCE = "levy.barak.ankihelper.activity_source";

    private GoogleImagesSources source;

    public class WebAppInterface {
        private Fragment mContext;

        WebAppInterface(Fragment c) {
            mContext = c;
        }

        @JavascriptInterface
        public void clearCache() {
            mContext.getActivity().runOnUiThread(() -> {
                WebView googleImagesWebView = (WebView) mContext.getActivity().findViewById(R.id.googleImagesWebView);
                googleImagesWebView.clearCache(true);
                googleImagesWebView.reload();
            });
        }

        @JavascriptInterface
        public void catchHref(String imageUrl) {
            // No support for vector graphics
            if (imageUrl.endsWith("svg")) {
                Toast.makeText(mContext.getContext(), "Unfortunately the app does not support .svg image files. Please pick another image", Toast.LENGTH_LONG).show();
                return;
            }

            // Move on to the next screen
            moveToNextScreen();

            new Thread(() -> {
                // Download it
                String downloadName = getDownloadPath();
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
                request.allowScanningByMediaScanner();
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "anki_helper/" + downloadName);
                AnkiHelperApp.currentWord.imagesUrl.add(downloadName);
                DownloadManager dm = (DownloadManager) mContext.getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                dm.enqueue(request);
            }).start();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_vocabulary_google_images, container, false);
        this.source = GoogleImagesSources.values()[getArguments().getInt(ACTIVITY_SOURCE)];

        WebView googleImagesWebView = (WebView) fragment.findViewById(R.id.googleImagesWebView);

        WebSettings settings = googleImagesWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        googleImagesWebView.setWebViewClient(new WebViewClient() {
            boolean isMainPageLoaded;
            boolean isScriptInjected;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // We can only inject the Javascript script if the main page was already loaded
                isMainPageLoaded = false;
                isScriptInjected = false;

                // Must clear the cache every time we start the screen
                // For some reason, the JS part doesn't work properly without it
                googleImagesWebView.clearCache(true);

                Log.d("google_images_url", "***RESET***");
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);

                //Log.d("google_images_url_load", url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                Log.d("google_images_url", url);

                if (url.contains("/search?")) {
                    isMainPageLoaded = true;
                }

                // We only attach the script AFTER we load the main URL
                if (isMainPageLoaded && !isScriptInjected) {
                    googleImagesWebView.evaluateJavascript(FileUtils.getFileContent(googleImagesWebView.getContext(), "googleImages.js"), null);
                    isScriptInjected = true; // this avoids injecting multiple times (for each loaded url)
                }
            }
        });

        if (source == GoogleImagesSources.VOCABULARY) {
            googleImagesWebView.loadUrl(getUrl(AnkiHelperApp.language.getSearchableWord()));
        }
        else {
            googleImagesWebView.loadUrl(getUrl(""));
        }

        // Enable the menu on this fragment
        if (source == GoogleImagesSources.VOCABULARY) {
            setHasOptionsMenu(true);
        }

        googleImagesWebView.addJavascriptInterface(new WebAppInterface(this), "Android");

        return fragment;
    }

    public void moveToNextScreen() {
        if (source == GoogleImagesSources.VOCABULARY) {
            ForvoFragment newFragment = new ForvoFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            transaction.replace(R.id.vocabularyFragmentsContainer, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if (source == GoogleImagesSources.GRAMMAR) {
            GrammarFormSentenceFragment newFragment = new GrammarFormSentenceFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            transaction.replace(R.id.grammarFragmentsContainer, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public String getUrl(String word) {
        return "https://www.google.de/search?q=" + word +
                "&hl=de&tbo=d&site=imghp&tbm=isch&gwd_rd=ssl";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.vocabulary_menu_images, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (source == GoogleImagesSources.VOCABULARY) {
            final WebView webView = (WebView) getActivity().findViewById(R.id.googleImagesWebView);
            switch (item.getItemId()) {
                case R.id.vocabulary_menu_images_search_in_english:
                    webView.loadUrl(getUrl(AnkiHelperApp.currentWord.firstLanguageWord));
                    return true;
                case R.id.vocabulary_menu_images_search_in_german:
                    webView.loadUrl(getUrl(AnkiHelperApp.language.getSearchableWord()));
                    return true;
                case R.id.vocabulary_menu_images_pick_image:
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE:
                // If the user chose an image, this is -1
                if (resultCode == -1) {
                    copyImageFromFile(data.getData());

                    // Move on to the next screen
                    moveToNextScreen();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public String getDownloadPath() {
        if (source == GoogleImagesSources.VOCABULARY) {
            return "anki_helper_image_" + AnkiHelperApp.currentWord.id + "_" + AnkiHelperApp.currentWord.imagesUrl.size();
        }

        return "anki_helper_image_" + AnkiHelperApp.currentSentence.id;
    }

    public void copyImageFromFile(Uri imageUri) {
        try {
            InputStream input = getActivity().getContentResolver().openInputStream(imageUri);
            FileOutputStream output = new FileOutputStream(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/anki_helper/" + getDownloadPath());

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            input.close();
            output.close();

            // Add the image's URL
            AnkiHelperApp.currentWord.imagesUrl.add(getDownloadPath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
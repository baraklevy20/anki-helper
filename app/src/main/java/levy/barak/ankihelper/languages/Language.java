package levy.barak.ankihelper.languages;

import android.app.Fragment;
import android.content.Context;

import org.jsoup.nodes.Document;

import java.util.HashMap;

import levy.barak.ankihelper.anki.Word;

/**
 * Created by baraklev on 2/10/2018.
 */

public abstract class Language {
    public HashMap<Word.WordCategory, String> wordCategoriesTranslations;
    public abstract String getGoogleTranslateLanguageCode();
    public abstract String getWiktionaryLanguageCode();
    public abstract String getSearchableWord();
    protected abstract void addWordCategoriesTranslations();
    public abstract String parseGoogleTranslateWord(String googleTranslateWord, Word.WordCategory wordCategory);
    public abstract void getInformationFromWiktionary(Fragment fragment, Document doc, boolean isFirstToSecondLanguage);

    public Language() {
        wordCategoriesTranslations = new HashMap<>();
        addWordCategoriesTranslations();
    }
}

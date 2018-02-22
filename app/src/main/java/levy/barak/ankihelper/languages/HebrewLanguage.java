package levy.barak.ankihelper.languages;

import android.app.Fragment;

import org.jsoup.nodes.Document;

import levy.barak.ankihelper.AnkiHelperApplication;
import levy.barak.ankihelper.anki.Word;

/**
 * Created by baraklev on 2/10/2018.
 */

public class HebrewLanguage extends Language {

    public HebrewLanguage() {
    }

    @Override
    protected void addWordCategoriesTranslations() {
        wordCategoriesTranslations.put(Word.WordCategory.NOUN, "שם עצם");
        wordCategoriesTranslations.put(Word.WordCategory.VERB, "פועל");
        wordCategoriesTranslations.put(Word.WordCategory.ADJECTIVE, "שם תואר");
        wordCategoriesTranslations.put(Word.WordCategory.ADVERB, "תואר הפועל");
    }

    @Override
    public String getGoogleTranslateLanguageCode() {
        return "iw";
    }

    @Override
    public String getWiktionaryLanguageCode() {
        return "he";
    }

    @Override
    public String getSearchableWord() {
        String result = "";
        String word = AnkiHelperApplication.currentWord.secondLanguageWord;

        for (int i = 0; i < word.length(); i++) {
            // Remove Niqqud
            if (word.charAt(i) >= 0x5D0 && word.charAt(i) <= 0x5EA) {
                result += word.charAt(i);
            }
        }

        return result;
    }

    @Override
    public String parseGoogleTranslateWord(String googleTranslateWord, Word.WordCategory wordCategory) {
        return googleTranslateWord;
    }

    @Override
    public void getInformationFromWiktionary(Fragment fragment, Document doc, boolean isFirstToSecondLanguage) {

    }
}
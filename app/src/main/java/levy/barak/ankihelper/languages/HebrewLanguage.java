package levy.barak.ankihelper.languages;

import android.app.Fragment;

import org.jsoup.nodes.Document;

import levy.barak.ankihelper.AnkiHelperApp;
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
        wordCategoriesTranslations.put(Word.WordCategory.PREPOSITION, "מילת יחס");
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
        String word = AnkiHelperApp.currentWord.secondLanguageWord;

        for (int i = 0; i < word.length(); i++) {
            // Removes Niqqud
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
    public String parseTypedWord(String word) {
        return word;
    }

    @Override
    public void getInformationFromWiktionary(Fragment fragment, Document doc, boolean isFirstToSecondLanguage) {

    }
}

package levy.barak.ankihelper.languages;

import java.util.HashMap;

import levy.barak.ankihelper.AnkiHelperApplication;

/**
 * Created by baraklev on 2/10/2018.
 */

public class GermanLanguage extends Language {
    public HashMap<String, Boolean> wordCategoriesUppercases;

    public GermanLanguage() {
        wordCategoriesUppercases = new HashMap<>();
        wordCategoriesUppercases.put("noun", true);
        wordCategoriesUppercases.put("verb", false);
        wordCategoriesUppercases.put("adjective", false);
        wordCategoriesUppercases.put("adverb", false);
    }

    @Override
    protected void addWordCategoriesTranslations() {
        wordCategoriesTranslations.put("noun", "Substantiv");
        wordCategoriesTranslations.put("verb", "Verb");
        wordCategoriesTranslations.put("adjective", "Adjektiv");
        wordCategoriesTranslations.put("adverb", "Adverb");
    }

    @Override
    public String getLanguageCode() {
        return "de";
    }

    @Override
    public String getMajorWordPart() {
        String[] split = AnkiHelperApplication.currentWord.secondLanguageWord.split(" ");
        return split[split.length - 1];
    }

    @Override
    public String parseGoogleTranslateWord(String googleTranslateWord, String lowerCaseCategory) {
        // Set the german word to either capitalized or lower cased
        if (wordCategoriesUppercases.get(lowerCaseCategory)) {
            String[] split = googleTranslateWord.split(" ");

            if (split.length == 2) {
                return split[0] + " " + split[1].substring(0, 1).toUpperCase() + split[1].substring(1);
            }
            else {
                return split[0].substring(0, 1).toUpperCase() + split[0].substring(1);
            }
        }

        return googleTranslateWord.toLowerCase();
    }
}

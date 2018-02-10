package levy.barak.ankihelper.languages;

import java.util.HashMap;

/**
 * Created by baraklev on 2/10/2018.
 */

public abstract class Language {
    public HashMap<String, String> wordCategoriesTranslations;
    public abstract String getLanguageCode();
    public abstract String getMajorWordPart();
    protected abstract void addWordCategoriesTranslations();
    public abstract String parseGoogleTranslateWord(String googleTranslateWord, String lowerCaseCategory);

    public Language() {
        wordCategoriesTranslations = new HashMap<>();
        addWordCategoriesTranslations();
    }
}

package levy.barak.ankihelper.anki;

import java.util.ArrayList;

/**
 * Created by baraklev on 11/27/2017.
 */

public final class Word {
    public long id;
    public String firstLanguageWord;
    public String secondLanguageWord;
    public ArrayList<String> imagesUrl;
    public ArrayList<String> soundsUrl;
    public String ipa;
    public String additionalInformation;
    public WordCategory wordCategory;
    public ArrayList<String> wordInASentences;

    public Word(String firstWord, boolean isFirstToSecondLanguage) {
        this.id = (long)(Math.random() * Long.MAX_VALUE);

        if (isFirstToSecondLanguage) {
            this.firstLanguageWord = firstWord;
        }
        else {
            this.secondLanguageWord = firstWord;
        }
        this.imagesUrl = new ArrayList<>();
        this.soundsUrl = new ArrayList<>();
        this.wordInASentences = new ArrayList<>();
    }

    public enum WordCategory {
        NOUN,
        VERB,
        ADJECTIVE,
        ADVERB
    }
}

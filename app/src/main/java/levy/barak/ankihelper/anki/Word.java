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
    public String translatedWordCategory;
    public ArrayList<String> wordInASentences;

    public Word(String firstLanguageWord) {
        this.id = (long)(Math.random() * Long.MAX_VALUE);
        this.firstLanguageWord = firstLanguageWord;
        this.imagesUrl = new ArrayList<>();
        this.soundsUrl = new ArrayList<>();
        this.wordInASentences = new ArrayList<>();
    }
}

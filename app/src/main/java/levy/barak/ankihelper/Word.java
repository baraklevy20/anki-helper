package levy.barak.ankihelper;

import java.util.ArrayList;

/**
 * Created by baraklev on 11/27/2017.
 */

public final class Word {
    public long id;
    public String englishWord;
    public String germanWord;
    public ArrayList<String> imagesUrl;
    public ArrayList<String> soundsUrl;
    public String ipa;
    public String personalConnection;
    public ArrayList<String> wordInASentences;

    public Word(String englishWord) {
        this.id = (long)(Math.random() * Long.MAX_VALUE);
        this.englishWord = englishWord;
        this.imagesUrl = new ArrayList<>();
        this.soundsUrl = new ArrayList<>();
        this.wordInASentences = new ArrayList<>();
    }
}

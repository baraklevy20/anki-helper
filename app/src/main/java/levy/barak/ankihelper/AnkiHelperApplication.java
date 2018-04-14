package levy.barak.ankihelper;

import android.app.Application;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

import levy.barak.ankihelper.anki.Sentence;
import levy.barak.ankihelper.anki.Word;
import levy.barak.ankihelper.languages.GermanLanguage;
import levy.barak.ankihelper.languages.HebrewLanguage;
import levy.barak.ankihelper.languages.Language;

/**
 * Created by baraklev on 12/2/2017.
 */

public class AnkiHelperApplication extends Application {
    public static final String PREFERENCES = "levy.barak.ankihelper";

    private static SharedPreferences prefs;

    public static Word currentWord;
    public static Sentence currentSentence;

    public static ArrayList<Word> allWords;

    public static ArrayList<Sentence> allSentences;

    public static Language language;

    @Override
    public void onCreate() {
        super.onCreate();
        this.prefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        language = new GermanLanguage();
    }

    public static void writeWords() {
        prefs.edit().putString("Words", new Gson().toJson(allWords)).apply();
    }

    public static void writeSentences() {
        prefs.edit().putString("Sentences", new Gson().toJson(allSentences)).apply();
    }

    public static void readWords() {
        Gson gson = new Gson();
        String json = prefs.getString("Words", "");
        Word[] words = gson.fromJson(json, Word[].class);

        json = prefs.getString("Sentences", "");
        Sentence[] sentences = gson.fromJson(json, Sentence[].class);

        allWords = new ArrayList<>();
        allSentences = new ArrayList<>();

        // Convert if there are words
        if (words != null) {
            // Convert to a list
            Arrays.stream(words).forEach(w -> allWords.add(w));
        }

        // Convert if there are words
        if (sentences != null) {
            // Convert to a list
            Arrays.stream(sentences).forEach(w -> allSentences.add(w));
        }
    }
}

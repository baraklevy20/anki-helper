package levy.barak.ankihelper;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

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

    public static HashMap<String, Long> decks;

    @Override
    public void onCreate() {
        super.onCreate();
        this.prefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        language = new GermanLanguage();
        decks = getDecks();
    }

    private static HashMap<String, Long> getDecks() {
        try {
            // Get all the available decks
            String path = Environment.getExternalStorageDirectory() + "/AnkiDroid/collection.anki2";
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null, null);

            // Get the decks
            String decksString = db.compileStatement("select decks from col").simpleQueryForString();

            HashMap<String, Long> decks = new HashMap<>();
            JSONObject decksJson = new JSONObject(decksString);
            Iterator<String> ids = decksJson.keys();
            while(ids.hasNext()) {
                String id = ids.next();
                decks.put(decksJson.getJSONObject(id).getString("name"), Long.parseLong(id));
            }

            return decks;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
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

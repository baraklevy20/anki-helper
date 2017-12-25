package levy.barak.ankihelper;

import android.app.Application;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by baraklev on 12/2/2017.
 */

public class AnkiHelperApplication extends Application {
    public static final String PREFERENCES = "levy.barak.ankihelper";
    public static final String PREFERENCES_CURRENT_WORD = "levy.barak.ankihelper.CURRENT_WORD";

    private static SharedPreferences prefs;

    public static Word currentWord;

    public static ArrayList<Word> allWords;

    @Override
    public void onCreate() {
        super.onCreate();
        this.prefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    }

    public static void writeWords() {
        prefs.edit().putString("Words", new Gson().toJson(allWords)).apply();
    }

    public static void readWords() {
        Gson gson = new Gson();
        String json = prefs.getString("Words", "");
        Word[] words = gson.fromJson(json, Word[].class);

        allWords = new ArrayList<>();

        // If there are no words, return
        if (words == null) {
            return;
        }

        // Convert to a list
        Arrays.stream(words).forEach(w -> {
            allWords.add(w);
        });
    }
}

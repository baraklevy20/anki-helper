package levy.barak.ankihelper.utils;

import levy.barak.ankihelper.AnkiHelperApplication;

/**
 * Created by baraklev on 2/3/2018.
 */

public class GermanUtils {
    public enum WordCategory {
        NOUN("Substantiv", true),
        VERB("Verb", false),
        ADJECTIVE("Adjektiv", false),
        ADVERB("Adverb", false);

        private String germanTranslation;
        private boolean isUppercase; // Will be used in accessing Wiktionary

        WordCategory(String germanTranslation, boolean isUppercase) {
            this.germanTranslation = germanTranslation;
            this.isUppercase = isUppercase;
        }

        public String getGermanTranslation() {
            return germanTranslation;
        }

        public boolean isUppercase() {
            return isUppercase;
        }
    }

    public static String getGermanWordWithoutPrefix() {
        String[] split = AnkiHelperApplication.currentWord.germanWord.split(" ");
        return split[split.length - 1];
    }
}
